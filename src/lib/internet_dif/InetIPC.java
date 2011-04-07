/**
 * An IPC process represents a host on a RINA network specified uniquely by a name. 
 * This interface can be used to create a new IPC process with a given name, which
 * then acts as a factory to create sockets that allow the application to communicate
 * with other members of the DIF.
 * 
 * This is the instantiation of the IPC process for the special-case DIF level that sits directly on
 * top of TCP/IP.
 */

package lib.internet_dif;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.NotYetConnectedException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import lib.DIF;
import lib.Member;
import lib.Message;
import lib.ResourceInformationBase;
import lib.interfaces.IPC;
import lib.interfaces.RINASocket;

public class InetIPC implements IPC {
	private String name;
	private DIF containingDIF;
	private InetIPC underlyingIPC;
	private ResourceInformationBase RIB;
	private InetDIFServerSocket serverSocket;
	private Hashtable<Integer, RINASocket> sockets;
	private Member listing;
	private Random rand;

/*
 * -----------------------------------------------------------------------------
 * PUBLIC INTERFACE
 * -----------------------------------------------------------------------------
 */
	public InetIPC(String name) {
		this.name = name;
		rand = new Random();
		RIB = new ResourceInformationBase();
		sockets = new Hashtable<Integer, RINASocket>();
		listing = new Member(name, new DIF("Internet"), "");
	}
	
	public synchronized boolean joinDIF(String difName) throws Exception {
		// get IDD address from DNS
		Socket toDNS = new Socket(Constants.DNS_IP, Constants.DNS_PORT);
		DataOutputStream out = new DataOutputStream(toDNS.getOutputStream());
		out.write(Message.newDNS_REQ(Constants.IDD_NAME));
		
		Message dnsResponse = Message.readFromSocket(toDNS);
		toDNS.close();
		
		if (dnsResponse.type != Message.DNS_RSP) {
			System.out.println("DNS error resolving IDD");
			return false;
		}
		
		String iddAddr = dnsResponse.text1;
		
		// get NMS Name from IDD
		Socket toIDD = new Socket(iddAddr, Constants.IDD_PORT);
		out = new DataOutputStream(toIDD.getOutputStream());
		out.write(Message.newCDAP_IDD_REQ(3, difName));
		Message reply = Message.readFromSocket(toIDD);
		if (reply.type != Message.CDAP_IDD_RSP || reply.errorCode != 1) {
			System.out.println("Could not resolve NMS of specified DIF");
			return false;
		}
		String nmsName = reply.text1;
		toIDD.close();
		
		// get NMS IP from DNS
		toDNS = new Socket(Constants.DNS_IP, Constants.DNS_PORT);
		out = new DataOutputStream(toDNS.getOutputStream());
		out.write(Message.newDNS_REQ(nmsName));
		
		dnsResponse = Message.readFromSocket(toDNS);
		toDNS.close();
		
		if (dnsResponse.type != Message.DNS_RSP) {
			System.out.println("DNS error resolving IDD");
			return false;
		}
		
		String nmsIP = dnsResponse.text1;
		
		// request to join the DIF from NMS
		RIB.addMember(new Member(nmsName, new DIF("Internet"), nmsIP, Constants.DIF_MANAGER_PORT));
		InetDIFSocket toNMS = this.openNewSocket(nmsName);
		toNMS.write(Message.newCDAP_CONNECT_REQ(Constants.username, Constants.password));
		reply = Message.parseMessage(toNMS.read());
		
		
		return true;
	}
	
	/**
	 * @return The data used to populate this process's entry in the DIF's Resource Information Base
	 */
	public synchronized Member getRIBListing() {
		return listing;
	}
	
	/**
	 * Open a server socket for this process. Only one ServerSocket can 
	 * be opened for a single IPC instance. Repeated calls should be ignored.
	 * @return Initialized ServerSocket
	 * @throws IOException
	 */
	public synchronized InetDIFServerSocket newServerSocket() throws IOException {
		if (serverSocket != null) {
			return null;
		}
		
		serverSocket = new InetDIFServerSocket(this);
		listing = serverSocket.bind();
		return serverSocket;
	}
	
	public synchronized InetDIFServerSocket newServerSocket(int port) throws IOException {
		if (serverSocket != null) {
			return null;
		}
		
		serverSocket = new InetDIFServerSocket(this);
		listing = serverSocket.bind(port);
		return serverSocket;
	}
	
	/**
	 * Open a new socket to the IPC process in this DIF at the given name.
	 * @param Name of hose to connect to
	 * @return Initialized socket for the new connection
	 * @throws IOException
	 */
	public synchronized InetDIFSocket openNewSocket(String destName) throws IOException {
		int newID = generateConnID();
		InetDIFSocket newSocket = new InetDIFSocket(this, newID);
		sockets.put(newID, newSocket);
		
		newSocket.connect(destName);
		
		return newSocket;
	}
	
	/**
	 * Update this IPC process's local view of the DIF's RIB with the 
	 * @param New Members
	 */
	public synchronized void updateRIB(Collection<Member> newMembers) {
		RIB.addMembers(newMembers);
	}
	
	
/*
 * -----------------------------------------------------------------------------
 * LOCAL METHODS
 * -----------------------------------------------------------------------------
 */
	
	protected synchronized int generateConnID() {
		int newID = Integer.MAX_VALUE - rand.nextInt(Integer.MAX_VALUE - 100);
		
		while(sockets.containsKey(newID)) {
			newID = rand.nextInt();
		}
		
		return newID;
	}
	
	protected synchronized boolean updateConnID(InetDIFSocket socket, int newID) {
		if (sockets.containsKey(newID)) {
			return false;
		}
		
		sockets.remove(socket);
		sockets.put(newID, socket);
		return true;
	}
	
	protected synchronized void addSocket(InetDIFSocket socket, int connID) {
		sockets.put(connID, socket);
	}
	
	protected synchronized String getName() {
		return name;
	}

	protected synchronized InetIPC getUnderlyingIPC() {
		return underlyingIPC;
	}

	protected synchronized ResourceInformationBase getRIB() {
		return RIB;
	}
	
	protected synchronized boolean connectionWithID(int id) {
		return sockets.containsKey(id);
	}
	
	protected synchronized void removeSocket(InetDIFSocket socket) {
		sockets.remove(socket);
	}


}
