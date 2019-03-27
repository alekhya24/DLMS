
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Replica1 {
    public DCMSServerImplementation mcgillServer;
    public DCMSServerImplementation concordiaServer;
    public DCMSServerImplementation montrealServer;
    static HashMap<String,DCMSServerImplementation> serverRepository;


    public Replica1(DCMSServerImplementation mcgillServer,DCMSServerImplementation concordiaServer,DCMSServerImplementation montrealServer){
        this.mcgillServer = mcgillServer;
        this.concordiaServer = concordiaServer;
        this.montrealServer = montrealServer;
    }

    public void startReplica(int replicaPort) throws IOException{
        DatagramSocket socket = new DatagramSocket(replicaPort);
        DatagramPacket packet = null;
        byte[] data = null;
        System.out.println("Replica one started");
        while (true){
            data = new byte[1024];
            packet = new DatagramPacket(data,data.length);
            socket.receive(packet);
            System.out.println("Replica one message"  + new String(packet.getData(), 0, packet.getLength()));
            String recievedData =   new String(packet.getData(), 0, packet.getLength());
            String serverImplementation = recievedData.split(":")[0];
            Thread thread = new Thread((new ExecuteFunction(socket,packet,getServerImplementation(serverImplementation))));
        }

    }
    public DCMSServerImplementation getServerImplementation(String  serverImplementation){
        if (serverImplementation.equalsIgnoreCase("MCG")){
            return this.mcgillServer;
        }else if (serverImplementation.equalsIgnoreCase("CON")){
            return this.concordiaServer;
        }else {
            return this.montrealServer;
        }
    }

    public static void main(String args[]) throws IOException{

        DCMSServerImplementation mcgillServer = new DCMSServerImplementation(com.Server.Constants.ServerLocation.MCG);
        DCMSServerImplementation concordiaServer = new DCMSServerImplementation(com.Server.Constants.ServerLocation.CON);
        DCMSServerImplementation montrealServer = new DCMSServerImplementation(com.Server.Constants.ServerLocation.MON);
        serverRepository = new HashMap<>();
        serverRepository.put("MCG",mcgillServer);
        serverRepository.put("MON",concordiaServer);
        serverRepository.put("CON",montrealServer);


        Replica1 replica1 = new Replica1(mcgillServer,concordiaServer,montrealServer);
        Runnable replicaTask = () -> {
            try {
                replica1.startReplica(1111);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread replicaThread = new Thread(replicaTask);
        replicaThread.start();


    }



}
