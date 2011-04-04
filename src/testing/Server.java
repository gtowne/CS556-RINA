/*
 * Simple test for RINA sockets. Start listening at "Server_Process", accept all incoming
 * connections, and print all messages received.
 */

package testing;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import lib.ResourceInformationBase;
import lib.internet_dif.InetDIFServerSocket;
import lib.internet_dif.InetDIFSocket;
import lib.internet_dif.InetIPC;

public class Server extends Thread {
		ResourceInformationBase RIB;
		Semaphore sSem;
		
		public Server(Semaphore sSem, ResourceInformationBase RIB) {
			this.RIB = RIB;
			this.sSem = sSem;
		}

		public void run() {
			try {
				doWork();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void doWork() throws Exception {
			InetIPC server = new InetIPC("Server_Process");

			InetDIFServerSocket SS = server.newServerSocket();
			
			RIB.addMember(server.getRIBListing());
			
			Thread.sleep(5000);
						
			server.updateRIB(RIB.getMemberList());
			
			Thread.sleep(5000);
			
			while (true) {
				InetDIFSocket socket = SS.accept();
						
				Writer w = new Writer(socket);
				w.start();
			}
		}
		
		public class Writer extends Thread {
			InetDIFSocket socket;
			
			public Writer (InetDIFSocket socket) {
				this.socket = socket;
			}
			
			public void run() {
				while (true) {
					try {
						System.out.println(new String(socket.read()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}