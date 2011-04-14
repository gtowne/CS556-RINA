package rina_server;

import java.io.IOException;
import java.util.LinkedList;

import lib.Member;
import lib.Message;
import lib.internet_dif.*;

public class RINAServer {
	private String name;
	private static final String resource = "Proin at eros non eros adipiscing mollis. Donec semper turpis sed diam. Sed consequat ligula nec tortor. Integer eget sem. Ut vitae enim eu est vehicula gravida. Morbi ipsum ipsum, porta nec, tempor id, auctor vitae, purus. Pellentesque neque. Nulla luctus erat vitae libero. Integer nec enim. Phasellus aliquam enim et tortor. Quisque aliquet, quam elementum condimentum feugiat, tellus odio consectetuer wisi, vel nonummy sem neque in elit. Curabitur eleifend wisi iaculis ipsum. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. In non velit non ligula laoreet ultrices. Praesent ultricies facilisis nisl. Vivamus luctus elit sit amet mi. Phasellus pellentesque, erat eget elementum volutpat, dolor nisl porta neque, vitae sodales ipsum nibh in ligula. Maecenas mattis pulvinar diam. Curabitur sed leo.";
	
	public RINAServer(String name, String hostDIF) {
		InetIPC ipc = new InetIPC(name);
		boolean joinSuccess = false;
		try {
			joinSuccess = ipc.joinDIF(hostDIF);
		} catch (Exception e) {
			System.out.println("RINA Server's attempt to join DIF \"" + hostDIF + "\" failed.");
			e.printStackTrace();
		}
		
		if (joinSuccess)
			System.out.println("RINA Server successfully joined DIF " + hostDIF);
		
		InetDIFServerSocket serverSocket = null;
		try {
			serverSocket = ipc.newServerSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while (true) {
			InetDIFSocket newSocket = null;
			try {
				newSocket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Message newMessage = null;
			try {
				newMessage = Message.parseMessage(newSocket.read());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (newMessage.type == Message.HTTP_GET) {
				System.out.println("RINA Server received HTTP-GET request, replying with response");
				try {
					newSocket.write(Message.newHTTP_RSP(resource));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (newMessage.type == Message.CDAP_UPDATE_RIB_REQ) {
				LinkedList<Member> members = newMessage.members;
				ipc.updateRIB(members);
				try {
					newSocket.write(Message.newCDAP_UPDATE_RIB_RSP(0));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				newSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
	}
}
