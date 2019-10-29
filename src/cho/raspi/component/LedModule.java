package cho.raspi.component;

import com.pi4j.io.gpio.GpioPinPwmOutput;
import cho.raspi.itf.LedItf;



/**
 * @author: Cheung Ho
 *
 */

public class LedModule implements LedItf{

	int pwmOn = 1000;
	
	int pwmOff = 0;
	
	GpioPinPwmOutput ledPin = null;
	
	boolean state = false;
	
	public LedModule(GpioPinPwmOutput ledPwm) {
		// TODO Auto-generated method stub
		ledPin = ledPwm;
	}

	// turn on light
	@Override
	public void turnOn() {
		// TODO Auto-generated method stub
		ledPin.setPwm(pwmOn);
		state = true;
		
	}

	// turn off light
	@Override
	public void turnOff() {
		// TODO Auto-generated method stub
		ledPin.setPwm(pwmOff);
		state = false;
	}

	
	@Override
	public boolean getState() {
		return this.state;
	}

}
