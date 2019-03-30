package Replica2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
        String[] ms = data.split(":");
        message = ms[1];
        System.out.println(message);
        int port = 4400;
        byte[] data2 = null;
        DatagramPacket packet2 = null;

        try {
            String[] function = message.split("_");
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
                    result = serverImplementation.borrowItem(function[1],function[2],Boolean.parseBoolean(function[3]));
                    break;
                case "findItem":
                    result = serverImplementation.findItem(function[1],function[2]);
                    break;
                case "returnItem":
                    result = serverImplementation.returnItem(function[1],function[2]);
                    break;
                case "exchangeItem":
                    result = serverImplementation.exchangeItem(function[1],function[2],function[3]);
                    break;
            }

            address = packet.getAddress();
            port = packet.getPort();
            data2 = result.getBytes();
            packet2 = new DatagramPacket(data2,data2.length,address,port);
            socket.send(packet2);
        } catch (Exception e){
            System.out.println(e);
        }

    }

}
