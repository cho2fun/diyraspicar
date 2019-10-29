package cho.raspi.training;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

import cho.raspi.helper.ThreadHelper;
import cho.raspi.component.BumperSensor;
import cho.raspi.component.MPU9250_GyroMagnet;
import cho.raspi.component.ObservatoryModule;
import cho.raspi.itf.MediatorItf;
import cho.raspi.map.MessageEnum;
import cho.raspi.itf.ModuleItf;

public class ExploreSurrounding implements MediatorItf {

	final public static GpioController gpio = GpioFactory.getInstance();



	private NavigationModule navigation = null;
	private ObservatoryModule observator = null;
	private MPU9250_GyroMagnet compass = null;
	private BumperSensor bumper = null;

	private ThreadHelper observatorThread=null;

	private volatile MessageEnum  messageStatus= null; 


	Logger logger = LoggerFactory.getLogger(ExploreSurrounding.class);



	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExploreSurrounding controller = new ExploreSurrounding();
		if (args.length < 1) {
			System.out.println("include parameter:");
			System.out.printf("M-compass {loop}");
			System.out.printf("U-narrowsweep");
			System.out.printf("T-rotating car");
			System.out.printf("E-explore");
		}
		
		if ("M".equals(args[0])) {
			int lap = 1;
			if (args.length == 2) lap = Integer.parseInt(args[1]);
			try {
				for (int i = 0;i< 100 ; i++) {
					double heading = controller.compass.readMagnetemeter(lap, true);
					System.out.printf("%d. %5.2f \n", i, heading);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if ("S".equals(args[0])) {
			 int angle = controller.runSurvey();
			 System.out.println("result angle "+ angle)
;		}
		else if ("U".equals(args[0])) {
			controller.observator.narrowSweep();
		}
		else if ("T".equals(args[0])) {
			int angle = 0;
			try {
				if (args.length == 2) angle = Integer.parseInt(args[1]);
				for (int i = 0 ; i< 4; i++ ) {
					controller.navigation.steering.rotate(angle, controller.compass);
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//			controller.caution(70);
		}
		else {
			controller.startControlRoom();
		}

	}

	public ExploreSurrounding() {
		super();
		logger.info("multi threading testing using motor,magnetometer.");

		System.out.println("multiple module Threading . ");


		observator = new ObservatoryModule(gpio,this );
		System.out.println("Observatory Module configured. ");
		System.out.println("*********************************************" + observator.getClass().getSimpleName() );

		navigation = new NavigationModule(gpio);
		System.out.println("Navigation Module configured. ");
		//		System.out.println("wheels  are starting. ");
		//		wheel.start();

		//		List<Double> data =  observator.survey();
		//		int divert = observator.analyzeSurvey(data);
		//		System.out.printf (" Detour %d \n", divert);

		compass = new MPU9250_GyroMagnet();

		double orientation = 0.0;
		try {
			orientation = compass.readMagnetemeter(5, false);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		System.out.printf("Compass orientation heading  %8.4f \n", orientation);
		
		
		this.bumper = new BumperSensor(this, RaspiPin.GPIO_29);
		
	}



	// this method controls acts as central control of modules on the car.
	public void startControlRoom() {
		logger.info("Field Test ****** "  );
		System.out.println(" control Room in running.");
		System.out.println(" ObservationModule is in thread.");


		//		this.observator
		observatorThread = new ThreadHelper("obs ", true);
		observatorThread.setModuleInterface((ModuleItf)observator);
		observatorThread.start();

		navigation.start();


		/*		
		try {
//			navigation.runRotatingWheel(compass);
			System.out.println("preturn angle " + degree);
//			double startPost = compass.readMagnetMeasurement(degree, true);
//			System.out.println("magnetometer heading" + startPost);


 			navigation.runReverseCircle(compass, degree);
//			navigation.steering.rotate(degree, compass);


		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 */		
		//		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		//		executorService.scheduleWithFixedDelay(observatorThread, 1000, 1000, TimeUnit.MILLISECONDS);
		//		navigation.start();


	}

	private int runSurvey() {
		int degree = 0;
		List<Double> surveyData = new ArrayList<>();
		surveyData = this.observator.survey();
		degree = observator.getDirection(surveyData);
		//		degree = observator.analyzeSurvey(surveyData);
		return degree;
	}


	@Override
	public void sendMessage(MessageEnum type, String msg) {
		// TODO Auto-generated method stub
		System.out.println("Car Mediatator sent message "+ msg);
	}

	@Override
	public void receiveMessage(MessageEnum type, String msg) {
		// TODO Auto-generated method stub
		logger.info("Car Mediatator received " + msg);

		if (type != messageStatus) {
			messageStatus = type;
			if (type == MessageEnum.ALERT) {

				this.executeDetour();
				messageStatus = null;
			}
			else if (type == MessageEnum.WARN ) {
				try {
					int angle = Integer.parseInt(msg);				
					this.caution(angle);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				messageStatus = null;
			}
			else if (type == MessageEnum.CLEAR) {
				if(navigation.isPaused()) navigation.unpause();
				if(observatorThread.isPaused()) observatorThread.unpause();
			}
		}
		else {
			logger.info("IGNORED! Car Mediatator received message " + msg);
		}
	}

	public void executeDetour() {
		// pause modules
		System.out.println("CarMediatorModule executeDetour()");
		navigation.pause();
		observatorThread.pause();

		// 2 step - do survey and turn to direction based on analysis of survey data.
		int detour = this.runSurvey();	
		try {
			System.out.println("detour at " + detour);
			navigation.runReverseCircle(compass, detour);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// unpause modules
		observatorThread.unpause();
	}


	public void caution(int objAngle) throws InterruptedException {
		navigation.pause();
		observatorThread.pause();
		int direction = 0;
		int maxInd = -1;
		double max = 0;
		System.out.println("caution steer away " + objAngle);
		for ( int i = 0;i< observator.narrowSweepAngles.length;i++) {
			// determine direction of turn based highest number from narrow sweep data
			if (observator.sweepData[i] > max) {
				System.out.println(i+ " - " + observator.sweepData[i]);
				max = observator.sweepData[i];
				maxInd = i;
			}
			//			if (observator.narrowSweepAngles[i] == objAngle) {
			//				direction = (i>=2? observator.narrowSweepAngles[i-2]:observator.narrowSweepAngles[i+2]);
			//			}
		}
		direction = observator.narrowSweepAngles[maxInd];
		//		it is difficult to measure angle of turn as readings from amgnet is unstable.
		//		work around is to use rotate method		
				System.out.println("steer away done runCircle " + objAngle);
				navigation.runCircle(compass,  direction );
		//		System.out.println("steer away done via runReverseCircle " + objAngle);
		//		navigation.runReverseCircle(compass, direction);// steering.rotate(direction, compass);
		//		System.out.println("caution steer away done via rotate " + direction);
		//		navigation.steering.rotate(direction, compass);
		//		navigation.unpause();
		System.out.println("observator unpausing");
		observatorThread.unpause();
	}
}
