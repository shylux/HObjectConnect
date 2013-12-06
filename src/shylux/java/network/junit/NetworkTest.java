package shylux.java.network.junit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import shylux.java.network.TCPConnection;
import shylux.java.network.ConnectionManager;
import shylux.java.network.IConnectionListener;
import shylux.java.network.INetworkListener;
import shylux.java.network.UDPMessage;

public class NetworkTest {
	ConnectionManager manager;
	
	private void doConnectAndClose() throws IOException {
		Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", ConnectionManager.DEFAULT_PORT), 200);
        // Let him time to connect
        try {
			Thread.sleep(200);
		} catch (InterruptedException e) {e.printStackTrace();}
        socket.close();
	}
	
	private class SingleConnectionProvider implements INetworkListener {
		public TCPConnection conn;

		public void onConnection(TCPConnection pCon) {
			System.out.println("onConnection");
			conn = pCon;
		}

		@Override
		public void onUDPMessage(UDPMessage pMsg) {
			// TODO Auto-generated method stub
			
		}
	}
	private class ConnectionCounter implements INetworkListener {
		int connectionCounter = 0;
		
		public void onConnection(TCPConnection pCon) {
			connectionCounter++;
		}
		
		public int getCount() {
			return connectionCounter;
		}

		@Override
		public void onUDPMessage(UDPMessage pMsg) {
			// TODO Auto-generated method stub
			
		}
	}
	private class MessageRegister implements IConnectionListener {
		Object lastMessage;
		
		public void onMessage(Object o) {
			System.out.println("Got Message: "+o.toString());
			lastMessage = o;
		}

		public void onClose() {}
	}
	
	@Before
	public void setUp() {
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		ConnectionManager.LOG.addHandler(handler);
		manager = new ConnectionManager();
		
		// give him time to start - TODO maybe move to manager?
		// btw spent half an hour on this shit -.-
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {e.printStackTrace();}
	}
	
	@After
	public void tearDown() {
		manager.stop();
	}

	@Test
	public void testManagerIsListening() throws IOException {
        this.doConnectAndClose();
	}
	
	@Test
	public void testManagerReturnsNewConnection() throws IOException {
		ConnectionCounter counter = new ConnectionCounter();
		manager.addNetworkListener(counter);
		assertEquals(0, counter.getCount());
		this.doConnectAndClose();
		assertEquals(1, counter.getCount());
		this.doConnectAndClose();
		assertEquals(2, counter.getCount());
	}
	
	@Test
	public void testMessageSendOverManager() {
		SingleConnectionProvider scp = new SingleConnectionProvider();
		manager.addNetworkListener(scp);
		TCPConnection conn = null;
		try {
			conn = ConnectionManager.connect("localhost");
		} catch (IOException e) {e.printStackTrace();}
		
		// wait for the connection
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {e.printStackTrace();}

		MessageRegister mr = new MessageRegister();
		scp.conn.addConnectionListener(mr);
		MessageRegister mr2 = new MessageRegister();
		conn.addConnectionListener(mr2);
		
		// check the other direction
		
		conn.sendMessage("Test");
		
		// wait for the message to be tranferred
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {e.printStackTrace();}
		
		assertEquals("Test", mr.lastMessage.toString());
		
		
		// write second message
		
		conn.sendMessage("Bla");
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {e.printStackTrace();}
		
		assertEquals("Bla", mr.lastMessage.toString());
		

		scp.conn.sendMessage("Blub");
		
		// wait for the message to be tranferred
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {e.printStackTrace();}
		
		assertEquals("Blub", mr2.lastMessage.toString());
	}

}
