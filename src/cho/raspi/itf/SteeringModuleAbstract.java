package cho.raspi.itf;

/**
 * @author: Cheung Ho
 *
 */

public abstract class SteeringModuleAbstract {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public abstract boolean stop();

	public abstract boolean forward() ;
	
	// positive - clockwise
	// negative - counter clockwise
	public abstract boolean forward(int degree)  throws InterruptedException ;

	public abstract boolean reverse() ;
	
	// positive - clockwise
	// negative - counter clockwise
	public abstract boolean reverse(int degree) throws InterruptedException ;

	public abstract boolean rotate(int degree) throws InterruptedException ;
	
	public abstract boolean turnLeft(int degree) ;

	public abstract boolean turnRight(int degree) ;
	

	
}
