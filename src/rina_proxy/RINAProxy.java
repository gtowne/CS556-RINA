package rina_proxy;

import lib.Message;
import lib.internet_dif.Constants;
import lib.internet_dif.InetDIFSocket;
import lib.internet_dif.InetIPC;

import java.io.*;
import java.net.*;
import java.util.*;


public class RINAProxy{

	public RINAProxy (){
		ServerSocket serv_sock;
		Random rand = new Random();
		try {
			System.out.println("Proxy attempting startup");
			serv_sock = new ServerSocket(Constants.PROXY_PORT);

			Socket dns_sock = new Socket(Constants.DNS_IP,Constants.DNS_PORT);
			dns_sock.getOutputStream().write(Message.newDNS_UPDATE_REQ(Constants.PROXY_NAME));
			Message rsp = Message.readFromSocket(dns_sock);
			dns_sock.close();

			// resolve IDD through DNS
			System.out.println("Proxy attempting to locate IDD through DNS");
			Socket toDNS = new Socket(Constants.DNS_IP, Constants.DNS_PORT);
			toDNS.getOutputStream().write(Message.newDNS_REQ(Constants.IDD_NAME));
			Message reply = Message.readFromSocket(toDNS);
			String iddAddr = reply.text1;
			toDNS.close();

			// attempt to register myself with the IDD
			System.out.println("Proxy attempting registration with IDD");
			Socket toIDD = new Socket(iddAddr, Constants.IDD_PORT);
			toIDD.getOutputStream().write(Message.newCDAP_REGISTER_PROXY_REQ());
			reply = Message.readFromSocket(toIDD);

			if (reply.type == Message.CDAP_REGISTER_PROXY_RSP && reply.errorCode == 0) {
				System.out.println("Proxy successfully registered with IDD");
			} else {
				System.out.println("Proxy ERROR: could not register with IDD");
				throw new IOException();
			}

			while(true) {
				Socket conn_sock = serv_sock.accept();
				Message req = Message.readFromSocket(conn_sock);

				switch (req.type) {
				case Message.HTTP_GET: // received HTTP get from a legacy client
					String serviceURL = req.text1;
					System.out.println("Proxy received HTTP GET for service: " + req.text1);
					
					System.out.println("Proxy contacting IDD to find host DIF for service " + req.text1);
					Socket idd_sock = new Socket(rsp.text1,Constants.IDD_PORT);
					idd_sock.getOutputStream().write(Message.newCDAP_IDD_REQ(1, req.text1));
					Message rsp_idd = Message.readFromSocket(idd_sock);
					idd_sock.close();

					if (rsp_idd.type == Message.CDAP_IDD_RSP && rsp_idd.errorCode == 1) {
						String hostDIFName = rsp_idd.text1;
						System.out.println("Proxy resolved service " + req.text1 + "to DIF " + hostDIFName + ", joining DIF...");
						
						InetIPC newIPC = new InetIPC("Proxy" + rand.nextInt());
						newIPC.joinDIF(hostDIFName);
						
						System.out.println("Proxy joined DIF, attempting to connect Server");
						
						InetDIFSocket toServer = newIPC.openNewSocket(serviceURL);
						
						System.out.println("Proxy forwarding HTTP-GET to Server");
						toServer.write(Message.newHTTP_GET(serviceURL));
						
						System.out.println("Proxy forwarding HTTP-RSP to Client");
						Message serverResponse = Message.parseMessage(toServer.read());
						
						conn_sock.getOutputStream().write(Message.newHTTP_RSP(serverResponse.text1));
					} else {
						System.out.println("Proxy could not resolve requested service to a DIF");
						break;
					}
					

					break;
					
				case Message.CDAP_PROXY_SRV_REQ: // received a request to act as proxy for a new service
					// open socket to DNS and attempt to register myself
					serviceURL = req.text1;
					System.out.println("Proxy received a request to register itself as proxy for service: " + serviceURL);
					
					toDNS = new Socket(Constants.DNS_IP, Constants.DNS_PORT);
					toDNS.getOutputStream().write(Message.newDNS_UPDATE_REQ(serviceURL));
					reply = Message.readFromSocket(toDNS);
					
					int responseToIDD = 1;
					if (reply.type == Message.DNS_UPDATE_RSP && reply.errorCode == 0 ) {
						System.out.println("    Proxy registration of " + serviceURL + " successful");
						responseToIDD = 0;
					} else {
						System.out.println("    Proxy ERROR registration of " + serviceURL + " NOT successful");
					}
					
					toDNS.close();
					
					conn_sock.getOutputStream().write(Message.newCDAP_PROXY_SRV_RSP(responseToIDD));
					break;
					
				}
				
				conn_sock.close();


			} // end of while
		} catch (Exception e) {
			System.out.println("Proxy failure");
			e.printStackTrace();
		}
	}

}