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
	
	
	public InetDIFServerSocket(InetIPC containingIPC) {
		name = containingIPC.getName();
		this.containingIPC = containingIPC;
		RIB = containingIPC.getRIB();
		
	}
	
	public RINASocket accept() throws IOException {
		Socket tcpSocket = tcpServerSocket.accept();
		InetDIFSocket newSocket = new InetDIFSocket(containingIPC, 0);
		newSocket.initUsingExistingSocket(tcpSocket);
		containingIPC.addSocket(newSocket, newSocket.getConnID());
		
		return newSocket;
	}

	public void close() throws IOException {
		tcpServerSocket.close();
	}

	public Member bind() throws IOException {
		tcpServerSocket = new ServerSocket(0);
		int port =  tcpServerSocket.getLocalPort();
		String hostName = tcpServerSocket.getInetAddress().getCanonicalHostName();
		
		Member listing = new Member(name, new DIF("Internet"), hostName);
		listing.setPort(port);
		return listing;
	}

}
