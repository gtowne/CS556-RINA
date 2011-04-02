package lib.interfaces;
import java.io.*;

public interface RINASocket {
	
	public int getConnID();
	public void connect(String name) throws IOException;
	public void close() throws IOException;
	public void write(byte[] data) throws IOException;
	public byte[] read() throws IOException;
	public boolean isOpen();
}
