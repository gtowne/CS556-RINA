/**
 * An IPC process represents a host on a RINA network specified uniquely by a name. 
 * This interface can be used to create a new IPC process with a given name, which
 * then acts as a factory to create sockets that allow the application to communicate
 * with other members of the DIF.
 */

package lib.interfaces;
import java.io.IOException;
import java.util.Collection;

import lib.Member;

public interface IPC {
	
	public boolean joinDIF(String difName) throws Exception;
	
	/**
	 * Open a new socket to the IPC process in this DIF at the given name.
	 * @param Name of hose to connect to
	 * @return Initialized socket for the new connection
	 * @throws IOException
	 */
	public RINASocket openNewSocket(String destName) throws IOException;
	
	/**
	 * Open a server socket for this process. Only one ServerSocket can 
	 * be opened for a single IPC instance. Repeated calls should be ignored.
	 * @return Initialized ServerSocket
	 * @throws IOException
	 */
	public RINAServerSocket newServerSocket() throws IOException;

	/**
	 * @return The data used to populate this processes entry in the DIF's Resource Information Base
	 */
	public Member getRIBListing();
	
	/**
	 * Update this IPC process's local view of the DIF's RIB with the 
	 * @param New Members
	 */
	public void updateRIB(Collection<Member> members);
}
