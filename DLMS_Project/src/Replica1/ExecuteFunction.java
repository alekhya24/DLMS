package Replica1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import Util.Constants;

public class ExecuteFunction implements Runnable {

    DatagramPacket packet;
    DatagramSocket socket;
    DLMSServerImplementation serverImplementation;
    boolean isFailureToBeHandled;

    public ExecuteFunction(DatagramSocket socket,DatagramPacket packet,DLMSServerImplementation serverImplementation,boolean isFailureToBeHandled){
        this.socket = socket;
        this.packet = packet;
        this.serverImplementation = serverImplementation;
        this.isFailureToBeHandled = isFailureToBeHandled;
    }

    @Override
    public void run(){
        InetAddress address = null;
        String data = new String(packet.getData(), 0 , packet.getLength());
        String message = null;
        if (!data.equals("Hi")){
            String[] ms = message.split(Constants.REQUEST_DELIMITER);
            message = ms[1];
        } else {
            message = "Hi";
        }

        //FE port
        byte[] data2 = null;
        DatagramPacket packet2 = null;

        try {
            String[] function = message.split(Constants.REQUEST_DELIMITER);
            String result = "";

            switch (function[0]){
                case "addItem":
                    result = serverImplementation.addItem(function[1],function[2],function[3],Integer.valueOf(function[4]));
                    break;
                case "removeItem":
                    result = serverImplementation.removeItem(function[1],function[2],Integer.valueOf(function[3]));
                    break;
                case "listItemAvailability":
                    result = serverImplementation.listItemAvailability(function[1]);
                    break;
                case "borrowItem":
                    result = serverImplementation.borrowItem(function[1],function[2]);
                    break;
                case "findItem":
                    result = serverImplementation.findItem(function[1],function[2],function[3]);
                    break;
                case "returnItem":
                    result = serverImplementation.returnItem(function[1],function[2]);
                    break;
                case "exchangeItem":
                    result = serverImplementation.exchangeItem(function[1],function[2],function[3]);
                    break;
                case "addInWaitlist":
                    result = serverImplementation.addInWaitlist(function[1],function[2]);
                    break;
                case "Hi":
                    replyEcho(this.packet);
                    break;
                default:
            }

            address = packet.getAddress();
            int port = packet.getPort();
            data2 = result.getBytes();
            packet2 = new DatagramPacket(data2,data2.length,address,port);
            socket.send(packet2);
        } catch (Exception e){
            System.out.println(e);
        }

    }
    private void replyEcho(DatagramPacket packet) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] reply = "Reply Hi".getBytes();
            DatagramPacket packet1 = new DatagramPacket(reply, 0, reply.length, packet.getAddress(), packet.getPort());
            socket.send(packet1);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
