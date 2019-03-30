package Replica1;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

import Sequencer.Port;
import Util.FailureHandling;


public class ReplicaManager {

    public String replicaNumber;
    public Integer sequenceNumber;
    public ArrayList<Request> arrRequestToPerform;
    public Boolean isFailureToBeHanlded = false;
    public int crashCount = 0;
    public ReplicaManager(){
        this.replicaNumber = "1";
        this.sequenceNumber = 0;
        this.arrRequestToPerform = new ArrayList<>();
    }

    public void startReplicaManager(int multicastPort){
		MulticastSocket socket = null;
		try {
			socket = new MulticastSocket(multicastPort);
			socket.joinGroup(InetAddress.getByName("230.1.1.5"));
			while (true) {
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				String receiveRequest = new String(packet.getData(), 0, packet.getLength());
//				String[] arrRequest = receiveRequest.split(":");
//				if (Integer.valueOf(arrRequest[0]) == FailureHandling.SoftwareFailure.ordinal()){
//                    String failedServerNumber = arrRequest[1];
//                    if (failedServerNumber.equalsIgnoreCase(this.replicaNumber)){
//                        handleFailedReplica();
//                    }
//                } else if (Integer.valueOf(arrRequest[0])==FailureHandling.SoftwareCrash.ordinal()){
//				    String crashServer = arrRequest[0];
//                    if (arrRequest.length > 2) {
//                        this.handleEchoFailedReplica();
//                    }else{
//                        if (crashServer!=this.replicaNumber){
//                            this.handleCrashCheck(replicaNumber);
//                        }
//                    }
//                } else {
                    Request recievedRequest = parseRecievedRequest(receiveRequest);
                    this.arrRequestToPerform.add(recievedRequest);
                    executeRequest();
//                }
			}
		} catch (Exception e) {
			System.out.println("Socket: " + e.getMessage());
		}  finally {
			if (socket != null)
				socket.close();
		}
        
    }

    public void handleCrashCheck(String replicaNum){
        Runnable crashCheck = () -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("" + threadName);
            try {
                checkEcho("230.1.1.5",replicaNum);
            } catch (Exception e){
                e.printStackTrace();
            }
        };
        crashCheck.run();

    }
    private void checkEcho(String rmAddress, String replica){
        try {
            InetAddress address = InetAddress.getByName(rmAddress);
            DatagramSocket socket = new DatagramSocket();
            byte[] data = "Hi".getBytes();
            DatagramPacket packet = new DatagramPacket(data, 0 ,data.length, address, Integer.parseInt(replica));
            socket.send(packet);

            byte[] data2 = new byte[1024];
            DatagramPacket newPacket = new DatagramPacket(data2,data2.length);
            Runnable timer = () -> {
                try {
                    Thread.sleep(5000);
                    socket.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            };
            timer.run();
            socket.receive(newPacket);
        } catch (SocketException e) {
            manageInformingReplicaManagers(rmAddress,replicaNumber);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleEchoFailedReplica(){
        if (this.crashCount >2){
        }else{
            this.crashCount++;
        }
    }
    public void manageInformingReplicaManagers(String rmAddress,String replicaNum){
        try {
            InetAddress address = InetAddress.getByName(rmAddress);
            DatagramSocket socket = new DatagramSocket();
            byte[] data =  (replicaNum +":"+ FailureHandling.SoftwareCrash.toString() +":"+ "ECHOFAILED").getBytes();
            DatagramPacket packet = new DatagramPacket(data, 0 ,data.length, address, Port.MULTICAST);
            socket.send(packet);
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleFailedReplica(){
        this.isFailureToBeHanlded = true;
    }


    public Request parseRecievedRequest(String receivedRequest){
        String[] message = receivedRequest.split(":");
        String sequencerID = message[0];
        String FEAddress = message[1];
        String serverImplementation = message[2];
        String request = message[3];
        return new Request(sequencerID,FEAddress,serverImplementation,request);
    }

    public Request searchRequestWithSequenceNumber(){
        for (Request request:arrRequestToPerform){
            if (Integer.valueOf(request.sequencerID) == this.sequenceNumber){
                this.sequenceNumber++;
                return request;
            }
        }
        return null;
    }

    private void executeRequest () throws IOException {
        Request requestToExecute = searchRequestWithSequenceNumber();
        InetAddress address = InetAddress.getByName("localhost");
        String ms = requestToExecute.serverImplementation + ":" + requestToExecute.request + ":"+ this.isFailureToBeHanlded.toString();
        byte[] data = ms.getBytes();
        //replica port
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, Port.MULTICAST);

        //execmsgtoreplica
        DatagramSocket socket = new DatagramSocket();

        socket.send(sendPacket);

        byte[] recvData = new byte[1024];
        DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);

        try{
            socket.receive(recvPacket);
            String receiveMessage = new String(recvPacket.getData(), 0, recvPacket.getLength());
            packageMsgAndSendToFE(socket, requestToExecute.FEAddress, receiveMessage);
        }catch (Exception e) {
            System.out.println(e);
        }
    }

    private void packageMsgAndSendToFE (DatagramSocket socket, String feHostAddress, String receiveMessage) throws IOException {
        InetAddress address = InetAddress.getByName("localhost");
        String msg = this.replicaNumber + ":" + receiveMessage;
        byte[] data = msg.getBytes();
        //feport
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, address , Port.FE);
        socket.send(sendPacket);
        socket.close();
    }

    public static void main(String[] args){
        System.setProperty("java.net.preferIPv4Stack", "true");

        ReplicaManager replicaManager = new ReplicaManager();

        Runnable replicaManagerThread = () -> {
            try {
                replicaManager.startReplicaManager(Port.MULTICAST);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        Thread t1 = new Thread(replicaManagerThread);
        t1.start();

    }

    enum ReplicaPort {
        REPLICA_PORT;
        final int port = 1111;
    }

//    Replicaport
//    replicamanagerport
//    feport
//    execmsgtoreplica

}
