package Replica1;

import Util.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Replica1 {
    public DLMSServerImplementation mcgillServer;
    public DLMSServerImplementation concordiaServer;
    public DLMSServerImplementation montrealServer;
    static HashMap<String, DLMSServerImplementation> serverRepository;


    public Replica1(DLMSServerImplementation mcgillServer, DLMSServerImplementation concordiaServer, DLMSServerImplementation montrealServer){
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

        DLMSServerImplementation mcgillServer = new DLMSServerImplementation(Constants.ServerLocation.MCG);
        DLMSServerImplementation concordiaServer = new DLMSServerImplementation(Constants.ServerLocation.CON);
        DLMSServerImplementation montrealServer = new DLMSServerImplementation(Constants.ServerLocation.MON);
        serverRepository = new HashMap<>();
        serverRepository.put("MCG",mcgillServer);
        serverRepository.put("MON",concordiaServer);
        serverRepository.put("CON",montrealServer);
        //load backup data
        Replica1 replica1 = new Replica1(mcgillServer,concordiaServer,montrealServer);
        Runnable replicaTask = () -> {
            try {
                replica1.startReplica(1211);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Thread replicaThread = new Thread(replicaTask);
        replicaThread.start();


    }



}
