package shylux.java.network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UDPMessage {
	DatagramPacket source;
	String message;

	public UDPMessage(DatagramPacket dp) {
		source = dp;
		message = new String(source.getData()).trim();
		message = message.substring(ConnectionManager.ID.toString().length());
	}

	public String getMessage() {
		return message;
	}
	
	public InetAddress getInetAddress() {
		return ((InetSocketAddress)source.getSocketAddress()).getAddress();
	}
	
	public int getPort() {
		return source.getPort();
	}
}
