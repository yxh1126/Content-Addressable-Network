import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Yi Huang
 * Implement the interface, the function will called remotely
 * */
public class BootStrap extends UnicastRemoteObject implements BootstrapInt {

	private static final long serialVersionUID = -5923114044293456959L;
	
	private String address;
	private int connectID;

	protected BootStrap() throws RemoteException {
		this.address = null;
		this.connectID = 0;
	}

	@Override
	public boolean ifFirstJoin(String address) throws RemoteException {
		
		this.connectID++;
		
		if(this.address == null){
			this.address = address;
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public String getEntryIP() throws RemoteException {
		return this.address;
	}

	@Override
	public void informLeave(String leaveIP, String peerIP) throws RemoteException {
		if(this.address.equals(leaveIP)) {
			this.address = peerIP;			
		}
	}

	@Override
	public void bootstrapInfo(String nodeIP) throws RemoteException {
		// The bootstrap console will showing the information
		System.out.println(this.connectID + "#\tPeer " + nodeIP + " is calling the bootstrap!");
	}

}
