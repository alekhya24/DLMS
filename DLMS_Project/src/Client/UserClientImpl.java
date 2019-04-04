package Client;

import java.io.File;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import FEApp.feInterface;
import FEApp.feInterfaceHelper;
import Util.Constants;
import Util.LogClient;
import Util.Servers;

public class UserClientImpl {
	Util.LogClient logClient = null;

	feInterface obj=null;

	UserClientImpl(String[] args, String UserId)
	{
	try
	{
	String folder="";
	ORB orb = ORB.init(args, null);
	org.omg.CORBA.Object objRef =   orb.resolve_initial_references("NameService");
	NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	//if ((server == Servers.CON)|| (server == Servers.MCG) 
		//	|| (server == Servers.MON)){
		//folder=server.getserverName().toString();
		obj = feInterfaceHelper.narrow(ncRef.resolve_str(Constants.SERVER_NAME.toString()));
	//}
	boolean userID = new File(Constants.LOG_DIR+folder +"\\"+UserId).mkdir();
	logClient = new LogClient(folder+"\\"+UserId+"\\",UserId);
	}
	catch(Exception e) {
		System.out.println("ERROR : " + e);
		e.printStackTrace(System.out);
	}		
	}
	
	public String borrowItem(String userId, String itemId,boolean isWaitlisted)
	{
		logClient.logger.info("UserClient: Initiating Borrow Item");
		String result = obj.borrowItem(userId, itemId,isWaitlisted);
		logClient.logger.info("Success");
		return result;
	}
	
	public String findItem(String userId,String itemName)
	{
		logClient.logger.info("UserClient: Initiating Find Item");
		String result=obj.findItem(userId, itemName);
		logClient.logger.info(result);
		return result;
	}
	
	public String returnItem(String userId,String itemId)
	{
		logClient.logger.info("UserClient: Initiating Return Item");
		String result=obj.returnItem(userId, itemId);
		logClient.logger.info(result);
		return result;
	}
	
	public String exchangeItem(String userId,String oldItemId,String newItemId)
	{
		logClient.logger.info("UserClient: Initiating Exchange Item");
		String result=obj.exchangeItem(userId, oldItemId, newItemId);
		logClient.logger.info(result);
		return result;
	}
}
