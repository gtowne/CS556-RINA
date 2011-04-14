package dns;

import lib.Message;
import lib.internet_dif.Constants;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.LinkedList;

public class DNS{


	public DNS(){
		try{
			// create
			LinkedList<Addr_pair> addr_table = new LinkedList<Addr_pair>();
			// start server socket
			ServerSocket serv_sock = new ServerSocket(Constants.DNS_PORT);
			while(true) {
				Socket conn_sock = serv_sock.accept();
				// accept request:
				InputStream in_stream = conn_sock.getInputStream();
				byte[] rcv_buf = new byte[Constants.MAXLINE];
				int ret = in_stream.read(rcv_buf, 0, Constants.MAXLINE);
				if(ret > 0){ // connetion not lost
					Message req = Message.parseMessage(rcv_buf);
					byte [] rsp = get_rsp(req, conn_sock.getInetAddress().getCanonicalHostName(), addr_table);
					//if(rsp == null) continue;
					conn_sock.getOutputStream().write(rsp);
				}
			} // end of while
		} catch(Exception e){

		}
	}

	/* Takes: byte string "r" that was read from a client socket
	 *        and the list of dns/ip mappings
	 * Returns: byte String formatted with the entire DNS response
	 */
	private byte []  get_rsp(Message req, String ip, LinkedList<Addr_pair> at){
		String reply = null;
		if(req.type == Message.DNS_REQ || req.type == Message.DNS_UPDATE_REQ){
			if(req.type == Message.DNS_REQ){
				System.out.println("DNS received REQ for " + req.text1);
				//reply = get_ip(req.text1, at);
				return Message.newDNS_RSP(get_ip(req.text1, at));
			}else if(req.type == Message.DNS_UPDATE_REQ){
				System.out.println("DNS received an update request for listing " + req.text1);
				int response = 0;
				if(set_ip(req.text1, ip, at))
					return Message.newDNS_UPDATE_RSP(response);
				String IDD = null;
				for(Addr_pair a : at){
					if(a.url.equals(Constants.IDD_NAME)){
						IDD = a.ip;
						break;
					}
				}
				if(IDD != null)
					return Message.newDNS_UPDATE_RSP(response, IDD);
				//else return null;
			} // else, invalid request
		} // else, invalid request
		return null;
	}

	/* adds mapping from "url" to "ip" in "at"
	 * returns true if url was found, false otherwise
	 */
	private static boolean set_ip(String url, String ip, LinkedList<Addr_pair> at){
		System.out.println("DNS adding dns record: " + url + ", " + ip);
		Addr_pair ap;
		ListIterator<Addr_pair> iter = at.listIterator();
		while(iter.hasNext()){
			ap = (Addr_pair)iter.next();
			if(ap.url == url){
				ap.ip = ip;
				return true;
			}
		}
		at.add(new Addr_pair(url,ip));
		return false;
	}

	/* returns the ip corresponding to the url argument in addr_table
	 */
	private static String get_ip(String url, LinkedList<Addr_pair> at){
		for (Addr_pair cur : at) {
			if (cur.url.equals(url)) {
				return cur.ip;
			}
		}
		return null;
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