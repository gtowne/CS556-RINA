package lib;

public class Member {
	private String name;
	private DIF supportingDIF;
	private String pointOfAttachment;
	private int port;
	

	public Member(String name, DIF supportingDIF, String pointOfAttachment) {
		this.name = name;
		this.supportingDIF = supportingDIF;
		this.pointOfAttachment = pointOfAttachment;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public DIF getSupportingDIF() {
		return supportingDIF;
	}
	public void setSupportingDIF(DIF supportingDIF) {
		this.supportingDIF = supportingDIF;
	}
	public String getPointOfAttachment() {
		return pointOfAttachment;
	}
	public void setPointOfAttachment(String pointOfAttachment) {
		this.pointOfAttachment = pointOfAttachment;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
