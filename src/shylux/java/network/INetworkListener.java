package shylux.java.network;

/**
 * Inteface for classes who listen for new connections.
 * @author Lukas Knoepfel <shylux@gmail.com>
 *
 */
public interface INetworkListener {
	/**
	 * A new connection has been established.
	 * @param pCon the new connection
	 */
	public void onConnection(Connection pCon);
}
