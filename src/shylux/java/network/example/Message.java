package shylux.java.network.example;

import java.io.Serializable;

public class Message implements Serializable {

	enum State {OVER, OUT}
	private String message;
	private State state;
	
	public Message(String pMessage, State pState) {
		message = pMessage;
		state = pState;
	}

	public String getMessage() {
		return message;
	}

	public State getState() {
		return state;
	}
}
