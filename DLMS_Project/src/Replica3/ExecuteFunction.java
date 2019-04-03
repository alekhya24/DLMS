package Replica3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import Util.Constants;
import Replica3.resource.*;

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
        String[] ms = data.split(Constants.DELIMITER);
        message = ms[1];
        int port = 4400;
        byte[] data2 = null;
        DatagramPacket packet2 = null;

        try {
            String[] function = message.split(Constants.REQUEST_DELIMITER);
            String result = "";

            switch (function[0]){
                case "addItem":
                    result = serverImplementation.addItem(new ID(function[1]), new ID(function[2]), function[3], Integer.valueOf(function[4]));
                    break;
                case "removeItem":
                    result = serverImplementation.deleteItem(new ID(function[1]), new ID(function[2]), Integer.valueOf(function[3]));
                    break;
                case "listItemAvailability":
                    result = serverImplementation.listItemAvailability(new ID(function[1]));
                    break;
                case "borrowItem":
                    result = serverImplementation.borrowItem(new ID(function[1]), new ID(function[2]), 10);
                    break;
                case "findItem":
                    result = serverImplementation.findItem(new ID(function[1]), function[2], true);
                    break;
                case "returnItem":
                    result = serverImplementation.returnItem(new ID(function[1]), new ID(function[2]));
                    break;
                case "exchangeItem":
                    result = serverImplementation.exchangeItem(new ID(function[1]), new ID(function[2]), new ID(function[3]));
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
