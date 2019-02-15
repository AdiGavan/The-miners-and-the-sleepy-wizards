import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Class for a miner.
 */
public class Miner extends Thread {
	Integer hashCount;
	CommunicationChannel channel;
	Set<Integer> solved;
	static Semaphore semaphore_miner = new Semaphore(1);
	static Object obj_sync = new Object();
	
	/**
	 * Creates a {@code Miner} object.
	 * 
	 * @param hashCount
	 *            number of times that a miner repeats the hash operation when
	 *            solving a puzzle.
	 * @param solved
	 *            set containing the IDs of the solved rooms
	 * @param channel
	 *            communication channel between the miners and the wizards
	 */
	public Miner(Integer hashCount, Set<Integer> solved, CommunicationChannel channel) {
		this.hashCount = hashCount;
		this.solved = solved;
		this.channel = channel;
	}

	@Override
	public void run() {
		// Used for a received message.
		Message received = null;
		// Used for the message that the miner will transmit.
		Message transmited = null;
		// Used for the final hashed string.
		String hashed_code = null;
		// Used for the parent room.
		Integer parent = 0;
		
		while(true) {
			/*
			 * A miner must take 2 messages from a wizard, the message with
			 * the parent room and the message with the adjacent room.
			 * 
			 * A semaphore is used to block the other miners while one of 
			 * them is taking the messages.
			 */
			try {
				semaphore_miner.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// The miner takes the first message.
			received = channel.getMessageWizardChannel();
			// If it is not an "EXIT" message, the parent room is saved
			// and the miner takes the other message.
			if (!received.getData().equals("EXIT")) {
					parent = received.getCurrentRoom();
					received = channel.getMessageWizardChannel();
			} else {
				// If it is an "EXIT" message, the miner does not take another message.
			}
			semaphore_miner.release();
			
			// If the miner received an "EXIT" message, it exits the loop
			// and his execution ends.
			if (received.getData().equals("EXIT")) {
				break;
			}
			
			// If the adjacent room received has already been solved,
			// the room is ignored.
			if (solved.contains(received.getCurrentRoom())) {
				continue;
			}
			
			// The new room that is going to be solved is added to the solved Set.
			synchronized(obj_sync) {
				solved.add(received.getCurrentRoom());
			}
			
			// The miner calculates the string that is going to be sent to the wizards.
			hashed_code = received.getData();
			for (int i = 0; i < hashCount; i++) {
			    try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				byte[] messageDigest = md.digest(hashed_code.getBytes(StandardCharsets.UTF_8));

				// convert to string
				StringBuffer hexString = new StringBuffer();
				for (int j = 0; j < messageDigest.length; j++) {
					String hex = Integer.toHexString(0xff & messageDigest[j]);
					if(hex.length() == 1) hexString.append('0');
				    	hexString.append(hex);
				}
				hashed_code = hexString.toString();

			    } catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			    }
			}
	        
			// The message for the wizards is created.
			transmited = new Message(parent, received.getCurrentRoom(), hashed_code);
			// The message for the wizards is transmitted.
			channel.putMessageMinerChannel(transmited);
		}
	}
}
