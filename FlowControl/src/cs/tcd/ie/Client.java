/**
 * 
 */
package cs.tcd.ie;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Scanner;

import javax.swing.JOptionPane;

import tcdIO.*;

/**
 *
 * Client class
 * 
 * An instance accepts user input 
 *
 */
public class Client extends Node {
	static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 50001;
	static final String DEFAULT_DST_NODE = "localhost";	
	static final int TIMEOUT_WAIT_PERIOD = 5000;
	private static final int MAXIMUM_CONNECTION_ATTEMPTS = 5; 
	Terminal terminal;
	InetSocketAddress dstAddress;
	int sequenceNumber = 0;
	private boolean acknowledged;

	/**
	 * Constructor
	 * 	 
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Client(Terminal terminal, String dstHost, int dstPort, int srcPort) {
		try {
			this.terminal= terminal;
			dstAddress= new InetSocketAddress(dstHost, dstPort);
			socket= new DatagramSocket(srcPort);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}


	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public synchronized void onReceipt(DatagramPacket packet) {
		byte[] headerData =  getHeaderData(packet);
		StringContent content= new StringContent(packet);
		//check sequence number
		int receivedSeqNum = headerData[0];
		if(receivedSeqNum == sequenceNumber)
		{
			acknowledged = true;
			this.notify();
			terminal.println(content.toString());
		}

		/*
		 * if the sequence number received is incorrect in this two number system is incorrect
		 * then we can consider that the last sent message wasn't received and proceed as if
		 * we hadn't received an acknowledgement at all.
		 */

	}


	/**
	 * Sender Method
	 * 
	 */
	public synchronized void start() throws Exception {

		DatagramPacket packet= null;

		byte[] payload= null;
		byte[] header= null;
		byte[] buffer= null;

		payload= (terminal.readString("String to send: ")).getBytes();

		header= new byte[PacketContent.HEADERLENGTH];
		header[0]=(byte) sequenceNumber;
		iterateSequenceNumber();
		buffer= new byte[header.length + payload.length];
		System.arraycopy(header, 0, buffer, 0, header.length);
		System.arraycopy(payload, 0, buffer, header.length, payload.length);

		terminal.println("Sending packet...");
		packet= new DatagramPacket(buffer, buffer.length, dstAddress);
		acknowledged = false;
		int sendCount = 0;
		while(!acknowledged)
		{
			if(sendCount<MAXIMUM_CONNECTION_ATTEMPTS)
			{
			socket.send(packet);
			terminal.println("Packet sent");
			this.wait(5000);
			sendCount++;
			}
			else
			{
				terminal.println("Exceeded Maximum Send attempts. Possible Connection Error");
				break;
			}
		}

	}


	private void iterateSequenceNumber() {
		if(this.sequenceNumber == 0)
		{
			this.sequenceNumber = 1;
		}
		else
		{
			this.sequenceNumber =0;
		}
		return;
	}

	private byte[] getHeaderData(DatagramPacket packet) 
	{
		byte[] payload;
		byte[] buffer;

		buffer= packet.getData();
		payload= new byte[PacketContent.HEADERLENGTH];
		System.arraycopy(buffer, 0, payload, 0, PacketContent.HEADERLENGTH);

		return payload;
	}

	public static void main(String[] args) {
		try {				
			int portNumber = Integer.parseInt(args[0]);
			Terminal terminal= new Terminal("Client " + portNumber);
			Client thisClient = new Client(terminal, DEFAULT_DST_NODE, DEFAULT_DST_PORT, portNumber);
			while(true)
			{
				thisClient.start();
			}
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
