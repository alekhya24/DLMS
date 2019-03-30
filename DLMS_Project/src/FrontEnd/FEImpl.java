package FrontEnd;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import org.omg.CORBA.ORB;
import FEApp.feInterfacePOA;
import Model.*;
import Sequencer.Port;
import Util.Constants;
import Util.LogManager;
import Util.Servers;



public class FEImpl extends feInterfacePOA {

	private static HashMap< String, InetSocketAddress > rmDetails = new HashMap <String, InetSocketAddress> ();
	public LogManager logManager;
	public static String FailureType;
	
	public static String getFailureType() {
		return FailureType;
	}

	public static void setFailureType(String failureType) {
		FailureType=failureType;
	}

	public String location;
	public FEImpl(Servers libraryLocation) {
		super();
		location=libraryLocation.toString();
		logManager=new LogManager(libraryLocation.getserverName().toString().toUpperCase());
	}
	
	private ORB orb;

	public void setORB(ORB orb_val) {
		orb = orb_val; 
	}
	
	private String getServer(String id)
	{
		if(id.contains("MON"))
		{
			return Servers.MON.name();
		}
		else if(id.contains("MCG"))
		{
			return Servers.MCG.name();
		}
		else
		{
			return Servers.CON.name();
		}
	}
	
	public String addItem(String managerID, String itemId, String itemName, int quantity) {
		String serverName=getServer(managerID);
		String request =Port.FE+ Constants.DELIMITER+ serverName+Constants.DELIMITER+"addItem"+Constants.REQUEST_DELIMITER + managerID + Constants.REQUEST_DELIMITER + itemId+Constants.REQUEST_DELIMITER+itemName+Constants.REQUEST_DELIMITER+quantity;
		logManager.logger.log(Level.INFO, " Sending request to Sequencer to Add Item: " + request);
		sendRequestToSequencer(request);
		String finalOp="";
		ArrayList<ServerResponse> responses;
		try {
			if(FailureType=="High Availability")
			{
				responses =	getResponseFromServerWithinTime();
			}
			else
			{
		responses =	getResponseFromServer();
			}
		finalOp=getMajority(responses);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalOp;
		}

	public String removeItem (String managerID,String itemID,int quantity) 
	{
		String serverName=getServer(managerID);
		String request = Port.FE+Constants.DELIMITER+serverName+Constants.DELIMITER+"removeItem" +Constants.REQUEST_DELIMITER+ managerID + Constants.REQUEST_DELIMITER + itemID+Constants.REQUEST_DELIMITER+quantity;
		logManager.logger.log(Level.INFO, " Sending request to Sequencer to Remove Item: " + request);
		sendRequestToSequencer(request);
		String finalOp="";
		ArrayList<ServerResponse> responses;
		try {
			if(FailureType=="High Availability")
			{
				responses =	getResponseFromServerWithinTime();
			}
			else
			{
		responses =	getResponseFromServer();
			}
		finalOp=getMajority(responses);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalOp;
		}
	
	
	public String listItemAvailability(String managerID)  {
		String serverName=getServer(managerID);
		String request = Port.FE+Constants.DELIMITER+serverName+Constants.DELIMITER+"listItemAvailability"+Constants.REQUEST_DELIMITER + managerID ;
		logManager.logger.log(Level.INFO, " Sending request to Sequencer to List Item Availability: " + request);
		sendRequestToSequencer(request);
		String finalOp="";
		ArrayList<ServerResponse> responses;
		try {
			if(FailureType=="High Availability")
			{
				responses =	getResponseFromServerWithinTime();
			}
			else
			{
		responses =	getResponseFromServer();
			}
		finalOp=getMajority(responses);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalOp;
		}
	
	
	public String findItem (String userID,String itemName)
	{
		String serverName=getServer(userID);
		String request =Port.FE+Constants.DELIMITER+ serverName+Constants.DELIMITER+"findItem"+Constants.REQUEST_DELIMITER + userID + Constants.REQUEST_DELIMITER+ itemName;
		logManager.logger.log(Level.INFO, " Sending request to Sequencer to Find Item: " + request);
		sendRequestToSequencer(request);
		String finalOp="";
		ArrayList<ServerResponse> responses;
		try {
			if(FailureType=="High Availability")
			{
				responses =	getResponseFromServerWithinTime();
			}
			else
			{
		responses =	getResponseFromServer();
			}
		finalOp=getMajority(responses);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalOp;
		}

	public String returnItem (String userID,String itemID) 
	{
		String serverName=getServer(userID);
		String request =Port.FE+Constants.DELIMITER+ serverName+Constants.DELIMITER+ "returnItem"+Constants.REQUEST_DELIMITER + userID + Constants.REQUEST_DELIMITER + itemID;
		logManager.logger.log(Level.INFO, " Sending request to Sequencer to Return Item: " + request);
		sendRequestToSequencer(request);
		String finalOp="";
		ArrayList<ServerResponse> responses;
		try {
			if(FailureType=="High Availability")
			{
				responses =	getResponseFromServerWithinTime();
			}
			else
			{
		responses =	getResponseFromServer();
			}
		finalOp=getMajority(responses);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalOp;
		}

	public String borrowItem(String userID, String itemID,boolean isWaitlisted)  {
		String serverName=getServer(userID);
		String request = Port.FE+":"+serverName+Constants.DELIMITER+ "borrowItem"+Constants.REQUEST_DELIMITER + userID + Constants.REQUEST_DELIMITER + itemID+Constants.REQUEST_DELIMITER+isWaitlisted;
		logManager.logger.log(Level.INFO, " Sending request to Sequencer to Borrow Item: " + request);
		 sendRequestToSequencer(request);
		 String finalOp="";
			ArrayList<ServerResponse> responses;
			try {
				if(FailureType=="High Availability")
				{
					responses =	getResponseFromServerWithinTime();
				}
				else
				{
			responses =	getResponseFromServer();
				}
			finalOp=getMajority(responses);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return finalOp;
			}
	
	
	public String exchangeItem(String userID, String oldItemId,String newItemId)  {
		String serverName=getServer(userID);
		String request = Port.FE+Constants.DELIMITER+serverName+Constants.DELIMITER+"exchangeItem"+Constants.REQUEST_DELIMITER + userID + Constants.REQUEST_DELIMITER + oldItemId+Constants.REQUEST_DELIMITER+newItemId;
		logManager.logger.log(Level.INFO, " Sending request to Sequencer to Exchange Item: " + request);
		 sendRequestToSequencer(request);
		 String finalOp="";
			ArrayList<ServerResponse> responses;
			try {
				if(FailureType=="High Availability")
				{
					responses =	getResponseFromServerWithinTime();
				}
				else
				{
			responses =	getResponseFromServer();
				}
			finalOp=getMajority(responses);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return finalOp;
			}
	
	
	public void sendRequestToSequencer(String data) {
		try {
			DatagramSocket ds = new DatagramSocket();
			byte[] dataBytes = data.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket dp = new DatagramPacket(dataBytes, dataBytes.length,
					aHost,Port.SEQUENCER);
			ds.send(dp);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public ArrayList<ServerResponse> getResponseFromServer() throws IOException {
ArrayList<ServerResponse> result = new ArrayList<ServerResponse> () ;
DatagramSocket ds = new DatagramSocket(Port.FE);
		try {
			//while ( true ) {
				byte[] receiveBuffer = new byte[512] ;
				DatagramPacket receivePacket = new DatagramPacket ( receiveBuffer, receiveBuffer.length );
				ds.receive(receivePacket);
				String bs = new String ( receivePacket.getData() ) ;
				String[] res=bs.split(Constants.DELIMITER);
				ServerResponse op = new ServerResponse(res[0],res[1]);
				result.add(op) ;
			//}
		} catch (SocketTimeoutException e ) {
			System.out.println(e.getMessage());
		}
		return result ;
	}
	
	public ArrayList<ServerResponse> getResponseFromServerWithinTime() throws IOException {
ArrayList<ServerResponse> result = new ArrayList<ServerResponse> () ;
DatagramSocket ds = new DatagramSocket();
ds.setSoTimeout(1000);
		try {
			while ( true ) {
				byte[] receiveBuffer = new byte[512] ;
				DatagramPacket receivePacket = new DatagramPacket ( receiveBuffer, receiveBuffer.length ) ;
				ds.receive(receivePacket);
				ByteArrayInputStream bs = new ByteArrayInputStream ( receivePacket.getData() ) ;
				ObjectInputStream is = new ObjectInputStream ( bs ) ;
				try {
					ServerResponse res = ( ServerResponse ) is.readObject() ;
					result.add(res) ;
				} catch ( ClassNotFoundException e ) {
					System.out.println ( e.getMessage() ) ;
				}
			}
		} catch (SocketTimeoutException e ) {
			System.out.println(e.getMessage());
		}
		return result ;
	}
	
	private String getMajority ( ArrayList<ServerResponse> response ) {
		System.out.println ( "Response size" + response.size() ) ;
		System.out.println ( "Responses are: " ) ;
		for ( ServerResponse b : response ) {
			System.out.println ( "Replica: " + b.getReplicaName() ) ;
			System.out.println ( "Response: "+ b.getResult() ) ;
		}
		if ( response.size() != rmDetails.size()  && response.size() != 0 ) {
		
			return response.get(0).getResult() ;			
		} 		
		String majorityResult="";
								
		int counter  = 0 ;					
		for ( ServerResponse r: response ) {
			if ( counter == 0 ) {
				majorityResult = r.getResult() ; 
				counter ++ ;
			}
			else if ( majorityResult == r.getResult() ) {
				counter ++ ;
			} 
			else {
				counter-- ;
			}
		}
		if ( counter == rmDetails.size() ) {
			return majorityResult ;
		}
		
		for ( ServerResponse r: response ) {
			if ( r.getResult() != majorityResult ) {
				notifyRM ( r.getReplicaName(), "" ) ;
				return majorityResult ;
			}
		}
		
		return "" ;
	}	
	
	private void notifyRM ( String replicaName, String libraryName ) {
		
		DatagramSocket socket = null ;
		try {
			
			socket = new DatagramSocket () ;
			String data = "FAILURE" + ":" + replicaName + ":" + libraryName ;
			byte[] sendBuffer = data.getBytes() ;
			DatagramPacket sendPacket = new DatagramPacket ( sendBuffer, sendBuffer.length, 
					rmDetails.get(replicaName).getAddress(), rmDetails.get(replicaName).getPort() ) ;
			socket.send(sendPacket);
		} catch ( SocketException e ) {
			System.out.println ( "Exception: " + e.getMessage() ) ;
		} catch ( IOException e ) {
			System.out.println ( "Exception: " + e.getMessage() ) ;
		} 
	}
	}
