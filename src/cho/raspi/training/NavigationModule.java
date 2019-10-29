package cho.raspi.training;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import cho.raspi.component.SteeringModule;
import cho.raspi.component.MPU9250_GyroMagnet;
import cho.raspi.itf.ModuleItf;

/* this module allow testing the car wheels module.
 * including drive car forward, turn,back
 */

public class NavigationModule extends Thread implements ModuleItf {
	private static GpioController gpio = GpioFactory.getInstance();
	int delay = 5000;
    public Logger logger = LoggerFactory.getLogger(NavigationModule.class);
	
	
//	private GpioPinDigitalInput motionPIRSensor = null;
//	private UltraSonicSensorModule ultraSonicSensor = null;
//	private ConcurrentLinkedQueue messageQueue = null;
//	private ObservationModule observator = null;
	
	SteeringModule steering = null;

	
    private volatile boolean paused = false;

	
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub

		if(args.length < 1) {
			System.out.println("test iteration - params turning degrees, iteration numbers. ");
			System.exit(1);
		}
		System.out.println("test Mission starts version 1.14");

		int degree = Integer.parseInt(args[0]);
		int loop = Integer.parseInt(args[1]);
		
		NavigationModule control = new NavigationModule(gpio);
		control.testWheels(degree, loop);
//		control.runCircle(degree, loop); // this loop is the degree of forward turn
		System.out.println("Mission Accomplished.");
		
	}


	private void testWheels(int degree, int loop) throws InterruptedException {
		System.out.println("rotate wheels v2");
//		steering.forward();
//		Thread.sleep(2000);
//		steering.reverse();
//		Thread.sleep(2000);
		for(int lap=1 ; lap <= loop ; lap++ ){
			steering.rotate(degree);
			System.out.printf("lap no. %d \n", lap);
			Thread.sleep(3000);
		}

	}
	
	@Override
	public void run() {execute();}
	
	@Override
	public void execute() {
		try {
			int direction = 1;
			System.out.println("car controller run method "  );
			steering.forward();
			Thread.sleep(1000);
			steering.reverse();
			Thread.sleep(1000);
			System.out.println("warmup stage completed "  );
			int i  = 0;
			while(true) {
				System.out.println("Navigation lap "+ i++);
				try {
					synchronized (this) {
						if (paused) {
							steering.stop();
							this.wait();
							continue;
						}
					}

					if (direction > 0 ) {
						steering.forward();
					}
					else {
						steering.reverse();
					}
					sleep(5000);
					/*		        
 //				this navigation plan drives in circle 
		        int degree = -30;
				int loop = 2 ;
				if (direction > 0) {
					runCircle(degree,loop); // this loop is the degree of forward turn
				}
				else {
					runReverseCircle(degree, loop); // this loop is the degree of forward turn
					direction = 1;
				}
					 */				
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("navi interrupted ");
					steering.stop();
				}
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	
	public NavigationModule (GpioController gpio) {
		// initialize wheels
		steering = new SteeringModule(gpio);
	}


	public void runRotatingWheel(MPU9250_GyroMagnet compass  ) throws InterruptedException {
		int[] iter = {45, 90, 180, 270};  // turnning 360 degree require algorithm changes and review.
		double head = compass.readMagnetemeter(3,false);
		
		System.out.printf("******* rotate wheels %8.4f\n", head);
		Thread.sleep(3000);
		try {
//			steering.forward();
//			Thread.sleep(500);
//			steering.reverse();
//			Thread.sleep(500);
			while (true ){
				for (int degree: iter) {
					System.out.printf("************************************\n");
					System.out.printf("******            ROTATE %d degrees \n", degree);
					steering.rotate(degree, compass);
					steering.stop(); // remove rumbling noise when car is idle

					Thread.sleep(2000);	
					System.out.printf("************************************\n");
					System.out.printf("******             ROTATE %d degrees \n", (-1* degree));
					steering.rotate(-1*degree, compass);
					steering.stop(); // remove rumbling noise when car is idle
					Thread.sleep(5000);	

				}
				System.out.println("taking 5 secs break ");
				steering.stop();
				Thread.sleep(5000);	
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public void runCircle(int degree, int loop) throws InterruptedException {
		System.out.println("runCircle "+ degree + " : "+ loop);
		for(int lap=1 ; lap < loop ; lap++ ){
			steering.forward(degree);
			System.out.printf("runCircle lap no. %d \n", ++lap);
		}
		steering.stop();
	}
	
	// this method use magnetometer to measure degree of rotation of the car
	public void runCircle(MPU9250_GyroMagnet compass, int degree) throws InterruptedException {
		int strongPwm = 900;
		int slowPwm = 200;
		double startPost = compass.readMagnetemeter(3, false);
		double distTravel = 0;
		double previousPost = startPost;
		int targetAngle = Math.abs(degree);
		while(true) {
			steering.rotate(degree);
			double nowPost = compass.readMagnetemeter(2, false);
			distTravel = this.trackAngle(degree,startPost,  previousPost, nowPost,distTravel);
			logger.info("track runCir {} ,s {} , {} -> {} t {} \n",degree, startPost, previousPost,nowPost, distTravel );
			previousPost = nowPost;
			if (distTravel > targetAngle) {
				logger.info("track fwd over {} > {}", distTravel , targetAngle);
				break;
			}
		}
		steering.stop();
		steering.setDefaultSpeed();
	}

	
	public void runReverseCircle(int degree, int loop) throws InterruptedException {
		System.out.println("runReverseCircle wheels ");
		for(int lap=1 ; lap < loop ; lap++ ){
			steering.reverse(degree);
			System.out.printf("lap no. %d \n", ++lap);
		}
		steering.stop();
	}

	// this method use magnetometer to measure degree of rotation of the car
	public void runReverseCircle(MPU9250_GyroMagnet compass, int degree) throws InterruptedException {
//		steering.reverse(degree, compass);
//		steering.stop();
		int rDegree = -1* degree;
		//	don't implement this until it is fully tested	
		//		compass.gyromagWake(AK8963_MapValues.AK8963_CNTL_MODE_SINGLEMEASURE);
		//		Thread.sleep(200);
		this.steering.reverse();
		Thread.sleep(500);
		double startPost = compass.readMagnetemeter(3, false);
		double distTravel = 0;
		double previousPost = startPost;
		int targetAngle = Math.abs(degree);
		this.steering.rotate(degree);
		//				this.reverse();
		//				Thread.sleep(300);
		while(true) {
			double nowPost = compass.readMagnetemeter(2, false);
			distTravel = this.trackAngle(degree,startPost,  previousPost, nowPost,distTravel);
			//			System.out.printf("%d %8.4f, %8.4f - %8.4f = %8.4f  \n",degree, startPost, previousPost,nowPost, distTravel);
			logger.info("track rotate {} ,s {} , {} -> {} =t {} \n",degree, startPost, previousPost,nowPost, distTravel );
			previousPost = nowPost;
			if (distTravel > targetAngle) {
				logger.info("track over {} > {}", distTravel , targetAngle);
				break;
			}
		}

		//		don't implement this until it is fully tested	
		//		compass.gyromagSleep();
		//		Thread.sleep(200);
		this.steering.stop();
		this.steering.setDefaultSpeed();
		
	}
	
	
	public void avoid() throws InterruptedException {
//		this.reverse();
		Thread.sleep(3000);
//		turnLeft();
		Thread.sleep(3000);
//		stop();
	}
	
	

	@Override
	public void pause() {
		System.out.println("Navigationmodule pause() ");
		this.paused = true;
		steering.stop();
		try {
		Thread.sleep(200);
		}
		catch (InterruptedException ex) {}
		
	}

	@Override
	public void unpause() {
		System.out.println("Navigationmodule unpause() ");
        synchronized (this) {
			this.paused = false;
			this.notify();
        }
	}


	@Override
	public boolean isPaused() {
		// TODO Auto-generated method stub
		return this.paused;
	}

	/* track total distance the car has turns.
	 * param direciton : 1 clockwise or -1 counter clockwise direction
	 * param prevPost and currentPost : 0 - 360
	 * 
	 */
	
	public double trackAngle(int direction, double startPost,   double prevPost, double curPost,double dist) {
		double ttl = 0;
//		if (Math.abs((prevPost-curPost)) <= 5 ) return dist; // ignore data for drifting
		if (direction > 0 )	{
			if (curPost >= startPost && curPost <= prevPost ) return dist;
			if (curPost < prevPost  && prevPost > 300) { //$review
				ttl = curPost + ( 360 - prevPost)+ dist;
			}
			else if (curPost > prevPost ) {
				ttl = dist + ( curPost- prevPost);
			}
			else return dist;
		}
		else {
			if (curPost <= startPost && curPost  >= prevPost ) return dist;
			if (curPost > prevPost) {
				ttl = (360 - curPost + prevPost) + dist;
			}
			else if (curPost < prevPost)  {
				ttl = dist + (prevPost - curPost);
			}
			else return dist;
			
		}
		// to oversome drift from magnetometer.
		if ((ttl - dist) > 270 ) return dist;
		return ttl;
	}

}
