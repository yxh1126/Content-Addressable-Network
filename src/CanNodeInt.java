import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Yi Huang
 * Define the interface of a CAN peer
 * */
public interface CanNodeInt extends Remote{

	public abstract void route(InetPoint point)
			throws RemoteException;
	
	public abstract boolean splitZone(String IP) 
			throws RemoteException;
			
	public abstract void setTargetArea(InetArea target) 
			throws RemoteException;	
	
	public abstract void setTargetRoutingTable(RoutingTable target) 
			throws RemoteException;	
	
	public abstract boolean insertFile(String keyword, InetPoint point)
			throws RemoteException;
	
	public abstract String searchFile(String keyword)
			throws RemoteException;
	
	public void printRouteInfo(String router)
			throws RemoteException;
	
	public void setWestRoutingArea(InetArea area, String IP)
			throws RemoteException;
	
	public void setEastRoutingArea(InetArea area, String IP)
			throws RemoteException;
	
	public void setNorthRoutingArea(InetArea area, String IP)
			throws RemoteException;
	
	public void setSouthRoutingArea(InetArea area, String IP)
			throws RemoteException;

}
