package shylux.java.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InterfaceAddress;

/** 
 * The ConnectionManager starts a server on a specific port.
 * When someone connects he will notify all INetworkListener attached to him.
 * 
 * @author Lukas Knoepfel <shylux@gmail.com>
 *
 */
public class ConnectionManager {	
	public static final int DEFAULT_PORT = 8228;
	public static final int MAX_UDP_MSG_SIZE = 500;
	public static Logger LOG = Logger.getLogger(ConnectionManager.class.getName());
	public static ConsoleHandler STD_LOG_HANDLER = new ConsoleHandler();
	
	public static final UUID ID = UUID.randomUUID();
	
	static {
		LOG.setLevel(Level.INFO);
		LOG.setUseParentHandlers(false);
		// set default level to all 
		LOG.addHandler(STD_LOG_HANDLER);
		STD_LOG_HANDLER.setLevel(Level.ALL);
	}
	
	
	private int portNumber;
	TCPListener tcplistener;
	UDPListener udplistener;
	private Thread tcpthread;
	private Thread udpthread;

	/**
	 * Creates server and listens on the port.
	 * Notifies attached listeners upon new connection.
	 */
	private class TCPListener implements Runnable {
		ServerSocket serverSocket;
		public void run() {
			try {
				serverSocket = new ServerSocket(portNumber);
				
				LOG.info("Starting TCP server on port "+portNumber);
				while (true) {
					LOG.fine("Waiting for connection...");
					Socket clientSocket = serverSocket.accept();
					LOG.fine(String.format("New connection from %s:%d", clientSocket.getInetAddress().toString(), clientSocket.getPort()));
					TCPConnection conn = new TCPConnection(clientSocket);
					// notify listener
					for (INetworkListener nl: listener) {
						nl.onConnection(conn);
					}
				}
			} catch (IOException e) {
				if (!e.getMessage().equals("socket closed"))
					LOG.warning(e.getMessage());;
			} finally {
				LOG.info("Shutting down TCP Server...");
				try {
					serverSocket.close();
				} catch (IOException e) {}
			}
		}
	}
	
	//TODO javadoc
	private class UDPListener implements Runnable {
		DatagramSocket datagramSocket;
		public void run() {
			try {
				datagramSocket = new DatagramSocket(portNumber);
			
				LOG.info("Starting UDP server on port "+portNumber);
				while (true) {
					byte[] receiveData = new byte[MAX_UDP_MSG_SIZE];
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					datagramSocket.receive(receivePacket);
					// ignore packets from yourself
					String message = new String(receivePacket.getData()).trim();

					LOG.fine("Got udp message: "+message+", from: "+receivePacket.getSocketAddress());
					if (message.startsWith(ID.toString())) continue;
					
					UDPMessage msg = new UDPMessage(receivePacket);
					
					for (INetworkListener nl: listener) {
						nl.onUDPMessage(msg);
					}
				}
			} catch (SocketException e) {
				// normal close
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				LOG.info("Shutting down UDP Server...");
				datagramSocket.close();
			}

		}
	}
	
	/**
	 * Starts ConnectionManager on default port.
	 */
	public ConnectionManager() {
		this(DEFAULT_PORT, true, true);
	}
	/**
	 * Starts ConnectionManager on a different port.
	 * Creates a new thread for listening on the port.
	 * @param pPortNumber port on which the server should listen
	 */
	public ConnectionManager(int pPortNumber, boolean useTCP, boolean useUDP) {
		portNumber = pPortNumber;
		
		if (useTCP) {
			tcplistener = new TCPListener();
			tcpthread = new Thread(tcplistener);
			tcpthread.setName("ConnectionManager-TCPListener");
			tcpthread.start();
		}
		
		if (useUDP) {
			udplistener = new UDPListener();
			udpthread = new Thread(udplistener);
			udpthread.setName("ConnectionManager-UDPListener");
			udpthread.start();
		}
	}
	
	public ConnectionManager(boolean useTCP, boolean useUDP) {
		this(DEFAULT_PORT, useTCP, useUDP);
	}
	
	/**
	 * Stops the Manager.
	 * By closing the socket, the thread will receive an exception and terminate itself.
	 */
	public synchronized void stop() {
		//TODO read why stop is deprecated http://docs.oracle.com/javase/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
		if (udplistener != null) {
			udplistener.datagramSocket.close();
			udpthread.stop();
		}
		try {
			if (tcplistener != null) {
				tcplistener.serverSocket.close();
				tcpthread.stop();
			}
		} catch (IOException e) {e.printStackTrace();}		
	}
	
	/**
	 * Connections to remote host on the default port.
	 * @param pHost hostname / ip of the remote host
	 * @return the new connection
	 * @throws IOException yea yea networking..
	 */
	public static TCPConnection connect(String pHost) throws IOException {
		return connect(pHost, DEFAULT_PORT);
	}
	/**
	 * Connects to remote host on specified port.
	 * @param pHost hostname / ip of the remote host
	 * @param pPortNumber port number on the remote host
	 * @return the new connection
	 * @throws IOException yea yea networking..
	 */
	public static TCPConnection connect(String pHost, int pPortNumber) throws IOException {
		LOG.fine(String.format("Connection to %s:%d", pHost, pPortNumber));
		Socket clientSocket = new Socket(pHost, pPortNumber);
		TCPConnection conn = new TCPConnection(clientSocket);
		
		return conn;
	}
	
	//TODO javadoc
	public static void sendBroadcastUDPMessage(String msg) throws SocketException {
		byte[] sendData = (ID.toString()+msg).getBytes();
		
		DatagramSocket socket = new DatagramSocket();
		socket.setBroadcast(true);
		
		// try 255.255.255.255 first
		try {
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), DEFAULT_PORT);
			socket.send(sendPacket);
		} catch (Exception e) {}
		
		try {
			Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();
				if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null) continue;
					LOG.fine("sending broadcast: "+broadcast);
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, DEFAULT_PORT);
					socket.send(sendPacket);
				}
			}
		} catch (IOException e) {}
	}

	/* LISTENER */
	List<INetworkListener> listener = new ArrayList<INetworkListener>();
	
	/**
	 * Adds connection listener. All listener are notified if a new connection established.
	 * @param pNet connection listener
	 */
	public synchronized void addNetworkListener(INetworkListener pNet) {
		LOG.finer("Added network listener.");
		listener.add(pNet);
	}
	/**
	 * Removes network listener from list. That listener wont receive notifications anymore.
	 * @param pNet Listener to remove.
	 */
	public synchronized void removeNetworkListener(INetworkListener pNet) {
		listener.remove(pNet);
	}
	/**
	 * Clears listener list. Every listener gets removed and wont receive notifications anymore.
	 */
	public synchronized void clearNetworkListener() {
		listener.clear();
	}
}
