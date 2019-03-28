package com.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import Sequencer.Port;

import java.net.MulticastSocket;


public class ReplicaManager {

    public String replicaNumber;
    public Integer sequenceNumber;
    public ArrayList<com.Server.Request> arrRequestToPerform;
    public ReplicaManager(){
        this.replicaNumber = "1";
        this.sequenceNumber = 0;
        this.arrRequestToPerform = new ArrayList<>();
    }


    public void startReplicaManager(int multicastPort) throws Exception{
		MulticastSocket socket = null;
		try {
			socket = new DatagramSocket(multicastPort);
			aSocket.joinGroup(InetAddress.getByName("230.1.1.5"));
			while (true) {
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(packet);
				String receiveRequest = new String(packet.getData(), 0, packet.getLength());
                Request recievedRequest = parseRecievedRequest(receiveRequest);
                this.arrRequestToPerform.add(recievedRequest);
                executeRequest();
			}

		} catch (Exception e) {
			System.out.println("Socket: " + e.getMessage());
		}  finally {
			if (aSocket != null)
				aSocket.close();
		}
        
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
        InetAddress address = InetAddress.getByName("230.1.1.5");
        String ms = requestToExecute.serverImplementation + ":" + requestToExecute.request;
        byte[] data = ms.getBytes();
        //replica port
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, Port.MULTICAST);

        //msgtoreplica
        DatagramSocket socket = new DatagramSocket(2000);

        socket.send(sendPacket);

        byte[] recvData = new byte[1024];
        DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);

        try{
            socket.receive(recvPacket);
            String receiveMessage = new String(recvPacket.getData(), 0, recvPacket.getLength());
            packageMsgAndSendToFE(socket, requestToExecute.FEAddress, receiveMessage);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void packageMsgAndSendToFE (DatagramSocket socket, String feHostAddress, String receiveMessage) throws IOException {
        InetAddress address = InetAddress.getByName(feHostAddress);
        String msg = this.replicaNumber + ":" + receiveMessage;
        byte[] data = msg.getBytes();
        //feport
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, address , 6000);
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
