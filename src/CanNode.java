import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * @author Yi Huang
 * Implement the CanNodeInt interface for the RMI to call remotely
 * */
public class CanNode extends UnicastRemoteObject implements CanNodeInt {

	private static final long serialVersionUID = 4762968301185693026L;
	
	public InetArea localSpace;
	public RoutingTable routingTable;
	public String localIP;
	
	public HashMap<String, String> fileInfo;
	public HashMap<InetPoint, String> pointInfo;

	protected CanNode(String localIP) throws RemoteException {
		this.localIP = localIP;
		this.fileInfo = new HashMap<String, String>();
		this.pointInfo = new HashMap<InetPoint, String>();
	}
	
	private void sendByTCP(String IP, String message) {
		// The point is in the area send a TCP message to the peer to inform the finding information
		try {
			Socket toSource = new Socket(IP, SysParameter.TCP_PORT);
			OutputStream out = toSource.getOutputStream();
			
			byte[] dataBytes = message.getBytes();
			out.write(dataBytes);
			
			toSource.close();
		} 
		catch (UnknownHostException e) {
			System.out.println("Error: " + e.getMessage());
			//e.printStackTrace();
		} 
		catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			//e.printStackTrace();
		}
	}
	
	private void routeForward(String forwardIP, InetPoint point) {	
		try {
			CanNodeInt forwardNode = (CanNodeInt)Naming.
					lookup("//" + forwardIP + ":" + SysParameter.CAN_PORT + "/NodeObject");
			forwardNode.route(point);
			
			// Print router information to source peer
			CanNodeInt sourcePeer = (CanNodeInt)Naming.
					lookup("//" + point.getSourceIP() + ":" + SysParameter.CAN_PORT + "/NodeObject");
			sourcePeer.printRouteInfo(this.localIP);
		} 
		catch (MalformedURLException e) {
			System.out.println("Error: " + e.getMessage());
			//e.printStackTrace();
		} 
		catch (NotBoundException e) {
			System.out.println("Error: " + e.getMessage());
			//e.printStackTrace();
		} 
		catch (RemoteException e) {
			System.out.println("Error: " + e.getMessage());
			//e.printStackTrace();
		}
	}

	/**
	 * @author Yi Huang
	 * Implement the route mechanism, just greedy forward the the route requirement to the neighbor
	 * peer, one found in the zone area, then send a TCP connect to the source IP
	 * */
	@Override
	public void route(InetPoint point) throws RemoteException {
		
		// Check if is in the local area
		if(this.localSpace.isInTheArea(point)) {
			sendByTCP(point.getSourceIP(), SysParameter.FIND_SIGN);
			return;
		}
		// Then check if in the surrounding area
		else {
			
			if(this.routingTable.westPeerIP != null) {
				if(this.routingTable.westPeerSpace.isInTheArea(point)) {
					routeForward(this.routingTable.westPeerIP, point);
					return;
				}
			}
			else if(this.routingTable.eastPeerIP != null) {
				if(this.routingTable.eastPeerSpace.isInTheArea(point)) {
					routeForward(this.routingTable.eastPeerIP, point);
					return;
				}
			}
			else if(this.routingTable.northPeerIP != null) {
				if(this.routingTable.northPeerSpace.isInTheArea(point)) {
					routeForward(this.routingTable.northPeerIP, point);
					return;
				}
			}
			else if(this.routingTable.southPeerIP != null) {
				if(this.routingTable.southPeerSpace.isInTheArea(point)) {
					routeForward(this.routingTable.southPeerIP, point);
					return;
				}
			}
		}
		
		float x = point.getCoord_X();
		float y = point.getCoord_Y();
		
		// Use the route mechanism to forward the information
		if(x < localSpace.fromX && routingTable.westPeerIP != null) {
			routeForward(this.routingTable.westPeerIP, point);
		}
		else if(x >= localSpace.toX && routingTable.eastPeerIP != null) {
			routeForward(this.routingTable.eastPeerIP, point);
		}
		else if(y < localSpace.fromY && routingTable.southPeerIP != null) {
			routeForward(this.routingTable.southPeerIP, point);
		}
		else if(y >= localSpace.toY && routingTable.northPeerIP != null) {
			routeForward(this.routingTable.northPeerIP, point);
		}
		else{
			// Failed to route the point, send fail information to the source IP
			sendByTCP(point.getSourceIP(), SysParameter.FAIL_SIGN);
		}		
	}
	
	private void fileMigrate(InetArea zone, CanNodeInt target) throws RemoteException {
		
		Iterator<Map.Entry<InetPoint, String>> it = this.pointInfo.entrySet().iterator();
		while(it.hasNext()) {
		
			Map.Entry<InetPoint, String> entry = it.next();
			InetPoint keyPoint = entry.getKey();
			
			/**
			 * Get the keyword mapping point, if the point lay in the new area, then insert the keyword to
			 * the new are, and remove it from the old zone.
			 * */
			if(zone.isInTheArea(keyPoint)) {
				
				String keyword = entry.getValue();
				target.insertFile(keyword, keyPoint);
				this.fileInfo.remove(keyword);
				
				// Remove the point from the old map
				it.remove();
			}
		}
	}
	
	private void updateNeighbor(RoutingTable table, InetArea newArea, String IP, boolean control) 
			throws RemoteException, MalformedURLException, NotBoundException {
		
		if(table.westPeerIP != null) {
			CanNodeInt targetNode = (CanNodeInt)Naming.
					lookup("//" + table.westPeerIP + ":" + SysParameter.CAN_PORT + "/NodeObject");
			targetNode.setWestRoutingArea(newArea, IP);
		}
		
		if(control) {
			if(table.eastPeerIP != null) {
				CanNodeInt targetNode = (CanNodeInt)Naming.
						lookup("//" + table.eastPeerIP + ":" + SysParameter.CAN_PORT + "/NodeObject");
				targetNode.setEastRoutingArea(newArea, IP);
			}
		}
		else {
			if(table.northPeerIP != null) {
				CanNodeInt targetNode = (CanNodeInt)Naming.
						lookup("//" + table.northPeerIP + ":" + SysParameter.CAN_PORT + "/NodeObject");
				targetNode.setNorthRoutingArea(newArea, IP);
			}
		}
		
		if(table.southPeerIP != null) {
			CanNodeInt targetNode = (CanNodeInt)Naming.
					lookup("//" + table.southPeerIP + ":" + SysParameter.CAN_PORT + "/NodeObject");
			targetNode.setSouthRoutingArea(newArea, IP);
		}	
	}

	/**
	 * @author Yi Huang
	 * This function is remotely called by the peer who want to join the CAN
	 * The target peer will split its own zone to half and half
	 * And then send a remote call to send the zone to the source peer
	 * Also, use the same way to assign the routing table to each peer
	 * */
	@Override
	public boolean splitZone(String IP) throws RemoteException {
		
		CanNodeInt targetNode;
		try {
			targetNode = (CanNodeInt)Naming.
					lookup("//" + IP + ":" + SysParameter.CAN_PORT + "/NodeObject");
		} 
		catch (MalformedURLException e) {
			return false;
		} 
		catch (NotBoundException e) {
			return false;
		}
		
		float length = localSpace.toX - localSpace.fromX;
		float height = localSpace.toY - localSpace.fromY;
		float len_divd = length/2;
		float hei_divd = height/2;
		
		// If it's a square or length than height then vertically split the zone
		// The LEFT half is retain by itself, the RIGHT half is retain by the peer who want to join
		if(length >= height) {
			float split_x = localSpace.fromX + len_divd;
			
			// Generate the space for the new peer, and set it to the peer that join the CAN
			InetArea otherHalf = new InetArea();
			otherHalf.setInetArea(split_x, localSpace.toX, localSpace.fromY, localSpace.toY);
			targetNode.setTargetArea(otherHalf);
			
			// Update its own zone, retain the LEFT part
			localSpace.toX = split_x;
			
			RoutingTable targetInfo = new RoutingTable();
						
			if(routingTable.eastPeerIP != null) {
				
				// The east part once belong to the local peer, after split will assign to the new peer
				targetInfo.eastPeerIP = routingTable.eastPeerIP;
				targetInfo.eastPeerSpace = routingTable.eastPeerSpace;
				
				//Inform the east peer there is a new zone start
				try {
					CanNodeInt updateEast = (CanNodeInt)Naming.
							lookup("//" + routingTable.eastPeerIP + ":" + SysParameter.CAN_PORT + "/NodeObject");
					updateEast.setEastRoutingArea(otherHalf, IP);
				} 
				catch (MalformedURLException | NotBoundException e1) {
					return false;
				}
			}
			
			if(routingTable.northPeerIP != null) {
				if(routingTable.northPeerSpace.xLength() > otherHalf.xLength()) {
					targetInfo.northPeerIP = routingTable.northPeerIP;
					targetInfo.northPeerSpace = routingTable.northPeerSpace;
				}
			}
			
			if(routingTable.southPeerIP != null) {
				if(routingTable.southPeerSpace.xLength() > otherHalf.xLength()) {
					targetInfo.southPeerIP = routingTable.southPeerIP;
					targetInfo.southPeerSpace = routingTable.southPeerSpace;
				}
			}
			
			// The west part of the new peer is the local peer
			targetInfo.westPeerIP = this.localIP;
			targetInfo.westPeerSpace = this.localSpace;
			
			targetNode.setTargetRoutingTable(targetInfo);
			
			// Update its own routing table
			routingTable.eastPeerIP = IP;
			routingTable.eastPeerSpace = otherHalf;
			
			// Migrate the file to the new zone, and remove it from old zone
			this.fileMigrate(otherHalf, targetNode);
			
			// Inform the surrounding peer about the change of the split zone
			// West part, North part, South part
			try {
				updateNeighbor(this.routingTable, this.localSpace, this.localIP, false);
			} 
			catch (MalformedURLException e) {
				return false;
			} 
			catch (NotBoundException e) {
				return false;
			}
		}
		// Otherwise, split the zone horizontally and retain the DOWN part
		// The join peer will retain the UP part
		else{
			float split_y = localSpace.fromY + hei_divd;
			
			InetArea otherHalf = new InetArea();
			otherHalf.setInetArea(localSpace.fromX, localSpace.toX, split_y, localSpace.toY);
			targetNode.setTargetArea(otherHalf);
			
			// Update its own zone, Retain the DOWN part
			localSpace.toY = split_y;
			
			RoutingTable targetInfo = new RoutingTable();
			
			if(routingTable.northPeerIP != null) {
				
				// The upper part once belong to the local peer, after split will assign to the new peer
				targetInfo.northPeerIP = routingTable.northPeerIP;
				targetInfo.northPeerSpace = routingTable.northPeerSpace;
				
				//Inform the east peer there is a new zone start
				try {
					CanNodeInt updateNorth = (CanNodeInt)Naming.
							lookup("//" + routingTable.northPeerIP + ":" + SysParameter.CAN_PORT + "/NodeObject");
					updateNorth.setNorthRoutingArea(otherHalf, IP);
				} 
				catch (MalformedURLException | NotBoundException e1) {
					return false;
				}
			}
			
			if(routingTable.eastPeerIP != null) {
				if(routingTable.eastPeerSpace.yLength() > otherHalf.yLength()) {
					targetInfo.eastPeerIP = routingTable.eastPeerIP;
					targetInfo.eastPeerSpace = routingTable.eastPeerSpace;
				}
			}
			
			if(routingTable.westPeerIP != null) {
				if(routingTable.westPeerSpace.yLength() > otherHalf.yLength()) {
					targetInfo.westPeerIP = routingTable.westPeerIP;
					targetInfo.westPeerSpace = routingTable.westPeerSpace;
				}
			}
			
			targetInfo.southPeerIP = this.localIP;
			targetInfo.southPeerSpace = this.localSpace;
			
			targetNode.setTargetRoutingTable(targetInfo);
			
			// Update its own routing table
			routingTable.northPeerIP = IP;
			routingTable.northPeerSpace = otherHalf;
			
			// Migrate the file to the new zone, and remove it from old zone
			this.fileMigrate(otherHalf, targetNode);
			
			// Inform the surrounding peer about the change of the split zone
			// West part, East part, South part
			try {
				updateNeighbor(this.routingTable, this.localSpace, this.localIP, true);
			} 
			catch (MalformedURLException e) {
				return false;
			} 
			catch (NotBoundException e) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void setTargetArea(InetArea target) throws RemoteException {
		this.localSpace = target;
	}

	@Override
	public void setTargetRoutingTable(RoutingTable target)
			throws RemoteException {
		this.routingTable = target;
	}

	@Override
	public boolean insertFile(String keyword, InetPoint point) throws RemoteException {
		String files = "file with keyword <" + keyword + "> is here!";
		
		if(this.fileInfo.get(keyword) == null) {
			
			this.fileInfo.put(keyword, files);
			this.pointInfo.put(point, keyword);
			return true;
		}
		else {
			return false;			
		}
	}

	@Override
	public String searchFile(String keyword) throws RemoteException {
		return this.fileInfo.get(keyword);
	}

	@Override
	public void printRouteInfo(String router) throws RemoteException {
		System.out.println("route info ---> " + router);
	}

	@Override
	public void setWestRoutingArea(InetArea area, String IP) throws RemoteException {
		this.routingTable.eastPeerSpace = area;
		this.routingTable.eastPeerIP = IP;
	}

	@Override
	public void setEastRoutingArea(InetArea area, String IP) throws RemoteException {
		this.routingTable.westPeerSpace = area;
		this.routingTable.westPeerIP = IP;
	}

	@Override
	public void setNorthRoutingArea(InetArea area, String IP) throws RemoteException {
		this.routingTable.southPeerSpace = area;
		this.routingTable.southPeerIP = IP;
	}

	@Override
	public void setSouthRoutingArea(InetArea area, String IP) throws RemoteException {
		this.routingTable.northPeerSpace = area;
		this.routingTable.northPeerIP = IP;
	}

}
