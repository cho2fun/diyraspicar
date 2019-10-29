package cho.raspi.component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.pi4j.gpio.extension.pca.PCA9685GpioProvider;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;

/**
 * @author Cheung Ho
 *
 */
public class ServoOperationModule extends Thread {

	
	public String name = "";
	public int position = 0; // position from 4 thru 27;
	protected GpioPinPwmOutput pwm = null;
	TimeUnit time = TimeUnit.MILLISECONDS;
	public int min = -1;
	public int max = -1;
	public int center = -1;
	public long sleep = 200; // this S90 needs 200ms delay between moves.
	public int restInterval = 60000; // in milliseconds 
	public int restTime = 10000; // in milliseconds 
	public int  direction = 1; 
	public int step = 20 ; // pwmunit per step of servo move. // this optimal step for S90 servo
	
	
	public ServoOperationModule(GpioPinPwmOutput _pwm,String _name,  int _min,int  _max) {
		name = _name;
		this.pwm = _pwm;
		this.min  = _min;
		this.max = _max;
		this.center = ((this.max-this.min)/2) +this.min;
		step = 20;
		position = center;

	}

	public void run() {
		Random rand = new Random();
		long lastRestTime = System.currentTimeMillis();
		int sPosition = 5 ; 
		int midPosition = 4;
		try {
			while(true) {
			this.sweep();
			}
/*
			while (true) {
				int n = rand.nextInt(10);
				sPosition = n  - midPosition;

				this.moveTo(sPosition+n);
				System.out.printf("%s PWM: %d  \n",name, sPosition);
				sleep(sleep);
				long ttlTime = lastRestTime + restInterval;
				long nw = System.currentTimeMillis();
				System.out.printf("%s test %d vs %d \n",name, ttlTime, nw);
				if (lastRestTime + restInterval < System.currentTimeMillis() ) {
					System.out.printf("%s resting \n",name);
					sleep(restTime);
					lastRestTime += restInterval;
				}
			}
*/
		}
		catch (Exception ex ) {
			System.out.printf( "exception %s - %s",this.getContextClassLoader().getName() , ex);
		}
	}
	

	//	move servo to new position randomly. 
	public void step() {
		Random rand = new Random();
		int midPosition = 5;
		try {
			int n = rand.nextInt(10);
			n = n  - midPosition;

			this.move(n);
			System.out.printf("%s PWM: %d  \n",name, this.position);
		}
		catch (Exception ex ) {
			System.out.printf( "exception %s - %s",this.getContextClassLoader().getName() , ex);
		}
	}

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		

	}
	

	
	

	boolean move( int degree) throws InterruptedException {
		System.out.println( this.position + "  move "+ degree);
		position += degree;
		if (position > max) {
			position = max;
			return false;
		}
		if (position < min ) {
			position = min;
			return false;
		}
		pwm.setPwm(position);
		time.sleep(sleep);
		return true;
	}
	
		
	
	void moveTo( int degree) throws InterruptedException {
		position =  degree;
		if (position > max) position = max;
		if (position < min ) position = min;
		pwm.setPwm(position);
		time.sleep(sleep);
	}
	

	public void sweep() {
		try {
			if (this.position <= min) direction = 1;
			else if (this.position>=max) direction = -1;
			this.move(direction*step);
			
			System.out.printf("%s PWM: %d\n",name, this.position);
		}
		catch (Exception ex ) {
			System.out.printf( "sweep exception %s : %s \n",this.getName() , ex);
		}
	}
	
	// stop sending signal to servo .
//	public void setToLow() {
//		pwm.setPwm(1);
//	}
}
