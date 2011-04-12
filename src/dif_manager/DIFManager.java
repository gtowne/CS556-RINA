package dif_manager;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;

import lib.DIF;
import lib.Member;
import lib.Message;
import lib.ResourceInformationBase;
import lib.internet_dif.*;


public class DIFManager {
	//private String ipAddr;
	private String rinaName;
	private String DIFName;
	private ResourceInformationBase internalData;
	private Hashtable<String,String> userPasswordPairs;
	
	public DIFManager(String rina){
		//ipAddr = addr;
		rinaName = Constants.DIF_MANAGER_NAME;
		DIFName = rina;
		
		userPasswordPairs = new Hashtable<String, String>();
		internalData = new ResourceInformationBase();
		userPasswordPairs.put(Constants.username, Constants.password);
		
		InetIPC ipc = new InetIPC(rinaName);
		try {
			
			//get the IDD from DNS and register in DNS

			Socket dns = new Socket(Constants.DNS_IP,Constants.DNS_PORT);
			String IDDAdress = "";
			boolean success2 = false;
			while(!success2){
				dns.getOutputStream().write(Message.newDNS_UPDATE_REQ(rinaName));
			
				Message response = Message.readFromSocket(dns);
				
				System.out.println("Manager received dns rsp");
				
				if(response.errorCode==0){
					System.out.println("Manager response contained correct error code");
					success2 = true;
					IDDAdress = response.text1;
					System.out.println("Manager received IDD address: " + IDDAdress);
					if(IDDAdress == null || IDDAdress.equals("")){
						System.out.println("IDD address not received\nexiting");
						System.exit(0);
					}
				}
			}
			
			System.out.println("idd adddress from dns to mgr: " + IDDAdress);
			
			//First use a regular java socket to register with IDD
			Socket s = new Socket(IDDAdress,Constants.IDD_PORT);
			boolean success = false;
			while(!success){
				s.getOutputStream().write(Message.newCDAP_IDD_UPDATE_REQ(DIFName, rinaName));
			
				Message response = Message.readFromSocket(s);
				
				if(response.errorCode==0) success = true;
			}
			
			//now receive connections and send responses appropriately
			
			InetDIFServerSocket idss = ipc.newServerSocket(Constants.DIF_MANAGER_PORT);
			
			while (true){
				InetDIFSocket ids = idss.accept();
				
				Message req = Message.parseMessage(ids.read());
				
				if(req.type == Message.CDAP_CONNECT_REQ){
					int auth = authenticate(req.text1,req.text2);
					ids.write(Message.newCDAP_CONNECT_RSP(auth));
					if(auth == 0){
						internalData.addMember(new Member(ids.getDestName(), new DIF("internet"), ids.getDestAddr()));
						boolean received = false;
						while(!received){
							byte [] update = Message.newCDAP_UPDATE_RIB_REQ(internalData.getMemberList());
							ids.write(update);
							if(Message.parseMessage(ids.read()).errorCode==0) received = true;
						}

						for(Member b : internalData.getMemberList()){
							InetDIFSocket cli_sock = ipc.openNewSocket(b.getName());
							received = false;
							while(!received){
								byte [] update = Message.newCDAP_UPDATE_RIB_REQ(internalData.getMemberList());
								cli_sock.write(update);
								if(Message.parseMessage(cli_sock.read()).errorCode==0) received = true;
							}
						}
					}
				} 
				ids.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int authenticate(String user, String password){
		if(!userPasswordPairs.containsKey(user)) return 1;
		return userPasswordPairs.get(user).equals(password) ? 0 : 2;
	}
}