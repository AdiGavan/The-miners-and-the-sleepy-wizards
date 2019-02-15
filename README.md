"Copyright [2018] Gavan Adrian-George, 334CA"
Nume, prenume: Gavan, Adrian-George
Grupa, seria: 334CA

Tema 2 APD - The miners and the sleepy wizards

Prezentarea implementarii:
==========================

A. Logica implementarii:
========================

- Problema poate fi vazuta ca o problema de tipul Producator-Consumator.
Vrajitorii si minerii sunt pe rand si producatori si comsumatori. Un vrajitor 
le trimite minerilor camera curenta si camerele adiacente => este producator, 
iar minerul le preia => este consumator. Cand minerul trimite vrajitorilor 
rezultatul, acesta este producator, iar cand vrajitorii il citesc acestia sunt 
consumatori.
- Vrajitorii trimit seturi de mesaje "current room, adjacent room,..., END".
Trebuie sa ne asiguram ca un miner primeste 2 mesaje de tipul "current room, 
adjacent room" si ca mesajele vrajitorilor nu se intercaleaza (astfel incat 
un miner sa primeasca 2 mesaje de tipul "current room" sau 2 mesaje de tipul 
"adjancent room").
- Ideea este ca vrajitorii nu trebuie sincronizati sa puna neaparat toate 
mesajele pana la "END", apoi altul sa puna toate mesajele pana la "END" si tot 
asa. De asemenea, un miner nu trebuie neaparat sa ia toate mesajele de la un 
wizard pana intalneste "END". 
- Un miner trebuie neaparat sa primeasca 2 mesaje de tipul "current room, 
adjacent room" pentru a putea debloca o camera => nu conteaza de la ce wizard 
vin cele 2 mesaje, conteaza doar sa vina impreuna. 
- Un vrajitor nu trebuie sa isi puna neaparat tot setul de mesaje "current room,
adjacent room,...,END" si restul sa astepte. Vrajitorii pot pune intercalat 
mesaje, atata timp cat fiecare pune neaparat seturi de mesaje de tipul "current 
room, adjacent room".
- Un miner nu trebuie sa ia un set intreg de date de la un anume vrajitor. El 
poate lua mesaje de la orice vrajitor, conditia fiind ca el sa ia neaparat 2 mesaje 
de tipul "current room, adjacent room" si sa nu lase alt miner sa intervina cand 
acesta ia cele 2 mesaje.  

B. CommunicationChannel.java:
=============================

- Se folosesc 2 buffere de tipul "ArrayBlockingQueue" pentru a realiza problma de 
tipul Producator-Consumator in ambele sensuri.
- Cand un miner pune un mesaj, cand un wizard citeste un mesaj de la miner sau 
cand un miner citeste un mesaj de la wizard doar se apeleaza metodele "put" sau 
"take" specifice "ArrayBlockingQueue" pentru buffer-ul corespunzator.
- Metoda pe care o apeleaza un wizard pentru a pune mesaje este diferita pentru ca
vrajitorii trebuie sincronizati intre ei.
- Pentru a pune mesaje 2 cate 2 se foloseste un buffer pentru mesaje. In acest 
buffer, in pozitia care corespunde unui vrajitor, se pune mesajul de tipul "current 
room" trimis de acel vrajitor. Cand se primeste si mesajul de tipul "adjacent room", 
ambele mesaje sunt puse in bufferul pentru Producator-Consumator.
- Cu alte cuvinte: se verifica daca mesajul primit este "EXIT". Daca este "EXIT" se 
adauga imediat in bufferul P-C. Altfel, daca mesajul este "END" se ignora pentru ca 
acest mesaj (in aceasta implementare) nu influenteaza cu nimic. Daca este un mesaj 
normal, se verifica daca pentru acel vrajitor se gaseste ceva in bufferul de mesaje.
Daca se gaseste, inseamna ca "current room" a fost primit => mesajul curent este 
camera adiacenta => se trimit ambele mesaje si se reseteaza pozitia vrajitorului din 
buffer. Daca nu se gaseste nimic in buffer, inseamna ca mesajul curent este de tipul
"current room" si se adauga in bufferul de mesaje.

C. Miners.java:
===============

- Minerii trebuie sincronizati astfel incat fiecare sa ia neaparat cate 2 mesaje.
- Se utilizeaza un semafor. Doar un miner va trece pentru a putea lua mesaje de la 
vrajitori. Ia initial un mesaj si verifica daca este de tipul "EXIT". Daca nu este 
de tipul "Exit" salveaza "current room" in variabila "parent" (current room primit 
de la vrajitori va fi defapt "parent room" in mesajul pe care il trimite un miner) 
si mai ia un mesaj de la vrajitori. Vrajitorii fiind sincronizati, un miner va lua 
sigur un mesaj de tipul "current room" si un mesaj de tipul "adjacent room".
- Se apeleaza semaphore.release() pentru a lasa alt miner sa preia mesaje.
- Daca minerul a primit un mesaj de tipul "EXIT" => isi incheie executia.
- Daca camera adiacenta primita a fost deja rezolvata => o ignora.
- Daca camera nu a fost rezolvata, se adauga in Set-ul cu camere rezolvate (aceasta 
operatie se face intr-un bloc synchronized pentru ca nu cumva mai multi mineri sa 
adauge camere in acelasi timp).
- Se calculeaza string-ul ce va fi trimis vrajitorilor (cu functia de hash din schelet).
- Se creeaza mesajul ce va fi transmis.
- Se pune mesajul in bufferul P-C in care minerii scriu si vrajitorii citesc.

