package Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sequencer {
	private final String multicastAddr = "230.1.1.5";
	private int sequenceNum = 0;
	
	public void start() {
		listenFromFE();
	}
	
	private void listenFromFE() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(Port.SEQUENCER);
			byte[] buffer = new byte[1000];
			
			System.out.println("Sequencer is ready to receive messages from the FE.............");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.receive(request);
				String data = new String(request.getData(), 0, request.getLength());
				data += (":" + Integer.toString(sequenceNum));
				sequenceNum++;
				System.out.println(data);
				multicast(data.getBytes());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void multicast(byte[] data) {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			InetAddress host = InetAddress.getByName(multicastAddr);

			DatagramPacket request = new DatagramPacket(data, data.length, host, Port.MULTICAST);
			socket.send(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Sequencer sequencer = new Sequencer();
		sequencer.start();
	}
}
