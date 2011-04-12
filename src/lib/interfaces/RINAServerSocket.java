package lib.interfaces;
import java.io.IOException;

public interface RINAServerSocket {
	/**
	 * Block waiting for incoming connections, when one is received,
	 * initialize a new socket and return it
	 * 
	 * @return Established RINA socket for new connection
	 * @throws IOException
	 */
	public RINASocket accept() throws IOException;
	
	/**
	 * Close this socket
	 * @throws IOException
	 */
	public void close() throws IOException;
}
