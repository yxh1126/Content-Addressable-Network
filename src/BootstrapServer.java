import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * @author Yi Huang
 * Assign a port number to the server, and bind the server to the IP of the current machine
 * The server is supposed to running on the glados.cs.rit.edu
 * */
public class BootstrapServer {

	public static void main(String[] args) {
		
		int bootPort = SysParameter.BOOT_PORT;
		
		try {
			LocateRegistry.createRegistry(bootPort);
			
			BootStrap obj = new BootStrap();
			Naming.rebind("//localhost:" + bootPort + "/BootstrapServer", obj);
			
			System.out.println("The bootstrap server is running on port " + bootPort);
			System.out.println("The server IP is " + InetAddress.getLocalHost());
		}
		catch (RemoteException e) {
			System.out.print("RemoteException: " + e.getMessage());
			e.printStackTrace();
		} 
		catch (Exception e) {
			System.out.println("Naming rebind error: " + e.getMessage());
			e.printStackTrace();
		}

	}

}
