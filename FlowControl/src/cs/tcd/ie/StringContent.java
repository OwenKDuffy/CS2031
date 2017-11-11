package cs.tcd.ie;

import java.net.DatagramPacket;

public class StringContent implements PacketContent {
	String string;
	byte[] header= new byte[HEADERLENGTH];
	
	public StringContent(DatagramPacket packet) {
		byte[] payload;
		byte[] buffer;
		
		buffer= packet.getData();
		payload= new byte[packet.getLength()-HEADERLENGTH];
		System.arraycopy(buffer, HEADERLENGTH, payload, 0, packet.getLength()-HEADERLENGTH);
		
		string = new String(payload);
	}
	
	public StringContent(String string) {
		this.string = string;
	}
	
	public StringContent(String string, byte[] header)
	{
		this.string=string;
		this.header=header;
	}
	
	public String toString() {
		return string;
	}
	
	public void setHeader(byte[] header)
	{
		this.header=header;
	}

	public DatagramPacket toDatagramPacket() {
		DatagramPacket packet= null;
		byte[] buffer= null;
		byte[] payload= null;

		try {
			payload= string.getBytes();
			buffer= new byte[header.length+payload.length];
			System.arraycopy(header, 0, buffer,0, header.length);
			System.arraycopy(payload, 0, buffer, HEADERLENGTH, payload.length);
			packet= new DatagramPacket(buffer, buffer.length);
		}
		catch(Exception e) {e.printStackTrace();}
		return packet;
	}
}
