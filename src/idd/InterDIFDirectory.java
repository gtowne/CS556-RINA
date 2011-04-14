package idd;

import java.util.Collection;
import java.util.Hashtable;

import lib.Message;
import lib.ResourceInformationBase;
import lib.internet_dif.InetIPC;

public class InterDIFDirectory {
	private Hashtable<String, Service> services;
	private Hashtable<String, DIFListing> difs;
	
	protected InterDIFDirectory() {
		services = new Hashtable<String, Service>();
		difs = new Hashtable<String, DIFListing>();		
	}
	
	public synchronized Service lookupByServiceName(String serviceName) {
		if (services.containsKey(serviceName)) {
			return services.get(serviceName);
		}
		
		return null;
	}
	
	public synchronized DIFListing lookupByDIF(String difName) {
		if (difs.containsKey(difName)) {
			return difs.get(difName);
		}
		
		return null;
	}
	
	public synchronized boolean updateListing(String difName, String nmsURL) {
		if (difs.containsKey(difName)) {
			DIFListing thisListing = difs.get(difName);
			thisListing.updateNMSURL(nmsURL);
			return true;
		}
		
		DIFListing newDIF = new DIFListing(difName, nmsURL);
		difs.put(difName, newDIF);
		
		return true;
	}
	
	public synchronized boolean addService(String difName, String serviceName) {
		if (!difs.containsKey(difName)) {
			return false;
		}
		
		DIFListing dif = difs.get(difName);
		
		// if that service name is already offered by this dif
		if (dif.hasService(serviceName)) {
			return true;
		}
		
		// if that service name is currently offered by a different DIF
		if (services.containsKey(serviceName)) {
			return false;
		}
		
		// else this is a new service, add it to the DIF
		Service newService = new Service(serviceName, difName, dif.nmsURL);
		dif.addService(newService);
		services.put(serviceName, newService);
		
		return true;
	}
	
	public synchronized Collection<Service> getAllServices() {
		return services.values();
	}
}
