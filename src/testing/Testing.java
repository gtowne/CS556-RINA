package testing;

import java.io.IOException;
import java.util.LinkedList;

import lib.*;
import lib.interfaces.RINASocket;
import lib.internet_dif.InetDIFServerSocket;
import lib.internet_dif.InetDIFSocket;
import lib.internet_dif.InetIPC;

public class Testing {

	public static void main(String[] args) {
		String data = "Cras sed ante. Phasellus in massa. Curabitur dolor eros, gravida et, hendrerit ac, cursus non, massa. Aliquam lorem. In hac habitasse platea dictumst. Cras eu mauris. Quisque lacus. Donec ipsum. Nullam vitae sem at nunc pharetra ultricies. Vivamus elit eros, ullamcorper a, adipiscing sit amet, porttitor ut, nibh. Maecenas adipiscing mollis massa. Nunc ut dui eget nulla venenatis aliquet. Sed luctus posuere justo. Cras vehicula varius turpis. Vivamus eros metus, tristique sit amet, molestie dignissim, malesuada et, urna.";
		
		InetIPC p1 = new InetIPC("Process1");		
		InetIPC p2 = new InetIPC("Process2");
		
		InetDIFServerSocket p2SS = null;
		try {
			p2SS = p2.newServerSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		LinkedList<Member> ribUpdate = new LinkedList<Member>();
		ribUpdate.add(p1.getRIBListing());
		ribUpdate.add(p2.getRIBListing());
		
		p1.updateRIB(ribUpdate);
		p2.updateRIB(ribUpdate);
		
		
		InetDIFSocket p1Sock = null;
		try {
			p1Sock = p1.openNewSocket("Process2");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		RINASocket p2Sock = null;
		try {
			p2Sock = p2SS.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			p1Sock.write(data.getBytes());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			System.out.println(new String(p2Sock.read()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}