package shylux.java.network.example;

import java.io.IOException;

import shylux.java.network.TCPConnection;
import shylux.java.network.ConnectionManager;
import shylux.java.network.IConnectionListener;

/**
 * This is a small program to demonstrate the functionality of shylux.java.network.
 * The setup consists of two parties, Alice and Bob.
 * First Bob starts a server. Alice then connects and send a message to which Bob replies.
 * When Alice receives the reply from Bob she sends a last message back to bob and then terminates her connection.
 * Bob receives the last message and then gets notified that Alice terminated the connection. He will then stop his server to terminate the program.
 * 
 *   Alice              Bob
 *                   start server..
 *     ------connect--->
 *     =======Hi=======>>
 *        <<=Hello=====
 *     =======Bye======>>
 *     --XX--terminate-->
 *                   stop server
 * 
 * @author Lukas Knoepfel <shylux@gmail.com>
 *
 */
public class AliceMain implements IConnectionListener {
	TCPConnection connToBob;
	
	public static void main(String[] args) {
		new Bob();
		new AliceMain();
	}
	
	public AliceMain() {
		try {
			connToBob = ConnectionManager.connect("localhost", ConnectionManager.DEFAULT_PORT);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		connToBob.addConnectionListener(this);
		ExampleMessage msg_hi = new ExampleMessage("Hi bob. Do you have new keys? Seems like i cant decrypt your messages anymore.", ExampleMessage.State.OVER);
		connToBob.sendMessage(msg_hi);
	}
	
	private void processMessage(ExampleMessage pMsg) {
		System.out.println("Alice got Message from Bob: "+pMsg.getMessage());
		ExampleMessage msg_bye = new ExampleMessage("Why am i even talking to you?", ExampleMessage.State.OUT);
		connToBob.sendMessage(msg_bye);
		connToBob.close();
	}


	@Override
	public void onMessage(Object o) {
		if (o instanceof ExampleMessage) {
			ExampleMessage msg = (ExampleMessage) o;
			processMessage(msg);
		}
	}

	@Override
	public void onClose() {
	}

}
