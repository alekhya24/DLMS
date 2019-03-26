package Client;

import java.io.File;
import java.rmi.RemoteException;
import java.util.logging.Level;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import FEApp.feInterface;
import FEApp.feInterfaceHelper;
import Util.Constants;
import Util.LogClient;
import Util.Servers;

public class ManagerClientImpl {

	Util.LogClient logClient = null;
	feInterface obj=null;

	ManagerClientImpl(String[] args,Servers server, String managerId)
 {
		try
		{
		String folder="";
		ORB orb = ORB.init(args, null);
		org.omg.CORBA.Object objRef =   orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		if ((server == Servers.CON)|| (server == Servers.MCG) 
				|| (server == Servers.MON)){
			folder=server.getserverName().toString();
			obj = feInterfaceHelper.narrow(ncRef.resolve_str(server.toString()));
		}
		boolean mgrID = new File(Constants.LOG_DIR+folder +"\\"+managerId).mkdir();
		logClient = new LogClient(folder+"\\"+managerId+"\\",managerId);
		}
		catch(Exception e) {
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}
	}
	
	
	public String addItem(String managerId,String itemId,String itemName,int quantity)
	{
		logClient.logger.info("ManagerClient: Initiating Add Item");
		String result=obj.addItem(managerId, itemId, itemName, quantity);
		logClient.logger.log(Level.INFO, result);
		return result;
	}
	
	public String listItemAvailability(String managerId)
	{
		logClient.logger.info("ManagerClient: Initiating listItemAvailability");	
		String output=obj.listItemAvailability(managerId);
			logClient.logger.log(Level.INFO, output.toString());
		return output;
	}
	
	public String removeItem(String managerId,String itemId,int quantity)
	{
		logClient.logger.info("ManagerClient: Initiating Remove Item");
		String result=obj.removeItem(managerId, itemId, quantity);
logClient.logger.log(Level.INFO, result);
		return result;
	}
}
