package cho.raspi.component;

import java.util.concurrent.Callable;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;

import cho.raspi.helper.ThreadHelper;
import cho.raspi.itf.MediatorItf;
import cho.raspi.itf.ModuleItf;
import cho.raspi.map.MessageEnum;

public class BumperSensor implements ModuleItf, MediatorItf{
	private boolean touched = false;

	public GpioController gpio = GpioFactory.getInstance();;

	public GpioPinDigitalInput switchPin = null;
	
	private MediatorItf mediator = null;

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		GPIOSwitch sw = new GPIOSwitch();
//			sw.start();
	}

	public void addListener () {
		
		// create and register gpio pin listener
		switchPin.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				// display motion pin state on console
				if (event.getState().isHigh()) {
					System.out.println("event current "+ touched  + "  " +  event.getPin() + " is High ");
					if (!touched) {
						System.out.println("BumperSensor triggered alert ");
						mediator.receiveMessage(MessageEnum.HALERT, "Bumper "+ MessageEnum.HALERT.getAction());
					}
				}
				if (event.getState().isLow()) {
					System.out.println("event " +  event.getPin() + " is low ");
					touched = false;
				}
			}
		});
		
		
	}


	public void addTrigger(MediatorItf _mediator, Pin pin) {
		switchPin.addTrigger(new GpioCallbackTrigger(new Callable<Void>() {
            public Void call() throws Exception {
                System.out.println(" --> GPIO TRIGGER CALLBACK RECEIVED ");
                return null;
            }
        }));
	}
	
	public BumperSensor(MediatorItf _mediator, Pin pin ) {
		//RaspiPin.GPIO_29
		this.mediator = _mediator;
		switchPin = gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_DOWN); 
        // set shutdown state for this pin

        this.addListener();
		
	} 	

/*	
	public void run() {
		System.out.println("thread version");
		while(true) {
			try {
				this.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
*/		
/*		try {
			this.testSwitch();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
//	}
	
	public void testSwitch() throws InterruptedException {
		boolean ok = true;
		
		
		while(ok) {
			if(switchPin.isHigh()) { System.out.println(switchPin.getName() + " is turned on !");
			break;
			}
			if(switchPin.isLow()) System.out.println(switchPin.getName() + " is turned off ");
			Thread.sleep(1000);
			
		}
		
		
		
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
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

	@Override
	public void sendMessage(MessageEnum type, String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveMessage(MessageEnum type, String msg) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
