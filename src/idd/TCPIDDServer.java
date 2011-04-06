package idd;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lib.Message;
import lib.ResourceInformationBase;

public class TCPIDDServer extends Thread {
	private static int PORT = 8888;

	private ServerSocket serverSocket;
	private InterDIFDirectory IDD;

	public TCPIDDServer(InterDIFDirectory IDD) {
		this.IDD = IDD;
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("Could not bind server socket to port");
			e.printStackTrace();
		}
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
			while (true) {
				Message newMessage = Message.readFromSocket(socket);
				
				if (newMessage == null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
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

					System.out.println("Handling service addition with DIF Name " + difName + " and Service URL " + serviceURL );

					success = IDD.addService(difName, serviceURL);

					int response = 0;
					if (!success) response = 1;

					try {
						output.write(Message.newCDAP_UPDATE_RSP(response));
					} catch (IOException e) {
						e.printStackTrace();
					}

					break;

				default:

				}
			}
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
