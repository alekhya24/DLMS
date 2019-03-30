package Replica2;

import Util.Constants;
import Util.Servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Replica2 {
    public DLMSServerImplementation mcgillServer;
    public DLMSServerImplementation concordiaServer;
    public DLMSServerImplementation montrealServer;
    static HashMap<String, DLMSServerImplementation> serverData;


    public Replica2(DLMSServerImplementation mcgillServer, DLMSServerImplementation concordiaServer, DLMSServerImplementation montrealServer){
        this.mcgillServer = mcgillServer;
        this.concordiaServer = concordiaServer;
        this.montrealServer = montrealServer;
    }

    public void startReplica(int replicaPort) throws IOException{
        DatagramSocket socket = new DatagramSocket(replicaPort);
        DatagramPacket packet = null;
        byte[] data = null;
        System.out.println("Replica two started");

        while (true){
            data = new byte[1024];
            packet = new DatagramPacket(data,data.length);
            socket.receive(packet);
            System.out.println("Replica two message"  + new String(packet.getData(), 0, packet.getLength()));
            String recievedData =   new String(packet.getData(), 0, packet.getLength());
            String[] arrData = recievedData.split(":");
            String serverImplementation = arrData[0];
            Boolean isFailureToBeHandled = Boolean.valueOf(arrData[2]);
            Thread thread = new Thread((new ExecuteFunction(socket,packet,getServerImplementation(serverImplementation),isFailureToBeHandled)));
            thread.start();
        }

    }
    public DLMSServerImplementation getServerImplementation(String  serverImplementation){
        if (serverImplementation.equalsIgnoreCase("MCG")){
            return this.mcgillServer;
        }else if (serverImplementation.equalsIgnoreCase("CON")){
            return this.concordiaServer;
        }else {
            return this.montrealServer;
        }
    }

    public static void main(String args[]) throws IOException{

        DLMSServerImplementation mcgillServer = new DLMSServerImplementation(Servers.MCG);
        DLMSServerImplementation concordiaServer = new DLMSServerImplementation(Servers.CON);
        DLMSServerImplementation montrealServer = new DLMSServerImplementation(Servers.MON);
        serverData = new HashMap<>();
        serverData.put("MCG",mcgillServer);
        serverData.put("MON",concordiaServer);
        serverData.put("CON",montrealServer);

        Replica2 replica2 = new Replica2(mcgillServer,concordiaServer,montrealServer);
        Runnable replicaTask = () -> {
            try {
                replica2.startReplica(1234);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread replicaThread = new Thread(replicaTask);
        replicaThread.start();

    }



}
