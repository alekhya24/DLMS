package Replica1;

import Util.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Logger;

public class ServerUDPManager extends Thread {
    DatagramPacket recievedPacket;
    DatagramSocket serverSocket;
    DatagramPacket sentPacket;
    int udpPortNo;
    Constants.ServerLocation location;
    DLMSServerImplementation serverImplementation;
    Logger logInstance;

    public ServerUDPManager(DLMSServerImplementation serverImplementation, Constants.ServerLocation location, Logger logger){
            this.location = location;
            this.serverImplementation = serverImplementation;
            logInstance = logger;
            try {
                switch (location){
                    case MCG:
                        serverSocket = new DatagramSocket(Constants.UDP_PORT_NUM_MCG);
                        udpPortNo = Constants.UDP_PORT_NUM_MCG;
                        break;
                    case CON:
                        serverSocket = new DatagramSocket(Constants.UDP_PORT_NUM_CON);
                        udpPortNo = Constants.UDP_PORT_NUM_CON;
                        break;
                    case MON:
                        serverSocket = new DatagramSocket(Constants.UDP_PORT_NUM_MON);
                        udpPortNo = Constants.UDP_PORT_NUM_MON;
                        break;
                }
                } catch (IOException e){

                }
    }

    @Override
    public void run(){
        byte[] recieveData;
        while (true){
            try {
                recieveData = new byte[1024];
                recievedPacket = new DatagramPacket(recieveData,recieveData.length);
                serverSocket.receive(recievedPacket);
                String inputPacket = new String(recievedPacket.getData()).trim();
                new UDPServer(recievedPacket,serverImplementation).start();
            } catch (Exception e){

            }
        }
    }
}
