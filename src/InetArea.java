import java.io.Serializable;

/**
 * @author Yi Huang
 * InetArea class for store the coordinate space the node own
 * */
public class InetArea implements Serializable{
	
	/**
	 * UID for RMI pass the InetPoint object 
	 */
	private static final long serialVersionUID = -511657167923560780L;
	
	/**
	 * The value from is smaller than the to value
	 * The from value is included in the are, the to value is not in the area
	 * */
	public float fromX;
	public float toX;
	public float fromY;
	public float toY;
	
	public void setInetArea(float fromX, float toX, float fromY, float toY) {
		this.fromX = fromX;
		this.toX = toX;
		this.fromY = fromY;
		this.toY = toY;
	}
	
	public boolean isInTheArea(InetPoint point) {
		
		float x = point.getCoord_X();
		float y = point.getCoord_Y();

		if(x >= this.fromX && x < this.toX && y >= this.fromY && y < this.toY) {
			return true;
		}
		else {
			return false;			
		}
	}
	
	public float xLength() {
		return this.toX - this.fromX;
	}
	
	public float yLength() {
		return this.toY - this.fromY;
	}
	
	@Override
	public String toString() {
		return "Coordinate space [" + fromX + " to " + toX + "] * [" + fromY + " to " + toY + "]";
	}
	
	/*public static void main(String[] args) {
		InetArea ia = new InetArea();
		ia.setInetArea(1, 2, 4, 9);
		System.out.println(ia);
	}*/

}
