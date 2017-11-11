package cs.tcd.ie;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import tcdIO.Terminal;

public class Gateway extends Node {
	static final int DEFAULT_PORT = 50001;
	static final int DEFAULT_DST_PORT = 50002;
	Terminal terminal;
	InetSocketAddress serverAddress = new InetSocketAddress("localhost", DEFAULT_DST_PORT);
	InetSocketAddress clientAddress = new InetSocketAddress("localhost", 50000);
	ArrayList<InetSocketAddress> connectedUsers = new ArrayList<InetSocketAddress>();
	/*
	 * 
	 */
	Gateway(Terminal terminal, int port) {
		try {
			this.terminal= terminal;
			socket= new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public void onReceipt(DatagramPacket packet) 
	{
		try 
		{
			DatagramPacket forward;
			byte[] headerData = getHeaderData(packet);
			InetSocketAddress sourceAddress = (InetSocketAddress) packet.getSocketAddress();
			if(!sourceAddress.equals(serverAddress))
			{
				int addressIndex=findConnection(sourceAddress);
				headerData[1] = (byte) addressIndex;
			}
			StringContent content= new StringContent(packet);
			String packetContent = content.toString();
			terminal.println(packetContent);
			content.setHeader(headerData);

			forward = content.toDatagramPacket();
			if(headerData[2] == 0)
			{
				forward.setSocketAddress(serverAddress);
			}
			else if (headerData[2] != 0)
			{
				int index = headerData[2];
				forward.setSocketAddress(connectedUsers.get(index-1));
			}

			socket.send(forward);
		}
		catch(Exception e) {e.printStackTrace();}
	}



	private int findConnection(InetSocketAddress sourceAddress) 
	{

		int index =	connectedUsers.indexOf(sourceAddress);
		if(index != -1)
		{
			return index+1;
		}
		else
		{
			connectedUsers.add(sourceAddress);
			return connectedUsers.size();
		}

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

	public synchronized void start() throws Exception {
		terminal.println("Waiting for contact");
		this.wait();
	}

	/*
	 * 
	 */
	public static void main(String[] args) {
		try {					
			Terminal terminal= new Terminal("Gateway");
			(new Gateway(terminal, DEFAULT_PORT)).start();
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
