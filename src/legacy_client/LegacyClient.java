package legacy_client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import lib.Message;
import lib.internet_dif.Constants;

public class LegacyClient {
	
	public LegacyClient(String resource){
		System.out.println("LegacyClient received data from server: " + retrieve(resource));
	}
	
	public static String retrieve(String serviceURL){
		return getResource(getHostByName(serviceURL), serviceURL);
	}
	
	
	public static String getHostByName(String serviceURL){
		Socket s;
		System.out.println("LegacyClient attempting to resolve " + serviceURL);
		try {
			s = new Socket(Constants.DNS_IP, Constants.DNS_PORT);
			s.getOutputStream().write(Message.newDNS_REQ(serviceURL));
			String proxyIP = Message.readFromSocket(s).text1;
			s.close();
			System.out.println("LegacyClient received server address from DNS: " + proxyIP);
			return proxyIP;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("LegacyClient ERROR resolving URL " + serviceURL);
		
		return null;
	}
	
	public static String getResource(String proxyIP, String serviceURL){
		Socket s;
		try {
			s = new Socket(proxyIP, Constants.PROXY_PORT);
			System.out.println("LegacyClient issuing HTTP_GET request for " + serviceURL);
			s.getOutputStream().write(Message.newHTTP_GET(serviceURL));
			Message m = Message.readFromSocket(s);
			s.close();
			return m.text1;
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
