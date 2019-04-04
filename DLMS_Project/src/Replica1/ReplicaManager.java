package Replica1;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import Sequencer.Port;
import Util.Constants;
import Util.FailureHandling;

public class ReplicaManager {

    public String replicaNumber;
    public Integer sequenceNumber;
    public ArrayList<Request> arrRequestToPerform;
    public ArrayList<Request> arrBackUp;

    public Boolean isFailureToBeHanlded = false;
    public int crashCount = 0;

    public ReplicaManager(){
        this.replicaNumber = "1";
        this.sequenceNumber = 0;
        this.arrRequestToPerform = new ArrayList<>();
        this.arrBackUp = new ArrayList<>();
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
				String[] arrRequest = receiveRequest.split(Constants.DELIMITER);
				if (arrRequest[0] == FailureHandling.SoftwareFailure.name()){
                    String failedServerNumber = arrRequest[1];
                    if (failedServerNumber.equalsIgnoreCase(this.replicaNumber)){
                        handleFailedReplica();
                    }
                } else if (arrRequest[0]==FailureHandling.SoftwareCrash.name()){
				    String crashServer = arrRequest[0];
                    if (arrRequest.length > 3 ) {
                        if (crashServer == this.replicaNumber){
                            this.handleEchoFailedCurrentReplica();
                        }
                    }else{
                        if (crashServer!=this.replicaNumber){
                            this.handleCrashCheck(replicaNumber);
                        }
                    }
                } else {
                    Request recievedRequest = parseRecievedRequest(receiveRequest);
                    handledNewIncomingRequest(recievedRequest);
                    prepareExecuteRequest();
                }
			}
		} catch (Exception e) {
			System.out.println("Socket: " + e.getMessage());
		} finally {
			if (socket != null)
				socket.close();
		}

    }

    public void handledNewIncomingRequest(Request request){
        this.arrRequestToPerform.add(request);
        this.arrBackUp.add(request);
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

    public void handleEchoFailedCurrentReplica()throws Exception {
        if (this.crashCount >2){
            this.handleCrashedReplica();
        }else{
            this.crashCount++;
        }
    }

    public void handleCrashedReplica() throws Exception{
        Runnable replica1 = () -> {
            try {
                Replica1.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        Thread thread = new Thread(replica1);
        thread.start();
        this.reExecuteBackUpRequest();
    }

    public void reExecuteBackUpRequest() throws Exception{
        Collections.sort(this.arrBackUp, new Comparator<Request>(){
            public int compare(Request o1, Request o2) {
                return o1.sequencerID.compareTo(o2.sequencerID);
            }
        });
        ArrayList<Request> arrBackUpCopy = new ArrayList(this.arrBackUp);

        for (Request r: arrBackUpCopy){
            executeRequest(r);
        }
    }

    public void manageInformingReplicaManagers(String rmAddress,String replicaNum){
        try {
            InetAddress address = InetAddress.getByName(rmAddress);
            DatagramSocket socket = new DatagramSocket();
            byte[] data =  (replicaNum + Constants.DELIMITER + FailureHandling.SoftwareCrash + Constants.DELIMITER + "ECHOFAILED").getBytes();
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
        String[] message = receivedRequest.split(Constants.DELIMITER);
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

    private void prepareExecuteRequest () throws IOException {
        Request requestToExecute = searchRequestWithSequenceNumber();
        executeRequest(requestToExecute);

    }

    public void executeRequest(Request request) throws IOException{
        InetAddress address = InetAddress.getByName("localhost");
        String ms = request.serverImplementation + Constants.DELIMITER + request.request + Constants.DELIMITER + this.isFailureToBeHanlded.toString();
        byte[] data = ms.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, 1211);

        DatagramSocket socket = new DatagramSocket();

        socket.send(sendPacket);

        byte[] recvData = new byte[1024];
        DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);

        try{
            socket.receive(recvPacket);
            String receiveMessage = new String(recvPacket.getData(), 0, recvPacket.getLength());
            this.arrBackUp.remove(request);
            packageMsgAndSendToFE(socket, request.FEAddress, receiveMessage);
        }catch (Exception e) {
            System.out.println(e);
        }
    }

    private void packageMsgAndSendToFE (DatagramSocket socket, String feHostAddress, String receiveMessage) throws IOException {
        InetAddress address = InetAddress.getByName("localhost");
        String msg = this.replicaNumber + Constants.DELIMITER + receiveMessage;
        byte[] data = msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, address , Port.FE);
        socket.send(sendPacket);
        socket.close();
    }

    public static void main(String[] args){

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
