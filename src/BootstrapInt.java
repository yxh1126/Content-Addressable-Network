import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Yi Huang
 * Define the interface of Bootstrap
 * */
public interface BootstrapInt extends Remote{
	
	// Every node join the network will call this function to check if it is the first node to join
	// If it is the first node, then assign his IP to the bootstrap, and return true
	// Otherwise, do not assign the IP, just return false
	public abstract boolean ifFirstJoin(String address)
		throws RemoteException;
	
	// Return a string of IP address, as the entry point when join a node
	public abstract String getEntryIP()
		throws RemoteException;
	
	// If the entry point machine going to leave the network
	// Inform the bootstrap, and replace a new IP in the network
	public abstract void informLeave(String leaveIP, String peerIP)
		throws RemoteException;
	
	public abstract void bootstrapInfo(String nodeIP)
		throws RemoteException;

}
