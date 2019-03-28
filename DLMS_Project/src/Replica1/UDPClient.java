package Replica1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient extends Thread {
    DCMSServerImplementation serverImplementation;
    String isBookBorrowd;
    String isBookReturned;
    String itemSearchResult;
    String itemID;

    public UDPClient(DCMSServerImplementation serverImplementation, String itemID) throws IOException {
        this.serverImplementation = serverImplementation;
        this.itemID = itemID;
    }

    @Override
    public void run(){
       DatagramSocket socket = null;
       try{
           socket = new DatagramSocket();
           byte[] data = itemID.getBytes();
           DatagramPacket packet = new DatagramPacket(data,data.length, InetAddress.getByName(serverImplementation.IPAddress), serverImplementation.serverUDPManager.udpPortNo);
           socket.send(packet);
           data = new byte[100];
           socket.receive(new DatagramPacket(data,data.length));
           String output =new String(data).trim();
           String operation = this.itemID.substring(0,1);
           switch (operation){
               case "B":
                   isBookBorrowd = output.trim();
                   break;
               case "R":
                   isBookReturned = output;
                   break;
               case "C":
                   itemSearchResult = output;
                   break;
           }

       } catch (Exception e){

       } finally {
            if (socket != null){
                socket.close();
            }
       }
    }

}
