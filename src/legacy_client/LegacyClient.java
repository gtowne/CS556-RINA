package legacy_client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import lib.Message;
import lib.internet_dif.Constants;


public class LegacyClient {
	
	public LegacyClient(String resource){
		System.out.println(retrieve(resource));
	}
	
	public static String retrieve(String serviceURL){
		return getResource(getHostByName(serviceURL), serviceURL);
	}
	
	
	public static String getHostByName(String serviceURL){
		Socket s;
		try {
			s = new Socket(Constants.DNS_IP, Constants.DNS_PORT);
			s.getOutputStream().write(Message.newDNS_REQ(serviceURL));
			String proxyIP = Message.readFromSocket(s).text1;
			s.close();
			System.out.println("received proxy address");
			return proxyIP;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getResource(String proxyIP, String serviceURL){
		Socket s;
		try {
			s = new Socket(proxyIP, Constants.PROXY_PORT);
			s.getOutputStream().write(Message.newHTTP_GET(serviceURL));
			Message m = Message.readFromSocket(s);
			s.close();
			System.out.println("Received from Server: " + m.text1);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
