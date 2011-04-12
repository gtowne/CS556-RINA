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
	    // retrieve Service_DNS
	    
	}else if(req.type == HTTP_RSP){
	    // handle UPDATE_REQ
	}else{
	    // error!
	}
	return reply;
    }

    private Message parse_req(String request){
	Mssage req;
	// parse request; populate req
	return req;
    }

    private class Message{
	int type;
	int len;
	int errcode;
	String url;
	String ip;
	Request(int type, int len, int errcode, String url, String ip){
	    this.type = type;
	    this.len = len;
	    this.errcode = errcode;
	    this.url = url;
	    this.ip = ip;
	}
    }

}