package cho.raspi.component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.RaspiPin;

import cho.raspi.helper.ThreadHelper;
import cho.raspi.itf.MediatorItf;
import cho.raspi.itf.ModuleItf;
import cho.raspi.map.MessageEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObservatoryModule  implements ModuleItf {

	public static GpioController gpio = null;
	//GpioFactory.getInstance();
	ServoOperationModule hServo = null;
	ServoOperationModule vServo = null;
	PCA9685ServoDriver servoDriver = null;
	LedModule ledModule = null;
	UltraSonicSensorModule ultraSonicSensor = null;
	static Logger logger = LoggerFactory.getLogger(ObservatoryModule.class);

    private volatile boolean paused = false;

    private int clearCount = 0;
    
	private MediatorItf mediator = null;
    
	public final int[] narrowSweepPosts = {1040,1220,1580,1760};  // from right to left
	public final int[] narrowSweepAngles = {70,30,-30,-70};  
	public double[] sweepData = {0,0,0,0 }; 
	//	experiment class controlling both individual servomotor and ultrasonic

	static public void main(String[] args) {

		
		
		int iteration = 0;
		if (Integer.parseInt(args[0]) > 0 ) {
			iteration = Integer.parseInt(args[0]);
		} else {
			System.out.println("missing parameter interation!  ");
		};
		gpio = GpioFactory.getInstance();
		ObservatoryModule observator = new ObservatoryModule(gpio);
		System.out.println("*********************************************" + observator.getClass().getSimpleName() );
		List<Double> data =  observator.survey();
		int divert = observator.getDirection(data);
		System.out.printf (" Detour %d \n", divert);
	}
	
		
	public ObservatoryModule(GpioController gpio,MediatorItf mediator ) {
		this.mediator = mediator;
		configureServo(gpio);
	}
	
	
	public ObservatoryModule(GpioController gpio) {
				configureServo(gpio);
	}

	public void configureServo(GpioController gpio) {
		try {
			System.out.println("Configuration starts");
			servoDriver = new PCA9685ServoDriver(gpio);
			GpioPinPwmOutput horizontalServoPwm = servoDriver.registerPwmPin(0);
			hServo  = new ServoOperationModule(horizontalServoPwm,"horiz",500,2300);
			hServo.move(hServo.center);
			System.out.println("center " + hServo.center);

			GpioPinPwmOutput verticalServoPwm = servoDriver.registerPwmPin(1);
			vServo  = new ServoOperationModule(verticalServoPwm,"vert",700,1700);
//			stopServos();

			this.faceForward();
			System.out.println("configuration complete.");

			System.out.println("ultraSonic sensor starts");
			ultraSonicSensor = new UltraSonicSensorModule(gpio, RaspiPin.GPIO_06,RaspiPin.GPIO_10, mediator) ;
/*
			GpioPinPwmOutput ledPwm = servoDriver.registerPwmPin(4);
			ledModule  = new LedModule(ledPwm);
			// test led
			System.out.println("LED is turned on.");
			ledModule.turnOn();
			Thread.sleep(1000);
//			ledModule.turnOff();

			servoDriver.stop(ledPwm.getPin());
			System.out.println("LED is off.");
*/			
			System.out.printf("Servo Motor S90 dual motor  via driver test started on  \n ");
			System.out.printf("horizontal: %s  \n ", horizontalServoPwm.getPin());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Deprecated
	public void registerUltraSonicSensor(UltraSonicSensorModule _ultraSonicSensor ) {
		ultraSonicSensor = _ultraSonicSensor;
	}
	
	public void narrowSweep() {
		int angleperunit = 25;
		try {
			int m = 0;
			while(true) {
				double objectDistance = 0;
				int i = 0;
				m++;
				int direction = 1;
				while (i >=0 && i <narrowSweepPosts.length) {
					hServo.moveTo(narrowSweepPosts[i]);
					System.out.printf("sweep Lap  %d-%d   pos: %d    %d\n",  m,i, narrowSweepPosts[i] , direction);
					TimeUnit.MILLISECONDS.sleep(200);
					try {
						ThreadHelper helpThread = new ThreadHelper( ("sonic"+m+"-"+i),false);
						helpThread.setModuleInterface((ModuleItf)ultraSonicSensor);				
						helpThread.setPriority(helpThread.MAX_PRIORITY);
						helpThread.start();
						helpThread.join(600);
						objectDistance = ultraSonicSensor.objectDistance;
						logger.info("sweep Lap  {}-{}  object at {} cm\n",  m,i,  objectDistance);
						sweepData[i] = objectDistance ;
						if (objectDistance < ultraSonicSensor.objectDistanceThreshold) {
							clearCount = 0;
							logger.info("sweep Lap  {}-{}  found {} cm\n",  m,i,  objectDistance);
							synchronized(this) {
								this.mediator.receiveMessage(MessageEnum.ALERT, "Observ "+ MessageEnum.ALERT.getAction());
								throw new Exception("object found in proximity.");
							}
						}
						else if (objectDistance < (ultraSonicSensor.objectDistanceThreshold *1.8)) {
							// calculate direction 
							logger.info("sweep Lap  {}-{}  WARN {} cm   angle {}\n",  m,i,  objectDistance , narrowSweepAngles[i]);
							this.mediator.receiveMessage(MessageEnum.WARN, ""+ narrowSweepAngles[i] );
						}
						else if(++clearCount > 4){
							clearCount = 0;
							synchronized(this) {
								this.mediator.receiveMessage(MessageEnum.CLEAR, MessageEnum.CLEAR.getAction());
							}
						}
						if(helpThread.getState() == Thread.State.RUNNABLE) {
							System.out.printf("%d with thread interrupt status %s\n",i, helpThread.getState());
							helpThread.interrupt();
						}
					} catch (InterruptedException e) {
						System.out.println("interrupted "+e);
					}
					if (i == (narrowSweepPosts.length-1)) direction = -1;
					else if (i == 0) direction = 1;
					i = i + direction;
				}
			}
//			this.faceForward();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("ObservatoryModule exception "+e);
		}
	}
	
	
	public List<Double> survey() {
		List<Double> survey = new ArrayList<>();
		try {
			hServo.moveTo(hServo.min);
			hServo.step = 180;
			double objectDistance = 0;
			int i = 0;
			while(hServo.position < (hServo.max-hServo.step)) {
				System.out.printf("* Lap  %d \n", ++i);
				hServo.sweep();
				TimeUnit.MILLISECONDS.sleep(200);
				try {
					ThreadHelper helpThread = new ThreadHelper("sonic ", false);
					helpThread.setModuleInterface((ModuleItf)ultraSonicSensor);				
					helpThread.setPriority(ThreadHelper.MAX_PRIORITY);
					helpThread.start();
					helpThread.join(600);

					objectDistance = ultraSonicSensor.objectDistance;
					logger.info(i+ " survey "+ objectDistance);
					survey.add(objectDistance);
					if(helpThread.getState() == Thread.State.RUNNABLE) {
						System.out.printf("%d with thread interrupt status %s\n",i, helpThread.getState());
						helpThread.interrupt();
					}
				} catch (InterruptedException e) {
					System.out.println("interrupted "+e);
				}

//				logger.info("stat {} {} {} cm. status {}\n",i,hMotor.position, objectDistance, sensor.getState());
//				System.out.printf("stat %d %d %5.5f cm. status %s\n",i,hMotor.position, objectDistance, sensor.getState());
//				TimeUnit.SECONDS.sleep(4);
			}
			this.faceForward();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {		
			//			gpio.shutdown();
		}
		return survey;
	}

	// look for obstacle on the way and return distance for nearest object 
	public double scanObject() {
		double distance = 0.0;
		return distance;

	}
	
	
	// return degrees from center negative number = no object on left
	// positive = no object on right.
	public double avoidObject() {
		double distance = 0.0;
		return distance;
		
		
		
	}
	
	// this method move the servo to face forward 
	public boolean faceForward() {
		int verticalPwm = 1700;
// to be revisit		int horizontalPwm = (hServo.max - hServo.min)/2 + hServo.min;
		int horizontalPwm = 1300;
		try {
			this.vServo.moveTo(verticalPwm);
			this.hServo.moveTo(horizontalPwm);
//			this.stopServos();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}

	
	public void stopServos() {
		servoDriver.stop(hServo.pwm.getPin(),vServo.pwm.getPin());
	}

	// this method analyze data and return angle with empty spots.
	// angle return is 0 = left, 90 = center, 180 = right
	public int getDirection(List<Double> rawData) {
		int threshold = 30;
		int degree = 0;
		int[] act2 = new int[rawData.size()];
//		new ArrayList<>(rawData.size());
		for (int i = 0;i< rawData.size(); i ++) {
			if (threshold < rawData.get(i)) {
				act2[i] ++;
				if ( (threshold*3) <  rawData.get(i) ) act2[i] ++;
				if (i> 0 && threshold < rawData.get(i-1)) {
					act2[i]++;
				}
				if (i < (rawData.size()-1) && threshold < rawData.get(i+1)) {
					act2[i]++;
				}
			}
			else {
				act2[i]= 0;
			}
		}
		if(act2.length == 9) {
			int i = act2.length;
			System.out.printf(" %5d  %5d  %5d  %5d  L  %5d  R  %5d  %5d  %5d  %5d  \n",
				act2[--i],act2[--i],act2[--i],act2[--i],act2[--i],act2[--i],act2[--i],act2[--i],act2[--i]);
		}
		// level 2 look for score of 3
		
		int ind = -1; int cnt = 0;
		int prevind = 0; // starting point of open area
		int prevcnt = 0;  // width of open area.
		for (int n = 3; n> 0;n--) {
			for (int m = 0;m< act2.length; m ++) {
				if (act2[m] >=n) {
					if(ind == -1) {
						ind = m;
						cnt = 0;
					}
					cnt ++;
				}
				else {
					if (cnt > prevcnt) {
						prevind = ind;
						prevcnt = cnt;
					}
					cnt =0;
					ind = -1;
				}
			}
			if (cnt > prevcnt) {
				prevind = ind;
				prevcnt = cnt;
				break;
			}
			ind = -1;
			cnt = 0;
		}

		double indP = 180 / (act2.length-1);
		double sp = prevind *indP ;
		double ep= (prevind + prevcnt ) * indP;
		System.out.printf("sp %3.2f to ep %3.2f \n", sp, ep);
		long midPoint = Math.round(((ep-sp)/2) + sp); 
		degree = (int) (midPoint>90 ? -1*( midPoint - 90) :90-midPoint);
		logger.info("start pt {} len {}  | mid pts {} -> {}  ",prevind,prevcnt,midPoint, degree);
		
		return degree;
	}

	
	// analyze surrounding objects and return direction ( degree) of open area 
	@Deprecated
	public int analyzeSurvey(List<Double> rawData) {
		int degree = 0;
		int mid = rawData.size()/2;
		int threshold = 24;
		int rightCnt = 0; 
		double rightSum = 0;
		int leftCnt = 0; 
		double leftSum = 0;
		
		for (int i = 0;i<rawData.size();++i) {
			double data = (double) rawData.get(i);
			if (data > threshold) {
				if (i < mid ) {
					rightCnt ++;
					rightSum += data;
				}
				else {
					leftCnt ++;
					leftSum += data;
				}
			}
		}
		logger.info("analyzeSurvey()  left c: {} s: {}   Right c: {}  s: {} ", leftCnt,leftSum,rightCnt, rightSum); 
		if (rightCnt > leftCnt || rightSum > leftSum) {
			degree = 90;
		}
		else {
			
			degree = -90;
		}
		
		return degree;
	}

	@Override
	public void pause() {
		System.out.println("ObservatoryModule received message");
		this.paused = true;
	}

	@Override
	public void unpause() {
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


	@Override
	public void execute() {
		// TODO Auto-generated method stub
		System.out.println("Observa - execute()");
		narrowSweep(); 
		System.out.println("Observa - execute() done");
	}

	
}
