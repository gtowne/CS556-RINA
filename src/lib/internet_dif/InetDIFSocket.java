/*
 * This is an implementation of the RINA socket interface for the specific DIF-layer
 * that sits directly on top of TCP/IP. 
 */

package lib.internet_dif;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.LinkedList;

import javax.activity.InvalidActivityException;

import lib.Member;
import lib.Message;
import lib.ResourceInformationBase;
import lib.interfaces.RINASocket;

public class InetDIFSocket implements RINASocket {
	private enum SocketState {CLOSED, INIT, OPEN, TEARDOWN, ERROR};

	private SocketState curState;
	private InetIPC containingIPC;
	private ResourceInformationBase RIB;
	private String destName;
	private int destListeningPort;
	private String hostName;
	private int connID;
	private LinkedList<InetDIFPacket> queuedPackets;

	private Socket tcpSocket;
	private DataOutputStream output;
	private DataInputStream input;

/*
 * -----------------------------------------------------------------------------
 * PUBLIC SOCKET INTERFACE
 * -----------------------------------------------------------------------------
 */
	/**
	 * Open a connection to another IPC process within the DIF
	 * @param The name of the process to connect to
	 * @throws IOException
	 */
	public synchronized void connect(String destName) throws IOException {
		if (curState != SocketState.CLOSED) {
			throw new InvalidActivityException();
		}
		
		if (!RIB.containsMember(destName)) {
			throw new UnknownHostException();
		}

		curState = SocketState.INIT;

		this.destName = destName;
		Member dest = RIB.getMemberByName(destName);
		tcpSocket = new Socket(dest.getPointOfAttachment(), dest.getPort());
		output = new DataOutputStream(tcpSocket.getOutputStream());
		input = new DataInputStream(tcpSocket.getInputStream());

		// Negotiate connection ID, ensure uniqueness for both hosts, keep suggesting
		// new connection IDs while the other endpoint's replied proposed IDs don't match
		// mine
		boolean connected = false;
		while (!connected) {
			send(InetDIFPacket.initPacket(connID, hostName, containingIPC.getRIBListing().getPort(), destName));

			InetDIFPacket response = receive();
			
			this.destListeningPort = response.senderListeningPort;

			if (response.proposedConnID == connID) {
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
	
	/**
	 * Close this socket
	 * @throws IOException
	 */
	public synchronized void close() throws IOException {
		if (curState != SocketState.OPEN) {
			throw new InvalidActivityException();
		}
		
		curState = SocketState.TEARDOWN;
		tcpSocket.close();
		curState = SocketState.CLOSED;
		containingIPC.removeSocket(this);
	}

	/**
	 * @param Data to be sent to the other hose
	 * @throws IOException
	 */
	public synchronized void write(byte[] data) throws IOException {
		if (curState != SocketState.OPEN) {
			throw new NotYetConnectedException();
		}
		
		// check to see if we might have relevant routing updates,
		// if we accidently read in a data packet, queue it for the
		// next time the application issues a read()
		while (input.available() > 0) {
			InetDIFPacket p = receive();
			if (p.type == InetDIFPacket.Type.CONTROL) {
				Message m = Message.parseMessage(p.data);
				
				if (m.type != Message.CDAP_UPDATE_RIB_REQ) {
					System.out.println("Error, unexpected message type, expecting RIB Update");
					break;
				}
				
				RIB.addMembers(m.members);
			} else if (p.type == InetDIFPacket.Type.DATA) {
				queuedPackets.add(p);
				break;
			}
		}
		
		send(InetDIFPacket.dataPacket(connID, hostName, containingIPC.getRIBListing().getPort(), destName, data));
	}

	/**
	 * Block indefinitely waiting for data. Return any data received as soon
	 * as it is.
	 * 
	 * @return Data received from the sender
	 * @throws IOException
	 */
	public synchronized byte[] read() throws IOException {
		if (curState != SocketState.OPEN) {
			throw new NotYetConnectedException();
		}
		
		// check to see if we might have relevant routing updates,
		// if we accidently read in a data packet, queue it for the
		// next time the application issues a read()
		while (true) {
			InetDIFPacket p = receive();
			if (p.type == InetDIFPacket.Type.CONTROL) {
				Message m = Message.parseMessage(p.data);
				
				if (m.type != Message.CDAP_UPDATE_RIB_REQ) {
					System.out.println("Error, unexpected message type, expecting RIB Update");
					break;
				}
				
				RIB.addMembers(m.members);
			} else if (p.type == InetDIFPacket.Type.DATA) {
				queuedPackets.add(p);
				break;
			}
		}
		
		// see if we have some queued packets before trying to read more off the wire
		if (!queuedPackets.isEmpty()) {
			return queuedPackets.remove().data;
		}
		
		//InetDIFPacket p = receive();
		
		if (p.type == InetDIFPacket.Type.DATA) {
			return p.payload;
		}
		
		throw new IOException();
	}
	
	/**
	 * @return Unique integer ID for the connection at this socket
	 */
	public synchronized int getConnID() {
		return connID;
	}
	
	/**
	 * @return RINA name of destination
	 */
	public synchronized String getDestName() {
		return destName;
	}
	
	/**
	 * @return RINA name of destination
	 */
	public synchronized String getDestAddr() {
		return tcpSocket.getInetAddress().getCanonicalHostName();
	}
	
	public synchronized int getDestListeningSocket() {
		return this.destListeningPort;
	}

	/**
	 * @return True iff this socket is open
	 */
	public synchronized boolean isOpen() {
		return curState == SocketState.OPEN;
	}

	
/*
 * -----------------------------------------------------------------------------
 * LOCAL METHODS
 * -----------------------------------------------------------------------------
 */
	protected InetDIFSocket(InetIPC containingIPC, int proposedConnID) {
		curState = SocketState.CLOSED;
		connID = proposedConnID;
		this.containingIPC = containingIPC;
		RIB = containingIPC.getRIB();
		this.hostName = containingIPC.getName();
		queuedPackets = new LinkedList<InetDIFPacket>();
	}
	
	protected void writeControl(byte[] bs) {
		try {
			send(InetDIFPacket.controlPacket(connID, hostName, containingIPC.getRIBListing().getPort(), destName, bs));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Wrap an existing TCP socket in this RINA interface. Used to create a RINA socket around
	 * the TCP socket spawned from a ServerSocket
	 */
	protected synchronized void initUsingExistingSocket(Socket socket) throws IOException{
		this.curState = SocketState.INIT;
		this.tcpSocket = socket;

		this.input = new DataInputStream(tcpSocket.getInputStream());
		this.output = new DataOutputStream(tcpSocket.getOutputStream());

		InetDIFPacket initPacket = this.receive();
		
		if (initPacket.type != InetDIFPacket.Type.INIT) {
			throw new IOException();
		}
		
		
		
		int proposedConnID = initPacket.proposedConnID;
		destName = initPacket.senderName;
		destListeningPort = initPacket.senderListeningPort;
		
		// Negotiate conn ID with other endpoint
		// While there's already a connection with the proposed ID,
		// reply with a proposal of your own.
		while (containingIPC.connectionWithID(proposedConnID)) {
			int myProposedID = containingIPC.generateConnID();
			send(InetDIFPacket.initPacket(myProposedID, hostName, containingIPC.getRIBListing().getPort(), destName));
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
			send(InetDIFPacket.initPacket(proposedConnID, hostName, containingIPC.getRIBListing().getPort(), destName));
			connID = proposedConnID;
			curState = SocketState.OPEN;
		}
	}

	private synchronized void send(InetDIFPacket p) throws IOException {
		int len = p.data.length;
		output.writeInt(len);
		output.write(p.data);
	}

	private synchronized InetDIFPacket receive() throws IOException {
		byte[] data = new byte[input.readInt()];
		input.readFully(data);
		return InetDIFPacket.parsePacket(data);
	}

}
