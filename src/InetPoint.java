import java.io.Serializable;

/**
 * @author Yi Huang
 * InetPoint class for 
 * 1, Determine a mapping point in the coordinate space by a given key string
 * 2, Store the key string and its source peer IP address
 * 
 * */
public class InetPoint implements Serializable{
	
	/**
	 * UID for RMI pass the InetPoint object 
	 */
	private static final long serialVersionUID = -8755318287286390518L;
	
	// The maximum side-length of the visual square
	public static final float SIDE_START = 0.0f;
	public static final float SIDE_LEN = 10.0f;
	
	// The system require the keywords have at least 5 characters
	// If the length is not in the bound, then set the coordinate as default 
	public static final int MIN_KEYWORD_LEN = 5;
	public static final float X_DEF = 2.0f;
	public static final float Y_DEF = 2.0f;
	
	private String localAddress;
	private float x_coord;
	private float y_coord;
	
	/**
	 * If a InetPoint object create, it will pass in the key string and the operation peer machine IP
	 * The mappingPoint(String) will called to map the key to the x and y coordinate
	 * */
	public InetPoint(String keyword, String localAddress) {
		this.localAddress = localAddress;
		mappingPoint(keyword);
	}
	
	/**
	 * This constructor for the peer who first join the CAN, and need to assign a random point 
	 * */
	public InetPoint(String localAddress) {
		this.localAddress = localAddress;
		this.x_coord = (int)(Math.random() * SIDE_LEN);
		this.y_coord = (int)(Math.random() * SIDE_LEN);
	}
	
	/**
	 * mappingPoint(String) is a simple modulo-based hash function, called in the constructor
	 * The x-coordinate of a keyword is computed as CharAtOdd mod 10
	 * The y-coordinate of a keyword is computed as CharAtEven mod 10
	 * */
	private void mappingPoint(String keyword) {		
		int len = keyword.length();
		
		if(len < MIN_KEYWORD_LEN) {
			this.x_coord = X_DEF;
			this.y_coord = Y_DEF;
		}
		else {
			char charAtOdd = 0;
			char charAtEven = 0;
			
			for(int index = 0; index < len; index++) {
				charAtOdd += keyword.charAt(index);
				index++;
			}
			
			for(int index = 1; index < len; index++){
				charAtEven += keyword.charAt(index);
				index++;
			}

			this.x_coord = charAtOdd % SIDE_LEN;
			this.y_coord = charAtEven % SIDE_LEN;
		}
	}
	
	public float getCoord_X() {
		return this.x_coord;
	}
	
	public float getCoord_Y() {
		return this.y_coord;
	}
	
	public String getSourceIP() {
		return this.localAddress;
	}
	
	@Override
	public String toString() {
		return "(" + this.x_coord + ", " + this.y_coord + ")";
	}
	
	/*public static void main(String[] args) {
		InetPoint p = new InetPoint("192.168.1.1");
		System.out.println(p);	
	}*/
	
}
