package lib;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

public class ResourceInformationBase {
	Hashtable<String,Member> members;
	
	public ResourceInformationBase() {
		members = new Hashtable<String,Member>();
	}
	
	public void addMembers(Collection<Member> newMembers) {
		for (Member newMember : newMembers) {
			members.put(newMember.getName(), newMember);
		}
	}
	
	public void addMember(Member newMember) {
		members.put(newMember.getName(), newMember);
	}
	
	public LinkedList<Member> getMemberList() {
		return new LinkedList<Member>(members.values());
	}
	
	public int numMembers() {
		return members.size();
	}
	
	public boolean containsMember(String name) {
		return members.containsKey(name);
	}
	
	public Member getMemberByName(String name) {
		return members.get(name);
	}
	
	
}
