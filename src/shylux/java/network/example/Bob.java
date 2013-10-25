package shylux.java.network.example;

import shylux.java.network.Connection;
import shylux.java.network.ConnectionManager;
import shylux.java.network.IConnectionListener;
import shylux.java.network.INetworkListener;
import shylux.java.network.example.Message.State;

public class Bob implements IConnectionListener, INetworkListener {
	ConnectionManager manager;
	Connection connToAlice;
	
	public Bob() {
		manager = new ConnectionManager();
		manager.addNetworkListener(this);
	}
	
	private void processMessage(Message pMsg) {
		System.out.println("Bob got Message from Alice: "+pMsg.getMessage());
		switch (pMsg.getState()) {
		case OVER:
			Message msg_reply = new Message("Yea i created new ones using this algorithm promoted by NIST, Dual_EC_DRBG!", State.OVER);
			connToAlice.sendMessage(msg_reply);
			break;
		default:
			break;
		}
	}

	@Override
	public void onConnection(Connection pCon) {
		connToAlice = pCon;
		connToAlice.addConnectionListener(this);
	}

	@Override
	public void onMessage(Object o) {
		if (o instanceof Message) {
			Message msg = (Message) o;
			processMessage(msg);
		}
	}

	@Override
	public void onClose() {
		manager.stop();
	}

}
