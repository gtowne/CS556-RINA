/*
 * Launches client and server test-beds in separate threads
 */

package testing;

import idd.InterDIFDirectory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import dif_manager.DIFManager;

import lib.*;
import lib.interfaces.RINASocket;
import lib.internet_dif.InetDIFServerSocket;
import lib.internet_dif.InetDIFSocket;
import lib.internet_dif.InetIPC;

public class Testing {
	
	/*public static void main(String[] args) {
		ResourceInformationBase r = new ResourceInformationBase();
		Client c = new Client(new Semaphore(0), r);
		Server s = new Server(new Semaphore(0), r);
		s.start();
		c.start();
	}
	*/
	
	
	public static void main(String[] args) {
		DIFManager nms = new DIFManager();
		
	}

}

