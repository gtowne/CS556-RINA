package dif_manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

import lib.ResourceInformationBase;
import lib.internet_dif.*;


public class DIFManager {
	private String ipAddr;
	private String rinaName;
	private ResourceInformationBase internalData;
	private Hashtable<String,String> userPasswordPairs;
	
	public DIFManager(){
		InetIPC ipc = new InetIPC(rinaName);
		try {
			InetDIFServerSocket idss = ipc.newServerSocket();
			while (true){
				InetDIFSocket ids = idss.accept();
				byte[] in = ids.read();
				
				DataInputStream ds = new DataInputStream(new ByteArrayInputStream(in));

				if(ds.readInt()==7){
					int length = ds.readInt();
					String user = ds.readUTF();
					String pass = ds.readUTF();
					int rsp = authenticate(user,pass);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(out);
					dos.writeInt(8);
					dos.writeInt(4);
					dos.writeInt(rsp);
					dos.close();
					ids.write(out.toByteArray());

					out = new ByteArrayOutputStream();
					dos = new DataOutputStream(out);
					
					byte [] rib = RIBBytes();
					
					dos.writeInt(9);
					dos.writeInt(rib.length);
					
					dos.write(rib);
					dos.close();
					
					ids.write(out.toByteArray());
					
					ids.close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private byte [] RIBBytes() throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream dos = new ObjectOutputStream(out);
		
		dos.writeObject(internalData.members);
		
		return out.toByteArray();
	}
	
	public int authenticate(String user, String password){
		if(!userPasswordPairs.containsKey(user)) return 1;
		return userPasswordPairs.get(user).equals(password) ? 0 : 2;
	}
}