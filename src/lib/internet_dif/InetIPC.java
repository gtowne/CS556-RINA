package lib.internet_dif;
import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

import lib.DIF;
import lib.Member;
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

	public InetIPC(String name) {
		this.name = name;
		rand = new Random();
		RIB = new ResourceInformationBase();
		sockets = new Hashtable<Integer, RINASocket>();
		listing = new Member(name, new DIF("Internet"), "");
	}
	
	public boolean joinDIF(String difName) throws Exception {
		throw new Exception();
	}
	
	public InetDIFServerSocket newServerSocket() throws IOException {
		if (serverSocket != null) {
			return null;
		}
		
		serverSocket = new InetDIFServerSocket(this);
		listing = serverSocket.bind();
		return serverSocket;
	}
	
	public InetDIFSocket openNewSocket(String destName) throws IOException {
		int newID = generateConnID();
		InetDIFSocket newSocket = new InetDIFSocket(this, newID);
		sockets.put(newID, newSocket);
		
		newSocket.connect(destName);
		
		return null;
	}
	
	public void updateRIB(Collection<Member> newMembers) {
		RIB.addMembers(newMembers);
	}
	
	public int generateConnID() {
		int newID = rand.nextInt();
		
		while(sockets.containsKey(newID)) {
			newID = rand.nextInt();
		}
		
		return newID;
	}
	
	protected boolean updateConnID(InetDIFSocket socket, int newID) {
		if (sockets.containsKey(newID)) {
			return false;
		}
		
		sockets.remove(socket);
		sockets.put(newID, socket);
		return true;
	}
	
	protected void addSocket(InetDIFSocket socket, int connID) {
		sockets.put(connID, socket);
	}
	
	protected String getName() {
		return name;
	}

	protected InetIPC getUnderlyingIPC() {
		return underlyingIPC;
	}

	protected ResourceInformationBase getRIB() {
		return RIB;
	}
	
	protected boolean connectionWithID(int id) {
		return sockets.containsKey(id);
	}
	
	public Member getRIBListing() {
		return listing;
	}


}
