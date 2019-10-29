package cho.raspi.training;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

import cho.raspi.helper.ThreadHelper;
import cho.raspi.component.ObservatoryModule;
import cho.raspi.component.UltraSonicSensorModule;
import cho.raspi.component.MPU9250_GyroMagnet;
import cho.raspi.itf.ModuleItf;

public class RotationalTraining  {

	final public static GpioController gpio = GpioFactory.getInstance();
	
	private Deque<String> deque = new LinkedList<>();
	
//	private HashMap<String, Object> moduleMap = new HashMap<>();  

	private NavigationModule navigation = null;
	private UltraSonicSensorModule ultraSonicSensor = null;
	private ObservatoryModule observator = null;
	private MPU9250_GyroMagnet compass = null;
    Logger logger = LoggerFactory.getLogger(RotationalTraining.class);

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		RotationalTraining controller = new RotationalTraining();
		controller.startControlRoom();
//		Thread thread = new Thread(runnable);
//		thread.setPriority(1);
//		thread.start();
	}

	public RotationalTraining() {
		super();
		logger.info("multi threading testing using motor,magnetometer.");
		System.out.println("multiple module Threading . ");

		ultraSonicSensor = new UltraSonicSensorModule(gpio,RaspiPin.GPIO_06,RaspiPin.GPIO_10, deque) ;
		System.out.println("ultraSonic module configured. ");

		navigation = new NavigationModule(gpio);
		System.out.println("Navigation Module configured. ");
//		System.out.println("wheels  are starting. ");
//		wheel.start();
		

		observator = new ObservatoryModule(gpio);
		System.out.println("Observatory Module configured. ");
		System.out.println("*********************************************" + observator.getClass().getSimpleName() );
		observator.registerUltraSonicSensor(ultraSonicSensor);
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
	}

	// this method controls acts as central control of modules on the car.
	public void startControlRoom() {
		System.out.println(" control Room in running.");
		
		ThreadHelper helpThread = new ThreadHelper("sonic ", false);
		helpThread.setModuleInterface((ModuleItf)ultraSonicSensor);
//		compass.start();
		
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleWithFixedDelay(helpThread, 1000, 1000, TimeUnit.MILLISECONDS);
//		executorService.scheduleWithFixedDelay(compass, 1000, 1000, TimeUnit.MILLISECONDS);
//		navigation.start();
		
		try {
			navigation.runRotatingWheel(compass);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		

		
	}
	

	private int runSurvey() {
		int degree = 0;
		List<Double> surveyData = new ArrayList<>();
		surveyData = this.observator.survey();
		degree = observator.analyzeSurvey(surveyData);
		return degree;
	}

	
}
