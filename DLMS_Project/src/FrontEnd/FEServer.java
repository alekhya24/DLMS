package FrontEnd;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import FEApp.feInterface;
import FEApp.feInterfaceHelper;
import Util.Constants;
import Util.Servers;

public class FEServer {
	static HashMap<String,FEImpl> serverData;
	public static void main(String[] args) throws Exception {	
		while ( true ) {
			try {
				InputStreamReader in = new InputStreamReader ( System.in ) ;
				BufferedReader r = new BufferedReader ( in ) ;
				System.out.println ( "Please Select:" ) ;
				System.out.println ( "1. Failure Tolerant" ) ;
				System.out.println ( "2. High Availibility" ) ;
				int choice = Integer.parseInt(r.readLine()) ;
				if ( choice == 1 ) {
					System.out.println ("The Failure Tolerant System is now ready to accept client requests") ;
					FEImpl.setFailureType("Fault Tolerance");
					break ;
				} else if ( choice == 2 ) {
					System.out.println ("The Highly Available System is now ready to accept client requests") ;
					FEImpl.setFailureType("High Availability");
					break ;
				} else {
					System.out.println( "You have entered a wrong choice" );
					System.out.println( "Press any key to continue: " ) ;
					r.readLine();
				}
			} catch ( IOException e ) {
				System.out.println( e.getMessage() ) ;
			}	catch ( NumberFormatException e ) {
				System.out.println( "You have entered a wrong choice. Please try again" ) ;
				continue ;
			} 	
		}		
		new File(Constants.LOG_DIR).mkdirs();
		new File(Constants.LOG_DIR+Servers.CON.getserverName().toString()).mkdirs();
		new File(Constants.LOG_DIR+Servers.MCG.getserverName().toString()).mkdir();
		new File(Constants.LOG_DIR+Servers.MON.getserverName().toString()).mkdir();	
		init_FE(args);
	}
	
	public static void init_FE(String[] args){
		try {
			Properties props = new Properties();
	        props.put("org.omg.CORBA.ORBInitialPort", Constants.ORB_INITIAL_PORT);
			ORB orb = ORB.init(args, props);
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();
//			FEImpl fe_Impl = new FEImpl();
//			fe_Impl.setORB(orb); 
//
//			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(fe_Impl);
//			feInterface href = feInterfaceHelper.narrow(ref);
//			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
//			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
//			String name = Constants.SERVER_NAME;
//			NameComponent path[] = ncRef.to_name(name);
//			ncRef.rebind(path, href);
			serverData = new HashMap<>();
			for (Servers location : Servers.values()) {
				FEImpl serverImpl = new FEImpl(location);
				serverData.put(location.toString(),serverImpl);
				serverImpl.setORB(orb); 
				org.omg.CORBA.Object ref = rootpoa.servant_to_reference(serverImpl);
				FEApp.feInterface href = feInterfaceHelper.narrow(ref);

				org.omg.CORBA.Object objRef =  orb.resolve_initial_references("NameService");
				NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
				NameComponent path[] = ncRef.to_name(location.toString().toUpperCase() );
				ncRef.rebind(path, href);

				System.out.println(location.toString().toUpperCase()+" Server ready and waiting ...");
		}
			

			System.out.println("Front End Server Ready And Waiting ...");
			for (;;){
				orb.run();
			}
		} catch (Exception e) {
			System.err.println("ERROR: " + e);
	        e.printStackTrace(System.out);
		}
		System.out.println("Front End Server Exiting ...");
	}

}
