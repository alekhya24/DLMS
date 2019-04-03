package Replica3;

import java.io.*;
import java.net.*;
import java.nio.file.Paths;

import Replica3.resource.*;

public class Server implements Runnable {
	
	public static final int CON_PORT = 4777;
	public static final int MCG_PORT = 4778;
	public static final int MON_PORT = 4779;
	

	public Log log;
	private int port;
	private String[] args;
	private ServerSocket serverSocket;
	protected DLMSServerImplementation library;
	protected LibraryName libraryName;
	

	public void initBook(String itemID, String itemName, int quantity) {
		library.initBook(itemID, itemName, quantity);
	}
	
	public Server(String[] args, int port) {
		this.port = port;
		this.args = args;
		
		switch (port) {
			case CON_PORT : 
				libraryName = LibraryName.CON; break;
			case MCG_PORT : 
				libraryName = LibraryName.MCG; break;
			case MON_PORT : 
				libraryName = LibraryName.MON; break;
		}
		
		createLog();
		library = new DLMSServerImplementation(this, libraryName);
	}
	
	private void createLog() {
		String rootDir = (Paths.get("").toAbsolutePath().toString());
		String[] path = {rootDir, "src", "server", "logs", libraryName.toString() + "_server_log.txt"};
		log = new Log(path);
	}
	
	public void run() {
		try {
			System.out.println("Server(" + libraryName + ") is Started.");
			log.write("Server(" + libraryName + ") is Started.");
			serverSocket = new ServerSocket(port);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				new Thread(new ClientHandler(this, clientSocket)).start();
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		} 
	}
	
	public DLMSServerImplementation getLibraryImplementation() {
		return library;
	}
	
	public static void main(String[] args) {
		Server CONServer = new Server(args, CON_PORT);
		Server MCGServer = new Server(args, MCG_PORT);
		Server MONServer = new Server(args, MON_PORT);
		
		new Thread(CONServer).start();
		new Thread(MCGServer).start();
		new Thread(MONServer).start();
	}
}

class ClientHandler implements Runnable {
	
	private Server server;
	private Socket socket;
	
	public ClientHandler(Server server, Socket clientSocket) {
		this.server = server;
		socket = clientSocket;
	}
	
	@Override
	public void run() {
		try {
			BufferedReader inFromClient = 
						new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String clientMsg = inFromClient.readLine();
			String replyMsg = "";
			String[] splitMsg = clientMsg.split(";");
			DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
				
			switch (splitMsg[0]) {
				case "Borrow" :{
					replyMsg = server.library.borrowItem(new ID(splitMsg[1]), new ID(splitMsg[2]), Integer.parseInt(splitMsg[3]));
					outToClient.writeBytes(replyMsg + "\n");
					break;
				}
				case "Find" : {
					replyMsg = server.library.findItem(new ID(splitMsg[1]), splitMsg[2], false);
					outToClient.writeBytes(replyMsg + "\n");
					break;
				}
				case "Return" : {
					replyMsg = server.library.returnItem(new ID(splitMsg[1]), new ID(splitMsg[2]));
					outToClient.writeBytes(replyMsg + "\n");
					break;
				}
				case "AddList" : {
					replyMsg = server.library.addWaitList(new ID(splitMsg[1]), new ID(splitMsg[2]));
					outToClient.writeBytes(replyMsg + "\n");
					break;
				}
				case "HasBorrowed" : {
					replyMsg = server.library.hasBorrowed(new ID(splitMsg[1]), new ID(splitMsg[2]));
					outToClient.writeBytes(replyMsg + "\n");
					break;
				}
				default : break;
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		}
 	}
}
