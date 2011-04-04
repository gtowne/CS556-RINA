package lib.interfaces;
import java.io.*;

public interface RINASocket {
	
	/**
	 * Open a connection to another IPC process within the DIF
	 * @param The name of the process to connect to
	 * @throws IOException
	 */
	public void connect(String name) throws IOException;
	
	/**
	 * Close this socket
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	/**
	 * @param Data to be sent to the other hose
	 * @throws IOException
	 */
	public void write(byte[] data) throws IOException;
	
	/**
	 * Block indefinitely waiting for data. Return any data received as soon
	 * as it is.
	 * 
	 * @return Data received from the sender
	 * @throws IOException
	 */
	public byte[] read() throws IOException;
	
	/**
	 * @return Unique integer ID for the connection at this socket
	 */
	public int getConnID();
	
	/**
	 * @return True iff this socket is open
	 */
	public boolean isOpen();
}
