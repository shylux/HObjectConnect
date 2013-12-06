package shylux.java.network.example;

import shylux.java.network.TCPConnection;
import shylux.java.network.ConnectionManager;
import shylux.java.network.IConnectionListener;
import shylux.java.network.INetworkListener;
import shylux.java.network.UDPMessage;
import shylux.java.network.example.ExampleMessage.State;

public class Bob implements IConnectionListener, INetworkListener {
	ConnectionManager manager;
	TCPConnection connToAlice;
	
	public Bob() {
		manager = new ConnectionManager();
		manager.addNetworkListener(this);
	}
	
	private void processMessage(ExampleMessage pMsg) {
		System.out.println("Bob got Message from Alice: "+pMsg.getMessage());
		switch (pMsg.getState()) {
		case OVER:
			ExampleMessage msg_reply = new ExampleMessage("Yea i created new ones using this algorithm promoted by NIST, Dual_EC_DRBG!", State.OVER);
			connToAlice.sendMessage(msg_reply);
			break;
		default:
			break;
		}
	}

	@Override
	public void onConnection(TCPConnection pCon) {
		connToAlice = pCon;
		connToAlice.addConnectionListener(this);
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
		manager.stop();
	}

	@Override
	public void onUDPMessage(UDPMessage pMsg) {
		// TODO Auto-generated method stub
		
	}

}
