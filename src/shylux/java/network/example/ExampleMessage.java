package shylux.java.network.example;

import java.io.Serializable;

public class ExampleMessage implements Serializable {

	enum State {OVER, OUT}
	private String message;
	private State state;
	
	public ExampleMessage(String pMessage, State pState) {
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
