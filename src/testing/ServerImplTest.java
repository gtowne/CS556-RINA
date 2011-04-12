package testing;

import rina_proxy.RINAProxy;
import rina_server.RINAServer;
import idd.InterDIFDirectory;
import idd.TCPIDDServer;
import dif_manager.DIFManager;
import dns.DNS;

public class ServerImplTest {
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
			
			System.out.println("MGR started");
			
			Thread.sleep(2000);

			SVRStarter svr = new SVRStarter();
			svr.start();
			
			System.out.println("server started");
			
			Thread.sleep(2000);

			ProxyStarter proxy = new ProxyStarter();
			proxy.start();

			System.out.println("proxy started");
			
			//Thread.sleep(1000);
			
			
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
		DIFManager mgr = new DIFManager("test");
	}
}

class SVRStarter extends Thread{
	SVRStarter(){}
	public void run(){
		RINAServer svr = new RINAServer("testService", "test");
	}
}

class ProxyStarter extends Thread{
	ProxyStarter(){}
	public void run(){
		RINAProxy proxy = new RINAProxy();
	}
}