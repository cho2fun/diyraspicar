package cho.raspi.component;


import java.util.Deque;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import cho.raspi.helper.ThreadHelper;
import cho.raspi.itf.MediatorItf;
import cho.raspi.itf.ModuleItf;

/*********
 * 
 * @author CHo
 * 
 * this module detects and measures object distance within predefined ranges in meters.
 * 
 * 		detect() manually object detection. 
 * 
 * Requirement:
 * 		trigger pin - GPIO pin with PWM builtin.
 * 		echo pin - any GPIO pin .
 *
 */


public class UltraSonicSensorModule implements ModuleItf {

	private static float SOUND_SPEED =  340.29f; // sound spped meter/second
	private static float SOUND_SPEED_CM =  0.034f; // sound spped centimeter/microsecond
	private static int TRIGGER_DURATION = 100; // duration of triggering signal in micro-seconds
	private static long WAIT_TIMEOUT = 1_000_000_000; // nano second

	private static GpioController gpio = null;
	
	public GpioPinDigitalOutput triggerSensorPin = null;
	public GpioPinDigitalInput echoSensorPin = null;
	//	public double distance = 0;
	public double objectDistanceMax = 5; //in meters;
	public double objectDistanceMin = 0; // in meters;
	public long testStartTime = -1;
	public boolean testInProgress = false;
	public double objectDistance = 0;
	public int objectDistanceThreshold = 0; // in inches
	public long lastUpdate = 0l;

	private Deque<String> que = null;
	
	private MediatorItf mediator = null;
	
	

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		UltraSonicSensorModule setup = new UltraSonicSensorModule(RaspiPin.GPIO_04,RaspiPin.GPIO_06 ) ;

		GpioPinDigitalOutput _triggerSensorPin = null;
		GpioPinDigitalInput _echoSensorPin = null;

		_triggerSensorPin = setup.triggerSensorPin;
		_echoSensorPin = setup.echoSensorPin;
		
		int lap = 0;
		while(true) {
			try {
//				try {
					System.out.printf("     multithread Lap %d\n",lap++);
//					UltraSonicSensorModule sensor = new UltraSonicSensorModule(_triggerSensorPin,_echoSensorPin ) ;
//					sensor.addListener();
//					sensor.detectObjectByListener();

					ThreadHelper helpThread = new ThreadHelper("sonic ", false);
					helpThread.setModuleInterface((ModuleItf)setup);
					
					helpThread.setPriority(helpThread.MAX_PRIORITY);
					helpThread.start();
					helpThread.join(1000);
					
//				}
//				catch (InterruptedException e) {
//					System.out.println("InterruptedException " + e);
//				}
				System.out.println("sleep for 5 sec thread count " + Thread.activeCount());
				TimeUnit.SECONDS.sleep(5);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	
	public UltraSonicSensorModule ( Pin _triggerPin, Pin _echoPin) {
		this (null, _triggerPin, _echoPin);
	}

	@Deprecated
	public UltraSonicSensorModule (GpioController _gpio, Pin _triggerPin, Pin _echoPin, Deque _deque) {
		this (_gpio, _triggerPin, _echoPin);
	}

	public UltraSonicSensorModule (GpioController _gpio, Pin _triggerPin, Pin _echoPin, MediatorItf mediator) {
		this (_gpio, _triggerPin, _echoPin);
		this.mediator = mediator;
	}
	
	
	public UltraSonicSensorModule (GpioController _gpio, Pin _triggerPin, Pin _echoPin) {
		if (_gpio != null ) gpio = _gpio;
		else if (gpio == null ) gpio = GpioFactory.getInstance();
		
		
		System.out.println("UltraSonicSensorModule gpio : "+ gpio.toString());

//		Locale currentLocale = new Locale("us", "EN");
		ResourceBundle appConfig = ResourceBundle.getBundle("application");
		this.objectDistanceThreshold = Integer.parseInt(appConfig.getString("objectDistanceThreshold"));
		System.out.println("default objectDistanceThreshold =" + objectDistanceThreshold);

//		System.out.println(this.getName() +  " setup");
		triggerSensorPin = gpio.provisionDigitalOutputPin(_triggerPin,"sensorTrigger ",PinState.LOW ); // assign trigger output pin.
		triggerSensorPin.setShutdownOptions(true,PinState.LOW);
		echoSensorPin = gpio.provisionDigitalInputPin(_echoPin, PinPullResistance.PULL_DOWN); // assign echo input Pin.class
		System.out.println("pins assigned and statuses ");
		System.out.printf("sensor %s, %s, %s \n",triggerSensorPin.getName(), triggerSensorPin.getPin(),triggerSensorPin.getState() );
		System.out.printf("sensor %s, %s, %s \n",echoSensorPin.getName(), echoSensorPin.getPin(),echoSensorPin.getState() );


	}

	

	public void execute( )  {
		//		System.out.println(this.getName() + " UltraSound Sensor Thread testing started. - max priority");
//		while(true) {
			try {
				triggerSensorPin.low(); // Make trigger pin low and give it 500 milisecs to calm down
				TimeUnit.MILLISECONDS.sleep(500);
				while(echoSensorPin.isHigh()){ //Wait until the ECHO pin gets HIGH
				}
//				System.out.println("triggered sensor");
				triggerSensorPin.high(); // Make trigger pin HIGH
				Thread.sleep((long) 0.01);// Delay for 10 microseconds
				// this method does not work				
				//			TimeUnit.MICROSECONDS.sleep(10);
				triggerSensorPin.low(); //Make trigger pin LOW	
				while(echoSensorPin.isLow()){ //Wait until the ECHO pin gets HIGH
				}
				long startTime= System.nanoTime(); // Store the current time to calculate ECHO pin HIGH time.
				//				long timeout = startTime + this.TIMEOUT_NANO;
				while(echoSensorPin.isHigh() ){ //Wait until the ECHO pin gets LOW
				}
				long endTime= System.nanoTime(); // Store the echo pin HIGH end time to calculate ECHO pin HIGH time.
				this.objectDistance = calculateObjectDistance(startTime, endTime);
				System.out.printf( "  dist %5.5fcm  %5.5f inch\n",  objectDistance , distanceInInch(objectDistance)  );
/* temporary turn off for testing moving this logic to ObservatoryModule
				if (distanceInInch(objectDistance) < objectDistanceThreshold) {
					this.mediator.receiveMessage(MessageEnum.ALERT, "ALERT");
/*					synchronized(que) {
					if (que.size() < 1 ) que.add("Object < "+ distanceInInch(objectDistance) );
					}
					
				}
				else {
					this.mediator.receiveMessage(MessageEnum.CLEAR, "Clear");
				}
				
				Thread.sleep(1000);
*/				
			}
			catch (InterruptedException e) {
				System.out.println("thread is interrupted and now exiting");
//				break;
			}
			catch (Exception ex) {
				System.out.println(this.getClass().getName()  +" Exception " + ex);
			}
			
//		}
	}


	// due to delay in triggering this listener, this method is not currently working
	@Deprecated
	public void addListener() {
		System.out.println("attaching listener..");
		// register listener for the pin.
		echoSensorPin.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				System.out.printf("Event %s\n", event.getState());
				if (testInProgress) {
					if(event.getState().isHigh()) {
						testStartTime = System.nanoTime();
						//						System.out.printf("Event strTime %d\n", testStartTime);
					} else
						if(event.getState().isLow()) {
							long endTime = System.nanoTime();
							double distance = calculateObjectDistance(testStartTime  , endTime);
							System.out.printf("Event strTime %d\n", testStartTime);
							System.out.printf("Event endTime %d\n", endTime);
							System.out.printf("event %s dist %5.5f cm, time %d\n", event.getState(), distance,  (endTime -testStartTime ));
							System.out.println("Distance :"+((((endTime-testStartTime)/1e3)/2) / 29.1) +" cm"); //Printing out the distance in cm  
							testStartTime= 0l;
							testInProgress = false;
						}
				}
			}

		}
				);

	}


	//*************************************
	//run thread is working. WHY NOT THIS METHOD WORK?
	//*************************************
	public double detectObject( )  {
		double distance = 0;
		System.out.println("UltraSonic Sensor started.");

		//			while(true) {
		try {
			while(echoSensorPin.isHigh()){ //Wait until the ECHO pin gets HIGH
			}
			//			System.out.println("echo sensor state is " + echoSensorPin.getState());
			//					this.sleep(500);
			triggerSensorPin.low(); // Make trigger pin low and give it 500 milisecs to calm down
			TimeUnit.MILLISECONDS.sleep(500);
			//			System.out.println("trigger sensor set to High");
			triggerSensorPin.high(); // Make trigger pin HIGH
			Thread.sleep((long) 0.01);// Delay for 10 microseconds
			//			TimeUnit.MICROSECONDS.sleep(10);
			triggerSensorPin.low(); //Make trigger pin LOW	
			//					long timeout = startTime + this.TIMEOUT_NANO;
			while(echoSensorPin.isLow()){ //Wait until the ECHO pin gets HIGH
			}
			long startTime= System.nanoTime(); // Store the surrent time to calculate ECHO pin HIGH time.
			while(echoSensorPin.isHigh() ){ //Wait until the ECHO pin gets LOW
			}
			long endTime= System.nanoTime(); // Store the echo pin HIGH end time to calculate ECHO pin HIGH time.
			if (endTime < startTime )  throw new Exception("timeout!");
			else {
				//						long endTime = System.nanoTime();
				this.objectDistance = calculateObjectDistance(startTime, endTime);
				System.out.printf("detectObject dist %5.5fcm  %5.5finch\n",  objectDistance , distanceInInch(objectDistance)  );
				//						sendAlertMessage();
			}
		}
		catch (Exception ex) {
			System.out.println("Exception " + ex);
		}
		//			}
		// forcefully shutdown all GPIO thread and controller

		return this.objectDistance;
	}





	@Deprecated
	public void detectObjectByListener( )  {


		//		while(true) {
		try {
			testInProgress = false;
			System.out.println("UltraSonic Sensor with Listener started.");
			Thread.sleep(1000);
			triggerSensorPin.low(); 
			while (!echoSensorPin.isLow()) { }
			Thread.sleep(0,2000);  // delay for 2microseconds
			testInProgress = true;
			triggerSensorPin.high(); // emit signal
//			Thread.sleep(0, 10000);  // set trigger for 10000 nano second / 10 microsecond
			Thread.sleep((long) .10);
			triggerSensorPin.low(); // stop emitting
			
			while (testInProgress)  {};
			System.out.println("----END");
		}
		catch (Exception ex) {
			System.out.println("Exception " + ex);
		}
		//		}
		// forcefully shutdown all GPIO thread and controller


	}

	


	// return calculate object distance and return distance in  CM 

	// distance = time (microsecond) * 0.034 (cm/microseconds) / 2; including sound wave travels forward and back from nearest object.
	// distance = time (second ) * 343 (meters/second) / 2 ; 

	/// time unit is nanoseconds
	double calculateObjectDistance(long startTime, long endTime) {
		//		long duration = TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
		if (endTime < startTime) {
			System.out.printf("stt %d\n", startTime);
			System.out.printf("end %d\n",  endTime);
			System.out.printf("dif %d\n", endTime-startTime);
			return -1;
		}
		double dist = 0;
		long duration =  (endTime - startTime);
		dist = duration * SOUND_SPEED_CM / ( 2 * 1000 ) ;
//		System.out.printf("dist : %5.5f cm\n", dist);
		return dist;
	}



	public double distanceInInch(double dist) {
		return dist  * 0.393701 ;
	}

	public double distanceInInch() {
		return distanceInInch(objectDistance) ;
	}


	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void unpause() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isPaused() {
		// TODO Auto-generated method stub
		return false;
	}

}
