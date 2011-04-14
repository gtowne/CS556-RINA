package idd;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import lib.Message;
import lib.ResourceInformationBase;
import lib.internet_dif.Constants;

public class TCPIDDServer extends Thread {

	private ServerSocket serverSocket;
	private InterDIFDirectory IDD;
	private InetAddress knownProxy;

	public TCPIDDServer() {
		this.IDD = new InterDIFDirectory();
		// register with DNS
		try{
			Socket toDNS = new Socket(Constants.DNS_IP, Constants.DNS_PORT);
			toDNS.getOutputStream().write(Message.newDNS_UPDATE_REQ(Constants.IDD_NAME));
			Message dnsReply = Message.readFromSocket(toDNS);
			if (dnsReply.errorCode != 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			System.out.println("Error registering with DNS");
			e.printStackTrace();
		}
		
		try {
			serverSocket = new ServerSocket(Constants.IDD_PORT);
		} catch (IOException e) {
			System.out.println("Could not bind server socket to port");
			e.printStackTrace();
		}
		
		this.start();
	}

	public class RequestHandleProcedure extends Thread {
		private InterDIFDirectory IDD;
		private Socket socket;
		private DataInputStream input;
		private DataOutputStream output;

		public RequestHandleProcedure(Socket socket, InterDIFDirectory idd) {
			this.socket = socket;
			this.IDD = idd;
			try {
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
			} catch (Exception e) {e.printStackTrace();}
		}

		public void run() {
			//while (true) {
				Message newMessage = Message.readFromSocket(socket);
				
				if (newMessage == null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					//break;
				}

				String difName;
				String nmsURL;
				String serviceURL;
				boolean success;
				switch (newMessage.type) {
				case Message.CDAP_IDD_REQ:
					int lookupType = newMessage.lookupType;					
					if (lookupType == 1) {
						serviceURL = newMessage.text1;

						Service lookupResult = IDD.lookupByServiceName(serviceURL);
						
						if (lookupResult == null) {
							try {
								output.write(Message.newCDAP_IDD_RSP(0, "", ""));
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							try {
								output.write(Message.newCDAP_IDD_RSP(1, lookupResult.difName, ""));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} else if (lookupType == 2) {
						serviceURL = newMessage.text1;

						Service lookupResult = IDD.lookupByServiceName(serviceURL);
						
						if (lookupResult == null) {
							try {
								output.write(Message.newCDAP_IDD_RSP(0, "", ""));
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							try {
								output.write(Message.newCDAP_IDD_RSP(1, lookupResult.difName, lookupResult.nmsURL));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} else if (lookupType == 3) {
						difName = newMessage.text1;
						DIFListing lookupResult = IDD.lookupByDIF(difName);
						
						if (lookupResult == null) {
							try {
								output.write(Message.newCDAP_IDD_RSP(0, "", ""));
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							try {
								output.write(Message.newCDAP_IDD_RSP(1, "", lookupResult.nmsURL));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
					break;

				case Message.CDAP_IDD_UPDATE_REQ:
					difName = newMessage.text1;
					nmsURL = newMessage.text2;

					System.out.println("Handling update with DIF Name " + difName + " and NMS URL " + nmsURL );

					success = IDD.updateListing(difName, nmsURL);

					int responseVal = 0;
					if (!success) responseVal = 1;

					try {
						output.write(Message.newCDAP_UPDATE_RSP(responseVal));
					} catch (IOException e) {
						e.printStackTrace();
					}

					break;

				case Message.CDAP_IDD_SERVADD_REQ:
					difName = newMessage.text1;
					serviceURL = newMessage.text2;

					System.out.println("IDD handling service addition with DIF Name " + difName + " and Service URL " + serviceURL );

					success = IDD.addService(difName, serviceURL);

					int response = 0;
					if (!success) response = 1;
					
					// if I have a proxy, tell it to register this new service with itself
					// in DNS
					if (success && knownProxy != null) {
						System.out.println("    IDD informing known proxy of new service: " + serviceURL);
						boolean registrationSuccess = false;
						try {
							Socket toProxy = new Socket(knownProxy, Constants.PROXY_PORT);
							
							toProxy.getOutputStream().write(Message.newCDAP_PROXY_SRV_REQ(serviceURL));
							Message reply = Message.readFromSocket(toProxy);
							if (reply.type == Message.CDAP_PROXY_SRV_RSP && reply.errorCode == 0) {
								registrationSuccess = true;
							} else {
								System.out.println("   IDD informed of unsuccessful proxy DNS registration of service: " + serviceURL);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					try {
						output.write(Message.newCDAP_UPDATE_RSP(response));
					} catch (IOException e) {
						e.printStackTrace();
					}

					break;
					
				case Message.CDAP_REGISTER_PROXY_REQ:
					// there's a proxy that wants to register itself with me
					System.out.println("IDD received a proxy registration request");
					
					if (knownProxy == null) {
						knownProxy = socket.getInetAddress();
						System.out.println("   IDD registering a new proxy, sending response");
						try {
							socket.getOutputStream().write(Message.newCDAP_REGISTER_PROXY_RSP(0));
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						// Inform new proxy of existing services
						System.out.println("   IDD informing Proxy of existing advertised services");
						
						for (Service curService : IDD.getAllServices()) {
							Socket toProxy = null;
							try {
								toProxy = new Socket(knownProxy, Constants.PROXY_PORT);
								System.out.println("    IDD informing Proxy of service: " + curService.serviceURL);
								toProxy.getOutputStream().write(Message.newCDAP_PROXY_SRV_REQ(curService.serviceURL));
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							Message prResponse = Message.readFromSocket(toProxy);
							if (prResponse.type == Message.CDAP_PROXY_SRV_RSP && prResponse.errorCode != 0) {
								System.out.println("    IDD ERROR on informing proxy of existing service: " + curService.serviceURL);
							}
							
							if (toProxy != null) {
								try {
									toProxy.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						
					} else {
						System.out.println("    IDD already has a registered proxy, rejecting request");
						try {
							socket.getOutputStream().write(Message.newCDAP_REGISTER_PROXY_RSP(1));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					break;
					
				default:

				}
			//}
		}
	}

	public void run() {
		while (true) {
			Socket newConnection = null;
			try {
				newConnection = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}

			RequestHandleProcedure proc = new RequestHandleProcedure(newConnection, IDD);
			proc.start();
		}
	}
}
