package lib.interfaces;
import java.io.IOException;
import java.util.Collection;

import lib.Member;

public interface IPC {
	public boolean joinDIF(String difName) throws Exception;
	public RINASocket openNewSocket(String destName) throws IOException;
	public RINAServerSocket newServerSocket() throws IOException;
	public int generateConnID();
	public Member getRIBListing();
	public void updateRIB(Collection<Member> members);
}
