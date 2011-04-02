package lib.internet_dif;
import java.io.*;
import java.net.*;
import java.nio.channels.*;

import lib.Member;
import lib.ResourceInformationBase;
import lib.interfaces.RINASocket;

public class InetDIFSocket implements RINASocket {
	private enum SocketState {CLOSED, INIT, OPEN, TEARDOWN, ERROR};

	private SocketState curState;
	private InetIPC containingIPC;
	private ResourceInformationBase RIB;
	private String destName;
	private String hostName;
	private int connID;

	private Socket tcpSocket;
	private OutputStream output;
	private DataInputStream input;

	public InetDIFSocket(InetIPC containingIPC, int proposedConnID) {
		curState = SocketState.CLOSED;
		connID = proposedConnID;
		this.containingIPC = containingIPC;
		RIB = containingIPC.getRIB();
		this.hostName = containingIPC.getName();
	}

	public void connect(String destName) throws IOException {
		if (!RIB.containsMember(destName)) {
			throw new UnknownHostException();
		}

		curState = SocketState.INIT;

		this.destName = destName;
		Member dest = RIB.getMemberByName(destName);
		tcpSocket = new Socket(dest.getPointOfAttachment(), dest.getPort());
		output = tcpSocket.getOutputStream();
		input = new DataInputStream(tcpSocket.getInputStream());

		// Negotiate connection ID, ensure uniqueness for both hosts, keep suggesting
		// 
		boolean connected = false;
		while (!connected) {
			send(InetDIFPacket.initPacket(connID, hostName, destName));

			InetDIFPacket response = receive();

			if (response.connID == connID) {
				connected = true;
				curState = SocketState.OPEN;
				break;
			} 

			else if (!containingIPC.connectionWithID(response.connID)) {
				connID = response.connID;
				containingIPC.updateConnID(this, connID);
				curState = SocketState.OPEN;
				connected = true;
				break;
			}
			connID = containingIPC.generateConnID();	
		}
	}

	protected void initUsingExistingSocket(Socket socket) throws IOException{
		this.curState = SocketState.INIT;
		this.tcpSocket = socket;

		this.input = new DataInputStream(tcpSocket.getInputStream());

		this.output = tcpSocket.getOutputStream();

		InetDIFPacket initPacket = this.receive();
		
		if (initPacket.type != InetDIFPacket.Type.INIT) {
			throw new IOException();
		}
		
		int proposedConnID = initPacket.proposedConnID;
		destName = initPacket.senderName;
		
		// Negotiate conn ID with other endpoint
		// While there's already a connection with the proposed ID,
		// reply with a proposal of your own.
		while (containingIPC.connectionWithID(proposedConnID)) {
			int myProposedID = containingIPC.generateConnID();
			send(InetDIFPacket.initPacket(myProposedID, hostName, destName));
			InetDIFPacket response = receive();
			proposedConnID = response.proposedConnID;
			
			// If the other endpoint is happy with my proposed ID,
			// consider the connection open
			if (proposedConnID == myProposedID) {
				this.curState = SocketState.OPEN;
				connID = proposedConnID;
				break;
			}
		}
		
		// If I'm happy with the proposed ID from the other endpoint,
		// reply with the same proposed ID and consider myself open
		if (curState != SocketState.OPEN) {
			send(InetDIFPacket.initPacket(proposedConnID, hostName, destName));
			curState = SocketState.OPEN;
		}
	}

	public void close() throws IOException {
		curState = SocketState.TEARDOWN;
		tcpSocket.close();
		curState = SocketState.CLOSED;
	}

	public void write(byte[] data) throws IOException {
		if (curState != SocketState.OPEN) {
			throw new NotYetConnectedException();
		}
		
		send(InetDIFPacket.dataPacket(connID, hostName, destName, data));
	}

	public byte[] read() throws IOException {
		if (curState != SocketState.OPEN) {
			throw new NotYetConnectedException();
		}
		
		InetDIFPacket p = receive();
		
		if (p.type == InetDIFPacket.Type.DATA) {
			return p.payload;
		}
		
		throw new IOException();
	}

	public int getConnID() {
		return connID;
	}

	public boolean isOpen() {
		return curState == SocketState.OPEN;
	}

	private void send(InetDIFPacket p) throws IOException {
		output.write(p.data);
	}

	private InetDIFPacket receive() throws IOException {
		byte[] data = new byte[input.available()];
		input.readFully(data);
		return InetDIFPacket.parsePacket(data);
	}

}
