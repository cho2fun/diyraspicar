package cho.raspi.component;

import java.util.ResourceBundle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.util.CommandArgumentParser;

/*
 * @Author: Cheung Ho
 */


public class DCMotorModule {

	protected GpioPinDigitalOutput forwardOutPin = null;
	protected GpioPinDigitalOutput reverseOutPin = null;
	protected GpioPinPwmOutput  motorSpeedPWMPin = null;
	public int DEFAULT_PWM_SPEED = 400;
	public int SLOW_PWM_SPEED = 200;
	

	// initialize motor module
	public DCMotorModule(GpioController gpio, Pin speedPWMPin, Pin forwardPin, Pin reversePin ) {

//		Locale currentLocale = new Locale("us", "EN");
		ResourceBundle appConfig = ResourceBundle.getBundle("application");
		DEFAULT_PWM_SPEED = Integer.parseInt(appConfig.getString("Wheel_Default_PWM_Speed"));
		System.out.println("default Wheels DEFAULT_PWM_SPEED =" + DEFAULT_PWM_SPEED);
		SLOW_PWM_SPEED = Integer.parseInt(appConfig.getString("Wheel_slow_PWM_Speed"));
		System.out.println("default Wheels SLOW_PWM_SPEED =" + SLOW_PWM_SPEED);

		forwardOutPin = gpio.provisionDigitalOutputPin(forwardPin, "turn forward trigger channel 21", PinState.LOW);
		forwardOutPin.setShutdownOptions(true, PinState.LOW);

		reverseOutPin = gpio.provisionDigitalOutputPin(reversePin, "turn backward trigger channel 22", PinState.LOW);
		reverseOutPin.setShutdownOptions(true, PinState.LOW);

		Pin PMWpin = CommandArgumentParser.getPin(RaspiPin.class, speedPWMPin);	
		// set shutdown state for this pin
		motorSpeedPWMPin = gpio.provisionPwmOutputPin(PMWpin);
		com.pi4j.wiringpi.Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS);
		com.pi4j.wiringpi.Gpio.pwmSetRange(1000);
		com.pi4j.wiringpi.Gpio.pwmSetClock(500);
		motorSpeedPWMPin.setPwm(DEFAULT_PWM_SPEED);
		
	}
	
	

	public boolean forward() {
//		System.out.println("DC motor forward method"  );

		boolean ok = true;
		if (motorSpeedPWMPin.getPwm() < DEFAULT_PWM_SPEED) this.setSpeed(DEFAULT_PWM_SPEED);
		if(reverseOutPin.isHigh()) reverseOutPin.low();
		if(forwardOutPin.isLow()) forwardOutPin.high();
//		System.out.println(toString());
		return ok ;
	}
	
	
	public boolean reverse() {
		boolean ok = true;
//		if (motorSpeedPWMPin.getPwm()< 300) this.setSpeed(DEFAULT_PWM_SPEED);
		if(forwardOutPin.isHigh()) forwardOutPin.low();
		if(reverseOutPin.isLow()) reverseOutPin.high();
		return ok ;
	}

	
	public boolean stop () {
		boolean ok = true;
		if(forwardOutPin.isHigh()) forwardOutPin.low();
		if(reverseOutPin.isHigh()) reverseOutPin.low();
//		motorSpeedPWMPin.setPwm(50);
		return ok;
		
	}

	
	
	/*
	* power in term of Pwm ranging frm 250 up to 1000 
	*/		
	public boolean setSpeed(int speed) {
		
		boolean ok = true;
		if (speed > 1000) speed = 1000;
		else if (speed < 0 ) speed = 50;
		motorSpeedPWMPin.setPwm(speed);
		return ok;
	}
	
	public int getSpeed() {
		return motorSpeedPWMPin.getPwm();
	}
	
	

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Pwm: "+motorSpeedPWMPin.getPwm());
		buf.append(" forPin: "+ this.forwardOutPin.getState());
		buf.append(" revPin: "+ this.reverseOutPin.getState());
		
		return buf.toString();		
	}
	

		
	
	
	
	
}



