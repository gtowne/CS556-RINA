
public class RINAProxy{

    private static final int HTTP_REQ = 1;
    private static final int HTTP_RSP = 2;
    private static final int DNS_REQ = 1;
    private static final int DNS_REQ = 2;
    private static final int CDAP_IDD_REQ = 2;

    public static void main (String args[]){
	// create server socket and wait for connection
	// should only recive:
	// HTTP_GET or HTTP_RSP

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
        } // end of while
    }

    private Message get_rsp(String request, LinkedList<Addr_pair> at){
	Message req;
	Message reply;
	req = parse_req(request);
	if(req.type == REQ){
	    // handle DNS_REQ
	}else if(req.type == UPDATE_REQ){
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