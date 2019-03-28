package Util;

import java.util.HashMap;
import java.util.Map;

public class Constants {
	
	public static int CON_PORT_NUM=1234;
	public static int MCG_PORT_NUM=2345;
	public static int MON_PORT_NUM=3456;
	
	public static int UDP_PORT_NUM_CON = 9999;
	public static int UDP_PORT_NUM_MCG = 1111;
	public static int UDP_PORT_NUM_MON = 7777;
	
	public static String PROJECT_DIR = System.getProperty("user.dir");
	public static String LOG_DIR = PROJECT_DIR+"\\Logs\\";
	
	public static String HOST_NAME = "127.0.0.1";
	public static String ORB_INITIAL_PORT = "1050";
	public static String SERVER_NAME = "front_end";
	
	// Request Generator IP and Port information
	static String REQUESTID_GENERATOR_IP = "127.0.0.1";
	static int REQUESTID_GENERATOR_PORT = 3000;
	
	public static int LOCAL_LISTENING_PORT = 3500;
	
	// Leader Host IP and Port
	static String PRIMARY_SERVER_IP = "127.0.0.1";
	public static int PRIMARY_SERVER_PORT = 6000;
	
	// Front end hash table for storing the request information in case of message lost.
	// First String is requestID, second String is request message for sending to leader host.like below
	// key:0001    value: 0001
	//   				  001
	//					  firstName
	//					  ....
	static Map<String, String> REQUEST_HASH_TABLE = new HashMap<String, String>();
	public static HashMap<Integer, String> liveHostsByName = new HashMap<Integer, String>();
}

