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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.omg.CORBA.ORB;
import FEApp.feInterfacePOA;
import Model.*;
import Sequencer.Port;
import Util.Constants;
import Util.FailureHandling;
import Util.LogManager;
import Util.Servers;



public class FEImpl extends feInterfacePOA {

	public LogManager logManager;
	public static FailureHandling FailureType;
	DatagramSocket ds;
	public static FailureHandling getFailureType() {
		return FailureType;
	}

	public static void setFailureType(FailureHandling failureType) {
		FailureType=failureType;
	}

	public String location;
	public FEImpl() {
		super();
				try {
					ds= new DatagramSocket(Port.FE);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
		//location=libraryLocation.toString();
		//logManager=new LogManager(libraryLocation.getserverName().toString().toUpperCase());
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
		//logManager.logger.log(Level.INFO, " Sending request to Sequencer to Add Item: " + request);
		sendRequestToSequencer(request);
		String finalOp="";
		Map<Integer,ServerResponse> responses;
		try {
			//if(FailureType==FailureHandling.SoftwareCrash)
			//{
				//responses =	getResponseFromServerWithinTime();
			//}
			//else
			//{
		responses =	getResponseFromServer();
		if(responses.size()<2)
		{
			notifyRMCrash(responses,managerID);
		}
		//else
		//{//}
		finalOp=getMajority(responses);
		//}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalOp;
		}

	public String removeItem (String managerID,String itemID,int quantity) 
	{
		String serverName=getServer(managerID);
		String request = Port.FE+Constants.DELIMITER+serverName+Constants.DELIMITER+"removeItem" +Constants.REQUEST_DELIMITER+ managerID + Constants.REQUEST_DELIMITER + itemID+Constants.REQUEST_DELIMITER+quantity;
		//logManager.logger.log(Level.INFO, " Sending request to Sequencer to Remove Item: " + request);
		sendRequestToSequencer(request);
		String finalOp="";
		Map<Integer,ServerResponse>  responses;
		try {
			//if(FailureType==FailureHandling.SoftwareCrash)
			//{
				//responses =	getResponseFromServerWithinTime();
			//}
			//else
			//{
		responses =	getResponseFromServer();
		if(responses.size()<2)
		{
			notifyRMCrash(responses,managerID);
		}
			//}
		finalOp=getMajority(responses);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalOp;
		}
	
	
	public String listItemAvailability(String managerID)  {
		String serverName=getServer(managerID);
		String request = Port.FE+Constants.DELIMITER+serverName+Constants.DELIMITER+"listItemAvailability"+Constants.REQUEST_DELIMITER + managerID ;
		//logManager.logger.log(Level.INFO, " Sending request to Sequencer to List Item Availability: " + request);
		sendRequestToSequencer(request);
		String finalOp="";
		Map<Integer,ServerResponse>  responses;
		try {
			//if(FailureType==FailureHandling.SoftwareCrash)
			//{
				//responses =	getResponseFromServerWithinTime();
			//}
			//else
			//{
		responses =	getResponseFromServer();
		if(responses.size()<2)
		{
			notifyRMCrash(responses,managerID);
		}
			//}
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
		//logManager.logger.log(Level.INFO, " Sending request to Sequencer to Find Item: " + request);
		sendRequestToSequencer(request);
		String finalOp="";
		Map<Integer,ServerResponse>  responses;
		try {
			//if(FailureType==FailureHandling.SoftwareCrash)
			//{
				//responses =	getResponseFromServerWithinTime();
			//}
			//else
			//{
		responses =	getResponseFromServer();
		if(responses.size()<2)
		{
			notifyRMCrash(responses,userID);
		}
			//}
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
		//logManager.logger.log(Level.INFO, " Sending request to Sequencer to Return Item: " + request);
		sendRequestToSequencer(request);
		String finalOp="";
		Map<Integer,ServerResponse>  responses;
		try {
			//if(FailureType==FailureHandling.SoftwareCrash)
			//{
				//responses =	getResponseFromServerWithinTime();
			//}
			//else
			//{
		responses =	getResponseFromServer();
		if(responses.size()<2)
		{
			notifyRMCrash(responses,userID);
		}
			//}
		finalOp=getMajority(responses);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalOp;
		}

	public String borrowItem(String userID, String itemID,boolean isWaitlisted)  {
		String serverName=getServer(userID);
		String request = Port.FE+":"+serverName+Constants.DELIMITER+ "borrowItem"+Constants.REQUEST_DELIMITER + userID + Constants.REQUEST_DELIMITER + itemID+Constants.REQUEST_DELIMITER+isWaitlisted;
		//logManager.logger.log(Level.INFO, " Sending request to Sequencer to Borrow Item: " + request);
		 sendRequestToSequencer(request);
		 String finalOp="";
		 Map<Integer,ServerResponse>  responses;
			try {
				//if(FailureType==FailureHandling.SoftwareCrash)
				//{
					//responses =	getResponseFromServerWithinTime();
				//}
				//else
				//{
			responses =	getResponseFromServer();
			if(responses.size()<2)
			{
				notifyRMCrash(responses,userID);
			}
				//}
			finalOp=getMajority(responses);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return finalOp;
			}
	
	
	public String exchangeItem(String userID, String oldItemId,String newItemId)  {
		String serverName=getServer(userID);
		String request = Port.FE+Constants.DELIMITER+serverName+Constants.DELIMITER+"exchangeItem"+Constants.REQUEST_DELIMITER + userID + Constants.REQUEST_DELIMITER + oldItemId+Constants.REQUEST_DELIMITER+newItemId;
		//logManager.logger.log(Level.INFO, " Sending request to Sequencer to Exchange Item: " + request);
		 sendRequestToSequencer(request);
		 String finalOp="";
			Map<Integer,ServerResponse> responses;
			try {
				//if(FailureType==FailureHandling.SoftwareCrash)
				//{
					//responses =	getResponseFromServerWithinTime();
				//}
				//else
				//{
			responses =	getResponseFromServer();
			if(responses.size()<2)
			{
				notifyRMCrash(responses,userID);
			}
				//}
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
	
	public Map<Integer,ServerResponse> getResponseFromServer() throws IOException {
		Map<Integer,ServerResponse> result =new HashMap<Integer,ServerResponse>();
ds.setSoTimeout(2000);
		try {
			while ( true ) {
				byte[] receiveBuffer = new byte[512] ;
				DatagramPacket receivePacket = new DatagramPacket ( receiveBuffer, receiveBuffer.length );
				ds.receive(receivePacket);
				String bs = new String ( receivePacket.getData() ) ;
				String[] res=bs.split(Constants.DELIMITER);
				ServerResponse op = new ServerResponse(res[0],res[1]);
				result.put(Integer.parseInt(res[0]), op);
			}
		} catch (SocketTimeoutException e ) {
			System.out.println(e.getMessage());
		}
		return result ;
	}
	
	public Map<Integer,ServerResponse> getResponseFromServerWithinTime() throws IOException {
Map<Integer,ServerResponse> result =new HashMap<Integer,ServerResponse>();
ds.setSoTimeout(1000);
		try {
			while ( true ) {
				byte[] receiveBuffer = new byte[512] ;
				DatagramPacket receivePacket = new DatagramPacket ( receiveBuffer, receiveBuffer.length ) ;
				ds.receive(receivePacket);
				String bs = new String ( receivePacket.getData() ) ;
				String[] res=bs.split(Constants.DELIMITER);
				ServerResponse op = new ServerResponse(res[0],res[1]);
					result.put(Integer.parseInt(res[0]), op);
			}
		} catch (SocketTimeoutException e ) {
			System.out.println(e.getMessage());
		}
		return result ;
	}
	
	private String getMajority ( Map<Integer,ServerResponse> responses ) {
		System.out.println ( "Response size" + responses.size() ) ;
		System.out.println ( "Responses are: " ) ;
		for (Entry<Integer, ServerResponse> response : responses.entrySet()) {
			ServerResponse serverResponse= response.getValue();
			System.out.println ( "Replica: " + serverResponse.getReplicaName() ) ;
			System.out.println ( "Response: "+ serverResponse.getResult() ) ;
		}

		if ( responses.size() != 2  && responses.size() != 0 ) {
		
			return responses.get(0).getResult() ;			
		} 		
		String majorityResult="";
								
		int counter  = 0 ;					
		for (Entry<Integer, ServerResponse> response : responses.entrySet()) {
			ServerResponse r= response.getValue();
			if ( counter == 0 && !r.getResult().contains("failure")) {
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
		if ( counter ==3 ) {
			return majorityResult ;
		}
		
		for (Entry<Integer, ServerResponse> response : responses.entrySet()) {
			ServerResponse r= response.getValue();
			if ( r.getResult() != majorityResult && r.getResult().contains("failure") ) {
				notifyRMFailure(r.getReplicaName());
			}
		}
		
		return majorityResult ;
	}	
	
	private void notifyRM (String message ) {
		
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			InetAddress host = InetAddress.getByName(Constants.multicastAddr);
			byte[] data=message.getBytes();
			DatagramPacket request = new DatagramPacket(data, data.length, host, Port.MULTICAST);
			socket.send(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void notifyRMFailure(String replicaNo) {
		String msg=replicaNo+Constants.DELIMITER+FailureHandling.SoftwareFailure;
		System.out.println(msg);
		notifyRM(msg);
	}
	
	private void notifyRMCrash(Map<Integer,ServerResponse> responses,String id)
	{
		boolean replica1Notified = false;
		boolean replica2Notified=false;
		boolean replica3Notified=false;
		int i=0;
		while(i<2)
		{
	        if (!responses.containsKey(1) && !replica1Notified) {
	        	replica1Notified=true;
	            String msg = "1"+Constants.DELIMITER +Constants.DELIMITER+ FailureHandling.SoftwareCrash;
	            System.out.println(msg);
	            notifyRM(msg);
	        } else if (!responses.containsKey(2)&& !replica2Notified) {
	        	replica2Notified=true;
	            String msg = "2 " +Constants.DELIMITER +Constants.DELIMITER+ FailureHandling.SoftwareCrash;
	            System.out.println(msg);
	            notifyRM(msg);
	        } else if (!responses.containsKey(3)&& !replica3Notified) {
	        	replica3Notified=true;
	            String msg = "3 "+Constants.DELIMITER +getServer(id)+Constants.DELIMITER+ FailureHandling.SoftwareCrash;
	            System.out.println(msg);
	            notifyRM(msg);
	        }
	        i++;
		}
	}
}
