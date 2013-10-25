package shylux.java.network;

/**
 * Inteface for classes who listen on a connection for messages.
 * @author Lukas Knoepfel <shylux@gmail.com>
 *
 */
public interface IConnectionListener {
	/**
	 * A new message arrived
	 * @param o new message
	 */
	public void onMessage(Object o);
	/**
	 * The connection has been closed.
	 */
	public void onClose();
}
