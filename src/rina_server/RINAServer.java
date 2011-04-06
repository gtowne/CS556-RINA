package rina_server;

import java.io.IOException;

import lib.Message;
import lib.internet_dif.*;

public class RINAServer {
	private String name;
	private static final String resource = "Proin at eros non eros adipiscing mollis. Donec semper turpis sed diam. Sed consequat ligula nec tortor. Integer eget sem. Ut vitae enim eu est vehicula gravida. Morbi ipsum ipsum, porta nec, tempor id, auctor vitae, purus. Pellentesque neque. Nulla luctus erat vitae libero. Integer nec enim. Phasellus aliquam enim et tortor. Quisque aliquet, quam elementum condimentum feugiat, tellus odio consectetuer wisi, vel nonummy sem neque in elit. Curabitur eleifend wisi iaculis ipsum. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. In non velit non ligula laoreet ultrices. Praesent ultricies facilisis nisl. Vivamus luctus elit sit amet mi. Phasellus pellentesque, erat eget elementum volutpat, dolor nisl porta neque, vitae sodales ipsum nibh in ligula. Maecenas mattis pulvinar diam. Curabitur sed leo.";
	
	public RINAServer(String name, String hostDIF) {
		InetIPC ipc = new InetIPC(name);
		try {
			ipc.joinDIF(hostDIF);
		} catch (Exception e) {
			System.out.println("Attempt to join DIF \"" + hostDIF + "\" failed.");
			e.printStackTrace();
		}
		
		System.out.println("DIF joined successfully");
		
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
				try {
					newSocket.write(resource.getBytes());
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
