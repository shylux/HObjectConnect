package shylux.java.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * A network connection used to send and receive messages.
 * Basically a wrapper for socket.
 * @author Lukas Knoepfel <shylux@gmail.com>
 *
 */
public class Connection implements Runnable {
	private Socket socket;
	
	/**
	 * Creates a new connection from a give socket.
	 * Also creates a new thread to listen for new messages.
	 * @param pSocket socket from which input/output streams are used.
	 */
	public Connection(Socket pSocket) {
		socket = pSocket;
		
		Thread th = new Thread(this);
		th.start();
		
		try {
			writer = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			ConnectionManager.LOG.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	

	/**
	 * Listens on the connection for new messages.
	 * Notifies all IConnectionListener on new message.
	 */
	public void run() {
		ObjectInputStream reader;
		try {
			reader = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e1) {e1.printStackTrace();this.onClose();return;}

		Object inData;
		try {
			while ((inData = reader.readObject()) != null) {
				ConnectionManager.LOG.finer(String.format("Received message from %s:%s (%s)", socket.getInetAddress().toString(), socket.getPort(), inData.toString()));
				// notify listener
				for (IConnectionListener cl: listener) {
					cl.onMessage(inData);
				}
			}
				
		} catch (IOException e) {
			this.onClose();
			return;
		} catch (ClassNotFoundException e) {
			ConnectionManager.LOG.warning("Remote sent some gibberish: "+e.getMessage());
		}
		this.onClose();
	}
	
	
	private ObjectOutputStream writer;
	/**
	 * Sends a new message to the remote host.
	 * @param pObj object to send
	 */
	public synchronized void sendMessage(Serializable pObj) {
		ConnectionManager.LOG.finer(String.format("Sending message to %s:%s (%s)", socket.getInetAddress().toString(), socket.getPort(), pObj.toString()));
		try {
			writer.writeObject(pObj);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			this.onClose();
			return;
		}
	}
	
	/**
	 * Terminates connection.
	 */
	public void close() {
		this.onClose();
	}
	
	/* LISTENER */
	List<IConnectionListener> listener = new ArrayList<IConnectionListener>();
	
	/**
	 * Adds a listener. All listener are notified on new message.
	 * @param pCl
	 */
	public synchronized void addConnectionListener(IConnectionListener pCl) {
		listener.add(pCl);
	}
	/**
	 * Removes connection listener from list. That listener wont receive notifications anymore.
	 * @param pCl Listener to remove.
	 */
	public synchronized void removeConnectionListener(IConnectionListener pCl) {
		listener.remove(pCl);
	}
	/**
	 * Clears listener list. Every listener gets removed and wont receive notifications anymore.
	 */
	public synchronized void clearConnectionListener() {
		listener.clear();
	}
	
	/**
	 * Closes the connection.
	 * By closing the socket the reading process gets interrupted and the thread gets terminated.
	 */
	private synchronized void onClose() {
		try {
			socket.close();
		} catch (IOException e) {e.printStackTrace();
		}
		for (IConnectionListener cl: listener) {
			cl.onClose();
		}
	}
}
