package cho.raspi.component;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.RaspiPin;

import cho.raspi.itf.SteeringModuleAbstract;

public class SteeringModule extends SteeringModuleAbstract {

	
	private DCMotorModule leftMotor = null;
	private DCMotorModule rightMotor = null;
    public Logger logger = LoggerFactory.getLogger(SteeringModule.class);

	
	
	public SteeringModule(final GpioController gpio) {
		// initialize wheels
		try {
			leftMotor = new DCMotorModule(gpio, RaspiPin.GPIO_23, RaspiPin.GPIO_21,RaspiPin.GPIO_22 );
			rightMotor = new DCMotorModule(gpio, RaspiPin.GPIO_01, RaspiPin.GPIO_04,RaspiPin.GPIO_05 );
		
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	
	@Override
	public boolean forward() {
//		System.out.println("forward ");
		leftMotor.forward();
		rightMotor.forward();
		return false;
	}

	public boolean slowDown() {
		leftMotor.stop();
		rightMotor.stop();
		System.out.println("slowing down ");
		leftMotor.setSpeed(leftMotor.SLOW_PWM_SPEED);
		rightMotor.setSpeed(leftMotor.SLOW_PWM_SPEED);
		return true;
	}

	
	@Override
	public boolean reverse() {
		System.out.println("reverse ");
		leftMotor.reverse();
		rightMotor.reverse();

		return false;
	}

	@Override
	public boolean turnLeft(int degree) {
		try {
			System.out.printf("turn left %d degree\n",  degree  );
			int currentSpeed = leftMotor.getSpeed();
			leftMotor.reverse();
			leftMotor.setSpeed(currentSpeed +200);
			rightMotor.forward();
			rightMotor.setSpeed(currentSpeed +200);
				Thread.sleep(1000);
			leftMotor.setSpeed(currentSpeed);
			rightMotor.setSpeed(currentSpeed);
			System.out.println(leftMotor.toString());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean turnRight(int degree) {
		try {
			System.out.printf("turn right %d degree\n",  degree  );
			int currentSpeed = leftMotor.getSpeed();
			leftMotor.forward();
			leftMotor.setSpeed(currentSpeed +200);
			rightMotor.reverse();
			rightMotor.setSpeed(currentSpeed +200);
			Thread.sleep(1000);
			leftMotor.setSpeed(currentSpeed);
			rightMotor.setSpeed(currentSpeed);
			System.out.println(leftMotor.toString());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean forward(int degree)  throws InterruptedException  {
		int step = Math.abs(degree)/4;
		int direction = (degree>0?1:-1);
		int absDegree = Math.abs(degree);
		if (direction > 0) { 
			for (int turn = step ;turn < absDegree;turn += step) {
				this.rotate(turn);
				this.forward();
				Thread.sleep(300);
			}
		}
		else {
			for (int turn = step ;turn < absDegree;turn += step) {
				this.rotate(-1*turn);
				this.forward();
				Thread.sleep(300);
			}
		}
		return false;
	}

	@Deprecated
	private boolean forward(int degree, MPU9250_GyroMagnet compass) throws InterruptedException    {
		//		don't implement this until it is fully tested	
		//			compass.gyromagWake(AK8963_MapValues.AK8963_CNTL_MODE_SINGLEMEASURE);
		//			Thread.sleep(200);
		int strongPwm = 900;
		int slowPwm = 200;
		double startPost = compass.readMagnetemeter(3, false);
		double distTravel = 0;
		double previousPost = startPost;
		int targetAngle = Math.abs(degree);
		while(true) {
			if (degree > 0) {
				forward();
				leftMotor.setSpeed(strongPwm);
				rightMotor.setSpeed(slowPwm);
			}
			else {
				forward();
				leftMotor.setSpeed(slowPwm);
				rightMotor.setSpeed(strongPwm);
			}
			Thread.sleep(180);
			double nowPost = compass.readMagnetemeter(3, false);
			distTravel = this.trackAngle(degree,startPost,  previousPost, nowPost,distTravel);
			//				System.out.printf("%d %8.4f, %8.4f - %8.4f = %8.4f  \n",degree, startPost, previousPost,nowPost, distTravel);
			logger.info("track fwd {} ,s {} , {} -> {} t {} \n",degree, startPost, previousPost,nowPost, distTravel );
			previousPost = nowPost;
			if (distTravel > targetAngle) {
				logger.info("track fwd over {} > {}", distTravel , targetAngle);
				break;
			}
		}
		this.stop();
		leftMotor.setSpeed(leftMotor.DEFAULT_PWM_SPEED);
		rightMotor.setSpeed(rightMotor.DEFAULT_PWM_SPEED);
		return false;
	}

	public boolean reverse(int degree, MPU9250_GyroMagnet compass) throws InterruptedException    {
		//	don't implement this until it is fully tested	
		//		compass.gyromagWake(AK8963_MapValues.AK8963_CNTL_MODE_SINGLEMEASURE);
		//		Thread.sleep(200);
		double startPost = compass.readMagnetemeter(3, false);
		double distTravel = 0;
		double previousPost = startPost;
		int targetAngle = Math.abs(degree);
		this.rotate(degree);
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
		this.stop();
		leftMotor.setSpeed(leftMotor.DEFAULT_PWM_SPEED);
		rightMotor.setSpeed(rightMotor.DEFAULT_PWM_SPEED);
		return false;
	}


	@Override
	public boolean reverse(int degree) throws InterruptedException    {
		//		int currentSpeed = leftMotor.getSpeed();
		//		System.out.printf("forward turn %d degree \n",  degree  );
		int step = Math.abs(degree)/4;
		int direction = (degree>0?1:-1);
		int absDegree = Math.abs(degree);
		if (direction > 0) { 
			for (int turn = step ;turn < absDegree;turn += step) {
				this.rotate(turn);
				this.reverse();
				Thread.sleep(300);
			}
		}
		else {
			for (int turn = step ;turn < absDegree;turn += step) {
				this.rotate(-1*turn);
				this.reverse();
				Thread.sleep(300);
			}
		}
		//		System.out.println(leftMotor.toString());
		return false;
	}



	@Override
	public boolean stop() {
		System.out.printf("Steering stop() \n" );
		leftMotor.stop();
		rightMotor.stop();
		return true;
	}


// param: degree = + 90 clockwise 90 degrees , -90 counter clockwise
// the car takes 6 seconds to complete 360 degrees
// this method use timing to determine when to stop turning	
	@Override
	public boolean rotate(int degree) throws InterruptedException {
		
			int currentSpeed = leftMotor.getSpeed();
//			int rotatePwm = (currentSpeed< 400? 400:currentSpeed) + 300;
			int rotatePwm = 900;
			System.out.printf("rotate %d degree at pwm %d   (from %d)\n",  degree , rotatePwm , currentSpeed);
			
			if (degree > 0) { 
				System.out.printf("left motor forward, right motor reverse");
				
				leftMotor.forward();
				leftMotor.setSpeed(rotatePwm);
				rightMotor.reverse();
				rightMotor.setSpeed(rotatePwm);
			}
			else {
				System.out.printf("left motor reverse, right motor forward");
				leftMotor.reverse();
				leftMotor.setSpeed(rotatePwm);
				rightMotor.forward();
				rightMotor.setSpeed(rotatePwm);
			}
			// 6000 /360 = 16 ms for each degree
			Thread.sleep(8*Math.abs(degree));  // wheels are losing power as more devices are connected.
		return false;
		
	}


	/* this method uses  magnetometer's data to decide when rotation stops. 
	* param: degree = + 90 clockwise 90 degrees , -90 counter clockwise
	* the car takes 6 seconds to complete 360 degrees	
	* 
	*/	
		@Deprecated  // refer to NavigationModule.rotate 
		public boolean rotate(int degree, MPU9250_GyroMagnet compass) throws InterruptedException {
//			compass.gyromagWake(AK8963_MapValues.AK8963_CNTL_MODE_SINGLEMEASURE);
//			Thread.sleep(200);
			int currentSpeed = leftMotor.getSpeed();
			int rotatePwm = 900;
//			(currentSpeed< 400? 400:currentSpeed) + 400;
			boolean ok = false;
			double startPost = compass.readMagnetemeter(3, false);
			double distTravel = 0;
			double previousPost = startPost;
			int targetDistance = Math.abs(degree);
			// track distance traveled instead of destination head facing.
//			double targetPost = compass.getTargetHeading(startPost, degree);
			logger.info("V2 outside loop rotate {} degree at pwm {}\n",  degree , rotatePwm );
			if (degree > 0) { 
				leftMotor.forward();
				leftMotor.setSpeed(rotatePwm);
				rightMotor.reverse();
				rightMotor.setSpeed(rotatePwm);
			}
			else {
				leftMotor.reverse();
				leftMotor.setSpeed(rotatePwm);
				rightMotor.forward();
				rightMotor.setSpeed(rotatePwm);
			}
			Thread.sleep(100); // give motor time to spin to prevent drift of  magnetometer 
			while(true) {
				double nowPost = compass.readMagnetemeter(2, false);
				distTravel = this.trackAngle(degree, startPost, previousPost, nowPost, distTravel);
				logger.info("track rotate {} ,s {} , {} -> {} =t {} \n",degree, startPost, previousPost,nowPost, distTravel );
				previousPost = nowPost;
				if (distTravel > targetDistance) {
					logger.info("track rotate over {} > {}", distTravel , targetDistance);
					
					break;
				}
			}
//			compass.gyromagSleep();
//			Thread.sleep(200);
			leftMotor.setSpeed(currentSpeed);
			rightMotor.setSpeed(currentSpeed);
			return ok;
		}

		/* track total distance the car has turns.
		 * param direciton : 1 clockwise or -1 counter clockwise direction
		 * param prevPost and currentPost : 0 - 360
		 * 
		 */
		@Deprecated // moving this function to NavigationModule.java
		private double trackAngle(int direction, double startPost,   double prevPost, double curPost,double dist) {
			double ttl = 0;
//			if (Math.abs((prevPost-curPost)) <= 5 ) return dist; // ignore data for drifting
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
		
		
		public boolean stopTurn(int direction, double oldPost, double nowPost, double targetPost) {
			
			boolean stop = false;
			if (direction == 1) {
				if (targetPost < oldPost) {
					if (nowPost < oldPost && nowPost > targetPost ) stop = true;
				}
				else {
					if (nowPost > targetPost  ) stop = true;
				}
			}
			else {
				/// if car turns from 15 degree to 330 degree counter clockwise
				// condition = -1 
				// 
				if (targetPost < oldPost) {
					if (nowPost > oldPost || nowPost < targetPost ) stop = true;
				}
				else if (nowPost < targetPost && nowPost > oldPost ) stop = true;
			}
			
			return stop;
			
		}
		
		public void setDefaultSpeed() {
			System.out.println("set wheels to default speed "+ leftMotor.DEFAULT_PWM_SPEED);
			leftMotor.setSpeed(leftMotor.DEFAULT_PWM_SPEED);
			rightMotor.setSpeed(rightMotor.DEFAULT_PWM_SPEED);
		}
		
}