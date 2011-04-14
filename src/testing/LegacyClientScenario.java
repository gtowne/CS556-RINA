package testing;

import legacy_client.LegacyClient;
import rina_proxy.RINAProxy;
import rina_server.RINAServer;
import idd.InterDIFDirectory;
import idd.TCPIDDServer;
import dif_manager.DIFManager;
import dns.DNS;

public class LegacyClientScenario {
	public static void main(String [] Args){
		
		try {
			DNSStarter dns = new DNSStarter();
			dns.start();
			
			System.out.println("DNS started");
			
			Thread.sleep(2000);

			IDDStarter idd = new IDDStarter();
			idd.start();
			
			System.out.println("IDD started");
			
			Thread.sleep(2000);

			MGRStarter mgr = new MGRStarter();
			mgr.start();
			
			System.out.println("DIFManager started");
			
			Thread.sleep(2000);

			SVRStarter svr = new SVRStarter();
			svr.start();
			
			System.out.println("Server started");
			
			Thread.sleep(2000);

			ProxyStarter proxy = new ProxyStarter();
			proxy.start();

			System.out.println("Proxy started");
			
			Thread.sleep(2000);
			
			System.out.println("Client started");
			
			ClientStarter cs = new ClientStarter();
			cs.run();
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


class DNSStarter extends Thread{
	public DNSStarter(){}
	public void run(){
		DNS dns = new DNS();
	}
}

class IDDStarter extends Thread{
	IDDStarter(){}
	public void run(){
		TCPIDDServer tis = new TCPIDDServer();
	}
}

class MGRStarter extends Thread{
	MGRStarter(){}
	public void run(){
		DIFManager mgr = new DIFManager("legacy-test-dif");
	}
}

class SVRStarter extends Thread{
	SVRStarter(){}
	public void run(){
		RINAServer svr = new RINAServer("latin-string.legacy-test-dif", "legacy-test-dif");
	}
}

class ProxyStarter extends Thread{
	ProxyStarter(){}
	public void run(){
		RINAProxy proxy = new RINAProxy();
	}
}

class ClientStarter extends Thread {
	ClientStarter(){}
	public void run(){
		LegacyClient lc = new LegacyClient("latin-string.legacy-test-dif");
	}
}