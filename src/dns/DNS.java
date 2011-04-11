package dns;

import lib.Message;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.LinkedList;

public class DNS{

    public static final int MAXLINE = 4096; // max number bytes per read() on socket

    public static final int DNS_PORT = 53; // according to Wikipedia.org
    public static final int DNS_REQ = 0; 
    public static final int DNS_RSP = 1; 
    public static final int DNS_UPDATE_REQ = 2; 
    public static final int DNS_UPDATE_RSP = 3; 
    public static final int TYPE_SIZE = 4; // standard size of Type field in a message header
    public static final int LENGTH_SIZE = 4; // standard size of Length field in a message header
    public static final int URL_LEN_SIZE = 4;
    public static final int ERRCODE_SIZE = 4;
    
    public static void main (String args[])throws IOException{
	// create
	LinkedList<Addr_pair> addr_table = new LinkedList<Addr_pair>();
	// start server socket
	ServerSocket serv_sock = new ServerSocket(DNS_PORT);
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

    /* Takes: byte string "r" that was read from a client socket
     *        and the list of dns/ip mappings
     * Returns: byte String formatted with the entire DNS response
     */
    private static String get_rsp(Message req, LinkedList<Addr_pair> at){
	String reply = null;
	if(req != DNS_REQ && req != DNS_UPDATE_REQ){
	    if(req.type == DNS_REQ){
		reply = get_ip(req.url, at);
	    }else if(req.type == DNS_UPDATE_REQ){
		if(set_ip(req.url, req.ip, at))
		    reply = "0";
		else // update was unsucessful
		    reply = "1";
	    } // else, invalid request
	} // else, invalid request
	return reply;
    }

    /* adds mapping from "url" to "ip" in "at"
     * returns true if url was found, false otherwise
     */
    private static Boolean set_ip(String url, String ip, LinkedList<Addr_pair> at){
	Addr_pair ap;
	ListIterator iter = at.listIterator();
	while(iter.hasNext()){
	    ap = (Addr_pair)iter.next();
	    if(ap.url == url){
		ap.ip = ip;
		return true;
	    }
	}
	return false;
    }

    /* returns the ip corresponding to the url argument in addr_table
     */
    private static String get_ip(String url, LinkedList<Addr_pair> at){
	Addr_pair ap;
	ListIterator iter = at.listIterator();
	while(iter.hasNext()){
	    ap = (Addr_pair)iter.next();
	    if(ap.url == url)
		return ap.ip;
	}
	return "";
    }
}

class Addr_pair{
    String url;
    String ip;
    Addr_pair(String url, String ip){
	this.url = url;
	this.ip = ip;
    }
}

// end of file