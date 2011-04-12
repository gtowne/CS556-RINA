package rina_proxy;

import lib.InetIPC;
import lib.Message;
import lib.internet_dif.Constants;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.LinkedList;


public class RINAProxy{

    public static void main (String args[]){
	ServerSocket serv_sock = new ServerSocket(PROXY_PORT);
	while(true) {
	    Socket conn_sock = serv_sock.accept();
	    // accept request:
	    InputStream in_stream = conn_sock.getInputStream();
	    byte[] rcv_buf = new byte[MAXLINE];
	    int ret = in_stream.read(rcv_buf, 0, MAXLINE);
	    if(ret > 0){ // connetion not lost
		Message req = Message.parseMessage(rcv_buf);
	    }
	    String rsp = get_rsp(req, addr_table);
	    // send response:
	    byte[] send_buf = new byte[MAXLINE];
	    send_buf = newDNS_RSP(rsp);
	    DataOutputStream cli_writer =
		new DataOutputStream(conn_sock.getOutputStream());
	    cli_writer.write(send_buf, 0, MAXLINE);
        } // end of while
    }

    /* returns the string containing the HTTP response from the RINA server
     * returns null if a failure occured
     */
    private String get_rsp(Message req){
	String rsp;
	if(req.type == HTTP_GET){
	    /* - retrieve Service_DNS
	     * - get IDD IP
	     * - make CDAP_IDD_REQ
	     * - connect to DIF using InetIPC and DIF_name
	     * - forward request
	     * - 
	     */
	    // establish connection with DNS server:
	    InetAddress dns_IA = InetAddress.getByAddress(DNS_IP.getBytes());
	    Socket dns_sock = new Socket(dns_IA, DNS_PORT);
	    // send dns request for IDD IP:
	    byte[] dns_req;
	    dns_req = newDNS_REQ(IDD_NAME);
	    DataOutputStream to_dns_stream =
		new DataOutputSream(dns_sock.getOutputStream());
	    to_dns_stream.write(dns_req, 0, dns_req.length);
	    // receive dns response:
	    byte[] raw_dsn_rsp = new byte[Constants.MAXLINE];
	    InputStream from_dns_stream = dns_sock.getInputStream();
	    int ret = from_dns_steam.read(raw_dns_rsp, 0, Constants.MAXLINE);
	    if(ret > 0){
		System.out.println("Error: failed to recieve DNS_RSP");
		return null;
	    }
	    dns_sock.close();
	    Message dns_rsp = Message.parseMessage(raw_dns_rsp);
	    String idd_ip = dns_rsp.text1;
	    // establish connection with IDD:
	    Socket idd_IA = InetAddress.getByAddress(idd_ip.getBytes());
	    Socket idd_sock = new Socket(idd_IA, Constants.IDD_PORT);
	    // send CDAP_IDD_REQ:
	    byte[] idd_req = newCDAP_IDD_REQ(1, req.text1.getBytes());
	    DataOutputStream to_idd_stream = 
		new DataOutputStream(idd_sock.getOutputStream());
	    to_idd_stream.write(idd_req, 0, idd_req.length);
	    // recieve idd response:
	    byte[] raw_idd_rsp = new byte[Constants.MAXLINE];
	    InputStream from_idd_stream = idd_sock.getInputStream();
	    ret = from_idd_stream.read(raw_idd_rsp, 0, Constants.MAXLINE);
	    if(ret > 0){
		System.out.println("Error: failed to recieve CDAP_IDD_RSP");
		return null;
	    }
	    idd_sock.close();
	    Message idd_rsp = Message.parseMessage(raw_idd_rsp);
	    String difman_addr = idd_rsp.text1; // ***** VERIFY THIS

	    // CONTINUE HERE ^^

	}else if(req.type == HTTP_RSP){
	    // handle UPDATE_REQ
	}else{
	    // error!
	}
	return reply;
    }

}