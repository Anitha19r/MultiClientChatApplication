import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatClientThread implements Runnable {

  // Creating client socket for connection
  private static Socket clientSocket = null;
  // The output stream
  private static PrintStream os = null;
  // The input stream
  private static DataInputStream is = null;

  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  private static boolean error=false;
  
  
  
  public static void main(String[] args) throws Exception {

    // The default port.
    int portNumber = 1908;
    // The default host.
    String host = "localhost";

    if (args.length < 2) {
      System.out.println("Welcome to Computer Network Chat application\n"
              + "Currently using host=" + host + ", portNumber=" + portNumber);
    } else {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }

    try {
      clientSocket = new Socket(host, portNumber);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());
      is = new DataInputStream(clientSocket.getInputStream());
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host+"\n");
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to the host \n"
          + host);
    }
    if (clientSocket != null && os != null && is != null) {
      try {

        /* Create a thread to read from the server. */
        new Thread(new ChatClientThread()).start();
        while (!closed) {
        	/*Check file transfers*/
        try{
        String inp = inputLine.readLine();
        
          if ((inp.startsWith("#file"))||
        		  (inp.startsWith("ffile"))){
        	  String inArr [] = inp.split("\\s");
        	  checkFileFormat(inArr);
        	  if(!error){	  
        	  Pattern pattern = Pattern.compile("'(.*?)'");
              Matcher matcher = pattern.matcher(inp);
              matcher.find();
        	  
        	  File file = new File(
                      matcher.group(1));
        	  
              FileInputStream fis = new FileInputStream(file);
              BufferedInputStream bis = new BufferedInputStream(fis); 
              
                      
              //Read File Contents into contents array 
              byte[] contents;
              long fileLength = file.length(); 
              long current = 0;
              os.println(inp);
              os.println(fileLength);//Write into and from the buffer until we have exhausted the size 
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
                 
                 os.write(contents);
                 System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!\n");
              }   
             
             os.flush(); 
             bis.close();
        	}
          }
          else{
        	  os.println(inp.trim());
          }
        }
          catch (Exception e){
              //e.printStackTrace(); 
        	  System.out.println("*Error* " +e.getMessage());
              System.out.println("\nPlease provide proper Input.\n ");
              }
        }
        /*
         * Close the output stream, close the input stream, close the socket.
         */
        os.close();
        is.close();
        clientSocket.close();
      } catch (IOException e) {
        System.err.println("IOException:  " + e);
        
      }
      catch (Exception e){
          //e.printStackTrace(); 
    	  System.out.println("*Error* " +e.getMessage());
          System.out.println("\nPlease provide proper Input.\n ");
          }
    }
  }
  private static void checkFileFormat(String [] words){	  
	  error=false;
	  if (words[0].startsWith("#file") && words.length != 3){
		  System.out.println("Invalid file transfer format. Please refer the menu.\n");
			error=true;
	  }
	 
	  
	  if((words[0].startsWith("ffile") && words.length != 2)){
		  System.out.println("Invalid file transfer format. Please refer the menu.\n");
			error=true;
	  }

  }
  private String findPath(String cName,String path)
  {
	  
  	Path path1 = Paths.get(path);
	String fileName = path1.getFileName().toString();		//Server.txt
	Path parentPath = path1.getParent();					//C:\Users\Anitha\Downloads\Client\Ani\
	String uName = parentPath.getFileName().toString();		//Ani
	Path clientPath =parentPath.getParent();				//C:\Users\Anitha\Downloads\Client
	Path parent = clientPath.getParent();					//C:\Users\Anitha\Downloads
	String finalDest = parent+File.separator+"Client";		//C:\Users\Anitha\Downloads\Server\
	File servDir = new File(finalDest);
    if (!servDir.exists()){
    	servDir.mkdir();
    }
    String serverPath = finalDest+File.separator+cName;
    finalDest= serverPath+File.separator+fileName;
    File finalDir = new File(serverPath);
	if (!finalDir.exists()) {
		finalDir.mkdir();
	}
	return finalDest;  
  }
  
  public void run() {
    String responseLine;
    try {
      while ((responseLine = is.readLine()) != null) {
    	try{
	    	if(responseLine.startsWith("*InFile-")){
	    	  String [] arg = responseLine.split("-");	    	  
	    	  int fileLen = Integer.parseInt(arg[1]);
	    	  String clientName = arg[3].substring(1);
	    	  
	    	  byte[] contents = new byte[10000];
	    	  String path = findPath(clientName,arg[2]);
	  		  FileOutputStream fos = new FileOutputStream(path);
	  	      BufferedOutputStream bos = new BufferedOutputStream(fos);
	  	      //No of bytes read in one read() call
	  	      int bytesRead = 0; 
	  	      long rem=fileLen; 
	  	      while((((bytesRead=is.read(contents,0,(int)Math.min(contents.length,rem))))!=-1) && (rem > 0)){
	  	          bos.write(contents, 0, bytesRead); 
	  	          rem-=bytesRead;
	    	}
	  	      
	  	      bos.flush();       
	  	      
	  	      fos.close();
	  	      //System.out.println("File recieved from ");
	    	}
	    	else{
	    		System.out.println(responseLine);
	    	}        
	        if (responseLine.indexOf("*** Bye") != -1)
	          break;
      }catch (Exception e){
          //e.printStackTrace(); 
    	  System.out.println("*Error* " +e.getMessage());
          System.out.println("\nPlease provide proper Input.\n ");
          }
      }
      closed = true;
    } catch (IOException e) {
    	System.out.println("*Error* " +e.getMessage());
    	if(e.getMessage().equals("Connection reset")){
    		closed = true;
  		  }
    	else{
    	System.out.println("\nPlease provide proper Input.\n ");
    	}
    }
    catch (Exception e){
        //e.printStackTrace(); 
    	System.out.println("*Error* " +e.getMessage());
        System.out.println("\nPlease provide proper Input.\n ");
        }
  }
}