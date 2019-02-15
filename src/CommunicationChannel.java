import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
public class CommunicationChannel {
	static final int QUEUE_DIMENSION = 10000;
	static final int BUFFER_MESS_DIMENSION = 1000;
	// The buffer used for miner channel (miners write to and wizards read from)
	BlockingQueue<Message> miner_wizard = new ArrayBlockingQueue<Message>(QUEUE_DIMENSION);
	// The buffer used for wizard channel (wizards write to and miners read from)
	BlockingQueue<Message> wizard_miner = new ArrayBlockingQueue<Message>(QUEUE_DIMENSION);
	// The buffer used for messages received from wizards (it is used to send 
	// 2 messages from the same wizard to a miner)
	Message buffer_messages[] = new Message[BUFFER_MESS_DIMENSION];
	
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {
	}

	/**
	 * Puts a message on the miner channel (i.e., where miners write to and wizards
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageMinerChannel(Message message) {
		try {
			miner_wizard.put(message);
		} catch (InterruptedException e) {
            e.printStackTrace();
        }
	}

	/**
	 * Gets a message from the miner channel (i.e., where miners write to and
	 * wizards read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageMinerChannel() {
		try {
			return miner_wizard.take();
		} catch (InterruptedException e) {
            e.printStackTrace();
        }
		return null;
	}

	/**
	 * Puts a message on the wizard channel (i.e., where wizards write to and miners
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageWizardChannel(Message message) {
		// If a wizard puts an "EXIT" message, it is immediately put into the buffer.
		if (message.getData().equals("EXIT")) {
			synchronized(wizard_miner.getClass()) {
				try {
					wizard_miner.put(message);
				} catch (InterruptedException e) {
		            e.printStackTrace();
		        }
			}
		} else if (message.getData().equals("END")) {
			// If a wizard puts an "END" message, the message is simply ignored.
		} else {
			/*
			 *  A wizard must put the message with the parent room and the message with
			 *  the adjacent room one after another, without letting another wizard 
			 *  to put a message between these 2.
			 *  
			 *  For each wizard, the first message (with the parent) is put into the messages buffer.
			 */
			if (buffer_messages[(int)Thread.currentThread().getId()] == null) {
				buffer_messages[(int)Thread.currentThread().getId()] = message;
			} else {
				// If the message with the parent is into the buffer when the message
				// with the adjacent room comes, both of them are put into the buffer.
				synchronized(wizard_miner.getClass()) {
					try {
						wizard_miner.put(buffer_messages[(int)Thread.currentThread().getId()]);
						wizard_miner.put(message);
					} catch (InterruptedException e) {
			            e.printStackTrace();
			        }
				}
				// After the messages are sent, the position corresponding to the wizard into
				// the messages buffer is reset.
				buffer_messages[(int)Thread.currentThread().getId()] = null;
			}
		}
	}

	/**
	 * Gets a message from the wizard channel (i.e., where wizards write to and
	 * miners read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageWizardChannel() {
		try {
			return wizard_miner.take();
		} catch (InterruptedException e) {
            e.printStackTrace();
        }
		return null;
	}
}