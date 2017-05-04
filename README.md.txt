Readme:
I)Introduction
The following code simulates a Chat Application using TCP/IP connection, which facilitates multiple clients (upto 10) to connect to a server. After connecting to the server, the clients can send/ recieve data to/from one another. The clients can unicast , broadcast or block cast messages to other clients. The client can also unicast or broadcast files to various clients. Here the port that the clients and the server could connect is 1908 which is hard coded(Assumption). Some code changes can be done in future to facilitate the port number as input via command line arguments.

II)Files included:
ChatServerThread.java
ChatServerThread.class
ChatClientThread.java
ChatClientThread.class
clientConnThread.class

III)How to run:
1) Download the file and place it in a folder. (The file contains .class and .java files)
2) Go to the desired folder where you have placed the files.
3) Start the server :Run the command 'java ChatServerThread' in the command prompt.
4) Repeat steps 2 for starting the client.
5) Start the client :Run the command 'java ChatClientThread' in the command prompt.
6) Repeat steps 4 and 5 for each client you wish to connect to.
7) The following are the commands that you can use:
	1) To Broadcast messge : <message>
	2) To Unicast message  : <@username message>
	3) To Blockcast message: <^username message>
	4) To Broadcast a file : <ffile 'path'>
	5) To unicast a file   : <#file 'path' username>
	6) To logout           : </quit>
Use the above mentioned commands to chat with other clients.

IV) Errors, Exception and Messages:
The system will prompt the necessary messages, exceptions and errors in following cases:
1) When unicast/ block cast message command format is not proper.
2) When unicast and brodcast file command format is not proper.
3) When the file path is invalid.
4) When the file is not found for transfer.
5) User enters the chatroom.
6) User exits the chat room.
7) When the reciever for the file/message unicast does not exist.

V) Behaviour:
1) When clients get added '*** A new user uname entered the chat room !!! ***' message is displayed at the server and at the existing clients.
2) Whenever a client sends a broad cast message '<SentFrom> Messgage' pops up at the reciever end, no message at the sender/server end.
3) Whenever a client sends a unicast message '<SentFrom> Messgage' pops up at the reciever end, '>SentFrom> Message' pops at the sender end and no message at the server end.
4) Whenever a client sends a unicast message '<SentFrom> Messgage' pops up at the unblocked reciever end, '=SentFrom> Message' pops at the sender end and no message at the server end and the blocked client end.
5) Whenever a client unicasts/broadcasts a file '<SentFrom> Sent you a file' pops up at the reciever end, 'Sending file ... 100% complete!' pops at the sender end and 'File recieved from sendername.Sending file ... 100% complete!' at the client end.
6) The following folder structure is maintained for both the client and server :
	Client -> Client 1 ->file
	       -> Client 2 ->file
	Server -> Client 1 ->file
	       -> Client 1 ->file
7) When a Client 1 sends a file from its folder to the client 2, the server chrcks if there is a server folder for that client, if not then creates and maintains a copy of file in that folder. It then transmits the file to the Client 2 in its client folder.

