/*
 * Simple test for the RINA sockets. Opens up 100 concurrent connections to a single server and sends strings. 
 */

package testing;
import java.util.concurrent.Semaphore;
import lib.ResourceInformationBase;
import lib.internet_dif.InetDIFServerSocket;
import lib.internet_dif.InetDIFSocket;
import lib.internet_dif.InetIPC;

public class Client extends Thread {
		ResourceInformationBase RIB;
		Semaphore cSem;
		
		public Client(Semaphore cSem, ResourceInformationBase RIB) {
			this.RIB = RIB;
			this.cSem = cSem;
		}

		public void run() {
			try {
				doWork();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void doWork() throws Exception {
			InetIPC client = new InetIPC("Client_Process");
			
			RIB.addMember(client.getRIBListing());
			
			Thread.sleep(5000);
			
			client.updateRIB(RIB.getMemberList());
			
			Thread.sleep(5000);
			
			for (int i = 0; i < 100; i++) {
				Sender s = new Sender(client);
				s.start();
			}
			
		}
		
		public class Sender extends Thread {
			InetIPC ipc;
			
			public Sender(InetIPC ipc) {
				this.ipc = ipc;
			}
			
			public void run() {
				try {
					doWork();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			public void doWork() throws Exception {
				InetDIFSocket socket = ipc.openNewSocket("Server_Process");
								
				socket.write(("From: " + socket.getConnID() + " This is some data coming from client").getBytes());
				
				Thread.sleep(10);
				
				socket.write(("From: " + socket.getConnID() + " This is some more data coming from the client").getBytes());
				
				Thread.sleep(10);
				
				socket.write(("From: " + socket.getConnID() + " This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. This is a whole bunch of data coming from the client. ").getBytes());
				
				Thread.sleep(10);
				
				socket.write(("From: " + socket.getConnID() + " This is the last piece of data coming from connection").getBytes());				
			}
		}
	}

	