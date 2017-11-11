package cs.tcd.ie;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import tcdIO.Terminal;

public class Server extends Node {
	static final int DEFAULT_PORT = 50002;

	Terminal terminal;
	ArrayList<Integer> connectedUsers = new ArrayList<Integer>();

	/*
	 * 
	 */
	Server(Terminal terminal, int port) {
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
	public void onReceipt(DatagramPacket packet) {
		try {
			StringContent content= new StringContent(packet);
			byte[] headerData = getHeaderData(packet);
			int userConnected = (int) headerData[1];
			if(userConnected > connectedUsers.size())
			{
				connectedUsers.add(0);
				terminal.print("New ");
			}
			StringContent response;
			byte[] header = new byte[PacketContent.HEADERLENGTH];
			if(checkSequenceNumber( userConnected-1 , (int) headerData[0]))
			{
				terminal.println("User " + userConnected + " :" + content.toString());
				response= (new StringContent("OK", header));
			}
			else
			{
				response= (new StringContent("NOK", header));
			}
			int returnSequenceNumber = (int) connectedUsers.get(userConnected-1); 
			// if the sequence number received was correct this will have been iterated to the next one it'll expect
			//if not it wont have changed and we'll ask for the same one again.
			header[0] = (byte) returnSequenceNumber;
			header[2]=(byte) userConnected;
			
			DatagramPacket message=response.toDatagramPacket();
			message.setSocketAddress(packet.getSocketAddress());
			socket.send(message);


		}
		catch(Exception e) {e.printStackTrace();}
	}

	private boolean checkSequenceNumber(int i, int j) {
		int userSequenceNumber = connectedUsers.get(i);
		if(userSequenceNumber == j)
		{
			iterate(connectedUsers, i);
			return true;
		}
		else
			return false;
	}

	private void iterate(ArrayList<Integer> userList, int index) {
		int sequenceNumber = userList.get(index);
		if(sequenceNumber == 0)
		{
			sequenceNumber = 1;
		}
		else
		{
			sequenceNumber =0;
		}
		userList.set(index, sequenceNumber);
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


	public synchronized void start() throws Exception {
		terminal.println("Waiting for contact");
		this.wait();
	}

	/*
	 * 
	 */
	public static void main(String[] args) {
		try {					
			Terminal terminal= new Terminal("Server");
			(new Server(terminal, DEFAULT_PORT)).start();
			terminal.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}
