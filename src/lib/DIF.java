package lib;

import java.io.Serializable;

public class DIF implements Serializable {
	private String name;
	
	public DIF(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
