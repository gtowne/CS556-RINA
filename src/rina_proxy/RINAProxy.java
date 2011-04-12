package rina_proxy;

import lib.Message;
import lib.internet_dif.Constants;
import java.io.*;
import java.net.*;
import java.util.*;


public class RINAProxy{

	public RINAProxy (){
		ServerSocket serv_sock;
		try {
			serv_sock = new ServerSocket(Constants.PROXY_PORT);

			Socket dns_sock = new Socket(Constants.DNS_IP,Constants.DNS_PORT);
			dns_sock.getOutputStream().write(Message.newDNS_UPDATE_REQ(Constants.PROXY_NAME));
			Message rsp = Message.readFromSocket(dns_sock);
			dns_sock.close();
			

			while(true) {
				Socket conn_sock = serv_sock.accept();
				Message req = Message.readFromSocket(conn_sock);
				

				Socket idd_sock = new Socket(rsp.text1,Constants.IDD_PORT);
				idd_sock.getOutputStream().write(Message.newCDAP_IDD_REQ(3, req.text1));
				Message rsp_idd = Message.readFromSocket(idd_sock);
				idd_sock.close();
				
				if(rsp.errorCode!=0)
					conn_sock.getOutputStream().write(Message.newDNS_RSP(rsp_idd.text2));
				else System.out.println("error received from IDD, sent: " + rsp.text1);
			} // end of while
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}