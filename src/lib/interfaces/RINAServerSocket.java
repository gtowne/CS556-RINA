package lib.interfaces;
import java.io.IOException;

import lib.Member;

public interface RINAServerSocket {
	public RINASocket accept() throws IOException;
	public void close() throws IOException;
	public Member bind() throws IOException;
}
