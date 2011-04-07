import java.io.*;
import java.net.*;
import java.util.*;
import java.util.LinkedList;

public class DNS{

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
	ServerSocket serv_sock = new ServerSocket(6789);
	while(true) {
	    // accept connection
	    Socket conn_sock = serv_sock.accept();
	    BufferedReader cli_reader =
	      new BufferedReader(new InputStreamReader(conn_sock.getInputStream()));
	    DataOutputStream cli_writer =
		new DataOutputStream(conn_sock.getOutputStream());
	    String request = cli_reader.readLine();
	    // handle request: generate response
	    String rsp = get_rsp(request, addr_table);
	    // send that daym message:
	    cli_writer.writeBytes(rsp);
        } // end of while
    }

    /* Takes: byte string "r" that was read from a client socket
     *        and the list of dns/ip mappings
     * Returns: byte String formatted with the entire DNS response
     */
    private static String get_rsp(String r, LinkedList<Addr_pair> at){
	String reply = null;
	Request req = parse_req(r);
	if(req != null){
	    if(req.type == DNS_REQ){
		String ip = get_ip(req.url, at);
		reply = DNS_RSP + ip.length() + ip;
	    }else if(req.type == DNS_UPDATE_REQ){
		if(set_ip(req.url, req.ip, at))
		    reply = DNS_UPDATE_RSP+""+ERRCODE_SIZE+""+0;
		else // update was unsucessful
		    reply = DNS_UPDATE_RSP+""+ERRCODE_SIZE+""+1;
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
	return null;
    }

    /* Takes: byte string "request" that was read from a client socket
     * Returns: Request object containing the parsed contents of "requst", with
     *          inpertinent fields left null or 0.
     *          null, if "request" is an invalid message
     */
    private static Request parse_req(String request){
	Request ret = null;
	String type_str = request.substring(0, TYPE_SIZE);
	String len_str = request.substring(TYPE_SIZE, LENGTH_SIZE);
	Integer type_int;
	try{
	    type_int = new Integer(type_str);
	}catch(NumberFormatException e){ // invalid Type field
	    System.out.println("invalid Type field in DNS request");
	    return null;
	};	
	Integer len_int;
	try{
	    len_int = new Integer(len_str);
	}catch(NumberFormatException e){ // invalid Length field
	    System.out.println("invalid Length field in DNS request");
	    return null;
	};
	int type = type_int.intValue();
	int len = len_int.intValue();
	int url_len;
	String url;
	String ip;
	int data_begin;
	int data_end;
	if(type == DNS_REQ){
	    data_begin = TYPE_SIZE+LENGTH_SIZE;
	    data_end = request.length();
	    url = request.substring(data_begin, data_end);
	    url_len = 0;
	    ip = null;
	    ret = new Request(type, len, 0, url, null);
	}else if(type == DNS_UPDATE_REQ){
	    String url_len_str = request.substring(TYPE_SIZE+LENGTH_SIZE, TYPE_SIZE+LENGTH_SIZE+URL_LEN_SIZE);
	    Integer url_len_int = new Integer(url_len_str);
	    url_len = url_len_int.intValue();
	    data_begin = TYPE_SIZE+LENGTH_SIZE+URL_LEN_SIZE;
	    data_end = url_len;
	    url = request.substring(data_begin, data_begin + url_len);
	    ip = request.substring(data_begin + url_len, data_end);
	    ret = new Request(type, len, url_len, url, ip);
	} // else, invalid request message type
	return ret;
    }
} // end of DNS

/* This class represents an incomming or outgoing message
 */
class Request{
    int type;
    int length;
    int url_len; // useless field...only included for moral reasons
    String url;
    String ip;
    Request(int type, int length, int url_len, String url, String ip){
	this.type = type;
	this.length = length;
	this.url_len = url_len;
	this.url = url;
	this.ip = ip;
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