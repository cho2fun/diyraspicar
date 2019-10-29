package cho.raspi.component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.pi4j.gpio.extension.pca.PCA9685GpioProvider;
import com.pi4j.gpio.extension.pca.PCA9685Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class PCA9685ServoDriver {
	
	private  GpioController gpio = null;
	private I2CBus bus = null;
	public PCA9685GpioProvider provider = null;
	private GpioPinPwmOutput[] servoBoardPins = null;
	
	
	public static void main(String[] args) throws UnsupportedBusNumberException, IOException {
		
		int pinNumber = -1;
		int tmpPin = -1;
		if (args.length < 1) System.out.println("please include module parameters pin#,  L(LED) or/and S(Servo)");
		
		
		PCA9685ServoDriver pca = new PCA9685ServoDriver(null);
		for (String s: args) {
			System.out.println("flag "+ s);
			try {
				tmpPin = Integer.parseInt(s);
			}
			catch (NumberFormatException nEx) {
				tmpPin = -1;
			}

			
			if (s.startsWith("L")) {
				pca.lightLED(pinNumber);
				
			}
			else if (s.startsWith("S")) {
				pca.driverServoS90(pinNumber);
			}
			else if (s.startsWith("P")) {
				int pos = Integer.parseInt(s.substring(1));
				pca.driverServoS90ToPosition(pinNumber, pos);
			}
			else { 
				pinNumber = tmpPin;
			}
		}
		pca.shutdown();
	}


	//	initialize gpio, pca9685 pwm driver	
	public PCA9685ServoDriver (GpioController _gpio) {
		try {
			if (gpio == null ) {
				if (_gpio == null )		gpio = GpioFactory.getInstance();
				else gpio = _gpio;
			}
			// This would theoretically lead into a resolution of 5 microseconds per step:
			// 4096 Steps (12 Bit)
			// T = 4096 * 0.000005s = 0.02048s
			// f = 1 / T = 48.828125
			BigDecimal frequency = new BigDecimal("48.828");
			// Correction factor: actualFreq / targetFreq
			// e.g. measured actual frequency is: 51.69 Hz
			// Calculate correction factor: 51.65 / 48.828 = 1.0578
			// --> To measure actual frequency set frequency without correction factor(or set to 1)
			BigDecimal frequencyCorrectionFactor = new BigDecimal("1.0578");
			// Create custom PCA9685 GPIO provider
			System.out.println("i2c bus .");
			bus = I2CFactory.getInstance(I2CBus.BUS_1);
			provider = new PCA9685GpioProvider(bus, 0x40, frequency, frequencyCorrectionFactor);
	        servoBoardPins = provisionPwmOutputs(provider);
			// Reset outputs
			provider.reset();
		
			System.out.println("gpio instance . "+ provider.getName());
			System.out.println("gpio .getPeriodDurationMicros() . "+ provider.getPeriodDurationMicros());
			
			
		} catch (UnsupportedBusNumberException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	/*
	 * register pin on the driver board
	 * param: must by pin 0 thru 15
	 * 
	 */
	
	public GpioPinPwmOutput registerPwmPin(int pinNum) throws Exception {
		if (pinNum < 0 || pinNum > 15 ) 
			throw new Exception("Invalid pin #. it must be 0 thru 15.");
		GpioPinPwmOutput pwmPin = servoBoardPins[pinNum];
		System.out.printf("registered pin %s\n", pwmPin.getName());
		return pwmPin;
	}
	
	
	public void getProperties() {
		
		System.out.printf("method Name %s \n", provider.getName() );
		System.out.println("getFrequency : " + provider.getFrequency()  );
		System.out.println("getPeriodDurationMicros : " + provider.getPeriodDurationMicros()  );
//		System.out.println("getPwmOnOffValues : " + provider.getPwmOnOffValues(null)  );
		
		
		
	}
	
	

	

	public void driverServoS90ToPosition(int pinNumber, int pos) {
		try {
//			provider.reset();
			
			Pin[] pins = PCA9685Pin.ALL;
			Pin pin = pins[pinNumber];
			System.out.println("<--Pi4J--> PCA9685 PWM Example ... driverServoS90()  configuring. "+ pin.getName() + " - " +pin.getAddress());
//			GpioPinPwmOutput motorPin = gpio.provisionPwmOutputPin(provider,pin, "Pulse 00");
			GpioPinPwmOutput servoPin = servoBoardPins[pin.getAddress()];
//			motorPin.setShutdownOptions(true,PinState.LOW);

			System.out.println("starting position.  " + pos  + " : "+ servoPin.toString());
//            provider.setPwm(pin, 0, 600);
            				
			for ( int i = pos - 100;i < pos + 100 ;i+=5) {
				System.out.printf("PWM = %d\n", i );
//				System.out.printf("PWM = %d  pulse = %d pwm= %d \n", i , provider.getPeriodDurationMicros(), provider.getPwm(PCA9685Pin.PWM_00));
				servoPin.setPwm(i);
				Thread.sleep(1000);
			}

			System.out.println("Done.");
			System.out.printf("Press Enter to continue.");
			String prompt = new Scanner(System.in).nextLine();

		}
		catch(Exception ex) {
			System.out.println("Exception " + ex);
		}
	}

	
	public void driverServoS90(int pinNumber) {
			try {
				int min = 500;
				int max = 2400;
				int mid = ((max-min)/2) + min;
				int step = 20;
				System.out.format("Servo specs min:%d mid:%d max:%d \n", min,mid,max);
				
//				provider.reset();

				Pin[] pins = PCA9685Pin.ALL;
				Pin pin = pins[pinNumber];
				
				System.out.println("<--Pi4J--> PCA9685 PWM Example ... driverServoS90()  configuring. "+ pin.getName() + " - " +pin.getAddress());
//				GpioPinPwmOutput motorPin = gpio.provisionPwmOutputPin(provider,pin, "Pulse 00");
				GpioPinPwmOutput motorPin = servoBoardPins[pin.getAddress()];
//				motorPin.setShutdownOptions(true,PinState.LOW);

				System.out.println("started.");
//	            provider.setPwm(pin, 0, 600);
	            				
				for ( int i = min;i< max;i+=step) {
					System.out.printf("PWM = %d\n", i );
					motorPin.setPwm(i);
					Thread.sleep(200);
				}
				for ( int i = max;i > min;i-=step) {
					System.out.printf("PWM = %d\n", i );
					motorPin.setPwm(i);
					Thread.sleep(200);
				}

				System.out.println("Done.");
				System.out.printf("Press Enter to continue.");
				String prompt = new Scanner(System.in).nextLine();

			}
			catch(Exception ex) {
				System.out.println("Exception " + ex);
			}
		}


	public void lightLED(int pinNumber) {

		try {
			Pin[] pins = PCA9685Pin.ALL;
			Pin pin = pins[pinNumber];

			System.out.println("<--Pi4J--> PCA9685 PWM Example ...lightLED()  configuring "+ pin.getName() + " - " +pin.getAddress());

			System.out.println("gpio instance . "+ provider.getName());
			System.out.println("gpio .getPeriodDurationMicros() . "+ provider.getPeriodDurationMicros());
//			GpioPinPwmOutput ledPin = gpio.provisionPwmOutputPin(provider,pin, "Pulse 03");
			GpioPinPwmOutput ledPin = servoBoardPins[pin.getAddress()];
			
			

			System.out.println("started.");
			for ( int i = 10;i< 1000;i+=20) {
				System.out.println("PWM =" + i);
				ledPin.setPwm(i);
				TimeUnit.SECONDS.sleep(1);
			}
			System.out.println("Done.");
			System.out.printf("Press Enter to continue.");
			new Scanner(System.in).nextLine();
			System.out.printf("reset.");


		}
		catch(Exception ex) {
			System.out.println("Exception " + ex);
		}
	}


	public void testWriteRawData() {

		try {

			int PCA9685_ADDR  = 0x40;


			int PWM_FREQ = 50;
			double precision = 1.057/8;
			int osc_clock = 20 ; // cycle/clock
			System.out.println("<--Pi4J--> MY PWM Example ... started.");
			int[] pwr = {10,20,40,80,0};
			
			

			I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
			I2CDevice device= i2c.getDevice(PCA9685_ADDR);
			for (int i: pwr) {
				System.out.printf("writing %d\n", i);
				device.write((byte)i);
				Thread.sleep(5000);
			}
		} catch (UnsupportedBusNumberException | IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


//	Shutdown gpio module at the end of this program.
	public void shutdown() {
		if (provider != null )
			provider.shutdown();
		try {
			if (bus != null ) bus.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gpio.shutdown();

		
	}

	public void stop(Pin ... pins ) {

		for(Pin pin: pins) {
			provider.setPwm(pin, 1);
		}
	}
	

    private GpioPinPwmOutput[] provisionPwmOutputs(final PCA9685GpioProvider gpioProvider) {
        GpioPinPwmOutput myOutputs[] = {
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_00, "Pulse 00"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_01, "Pulse 01"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_02, "Pulse 02"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_03, "Pulse 03"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_04, "Pulse 04"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_05, "Pulse 05"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_06, "Pulse 06"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_07, "Pulse 07"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_08, "Pulse 08"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_09, "Pulse 09"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_10, "Always ON"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_11, "Always OFF"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_12, "Servo pulse MIN"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_13, "Servo pulse NEUTRAL"),
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_14, "Servo pulse MAX")         ,
                gpio.provisionPwmOutputPin(gpioProvider, PCA9685Pin.PWM_15, "not used")
                };
        return myOutputs;
    }

}
