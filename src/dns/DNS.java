import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class DNS{

    public static final int DNS_PORT = 53; // according to Wikipedia.org
    
    public static void main (String args[]){
	// create
	LinkedList<Addr_pair> addr_table;
	// start server socket
	ServerSocket serv_sock = new ServerSocket(6789);
	while(true) {
	    // accept connection
	    Socket conn_sock = welcomeSocket.accept();
	    BufferedReader cli_reader =
	      new BufferedReader(new InputStreamReader(conn_sock.getInputStream()));
	    DataOutputStream cli_writer =
		new DataOutputStream(conn_sock.getOutputStream());
	    String req = client_reader.readLine();
	    // handle request: generate response
	    String rsp = get_rsp(req, addr_table);
	    client_writer.writeBytes(rsp);
        }
    }

    private get_rsp(String req, LinkedList<Addr_pair> at) {
	// parse req...do stuff
    }

    private class Addr_pair{
	private String url;
	private String ip;
	Addr_pair(String url, String ip){
	    this.url = url;
	    this.ip = ip;
	}
	
        private String get_ip(){
	    return this.ip;
	}

	private void set_ip(String ip){
	    this.ip = ip;
	}
    }

}