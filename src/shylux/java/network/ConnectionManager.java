package shylux.java.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * The ConnectionManager starts a server on a specific port.
 * When someone connects he will notify all INetworkListener attached to him.
 * 
 * @author Lukas Knoepfel <shylux@gmail.com>
 *
 */
public class ConnectionManager implements Runnable {	
	public static final int DEFAULT_PORT = 8228;
	public static Logger LOG = Logger.getLogger(ConnectionManager.class.getName());
	
	static {
		LOG.setLevel(Level.ALL);
	}
	
	private ServerSocket serverSocket;
	private int portNumber;
	private Thread selfReference;

	/**
	 * Creates server and listens on the port.
	 * Notifies attached listeners upon new connection.
	 */
	public void run() {
		try {
			serverSocket = new ServerSocket(portNumber);
			
			LOG.info("Starting server on port "+portNumber);
			while (true) {
				LOG.fine("Waiting for connection...");
				Socket clientSocket = serverSocket.accept();
				LOG.fine(String.format("New connection from %s:%d", clientSocket.getInetAddress().toString(), clientSocket.getPort()));
				Connection conn = new Connection(clientSocket);
				// notify listener
				for (INetworkListener nl: listener) {
					nl.onConnection(conn);
				}
			}
		} catch (IOException e) {
			if (!e.getMessage().equals("socket closed"))
				LOG.warning(e.getMessage());;
		} finally {
			LOG.info("Shutting down...");
			try {
				serverSocket.close();
			} catch (IOException e) {}
		}
	}
	
	/**
	 * Starts ConnectionManager on default port.
	 */
	public ConnectionManager() {
		this(DEFAULT_PORT);
	}
	/**
	 * Starts ConnectionManager on a different port.
	 * Creates a new thread for listening on the port.
	 * @param pPortNumber port on which the server should listen
	 */
	public ConnectionManager(int pPortNumber) {
		portNumber = pPortNumber;
		
		// fork manager
		selfReference = new Thread(this);
		selfReference.start();
	}
	
	/**
	 * Stops the Manager.
	 * By closing the socket, the thread will receive an exception and terminate itself.
	 */
	public synchronized void stop() {
		try {
			serverSocket.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	/**
	 * Connections to remote host on the default port.
	 * @param pHost hostname / ip of the remote host
	 * @return the new connection
	 * @throws IOException yea yea networking..
	 */
	public static Connection connect(String pHost) throws IOException {
		return connect(pHost, DEFAULT_PORT);
	}
	/**
	 * Connects to remote host on specified port.
	 * @param pHost hostname / ip of the remote host
	 * @param pPortNumber port number on the remote host
	 * @return the new connection
	 * @throws IOException yea yea networking..
	 */
	public static Connection connect(String pHost, int pPortNumber) throws IOException {
		LOG.fine(String.format("Connection to %s:%d", pHost, pPortNumber));
		Socket clientSocket = new Socket(pHost, pPortNumber);
		Connection conn = new Connection(clientSocket);
		
		return conn;
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
