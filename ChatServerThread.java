import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatServerThread {

  // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;
  // This chat server can accept up to maxClientsAllowed clients' connections.
  private static final int maxClientsAllowed = 10;
  private static boolean connected = false;
  private static final clientConnThread[] clientConn = new clientConnThread[maxClientsAllowed];

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 1908;
    if (args.length < 1) {
      System.out.println("Welcome to Computer Network Chat application \n"
          + "Currently using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    /*
     * Open socket for clients to connect to the port 1908
     */
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        connected = true;
        int i = 0;
        for (i = 0; i < maxClientsAllowed; i++) {
          if (clientConn[i] == null) {
            (clientConn[i] = new clientConnThread(clientSocket, clientConn,connected)).start();
            break;
          }
        }
        if (i == maxClientsAllowed) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

/*This class acts as the handler class and decides which client to send data to 
  It maintains the client threads and connects and sends data and file pertaining to the appropriate clients.*/
class clientConnThread extends Thread {

  private String clientName = null;
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientConnThread[] clientConn;
  private int maxClientsAllowed;
  private boolean connected = false;
  private boolean error = false;
  
  public clientConnThread(Socket clientSocket, clientConnThread[] clientConn, boolean connected) {
    this.clientSocket = clientSocket;
    this.clientConn = clientConn;
    this.connected = connected;
    maxClientsAllowed = clientConn.length;
    
  }

  private String findPath(String path)
  {
	  //C:\Users\Anitha\Downloads\Client\Ani\Server.txt
	 
	Path path1 = Paths.get(path);
	String fileName = path1.getFileName().toString();					//Server.txt
	Path parentPath = path1.getParent();								//C:\Users\Anitha\Downloads\Client\Ani\
	String uName = parentPath.getFileName().toString();					//Ani
	Path clientPath =parentPath.getParent();							//C:\Users\Anitha\Downloads\Client
	Path parent = clientPath.getParent();								//C:\Users\Anitha\Downloads
	String finalDest = parent+File.separator+"Server";					//C:\Users\Anitha\Downloads\Server\
	File servDir = new File(finalDest);
    if (!servDir.exists()){
    	servDir.mkdir();
    }
    String serverPath = finalDest+File.separator+uName;
    finalDest= serverPath+File.separator+fileName;
    File finalDir = new File(serverPath);
	if (!finalDir.exists()) {
		finalDir.mkdir();
	}
	return finalDest;  
  }
  public void welcomeUsers(String name){
	  

      /* Message to the new client. */
      os.println("Welcome " + name
          + " to our chat room.\nTo leave enter /quit in a new line.\n"
          + "The following are the commands that you can use:\n"
          + "1) To Broadcast messge : <message>\n"
          + "2) To Unicast message  : <@username message>\n"
          + "3) To Blockcast message: <^username message>\n"
          + "4) To Broadcast a file : <ffile 'path'> \n"
          + "5) To unicast a file   : <#file 'path' username> \n"
          + "6) To logout           : </quit>\n"
          + "Pease enter your commands.\n");
      System.out.println("*** A new user " + name + " entered the chat room !!! ***\n");
      synchronized (this) {
        for (int i = 0; i < maxClientsAllowed; i++) {
          if (clientConn[i] != null && clientConn[i] == this) {
            clientName = "@" + name;
            break;
          }
        }
        for (int i = 0; i < maxClientsAllowed; i++) {
          if (clientConn[i] != null && clientConn[i] != this) {
            clientConn[i].os.println("*** A new user " + name
                + " entered the chat room !!! ***\n");
          }
        }
      }
  }
  public void checkMessages(String clientInput, String name){
	  boolean checkClient=false;
	  error=false;
	  if (clientInput.startsWith("^")) {
          String[] words = clientInput.split("\\s", 2);
          if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            String check = words[0].replace((words[0].charAt(0)),'@');
            if (!words[1].isEmpty()) {
              synchronized (this) {
                for (int i = 0; i < maxClientsAllowed; i++) {
                  if (clientConn[i] != null 
                      && clientConn[i].clientName != null
                      ) {
                	if (!(clientConn[i].clientName.equals(check)) && (!clientConn[i].clientName.equals(this.clientName)))
                	{
                    clientConn[i].os.println("<" + name + "> " + words[1]);
                    checkClient=true;
                	}
                     	
                    
                  }
                }
                if(!checkClient){
            		os.println("Invalid user for block cast format. Please enter a valid user name\n");
            		error=true;
            	}
                else{
                	 this.os.println("=" + name + "> " + words[1]);
                }
               
               
              }
            }
            else{
            	this.os.println("Invalid block cast format. Please enter ^<uname> message\n");
            	error=true;
            }
          }
        }
        /*Send unicast messages */
          else  if (clientInput.startsWith("@")) {
          String[] words = clientInput.split("\\s", 2);
          if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
              synchronized (this) {
                for (int i = 0; i < maxClientsAllowed; i++) {
                  if (clientConn[i] != null && clientConn[i] != this
                      && clientConn[i].clientName != null
                      && clientConn[i].clientName.equals(words[0])) {
                	
                    clientConn[i].os.println("<" + name + "> " + words[1]);
                    checkClient=true;
                    /*
                     * Echo this message to let the client know the private
                     * message was sent.
                     */
                    
                  }
                }
                if(!checkClient){
            		os.println("Invalid user for unicast. Please enter a valid user name\n");
            		error=true;
            	}
                else{
                this.os.println(">" + name + "> " + words[1]);
                }
                
              }
            }
            else{
            	this.os.println("Invalid unicast format. Please enter @<uname> message\n");
            	error=true;
            }
          }
        } else {
          /* The message is public, broadcast it to all other clients. */
          synchronized (this) {
            for (int i = 0; i < maxClientsAllowed; i++) {
              if (clientConn[i] != null && clientConn[i].clientName != null /*&& this.clientName != ('@'+name)*/) {
            	  if(clientConn[i].clientName!=this.clientName)
            		  clientConn[i].os.println("<" + name + "> " + clientInput);
              }
            }
          }
        }
	  
  }
  public void checkFile(String [] words){	
	  error=false;
	  if (words[0].startsWith("#file") && words.length != 3){
		  this.os.println("Invalid file transfer format. Please refer the menu.\n");
			error=true;
	  }
	  if((words[0].startsWith("ffile") && words.length != 2)){
		  this.os.println("Invalid file transfer format. Please refer the menu.\n");
			error=true;
	  }

  }
  public String getFile(String clientInput,String [] words,long fileLength) throws NumberFormatException, IOException
  {
	  Pattern pattern = Pattern.compile("'(.*?)'");
      Matcher matcher = pattern.matcher(clientInput);
      matcher.find();
      String path = findPath(matcher.group(1));
		  byte[] contents = new byte[10000];
		  FileOutputStream fos = new FileOutputStream(path);
	      BufferedOutputStream bos = new BufferedOutputStream(fos);
	      //No of bytes read in one read() call
	      int bytesRead = 0; 
	      long rem=fileLength; 
	      while((((bytesRead=is.read(contents,0,(int)Math.min(contents.length,rem))))!=-1) && (rem > 0)){
	          bos.write(contents, 0, bytesRead); 
	          rem -= bytesRead;
	      }
	      bos.flush();       
	      fos.close();
	      System.out.println("File recieved from " +this.clientName.substring(1));
	      return path;
	  
  }
  public void unicastFile(String clientInput,String[] words, String path, long fileLength) throws Exception{
	  File file = new File(path); 
	  boolean checkClient=false;
	  error=false;
	  synchronized (this) {
          for (int i = 0; i < maxClientsAllowed; i++) {
            if (clientConn[i] != null && clientConn[i] != this
                && clientConn[i].clientName != null
                && clientConn[i].clientName.equals('@'+words[2])) {
         	   long current = 0;
         	  byte[] contents = new byte[10000];
         	   FileInputStream fis = new FileInputStream(file);
      	      BufferedInputStream bis = new BufferedInputStream(fis);   
      	      clientConn[i].os.println("*InFile-"+fileLength+"-"+path+"-"+clientConn[i].clientName+"-");
         	   while(current!=fileLength){ 
       	         int size = 10000;
       	         if(fileLength - current >= size)
       	             current += size;    
       	         else{ 
       	             size = (int)(fileLength - current); 
       	             current = fileLength;
       	         } 
       	         contents = new byte[size]; 
       	         bis.read(contents, 0, size);
                  clientConn[i].os.write(contents);
              /*
               * Echo this message to let the client know the private
               * message was sent.
               */
                  System.out.print("Sending file to "+clientConn[i].clientName.substring(1)+" ... "+(current*100)/fileLength+"% complete!\n");
       	     }
         	    os.flush(); 
                bis.close();
                clientConn[i].os.println(">"+this.clientName.substring(1)+ ">Sent you a file!\n");
                checkClient=true;
            }
          }
          if(!checkClient){
      		os.println("Invalid user for unicast file.  Please enter a valid user name\n");
      		error=true;
      	  }         
 	 }
	  
  }
  public void broadcastFile(String clientInput,String [] words,String path,long fileLength) throws Exception{
	  synchronized (this) {
	    File file = new File(path); 
      	for (int i = 0; i < this.maxClientsAllowed; i++) {
             if (clientConn[i] != null && clientConn[i].clientName != null ) {
          	  FileInputStream fis = new FileInputStream(file);
       	      BufferedInputStream bis = new BufferedInputStream(fis);  
       	      long current = 0;
       	      byte[] contents = new byte[10000];
       	      clientConn[i].os.println("*InFile-"+fileLength+"-"+path+"-"+clientConn[i].clientName+"-");
          	   while(current!=fileLength){ 
          	         int size = 10000;
          	         if(fileLength - current >= size)
          	             current += size;    
          	         else{ 
          	             size = (int)(fileLength - current); 
          	             current = fileLength;
          	         } 
          	         contents = new byte[size]; 
          	         bis.read(contents, 0, size);
                     clientConn[i].os.write(contents);
                 /*
                  * Echo this message to let the client know the private
                  * message was sent.
                  */
                     System.out.print("Sending file to "+clientConn[i].clientName.substring(1)+" ... "+(current*100)/fileLength+"% complete!\n");
          	     }
          	     os.flush(); 
                 bis.close();
                 if(clientConn[i].clientName!=this.clientName)
                 clientConn[i].os.println("<"+this.clientName.substring(1)+ ">Sent you a file!\n");
                 
             }
           }
         }
  }
  public void closeConnection(String name) throws Exception{
	  if (!this.connected){
          synchronized (this) {
            for (int i = 0; i < maxClientsAllowed; i++) {
              if (clientConn[i] != null && clientConn[i] != this
                  && clientConn[i].clientName != null) {
                clientConn[i].os.println("*** The user " + name
                    + " left the chat room !!! ***\n");
              }
            }
          }
          os.println("*** Bye " + name + " ***");
          System.out.println("*** The user " + name+ " left the chat room !!! ***\n");
          }
          synchronized (this) {
            for (int i = 0; i < maxClientsAllowed; i++) {
              if (clientConn[i] == this) {
                clientConn[i] = null;
              }
            }
          }
          /*
           * Close the output stream, close the input stream, close the socket.
           */
          is.close();
          os.close();
          clientSocket.close();
  }
  public void run() {
    int maxClientsAllowed = this.maxClientsAllowed;
    clientConnThread[] clientConn = this.clientConn;
    boolean connected = this.connected;

    try {
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      String name;
      while (true) {
        os.println("Enter your name.");
        name = is.readLine().trim();
       
        if (name.indexOf('@') == -1 && name.indexOf('^') == -1 && name.indexOf('#') == -1 
        		&& !name.startsWith("ffile") && !name.startsWith("#file")) {
          break;
        } else {
          os.println("The name should not contain '@','^','#','ffile','#file' characters.\n");
          
        }
      }
      welcomeUsers(name);
      while (this.connected) {        
        /*check for file send*/
        try{
        	String clientInput = is.readLine();
        	String path = null;
        	long fileLength;
        	String switching [] = clientInput.split("\\s");
        	switch (switching[0]){
        	case "/quit":
        		this.connected =false;
        		closeConnection(name);
        		break;
        	case "#file":
        		checkFile(switching);
        		if(!error){
        		fileLength = Long.parseLong(is.readLine());
        		path=getFile(clientInput,switching,fileLength);
        		unicastFile(clientInput,switching,path,fileLength);
        		}
        		break;
        	case "ffile":
        		checkFile(switching);
        		if (!error){        			
        		fileLength = Long.parseLong(is.readLine());
        		path= getFile(clientInput,switching,fileLength);
        		broadcastFile(clientInput,switching,path,fileLength);
        		}
        		break;
        	default:
        		checkMessages(clientInput,name);
        		if(error){
        			break;
        		}
        		break;
        		
        	}
        }
      catch (Exception e){
      //e.printStackTrace(); 
    	  System.out.println("*Error* " +e.getMessage());
    	  if(e.getMessage().equals("Connection reset")){
    		  this.connected=false;
    		  closeConnection(name);
    		  break;}
    	  else
    	  os.println("\n Please provide proper I/P. \n");
      }
      }
      
    } catch (Exception e) {
    	//e.printStackTrace(); 
    	System.out.println("*Error* " +e.getMessage());
    	os.println("\n Please provide proper I/P. \n");
    }
  }
}

/*Certain references are taken from http://makemobiapps.blogspot.com/*/
