import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.Set;

public class CanNodePeer {
	
	public boolean joinStatus;
	public String localAddress;
	public CanNode canNode;
	public ServerSocket server;
	
	/**
	 * @author Yi Huang
	 * Constructor of a new peer, get the peer local IP
	 * */
	public CanNodePeer(){
		
		/**
		 * Get the IP address of the running machine
		 * */
		try {
			InetAddress localID = InetAddress.getLocalHost();
			this.localAddress = localID.getHostAddress();
		} 
		catch (Exception e) {
			System.out.println("Get IP Error: " + e.getMessage());
			//e.printStackTrace();
		}
		
		/**
		 * Create a registry to a port and bind the CanNode object to RMI
		 * */
		try {
			LocateRegistry.createRegistry(SysParameter.CAN_PORT);
			this.canNode = new CanNode(this.localAddress);
			Naming.rebind("//" + this.localAddress + ":" + SysParameter.CAN_PORT + "/NodeObject", canNode);
		} 
		catch (Exception e) {
			System.out.println("Rebind Error: " + e.getMessage());
			//e.printStackTrace();
		}
		
		/**
		 * Create a server socket to receive any inform information
		 * */
		try {
			this.server = new ServerSocket(SysParameter.TCP_PORT);
		} 
		catch (IOException e) {
			System.out.println("TCP Error: " + e.getMessage());
		}
		
		/**
		 * Set the join status to false, once join the CAN, set it as true
		 * */
		this.joinStatus = false;
		
	}
	
	/**
	 * First peer need to join the CAN, call the ifFirstJoin(String) function first
	 * Return true means no active peer in the CAN, and the peer set its IP to bootstrap as the entry point
	 * Return false means there is already some peer server as the entry point, the peer's IP will not record to the bootstrap
	 * */
	public void join() {
		
		// Test joinStatus first, if the current machine is already in the CAN, then print out a warring information
		// Otherwise do the join process
		if(!this.joinStatus) {
			// Set a handle of the bootstrap sever, and use RMI to call the function to get the entry IP	
			try {
				BootstrapInt bootServer = (BootstrapInt)Naming.
						lookup("//" + SysParameter.BOOT_IP + ":" + SysParameter.BOOT_PORT + "/BootstrapServer");	
				// Inform the bootstrap there is a connection request
				bootServer.bootstrapInfo(this.localAddress);
				
				// Return true means this is the first peer join the can
				// The bootstrap will record this IP as the entry point IP
				if(bootServer.ifFirstJoin(this.localAddress)) {
					
					this.canNode.routingTable = new RoutingTable();
					this.canNode.localSpace = new InetArea();
					this.canNode.localSpace.setInetArea(InetPoint.SIDE_START, InetPoint.SIDE_LEN, InetPoint.SIDE_START, InetPoint.SIDE_LEN);
					this.joinStatus = true;
					System.out.println("The first peer join CAN successfully!");
					this.view();
				}
				else{		
					// Connect the bootstrap and get the entry peer IP and connect with the entry IP
					String entryIP = bootServer.getEntryIP();
					CanNodeInt entryNode = (CanNodeInt)Naming.
							lookup("//" + entryIP + ":" + SysParameter.CAN_PORT + "/NodeObject");
					
					// Randomly choose a point and then start to find the location
					InetPoint joinLoc = new InetPoint(this.localAddress);
					System.out.println("The random point is: " + joinLoc);
					System.out.println("start at entry: " + entryIP);
					
					// Call the route function through the entry point
					entryNode.route(joinLoc);

					// Set a TCP connection if join success, return some string
					Socket targetPeer = this.server.accept();
					String targetIP = targetPeer.getInetAddress().getHostAddress(); // Get the IP of the target peer
					
					InputStream dataIn = targetPeer.getInputStream();
					byte[] buffer = new byte[1024];
					int len = dataIn.read(buffer);
					String getData = new String(buffer, 0, len);
					
					targetPeer.close();
					System.out.println("route end at: " + targetIP);
					
					if(SysParameter.FIND_SIGN.equals(getData)){
						System.out.println("Find the target zone!");
						
						// Once the find the random point belonging zone, call the remote function to split the zone
						// First use the target peer IP to call the remote function
						CanNodeInt targetNode = (CanNodeInt)Naming.
								lookup("//" + targetIP + ":" + SysParameter.CAN_PORT + "/NodeObject");
						
						if(targetNode.splitZone(this.localAddress)){
							// Set the join status to true once the node successfully join the CAN
							this.joinStatus = true;
							System.out.println("Peer join successfully!");
							this.view();
						}
						else {
							System.out.println("Failure: RMI network error.");
						}	
					}
					else if(SysParameter.FAIL_SIGN.equals(getData)){
						System.out.println("Failure: Can't find the target peer.");
					}
					else{
						System.out.println("Failure: Network transport error.");
					}	
				}	
			} 
			catch (MalformedURLException e) {
				System.out.println("Malformed: " + e.getMessage());
				//e.printStackTrace();
			} 
			catch (RemoteException e) {
				System.out.println("Remote Error: " + e.getMessage());
				//e.printStackTrace();
			} 
			catch (NotBoundException e) {
				System.out.println("NotBound Error: " + e.getMessage());
				//e.printStackTrace();
			}
			catch (IOException e) {
				System.out.println("IO Error: " + e.getMessage());
				//e.printStackTrace();
			}
		}
		else {
			System.out.println("Failure: Can't join twice!");
		}			
	}
	
	public void view() {
		
		// Before call this function, the node must already in the CAN
		if(this.joinStatus) {
			System.out.println("-------------------------------Peer Info List-------------------------------");
			System.out.println("Local peer IP: " + this.localAddress);
			System.out.println("Local peer " + this.canNode.localSpace);
			Set<String> keywordList = this.canNode.fileInfo.keySet();
			System.out.println("Current stored items: " + keywordList);
			
			String up = this.canNode.routingTable.northPeerIP;
			if(up != null) {
				System.out.println("\nUP peer IP: " + up);
				System.out.println(this.canNode.routingTable.northPeerSpace);
			}
			else {
				System.out.println("\nNo UP peer exist.");
			}
			
			String down = this.canNode.routingTable.southPeerIP;
			if(down != null) {
				System.out.println("\nDOWN peer IP: " + down);
				System.out.println(this.canNode.routingTable.southPeerSpace);
			}
			else {
				System.out.println("\nNo DOWN peer exist.");
			}
			
			String left = this.canNode.routingTable.westPeerIP;
			if(left != null) {
				System.out.println("\nLEFT peer IP: " + left);
				System.out.println(this.canNode.routingTable.westPeerSpace);
			}
			else {
				System.out.println("\nNo LEFT peer exist.");
			}
			
			String right = this.canNode.routingTable.eastPeerIP;
			if(right != null) {
				System.out.println("\nRIGHT peer IP: " + right);
				System.out.println(this.canNode.routingTable.eastPeerSpace);
			}
			else {
				System.out.println("\nNo RIGHT peer exist.");
			}
			
			System.out.println("-------------------------------------------------------------------------------");
		}
		else {
			System.out.println("Failure: Join network first!");
		}
	}
	
	public void leave() {
		System.out.println("Not implement the <leave> function!");
		System.out.println("Remember to RESTART bootstrap server before run the program again.");
	}
	
	public void insert(String keyword) {
		
		// Before call this function, the node must already in the CAN
		// Unless do nothing and print error message
		if(this.joinStatus) {
			if(keyword.length() >= InetPoint.MIN_KEYWORD_LEN) {
				
				InetPoint findTarget = new InetPoint(keyword, this.localAddress);
				System.out.println("Keyword map to point " + findTarget);
				
				if(this.canNode.localSpace.isInTheArea(findTarget)) {					
					
					boolean result = false;
					try {
						result = canNode.insertFile(keyword, findTarget);
					} 
					catch (RemoteException e) {
						System.out.println("Remote Error: " + e.getMessage());
						//e.printStackTrace();
					}
					
					if(result) {
						System.out.println("Insert Success! Store at localhost.");
					}
					else {
						System.out.println("Failure: Duplicate keyword at localhost.");
					}
				}
				else {
					System.out.println("start at local: "  + this.localAddress);
					
					try {
						canNode.route(findTarget);
					} 
					catch (RemoteException e) {
						System.out.println("Remote Error: " + e.getMessage());
						//e.printStackTrace();
					}
					
					try {
						// Set a TCP connection if join success, return some string
						Socket targetPeer = this.server.accept();
						String targetIP = targetPeer.getInetAddress().getHostAddress(); // Get the IP of the target peer
						
						InputStream dataIn = targetPeer.getInputStream();
						byte[] buffer = new byte[1024];
						int len = dataIn.read(buffer);
						String getData = new String(buffer, 0, len);
						
						targetPeer.close();
						System.out.println("route end at: " + targetIP);
						
						if(SysParameter.FIND_SIGN.equals(getData)){
							
							CanNodeInt targetNode = (CanNodeInt)Naming.
									lookup("//" + targetIP + ":" + SysParameter.CAN_PORT + "/NodeObject");
							
							if(targetNode.insertFile(keyword, findTarget)) {
								System.out.println("Insert <" + keyword + "> at peer: " + targetIP);
							}
							else {
								System.out.println("Failure: Duplicate keyword.");
							}
						}
						else if(SysParameter.FAIL_SIGN.equals(getData)){
							System.out.println("Failure: Fail to find the target.");
						}
						else{
							System.out.println("Failure: Network transport error!");
						}		
					}
					catch(Exception e) {
						System.out.println("TCP Error: " + e.getMessage());
						//e.printStackTrace();
					}
				}
			}
			else {
				System.out.println("Failure: Keyword length illegal.");
			}
		}
		else{
			System.out.println("Failure: Join network first!");
		}
	}
	
	public void retrieve(String keyword) {
		
		// Before call this function, the node must already in the CAN
		// Unless do nothing and print error message
		if(this.joinStatus) {
			if(keyword.length() >= InetPoint.MIN_KEYWORD_LEN) {
				
				InetPoint findTarget = new InetPoint(keyword, this.localAddress);
				System.out.println("Keyword map to point " + findTarget);
				
				// First check if the point is lay at the local area
				if(this.canNode.localSpace.isInTheArea(findTarget)) {	
					
					String file = null;	
					try {
						file = canNode.searchFile(keyword);
					} 
					catch (RemoteException e) {
						System.out.println("Remote Error: " + e.getMessage());
					}
					
					if(file != null) {
						System.out.println("Find at localhost. File info: " + file);
					}
					else {
						System.out.println("Failure: No such file at localhost.");
					}
				}
				else {
					System.out.println("start at local: "  + this.localAddress);
					
					try {
						canNode.route(findTarget);
					} 
					catch (RemoteException e) {
						System.out.println("Remote Error: " + e.getMessage());
						//e.printStackTrace();
					}
					
					try {
						// Set a TCP connection if join success, return some string
						Socket targetPeer = this.server.accept();
						String targetIP = targetPeer.getInetAddress().getHostAddress(); // Get the IP of the target peer
						
						InputStream dataIn = targetPeer.getInputStream();
						byte[] buffer = new byte[1024];
						int len = dataIn.read(buffer);
						String getData = new String(buffer, 0, len);
						
						targetPeer.close();
						System.out.println("route end at: " + targetIP);
						
						if(SysParameter.FIND_SIGN.equals(getData)){
							
							CanNodeInt targetNode = (CanNodeInt)Naming.
									lookup("//" + targetIP + ":" + SysParameter.CAN_PORT + "/NodeObject");
							
							String file = targetNode.searchFile(keyword);
							if(file != null) {
								System.out.println("Find file at peer: " + targetIP);
								System.out.println("File info: " + file);
							}
							else {
								System.out.println("Failure: No such file in the network.");
							}			
						}
						else if(SysParameter.FAIL_SIGN.equals(getData)){
							System.out.println("Failure: Fail to find the target.");
						}
						else{
							System.out.println("Failure: Network transport error!");
						}
						
					}
					catch(Exception e) {
						System.out.println("TCP Error: " + e.getMessage());
						e.printStackTrace();
					}	
				}
			}
			else {
				System.out.println("Failure: Keyword length illegal.");
			}
		}
		else{
			System.out.println("Failure: Join network first!");
		}
	}
	
}
