import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Yi Huang
 * This class contain the main function to start this this CAN system
 * */
public class RunNetwork {
	
	public static void main(String[] args) {
		// Start the CAN peer
		CanNodePeer canPeer = new CanNodePeer();
		System.out.println("*****************Welcom to the CAN system!*****************");
		System.out.println("Note: 'exit' or 'quite' to end this program.");
		System.out.println("Accept the following command:");
		System.out.println("1#\tjoin");
		System.out.println("2#\tview");
		System.out.println("3#\tinsert keyword");
		System.out.println("4#\tsearch keyword");
		
		// Add something keyboard input for command in a while loop
		String line = null;
		BufferedReader bufr = new BufferedReader(new InputStreamReader(System.in));	
		
		try {
			while((line = bufr.readLine()) != null) {
				if("exit".equals(line) || "quit".equals(line)) {
					// Leave the CAN first
					canPeer.leave();
					
					// Close server
					canPeer.server.close();
					
					// Unbind the object from RMI and unregistry the port
					try {
						Naming.unbind("//" + canPeer.localAddress + ":" + SysParameter.CAN_PORT + "/NodeObject");
						UnicastRemoteObject.unexportObject(canPeer.canNode, false);
					} 
					catch (NotBoundException e) {
						e.printStackTrace();
					}
					System.out.println("System shut down.........");
					break;
				}
				
				String[] command = line.split(" ");
				try{
					if(command.length > 2) {
						System.out.println("Error: command not accepted!");
					}
					else if("insert".equals(command[0])) {
						canPeer.insert(command[1]);
					}
					else if ("search".equals(command[0])) {
						canPeer.retrieve(command[1]);
					}
					else if ("view".equals(command[0]) && command.length == 1) {
						canPeer.view();
					}
					else if ("join".equals(command[0]) && command.length  == 1) {
						canPeer.join();
					}
					else if ("leave".equals(command[0]) && command.length == 1) {
						canPeer.leave();
					}
					else {
						System.out.println("Error: command not accepted!");
					}		
				}
				catch(ArrayIndexOutOfBoundsException e) {
					System.out.println("Error: keyword missing!");
				}		
			}
			bufr.close();
		} 
		catch (IOException e) {
			System.out.println("IO Error: " + e.getMessage());
			//e.printStackTrace();
		}
	}	
}
