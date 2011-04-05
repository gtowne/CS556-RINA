package idd;
import java.util.Hashtable;

public class DIFListing {
	public String name;
	public String nmsURL;
	public Hashtable<String, Service> services;
	
	public DIFListing(String difName, String nmsURL) {
		name = difName;
		this.nmsURL = nmsURL;
		services = new Hashtable<String, Service>();
	}
	
	public boolean removeService(String serviceName) {
		if (!hasService(serviceName)) {
			return false;
		}
		
		services.remove(serviceName);
		return true;
	}
	
	public boolean hasService(String service) {
		return services.containsKey(service);
	}
	
	public void addService(Service service) {
		if (services.containsKey(service.serviceURL)) {
			return;
		}
		
		services.put(service.serviceURL, service);
	}
	
	public void updateNMSURL(String newURL) {
		nmsURL = newURL;
		
		for (Service s : services.values()) {
			s.nmsURL = newURL;
		}
	}
}
