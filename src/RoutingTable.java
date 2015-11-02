import java.io.Serializable;

/**
 * @author Yi Huang
 * The routing table contains the four peer information surrounding the current peer
 * */
public class RoutingTable implements Serializable{
	
	/**
	 * UID for RMI pass the InetPoint object 
	 */
	private static final long serialVersionUID = 9164886621720954799L;
	
	public String eastPeerIP;
	public InetArea eastPeerSpace;
	
	public String westPeerIP;
	public InetArea westPeerSpace;
	
	public String northPeerIP;
	public InetArea northPeerSpace;
	
	public String southPeerIP;
	public InetArea southPeerSpace;
	
	
	/*public RoutingTable() {
		this.eastPeerSpace = new InetArea();
		this.westPeerSpace = new InetArea();
		this.northPeerSpace = new InetArea();
		this.southPeerSpace = new InetArea();
	}
	*/

	/*public static void main(String[] args) {
		RoutingTable rt = new RoutingTable();
		System.out.println(rt.eastPeerSpace);
	}*/

}
