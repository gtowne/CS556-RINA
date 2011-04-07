/*
 * InetDIFServerSocket
 * 
 * This is the implementation of the RINAServerSocket interface for the DIF layer
 * that sits directly on top of TCP. It provides an interface to 
 */

package lib.internet_dif;

import java.io.*;
import java.net.*;
import lib.DIF;
import lib.Member;
import lib.ResourceInformationBase;
import lib.interfaces.RINAServerSocket;
import lib.interfaces.RINASocket;

public class InetDIFServerSocket implements RINAServerSocket {
	private String name;
	private InetIPC containingIPC;
	private ResourceInformationBase RIB;
	private ServerSocket tcpServerSocket;
	
	
/*
 * -----------------------------------------------------------------------------
 * PUBLIC SOCKET INTERFACE
 * -----------------------------------------------------------------------------
 */
	
	/**
	 * Block waiting for incoming connections, when one is received,
	 * initialize a new socket and return it
	 * 
	 * @return Established RINA socket for new connection
	 * @throws IOException
	 */
	public synchronized InetDIFSocket accept() throws IOException {
		Socket tcpSocket = tcpServerSocket.accept();
		InetDIFSocket newSocket = new InetDIFSocket(containingIPC, 0);
		newSocket.initUsingExistingSocket(tcpSocket);
		containingIPC.addSocket(newSocket, newSocket.getConnID());
		
		return newSocket;
	}

	/**
	 * Close this socket
	 * @throws IOException
	 */
	public synchronized void close() throws IOException {
		tcpServerSocket.close();
	}

/*
 * -----------------------------------------------------------------------------
 * LOCAL METHODS
 * -----------------------------------------------------------------------------
 */
	
	protected InetDIFServerSocket(InetIPC containingIPC) {
		name = containingIPC.getName();
		this.containingIPC = containingIPC;
		RIB = containingIPC.getRIB();
		
	}

	protected synchronized Member bind() throws IOException {
		tcpServerSocket = new ServerSocket(0);
		int port =  tcpServerSocket.getLocalPort();
		String hostName = tcpServerSocket.getInetAddress().getCanonicalHostName();
		
		Member listing = new Member(name, new DIF("Internet"), hostName);
		listing.setPort(port);
		return listing;
	}
	
	protected synchronized Member bind(int port) throws IOException {
		tcpServerSocket = new ServerSocket(port);
		int port =  tcpServerSocket.getLocalPort();
		String hostName = tcpServerSocket.getInetAddress().getCanonicalHostName();
		
		Member listing = new Member(name, new DIF("Internet"), hostName);
		listing.setPort(port);
		return listing;
	}

}
