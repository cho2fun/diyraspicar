package cho.raspi.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import cho.raspi.map.AK8963_Map;
import cho.raspi.map.AK8963_MapValues;
import cho.raspi.GyroDataStruc;
import cho.raspi.helper.ByteOperations;
import cho.raspi.helper.I2CProtocolHelper;
import cho.raspi.map.MPU9250_Map;
import cho.raspi.map.MPU9250_MapValues;
import cho.raspi.helper.MagnetometerHelper;
import cho.raspi.helper.LowPassFilter;

/**
 * @author Cheung Ho
 * 
 * formula for yaw = 180 * atan (accelerationZ/sqrt(accelerationX*accelerationX + accelerationZ*accelerationZ))/M_PI
 *
 */
public class MPU9250_GyroMagnet extends Thread implements AK8963_Map, MPU9250_Map, MPU9250_MapValues {



	static int DEVICE_ADDR = 0X68;  // device address on i2c bus


	private static I2CBus bus = null;
	private static I2CDevice mpu9250 = null;
	private static I2CDevice ak8963 = null;
	private GyroDataStruc gStruc = null;
	private ArrayList<Double> dataList = new ArrayList<>();
	private static final Logger logger = Logger.getLogger(MPU9250_GyroMagnet.class.getName());

	public static void main(String[] args) throws InterruptedException {

		MPU9250_GyroMagnet test = new MPU9250_GyroMagnet();


		if ("M".equals(args[0])) {
			int lap = 1;
			if (args.length == 2) lap = Integer.parseInt(args[1]);
//			System.out.println("single measure mode");
//			test.gyromagWake(AK8963_MapValues.AK8963_CNTL_MODE_SINGLEMEASURE);
//			System.out.println("continuous measure mode");
//			test.gyromagWake(AK8963_MapValues.AK8963_CNTL_MODE_CONT1MEASURE);
//			System.out.println("sleep mode");
//			test.gyromagSleep();
//			test.readMagnetMeasurement(lap, true);
			test.readMagnetemeter(lap, false);
		}
		else if ("R".equals(args[0])) {
			test.readFromDevice(args[1]);
		}
		else if ("W".equals(args[0])) {
			test.writeToDevice(args[1]);
		}
		else if ("T".equals(args[0])) {
			int lap = -1;
			test.selfTest();
			if (args.length == 2) lap = Integer.parseInt(args[1]);
			test.testDevice(lap);
			test.testDevice(10);
		}
		else if ("F".equals(args[0])) {
			test.readAK8963FuseMode();
		}
		else if ("I".equals(args[0])) {
			test.information();
		}
		else if ("S".equals(args[0])) {
			test.selfTest();
		}
		else if ("BR".equals(args[0])) {
			int lap = -1;
			if (args.length == 2) lap = Integer.parseInt(args[1]);
			test.burstReadFromDevice(lap, true);
		}
		else if ("SC".equals(args[0])) {
			try {
				I2CProtocolHelper.scanDevice();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public void run() {
		try {

			long startTime = System.currentTimeMillis();   
			double startPoint = 0;
				startPoint = readMagnetMeasurement(5, false);
			System.out.printf("degrees turned  %8.3f \n",  startPoint);
			sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
 * //		displayInfo)burstReadFromDevice(-1,true);
 
		long stopTime = System.currentTimeMillis();   	
		OptionalDouble average = getAverageTurnInDegree();
		double degreeTurned = this.getTurnInDegree(startTime, stopTime, average.getAsDouble()); 
*/		
	}


	private OptionalDouble getAverageTurnInDegree() {
		return dataList.stream().mapToDouble(Double::doubleValue).average();
	}

	private double getTurnInDegree(long startTime, long stopTime, double avgSpeed) {
		double lapse = stopTime - startTime;
		double degree = (lapse /1000)  * avgSpeed;
		System.out.printf("lapse %8.3f sp %8.3f degree %8.3f  \n", lapse, avgSpeed, degree );
		return degree;
	}

	// this method test communicaiton to the device
	public int readFromDevice(String register) {
		int data = 0;
		data = I2CProtocolHelper.readFromDevice(mpu9250,Integer.getInteger(register).byteValue() );
		System.out.println("Reg "+ register +  "  received "+ data + " - "+ Integer.toBinaryString(data));
		return data;
	}


	// this method write sample data to a register to test communication to I2Cdevice
	public void writeToDevice(String register) {

		try {
			I2CProtocolHelper.writeToDevice(mpu9250, Integer.getInteger(register).byteValue(), (byte)0x00);
			Thread.sleep(500);
			System.out.println("sent data to reg "+ register);
		} catch ( InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public void burstReadFromDevice(int cycle, boolean displayInfo ) {
		System.out.println(" Burst Read test started!");
		int position = 0;
		int lap = 0;
		while (true) {
			byte[] rawData = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_ACCEL_XOUT_H, 14);
			int i = 0;
			gStruc = new GyroDataStruc();
			if (rawData.length == 14) {
				//        		System.out.println("RA_ACCEL ");
				gStruc.setAcclX(ByteOperations.msblsbToInt(rawData[i++],rawData[i++]));
				gStruc.setAcclY(ByteOperations.msblsbToInt(rawData[i++],rawData[i++]));
				gStruc.setAcclZ(ByteOperations.msblsbToInt(rawData[i++],rawData[i++]));
				//        		System.out.println("RA_TEMP ");
				gStruc.setTemperature(ByteOperations.msblsbToInt(rawData[i++], rawData[i++]));
				//        		System.out.println("RA_GYRO ");
				gStruc.setGyroX(ByteOperations.msblsbToInt(rawData[i++],rawData[i++]));
				gStruc.setGyroY(ByteOperations.msblsbToInt(rawData[i++],rawData[i++]));
				gStruc.setGyroZ(ByteOperations.msblsbToInt(rawData[i++],rawData[i++]));
				//        		System.out.println("P " + ( position += gStruc.getGyroZScaled()) + " " + gStruc.toString());
				if (gStruc.getGyroZScaled()> 0 ) dataList.add(gStruc.getGyroZScaled());

				//        		double xRotation = getXRotation(gStruc.getAcclXScaled(), gStruc.getAcclYScaled(), gStruc.getAcclZScaled());
				//        		double yRotation = getYRotation(gStruc.getAcclXScaled(), gStruc.getAcclYScaled(), gStruc.getAcclZScaled());
				//        		System.out.printf("Rotation X  %8.4f , Y %8.4f  ", xRotation, yRotation);
				//        		xRotation = getXRotation(gStruc.getGyroXScaled(), gStruc.getGyroYScaled(), gStruc.getGyroZScaled());
				//        		yRotation = getYRotation(gStruc.getGyroXScaled(), gStruc.getGyroYScaled(), gStruc.getGyroZScaled());
				//        		System.out.printf("Rotation X  %8.4f , Y %8.4f \n ", xRotation, yRotation);
			}
			else {
				System.out.printf("Data requested %d, but returned %d \n", 14, rawData.length);
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int st1 = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_ST1);
//			System.out.println("STA_1 :"+ st1+  " " +  Integer.toBinaryString(st1));
			if ((st1&0x01) >0) { // true if last bit of byte is 1 
				byte[] magData = I2CProtocolHelper.readFromDevice(ak8963,AK8963_XOUT_L, 7);
				i = 0;
				if (magData.length == 7) {
					if (!((magData[6] & 0x08)> 0) ) {
						gStruc.setMagnX(ByteOperations.lsbmsbToInt(magData[i++],magData[i++]));
						gStruc.setMagnY(ByteOperations.lsbmsbToInt(magData[i++],magData[i++]));
						gStruc.setMagnZ(ByteOperations.lsbmsbToInt(magData[i++],magData[i++]));
					}
					else {
						System.out.println("overflow: " );
					}
					if(displayInfo) System.out.println( gStruc.toString() + ", d " + MagnetometerHelper.magnetTeslaToDegree((int)gStruc.getMagnX(),(int) gStruc.getMagnY()));
				}
				else {
					System.out.println("Magnetometer status data is not received " + magData.length );
				}
			}
			else {
				System.out.println("magnetometer data is not ready.");
			}
			try {
				Thread.sleep(100);
			}
			catch (Exception ex) {
				System.out.println(ex);
				break;
			}
			if (cycle > -1 ) {
				lap++;
				if (cycle < lap ) break;
			}
		}
	}

	public void testDevice(int cycle) {
		System.out.println(" test individual calls started!");
		int msb = 0;
		int lsb = 0;
		int lap = 0;
		while (true) {

			gStruc = new GyroDataStruc();
			//    		System.out.println("RA_ACCEL ");
			msb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_ACCEL_XOUT_H);
			lsb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_ACCEL_XOUT_L);
			gStruc.setAcclX(ByteOperations.msblsbToInt(msb, lsb));
			msb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_ACCEL_YOUT_H);
			lsb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_ACCEL_YOUT_L);
			gStruc.setAcclY(ByteOperations.msblsbToInt(msb, lsb));
			msb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_ACCEL_ZOUT_H);
			lsb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_ACCEL_ZOUT_L);
			gStruc.setAcclZ(ByteOperations.msblsbToInt(msb, lsb));
			//    		System.out.println("RA_TEMP ");
			msb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_TEMP_OUT_H);
			lsb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_TEMP_OUT_L);
			gStruc.setTemperature(ByteOperations.msblsbToInt(msb, lsb));
			//    		System.out.println("RA_GYRO ");

			msb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_GYRO_XOUT_H);
			lsb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_GYRO_XOUT_L);
			gStruc.setGyroX(ByteOperations.msblsbToInt(msb, lsb));
			msb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_GYRO_YOUT_H);
			lsb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_GYRO_YOUT_L);
			gStruc.setGyroY(ByteOperations.msblsbToInt(msb, lsb));
			msb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_GYRO_ZOUT_H);
			lsb = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_GYRO_ZOUT_L);
			gStruc.setGyroZ(ByteOperations.msblsbToInt(msb, lsb));

			System.out.println(gStruc.toString());
			double xRotation = getXRotation(gStruc.getAcclXScaled(), gStruc.getAcclYScaled(), gStruc.getAcclZScaled());
			double yRotation = getYRotation(gStruc.getAcclXScaled(), gStruc.getAcclYScaled(), gStruc.getAcclZScaled());
			System.out.printf("Rotation X  %8.4f , Y %8.4f  ", xRotation, yRotation);
			xRotation = getXRotation(gStruc.getGyroXScaled(), gStruc.getGyroYScaled(), gStruc.getGyroZScaled());
			yRotation = getYRotation(gStruc.getGyroXScaled(), gStruc.getGyroYScaled(), gStruc.getGyroZScaled());
			System.out.printf("Rotation X  %8.4f , Y %8.4f \n ", xRotation, yRotation);
			try {
				Thread.sleep(100);
			}
			catch (Exception ex) {
				System.out.println(ex);
			}
			if (cycle > -1 ) {
				lap++;
				if (cycle < lap ) break;
			}
		}
		System.out.println("set gyroscope to sleep vis PWR_MGMT_1" );
		byte pwr_sleep = 0b01001000; // sleep and disable temperature
		try {
			mpu9250.write(MPU9250_RA_PWR_MGMT_1_VALUE, pwr_sleep);
			Thread.sleep(5000);
			pwr_sleep = 0b00001000; // wake up and disable temperature
			mpu9250.write(MPU9250_RA_PWR_MGMT_1_VALUE, pwr_sleep);
			System.out.println("set gyroscope to wake up vis PWR_MGMT_1" );

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	// configure device
	public MPU9250_GyroMagnet() {

		try {

			bus = I2CFactory.getInstance(I2CBus.BUS_1);
			mpu9250 = bus.getDevice(DEVICE_ADDR);
			this.enableAK8963(); // this method does bypass and disable master mode so ak8963 show up on i2cbus

			ak8963 = bus.getDevice((byte) AK8963_ADDRESS );
			ak8963.write((byte)0x0);
			System.out.println("verifying ak8963 via 0x0c completed.");
			this.configuration();
			System.out.println("I2C Bus Initialization completed for  "+ mpu9250.toString());




		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	// configuration for MPU9250 and AK8963
	public void configuration() throws InterruptedException {

		try {
			// configuration for MPU9250
			//			this.resetdevice();

			// configuration for MPU 9250

			mpu9250.write(MPU9250_RA_PWR_MGMT_1,MPU9250_RA_PWR_MGMT_1_VALUE);
			mpu9250.write(MPU9250_RA_GYRO_CONFIG,MPU9250_RA_GYRO_CONFIG_VALUE );
			mpu9250.write(MPU9250_RA_FIFO_EN,MPU9250_RA_FIFO_EN_VALUE);
			mpu9250.write(MPU9250_RA_ACCEL_CONFIG,MPU9250_RA_ACCEL_CONFIG_VALUE);
			mpu9250.write(MPU9250_RA_INT_ENABLE,MPU9250_RA_INT_ENABLE_VALUE);
			mpu9250.write(MPU9250_RA_PWR_MGMT_2,MPU9250_RA_PWR_MGMT_2_VALUE);
			mpu9250.write(MPU9250_RA_SMPLRT_DIV,MPU9250_RA_SMPLRT_DIV_VALUE);
			mpu9250.write(MPU9250_RA_CONFIG,MPU9250_RA_CONFIG_VALUE);

			int test= mpu9250.read(INT_PIN_CFG);
			System.out.println(" INT_PIN_CFG before " +test +  " - " + Integer.toBinaryString(test) );
			//			test= ak8963.read(AK8963_CNTL);
			//			System.out.println(" AK8963_CNTL before  " +test +  " - " + Integer.toBinaryString(test) );

			//			mpu9250.write(INT_PIN_CFG,(byte) 0x22);  // allow bypass mode
			//			these 2 values will enable ak8963, will test and confirm
			//			writeByte(INT_PIN_CFG, 0x22);
			//			writeByte(INT_ENABLE, 0x01);
			System.out.println("MPU9250 configuraitons completed. ");

			// configuration for AK8963

			//	    	I2CProtocolHelper.writeToDevice(mpu9250,PWR_MGMT_1,(byte) 0x00);       
			I2CProtocolHelper.writeToDevice(mpu9250,USER_CTRL,(byte) 0x00);      // disable master mode
			I2CProtocolHelper.writeToDevice(mpu9250,INT_PIN_CFG,(byte) 0x02);     // enable bypass mode 
			I2CProtocolHelper.writeToDevice(ak8963,AK8963_CNTL,(byte) 0x02); // setup magnetic sensor to measure continuously   
			I2CProtocolHelper.writeToDevice(ak8963,(byte) AK8963_ASTC, (byte) 0b00000000); // set to normal mode

			test= mpu9250.read(INT_PIN_CFG);
			System.out.println(" INT_PIN_CFG after  " +test +  " - " + Integer.toBinaryString(test) );
			test= ak8963.read(AK8963_CNTL);
			System.out.println(" AK8963_CNTL after  " +test +  " - " + Integer.toBinaryString(test) );

			// configuration for AK8963

			//			float[] magData = initAK8963Slave(1,2); 

			//			I2CProtocolHelper.writeToDevice(ak8963,(byte) AK8963_ASTC, (byte) 0b00000000); // normal mode

			//			I2CProtocolHelper.writeToDevice(ak8963,(byte) AK8963_CNTL, (byte) 0b00010010); // 16 bit output, continous mode 1(slow) 


			//			int check = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_CNTL);
			//            System.out.println("CNTL :"+ check + " " +  Integer.toBinaryString(check));

			//			I2CProtocolHelper.writeToDevice(ak8963,(byte) AK8963_CNTL, (byte) 0b00000000); // power down mode

			//			System.out.println("AK8963 write cnl  test  completed. ");
			//			this.gyromagSleep();
			//			System.out.println("AK8963 power down completed. ");
			//			byte mMode = 0b00011000; //16bit output selftest mode
			//			this.gyromagWake(mMode);
			//			System.out.println("AK8963 power up completed. ");
			//			System.out.println("AK8963 configuraitons completed. ");

			//			checkNewMagData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}

	public boolean enableAK8963() throws InterruptedException {

		I2CProtocolHelper.writeToDevice(mpu9250,USER_CTRL,(byte) 0x00);      // disable master mode
		I2CProtocolHelper.writeToDevice(mpu9250,INT_PIN_CFG,(byte) 0x02);     // enable bypass mode 
		System.out.println("configured ak8963 to show up i2cbus.");
		Thread.sleep(500);
		return true;
	}

	public boolean selfTest() {
		boolean ok = false;
		int fs = 0;
		float[] result = new float[6];
		float factoryTrim[] = new float[6];
		int testCases = 200;
		int gAvg[] = { 0,0,0 } 
		, aAvg[] = { 0,0,0 }, aSTAvg[] = { 0,0,0 }, gSTAvg[] = { 0,0,0 };
		System.out.println("self test started.");
		try {
			I2CProtocolHelper.writeToDevice(mpu9250,(byte) SMPLRT_DIV,(byte) 0x00);    // Set gyro sample rate to 1 kHz
			I2CProtocolHelper.writeToDevice(mpu9250,(byte) MPU9250_RA_CONFIG, (byte)0x02);        // Set gyro sample rate to 1 kHz and DLPF to 92 Hz
			I2CProtocolHelper.writeToDevice(mpu9250,(byte) GYRO_CONFIG,(byte)( 1 << fs));  // Set full scale range for the gyro to 250 dps
			I2CProtocolHelper.writeToDevice(mpu9250,(byte) ACCEL_CONFIG2,(byte) 0x02); // Set accelerometer rate to 1 kHz and bandwidth to 92 Hz
			I2CProtocolHelper.writeToDevice(mpu9250,(byte) ACCEL_CONFIG,(byte) ( 1 << fs)); // Set full scale range for the accelerometer to 2 g
			Thread.sleep(500);
			for (int ii = 0; ii < testCases ; ii++)
			{  // get average current values of gyro and acclerometer

				byte[] rawData = I2CProtocolHelper.readFromDevice(mpu9250, ACCEL_XOUT_H, 6);        // Read the six raw data registers into data array
				aAvg[0] += ((rawData[0] << 8) | rawData[1]);  // Turn the MSB and LSB into a signed 16-bit value
				aAvg[1] += ((rawData[2] << 8) | rawData[3]);
				aAvg[2] += ((rawData[4] << 8) | rawData[5]);

				rawData  = I2CProtocolHelper.readFromDevice(mpu9250, GYRO_XOUT_H, 6);       // Read the six raw data registers sequentially into data array
				gAvg[0] += ((rawData[0] << 8) | rawData[1]);  // Turn the MSB and LSB into a signed 16-bit value
				gAvg[1] += ((rawData[2] << 8) | rawData[3]);
				gAvg[2] += ((rawData[4] << 8) | rawData[5]);
			}
			System.out.printf("Current Average Values \n" );
			System.out.printf("A %8d %8d %8d \n", aAvg[0], aAvg[1], aAvg[2] );
			System.out.printf("G %8d %8d %8d \n", gAvg[0], gAvg[1], gAvg[2] );

			for (int ii = 0; ii < 3; ii++)
			{  // Get average of 200 values and store as average current readings
				aAvg[ii] /= testCases;
				gAvg[ii] /= testCases;
			}


			// Configure the accelerometer for self-test
			I2CProtocolHelper.writeToDevice(mpu9250,(byte) MPU9250_RA_ACCEL_CONFIG,(byte) 0xE0); // Enable self test on all three axes and set accelerometer range to +/- 2 g
			I2CProtocolHelper.writeToDevice(mpu9250,(byte) MPU9250_RA_GYRO_CONFIG, (byte) 0xE0); // Enable self test on all three axes and set gyro range to +/- 250 degrees/s
			I2CProtocolHelper.writeToDevice(mpu9250,(byte) MPU9250_RA_CONFIG, (byte) 0xE0); // Enable self test on all three axes and set gyro range to +/- 250 degrees/s

			Thread.sleep(250);  // Delay a while to let the device stabilize
			System.out.printf("Configure SelfTest Completed \n" );

			for (int ii = 0; ii < testCases; ii++)
			{  // get average current values of gyro and acclerometer

				byte[] rawData = I2CProtocolHelper.readFromDevice(mpu9250, ACCEL_XOUT_H, 6);        // Read the six raw data registers into data array
				aSTAvg[0] += ((rawData[0] << 8) | rawData[1]);  // Turn the MSB and LSB into a signed 16-bit value
				aSTAvg[1] += ((rawData[2] << 8) | rawData[3]);
				aSTAvg[2] += ((rawData[4] << 8) | rawData[5]);

				rawData  = I2CProtocolHelper.readFromDevice(mpu9250, GYRO_XOUT_H, 6);       // Read the six raw data registers sequentially into data array
				gSTAvg[0] += ((rawData[0] << 8) | rawData[1]);  // Turn the MSB and LSB into a signed 16-bit value
				gSTAvg[1] += ((rawData[2] << 8) | rawData[3]);
				gSTAvg[2] += ((rawData[4] << 8) | rawData[5]);
			}

			for (int ii = 0; ii < 3; ii++)
			{  // Get average of 200 values and store as average self-test readings
				aSTAvg[ii] /= testCases;
				gSTAvg[ii] /= testCases;
			}

			System.out.printf("SelfTest  Average Values \n" );
			System.out.printf("A %8d %8d %8d \n", aSTAvg[0], aSTAvg[1], aSTAvg[2] );
			System.out.printf("G %8d %8d %8d \n", gSTAvg[0], gSTAvg[1], gSTAvg[2] );

			// Configure the gyro and accelerometer for normal operation
			I2CProtocolHelper.writeToDevice(mpu9250,(byte) MPU9250_RA_ACCEL_CONFIG,(byte) 0x00);
			I2CProtocolHelper.writeToDevice(mpu9250,(byte) MPU9250_RA_GYRO_CONFIG,(byte) 0x00);


			Thread.sleep(250);  // Delay a while to let the device stabilize
			int[] selfTest = new int[6]; 
			// Retrieve accelerometer and gyro factory Self-Test Code from USR_Reg
			selfTest[0] = I2CProtocolHelper.readFromDevice(mpu9250,(byte) SELF_TEST_X_ACCEL); // X-axis accel self-test results
			selfTest[1] = I2CProtocolHelper.readFromDevice(mpu9250,(byte) SELF_TEST_Y_ACCEL); // Y-axis accel self-test results
			selfTest[2] = I2CProtocolHelper.readFromDevice(mpu9250,(byte) SELF_TEST_Z_ACCEL); // Z-axis accel self-test results
			selfTest[3] = I2CProtocolHelper.readFromDevice(mpu9250,(byte) SELF_TEST_X_GYRO);  // X-axis gyro self-test results
			selfTest[4] = I2CProtocolHelper.readFromDevice(mpu9250,(byte) SELF_TEST_Y_GYRO);  // Y-axis gyro self-test results
			selfTest[5] = I2CProtocolHelper.readFromDevice(mpu9250,(byte) SELF_TEST_Z_GYRO);  // Z-axis gyro self-test results

			// Retrieve factory self-test value from self-test code reads
			factoryTrim[0] = (float) ((float)(2620 / 1 << fs) * (Math.pow(1.01, ((float)selfTest[0] - 1.0)))); // FT[Xa] factory trim calculation
			factoryTrim[1] = (float) ((float)(2620 / 1 << fs) * (Math.pow(1.01, ((float)selfTest[1] - 1.0)))); // FT[Ya] factory trim calculation
			factoryTrim[2] = (float) ((float)(2620 / 1 << fs) * (Math.pow(1.01, ((float)selfTest[2] - 1.0)))); // FT[Za] factory trim calculation
			factoryTrim[3] = (float) ((float)(2620 / 1 << fs) * (Math.pow(1.01, ((float)selfTest[3] - 1.0)))); // FT[Xg] factory trim calculation
			factoryTrim[4] = (float) ((float)(2620 / 1 << fs) * (Math.pow(1.01, ((float)selfTest[4] - 1.0)))); // FT[Yg] factory trim calculation
			factoryTrim[5] = (float) ((float)(2620 / 1 << fs) * (Math.pow(1.01, ((float)selfTest[5] - 1.0)))); // FT[Zg] factory trim calculation

			// Report results as a ratio of (STR - FT)/FT; the change from Factory Trim of the Self-Test Response
			// To get percent, must multiply by 100
			for (int i = 0; i < 3; i++)
			{
				result[i] = 100.0f * ((float)(aSTAvg[i] - aAvg[i])) / factoryTrim[i] - 100.0f;   // Report percent differences
				result[i + 3] = 100.0f * ((float)(gSTAvg[i] - gAvg[i])) / factoryTrim[i + 3] - 100.0f; // Report percent differences
			}

			System.out.printf("RESULT   ratio of (STR - FT)/FT \n" );
			System.out.printf("A %8.4f %8.4f %8.4f \n", result[0], result[1], result[2] );
			System.out.printf("G %8.4f %8.4f %8.4f \n", result[3], result[4], result[5] );




			// SelfTest for Magnetometer
			// Configure the magnetometer for selftest
			//			I2CProtocolHelper.writeToDevice(ak8963,(byte) AK8963_ASTC, (byte) 0b01000000); // set to selftest mode
			ak8963.write(AK8963_ASTC, (byte) 0b01000000);
			int test = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_ASTC);
			System.out.println("ASTC  0b01000000  "+ test +  " - " + Integer.toBinaryString(test) );

			I2CProtocolHelper.writeToDevice(ak8963,(byte) AK8963_CNTL, (byte) 0b00001000); // selftest mode
			Thread.sleep(500);

			byte[] magData = new byte[7];
			gStruc = new GyroDataStruc();
			for(int i = 0 ;i<5;i++ ) {
				int check = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_ST1);
				if ((check & 0x01) > 0) {
					magData = I2CProtocolHelper.readFromDevice(ak8963,AK8963_XOUT_L, 7);
					i = 0;
					if (magData.length == 7) {
						System.out.printf("magnetometer status %d \n",magData[6]);
						System.out.printf("X %d %d = %d \n",magData[0],magData[1], ByteOperations.lsbmsbToInt(magData[0], magData[1]));
						System.out.printf("Y %d %d = %d \n",magData[2],magData[3], ByteOperations.lsbmsbToInt(magData[2], magData[3]));
						System.out.printf("Z %d %d = %d \n",magData[4],magData[5], ByteOperations.lsbmsbToInt(magData[4], magData[5]));
						gStruc.setMagnX( ByteOperations.lsbmsbToInt(magData[0],magData[1]));
						gStruc.setMagnY( ByteOperations.lsbmsbToInt(magData[2],magData[3]));
						gStruc.setMagnZ(  ByteOperations.lsbmsbToInt(magData[4],magData[5]));
					}
					else {
						System.out.println(i + ". magData "+ magData.length);
					}
				}
				else {
					System.out.println(i + ". data not ready status "+ check +  " - " + Integer.toBinaryString(check) );
					Thread.sleep(1000);
				}
			}

			System.out.printf("SELFTEST magnetometer \n" );
			//			System.out.printf("M %8.4f %8.4f %8.4f \n", result[0], result[1], result[2] );



			// put magnetometer back normal mode and power down it
			I2CProtocolHelper.writeToDevice(ak8963,(byte) AK8963_ASTC, (byte) 0b00000000); // set to normal mode
			I2CProtocolHelper.writeToDevice(ak8963,(byte) AK8963_CNTL, (byte) 0b00000000); // power down mode


		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}


		return ok ;
	}





	public double getXRotation (double x,double y,double z) {
		double radians = Math.atan2(x,  Math.sqrt((y*y)+(z*z)));
		return Math.toDegrees(radians);
	}

	public double getYRotation (double x,double y,double z) {
		double radians = Math.atan2(y,  Math.sqrt((y*y)+(z*z)));
		return Math.toDegrees(radians);
	}


	public void information() {
		// who am i 
		int whoami = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_WHO_AM_I);
		System.out.println("who Am I :"+ whoami   + " " +  Integer.toBinaryString(whoami));
		int information = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_INFORMATION);
		System.out.println("Information :"+ information + " " +  Integer.toBinaryString(information) );
		int status = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_STATUS_1);
		System.out.println("Status 1 :"+ status + " " +  Integer.toBinaryString(status));
		status = I2CProtocolHelper.readFromDevice(mpu9250,MPU9250_RA_STATUS_2);
		System.out.println("Status 2 :"+ status + " " +  Integer.toBinaryString(status));
		status = I2CProtocolHelper.readFromDevice(mpu9250,I2C_SLV0_ADDR);
		System.out.println("I2C_SLV0_ADDR :"+ status + " " +  Integer.toBinaryString(status));
		status = I2CProtocolHelper.readFromDevice(mpu9250,I2C_SLV0_REG);
		System.out.println("I2C_SLV0_REG :"+ status + " " +  Integer.toBinaryString(status));
		status = I2CProtocolHelper.readFromDevice(mpu9250,I2C_SLV0_CTRL);
		System.out.println("I2C_SLV0_CTRL :"+ status + " " +  Integer.toBinaryString(status));
		//        whoami = getAK8963CID();
		//        System.out.println("AK8963C getAddress :"+ ak8963.getAddress());
		whoami = I2CProtocolHelper.readFromDevice(ak8963,WHO_AM_I_AK8963);
		System.out.println("AK8963C who Am I :"+ whoami   + " " +  Integer.toBinaryString(whoami));
		status = I2CProtocolHelper.readFromDevice(ak8963, AK8963_INFO );
		System.out.println("AK8963 info :"+ status +  " " +  Integer.toBinaryString(status));
		status = I2CProtocolHelper.readFromDevice(ak8963, AK8963_CNTL );
		System.out.println("AK8963 CNTL :"+ status +  " " +  Integer.toBinaryString(status));
		status = I2CProtocolHelper.readFromDevice(ak8963, AK8963_ASTC );
		System.out.println("AK8963 ASTC :"+ status +  " " +  Integer.toBinaryString(status));
		status = I2CProtocolHelper.readFromDevice(ak8963, AK8963_ST1 );
		System.out.println("AK8963 STA 1 :"+ status +  " " +  Integer.toBinaryString(status));
		status = I2CProtocolHelper.readFromDevice(ak8963, AK8963_ST2 );
		System.out.println("AK8963 StA 2 :"+ status +  " " +  Integer.toBinaryString(status));
		status = I2CProtocolHelper.readFromDevice(ak8963, AK8963_ASAX );
		System.out.println("AK8963_ASAX :"+ status +  " " +  Integer.toBinaryString(status));
		status = I2CProtocolHelper.readFromDevice(ak8963, AK8963_ASAY );
		System.out.println("AK8963_ASAY :"+ status +  " " +  Integer.toBinaryString(status));
		status = I2CProtocolHelper.readFromDevice(ak8963, AK8963_ASAZ );
		System.out.println("AK8963_ASAZ :"+ status +  " " +  Integer.toBinaryString(status));
	}


	public void gyromagSleep() {
		int temp = 0;
		temp = I2CProtocolHelper.readFromDevice(ak8963,AK8963_CNTL);
		I2CProtocolHelper.writeToDevice(ak8963,AK8963_CNTL,(byte) (temp & ~(0x0f)));
		temp = I2CProtocolHelper.readFromDevice(mpu9250, PWR_MGMT_1);
		I2CProtocolHelper.writeToDevice(mpu9250, PWR_MGMT_1,(byte) (temp |0x10));   // return gyro and accel normal mode
//		public static final byte  AK8963_CNTL     = 0x0A;  // Power down (0000), single-measurement (0001), self-test (1000) and Fuse ROM (1111) modes on bits 3:0

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Wait for all registers to reset 
	}

	public void gyromagWake(int Mmode)
	{
		int  temp = 0;  // uint8_t temp = 0;
		I2CProtocolHelper.writeToDevice(ak8963, AK8963_CNTL,(byte) (AK8963_MapValues.AK8963_CNTL_OUTPUT_16BIT_SETTING << 4 | (byte) (Mmode & ~(0x0f)))); // Reset normal mode for  magnetometer  
		temp = I2CProtocolHelper.readFromDevice(ak8963, AK8963_CNTL);
		System.out.println("AK8963_CNTL "+ temp +" "+  Integer.toBinaryString(temp) + "   "+Integer.toBinaryString(Mmode) );
		temp = I2CProtocolHelper.readFromDevice(mpu9250, PWR_MGMT_1);
		I2CProtocolHelper.writeToDevice(mpu9250, PWR_MGMT_1, (byte) 0x01);   // return gyro and accel normal mode
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Wait for all registers to reset 
	}

	public void resetdevice() {
		I2CProtocolHelper.writeToDevice(mpu9250,MPU9250_RA_PWR_MGMT_1, (byte) 0x80);
	}


	public boolean checkNewMagData()
	{
		int test;
		//  test = (readByte(AK8963_ADDRESS, AK8963_ST1) & 0x01);
		I2CProtocolHelper.writeToDevice(mpu9250,(byte) I2C_SLV0_ADDR, (byte) (AK8963_ADDRESS | 0x80));    // Set the I2C slave address of AK8963 and set for read.
		I2CProtocolHelper.writeToDevice(mpu9250, I2C_SLV0_REG, AK8963_ST1);                // I2C slave 0 register address from where to begin data transfer
		I2CProtocolHelper.writeToDevice(mpu9250,(byte) I2C_SLV0_CTRL,(byte) 0x81);                     // Enable I2C and transfer 1 byte
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		test = I2CProtocolHelper.readFromDevice(mpu9250, (byte) EXT_SENS_DATA_00) ;         // Check data ready status byte
		System.out.println("checkNewMagData "+ test  + " - " + Integer.toBinaryString(test));
		return (test>0?true:false);
	}



	public float[] initAK8963Slave(int Mscale, int Mmode) throws InterruptedException
	{
		// MPU9250 Configuration
		// Specify sensor full scale
		/* Choices are:
		 *  Gscale: GFS_250 == 250 dps, GFS_500 DPS == 500 dps, GFS_1000 == 1000 dps, and GFS_2000DPS == 2000 degrees per second gyro full scale
		 *  Ascale: AFS_2G == 2 g, AFS_4G == 4 g, AFS_8G == 8 g, and AFS_16G == 16 g accelerometer full scale
		 *  Mscale: MFS_14BITS ==0x00 0.6 mG per LSB and MFS_16BITS ==0x01 0.15 mG per LSB
		 *  Mmode: Mmode == 0x02 M_8Hz for 8 Hz data rate or Mmode = 0x06 M_100Hz for 100 Hz data rate
		 *  (1 + sampleRate) is a simple divisor of the fundamental 1000 kHz rate of the gyro and accel, so 
		 *  sampleRate = 0x00 means 1 kHz sample rate for both accel and gyro, 0x04 means 200 Hz, etc.
		 */    	
		//    	uint8_t Gscale = GFS_250DPS, Ascale = AFS_2G, Mscale = MFS_16BITS, Mmode = M_100Hz, sampleRate = 0x04;    

		Mscale = 1;
		Mmode = 2; 


		// First extract the factory calibration for each magnetometer axis
		//       int rawData[] = new int[3];  // x/y/z gyro calibration data stored here
		byte test ;
		test = this.readSlave(AK8963_CNTL);
		System.out.println("AK8963_CNTL :"+ test + " " +  Integer.toBinaryString(test));
		Thread.sleep(500);

		this.writeSlave( AK8963_CNTL2, (byte) 0x01);

		test = this.readSlave(AK8963_CNTL2);
		System.out.println("AK8963_CNTL2 0x01:"+ test + " " +  Integer.toBinaryString(test));
		//       I2CProtocolHelper.writeToDevice(mpu9250, I2C_SLV0_ADDR, AK8963_ADDRESS);           // Set the I2C slave address of AK8963 and set for write.
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_REG, AK8963_CNTL2);              // I2C slave 0 register address from where to begin data transfer
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_DO, 0x01);                       // Reset AK8963
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_CTRL, 0x81);                     // Enable I2C and write 1 byte
		Thread.sleep(500);
		//       delay(50);
		this.writeSlave( AK8963_CNTL, (byte) 0x00);
		Thread.sleep(500);
		test = this.readSlave(AK8963_CNTL);
		System.out.println("AK8963_CNTL 0x00 :"+ test + " " +  Integer.toBinaryString(test));
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_ADDR, AK8963_ADDRESS);           // Set the I2C slave address of AK8963 and set for write.
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_REG, AK8963_CNTL);               // I2C slave 0 register address from where to begin data transfer
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_DO, 0x00);                       // Power down magnetometer  
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_CTRL, 0x81);                     // Enable I2C and write 1 byte
		Thread.sleep(500);
		//       delay(50);

		this.writeSlave( AK8963_CNTL, (byte) 0x0F);

		Thread.sleep(500);

		test = this.readSlave(AK8963_CNTL);
		System.out.println("AK8963_CNTL 0x0f:"+ test + " " +  Integer.toBinaryString(test));

		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_ADDR, AK8963_ADDRESS);           // Set the I2C slave address of AK8963 and set for write.
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_REG, AK8963_CNTL);               // I2C slave 0 register address from where to begin data transfer
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_DO, 0x0F);                       // Enter fuze mode
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_CTRL, 0x81);                     // Enable I2C and write 1 byte
		Thread.sleep(500);
		//       delay(50);

		byte[] rawData = this.readSlave( AK8963_ASAX,  3);
		Thread.sleep(500);

		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_ADDR, AK8963_ADDRESS | 0x80);    // Set the I2C slave address of AK8963 and set for read.
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_REG, AK8963_ASAX);               // I2C slave 0 register address from where to begin data transfer
		//      writeByte(MPU9250_ADDRESS, I2C_SLV0_CTRL, 0x83);                     // Enable I2C and read 3 bytes
		//       delay(50);
		//       readBytes(MPU9250_ADDRESS, EXT_SENS_DATA_00, 3, &rawData[0]);        // Read the x-, y-, and z-axis calibration values
		float[] _magCalibration = new float[3] ;
		float[] magCalibration = new float[3];
		magCalibration[0] =  (float)((rawData[0] - 128)/256.0f + 1.0f);        // Return x-axis sensitivity adjustment values, etc.
		magCalibration[1] =  (float)(rawData[1] - 128)/256.0f + 1.0f;  
		magCalibration[2] =  (float)(rawData[2] - 128)/256.0f + 1.0f; 
		_magCalibration[0] = magCalibration[0];
		_magCalibration[1] = magCalibration[1];
		_magCalibration[2] = magCalibration[2];
		int _Mmode = Mmode;


		this.writeSlave((byte) AK8963_CNTL,(byte) 0x00);
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_ADDR, AK8963_ADDRESS);           // Set the I2C slave address of AK8963 and set for write.
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_REG, AK8963_CNTL);               // I2C slave 0 register address from where to begin data transfer
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_DO, 0x00);                       // Power down magnetometer  
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_CTRL, 0x81);                     // Enable I2C and transfer 1 byte
		//       delay(50);
		Thread.sleep(500);

		byte mscale = (byte) (Mscale << 4 | Mmode);
		this.writeSlave(AK8963_CNTL,mscale);
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_ADDR, AK8963_ADDRESS);           // Set the I2C slave address of AK8963 and set for write.
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_REG, AK8963_CNTL);               // I2C slave 0 register address from where to begin data transfer 
		// Configure the magnetometer for continuous read and highest resolution
		// set Mscale bit 4 to 1 (0) to enable 16 (14) bit resolution in CNTL register,
		// and enable continuous mode data acquisition Mmode (bits [3:0]), 0010 for 8 Hz and 0110 for 100 Hz sample rates
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_DO, Mscale << 4 | Mmode);        // Set magnetometer data resolution and sample ODR
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_CTRL, 0x81);                     // Enable I2C and transfer 1 byte
		Thread.sleep(500);
		//       delay(50);

		test = this.readSlave(AK8963_CNTL);
		System.out.println("AK8963_CNTL "+mscale+" :"+ test + " " +  Integer.toBinaryString(test));

		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_ADDR, AK8963_ADDRESS | 0x80);    // Set the I2C slave address of AK8963 and set for read.
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_REG, AK8963_CNTL);               // I2C slave 0 register address from where to begin data transfer
		//       writeByte(MPU9250_ADDRESS, I2C_SLV0_CTRL, 0x81);                     // Enable I2C and transfer 1 byte
		Thread.sleep(500);
		//       delay(50);

		return magCalibration;
	}

	// update slave ak8963's register
	public boolean writeSlave(byte register, byte value) throws InterruptedException { 
		boolean ok = true;
		I2CProtocolHelper.writeToDevice(mpu9250, I2C_SLV0_ADDR, AK8963_ADDRESS);  // Set the I2C slave address of AK8963 and set for write.
		I2CProtocolHelper.writeToDevice(mpu9250, I2C_SLV0_REG, register);              // I2C slave 0 register address from where to begin data transfer
		I2CProtocolHelper.writeToDevice(mpu9250, I2C_SLV0_DO, value);                       // Reset AK8963
		I2CProtocolHelper.writeToDevice(mpu9250, I2C_SLV0_CTRL,(byte) 0x81);                     // Enable I2C and write 1 byte
		Thread.sleep(50);
		return ok;
	}

	public byte readSlave(byte register ) throws InterruptedException {
		byte[] data = readSlave(register,1);
		return data[0];
	}

	public byte[] readSlave(byte register, int byteCount ) throws InterruptedException {

		I2CProtocolHelper.writeToDevice(mpu9250, I2C_SLV0_ADDR,(byte) (AK8963_ADDRESS | 0x80));    // Set the I2C slave address of AK8963 and set for read.
		I2CProtocolHelper.writeToDevice(mpu9250, I2C_SLV0_REG, AK8963_ASAX);               // I2C slave 0 register address from where to begin data transfer
		I2CProtocolHelper.writeToDevice(mpu9250, I2C_SLV0_CTRL, (byte) (0x80 + byteCount));                     // Enable I2C and read 3 bytes
		Thread.sleep(500);
		byte[] rawData = new byte[byteCount];
		rawData = I2CProtocolHelper.readFromDevice(mpu9250,(byte) EXT_SENS_DATA_00, byteCount);        // Read the x-, y-, and z-axis calibration values

		return rawData;
	}

	@Deprecated
	private double readMagnetMeasurement(int lap, boolean displayInfo) throws InterruptedException  {

		System.out.println("Ver3 readMagnetMeasurement low filter - lap "+ lap  + " info: "+displayInfo);
		List<GyroDataStruc> dataList = new ArrayList<>();
		for(int i = 0;i<lap;i++ ) {
			GyroDataStruc gStruc = new GyroDataStruc();
			//			I2CProtocolHelper.writeToDevice(ak8963,(byte) AK8963_CNTL,(byte) 0b0001_0001); // single measurement and 16 bit output
			//			Thread.sleep(1000);

			int st1 = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_ST1);
			//	        System.out.println("STA_1 :"+ st1+  " " +  Integer.toBinaryString(st1));
			if ((st1&0x01) >0) { // true if last bit of byte is 1 
				byte[] magData = I2CProtocolHelper.readFromDevice(ak8963,AK8963_XOUT_L, 7);
				if (magData.length == 7) {
					//                    System.out.println("STA_2 :"+ magData[6] + " " +  Integer.toBinaryString(magData[6]));
					if (!((magData[6] & 0x08)> 0) ) {
						int m = 0;
						gStruc.setMagnX(ByteOperations.lsbmsbToInt(magData[m++],magData[m++]));
						gStruc.setMagnY(ByteOperations.lsbmsbToInt(magData[m++],magData[m++]));
						gStruc.setMagnZ(ByteOperations.lsbmsbToInt(magData[m++],magData[m++]));
						dataList.add(gStruc);
					}
					else {
						System.out.println("overflow: " );
					}
					//					System.out.printf("x- %8d %8d, y- %8d %8d, z- %8d %8d \n", magData[0],magData[1],magData[2],magData[3],magData[4],magData[5]);
					if (displayInfo) System.out.println(i +". " + gStruc.toMagString() + ", head-> " +MagnetometerHelper.magnetTeslaToDegree(gStruc.getMagnX(), gStruc.getMagnY()));
					//					 logger.log(Level.INFO,  gStruc.toString() + ", head-> " +MagnetometerHelper.convertMagnetTeslaToDegre(gStruc.getMagnXScaled(), gStruc.getMagnYScaled()));
				}
				else {
					System.out.println("Magnetometer status data is not received " + magData.length );
				}
			}
			else {
				System.out.println("magnetometer data is not ready.");
			}
			Thread.sleep(180);
		}
		List<Integer> arX = new ArrayList<>();
		List<Integer> arY = new ArrayList<>();
		List<Integer> arZ = new ArrayList<>();
		for (int i1 = 0;i1< dataList.size();i1 ++) {
			arX.add(dataList.get(i1).getMagnX());
			arY.add(dataList.get(i1).getMagnY());
			if (displayInfo)	arZ.add(dataList.get(i1).getMagnZ());
		}

		if (lap > 2) {
			arX = LowPassFilter.smoother(arX);
			arY = LowPassFilter.smoother(arY);
			if (displayInfo)	arZ = LowPassFilter.smoother(arZ);

//			System.out.println("after low pass filter");
//			for (int i1 =0; i1 < dataList.size();i1++) {
//				System.out.println(i1 +". " + arX.get(i1) + " " + arY.get(i1) + ", head-> " +MagnetometerHelper.magnetTeslaToDegree(arX.get(i1),arY.get(i1)));
//			}
		}
		
		double avgX=0,avgY=0 ,avgZ=0;
		avgX  =  arX.stream().mapToDouble(a-> a).average().getAsDouble();
		avgY =  arY.stream().mapToDouble(a-> a).average().getAsDouble();
//		avgZ =  arZ.stream().mapToDouble(a-> a).average().getAsDouble();
		double heading = MagnetometerHelper.magnetTeslaToDegree((int)Math.round(avgX),(int)Math.round(avgY));
		if (displayInfo) {

			double stdX,stdY,stdZ;
			stdX = MagnetometerHelper.standardDeviation(arX);
			stdY = MagnetometerHelper.standardDeviation(arY);
			stdZ = MagnetometerHelper.standardDeviation(arZ);
			System.out.println("Summary of data from this run.");
			System.out.printf("Count:   %5d \n", dataList.size() );
			System.out.printf("average: %8.4f  %8.4f %8.4f head facing %5.2f \n", avgX, avgY, avgZ, heading);
			System.out.printf("std Dev: %6.2f  %6.2f %6.2f\n", stdX, stdY, stdZ);
		}
		return heading;
	}

	
	public double getTargetHeading(double current, int degree) {
		double nHead = current + degree;
		if (degree > 0) nHead --;
		else nHead ++;
		if (nHead < 0) {
			nHead = 360 + nHead;
		}
		else if (nHead  > 360) {
			nHead = nHead - 360;
		}
		System.out.println("cur "+ current + " turn "+ degree + " target "+ nHead);
		return nHead;
	}

	// Fusemode registers contains adjustment rate for each axis of magnetometer.
	public void readAK8963FuseMode() {

		int test=0;
		try {
		
			int cntl = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_CNTL);
			System.out.println(" before AK8963_CNTL "   + Integer.toBinaryString(cntl));
//			I2CProtocolHelper.writeToDevice(ak8963,AK8963_CNTL,(byte) 31); // setup magnetic sensor to measure continuously   
			
			I2CProtocolHelper.writeToDevice(ak8963, AK8963_CNTL, (byte) 0b0001_1111 );
			Thread.sleep(200);
			cntl = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_CNTL);
			System.out.println("after AK8963_CNTL "   + Integer.toBinaryString(cntl));
			test = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_CNTL);
			System.out.println(" AK8963_CNTL  " +test +  " - " + Integer.toBinaryString(test) );

			test = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_ASAX);
			System.out.println(" AK8963_ASAX  " +test +  " - " + Integer.toBinaryString(test) );

			test = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_ASAY);
			System.out.println(" AK8963_ASAY  " +test +  " - " + Integer.toBinaryString(test) );
			 			
			test = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_ASAZ);
			System.out.println(" AK8963_ASAZ  " +test +  " - " + Integer.toBinaryString(test) );
			
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public double readMagnetemeter(int lap, boolean displayInfo) throws InterruptedException  {
		List<Integer> arX = new ArrayList<>();
		List<Integer> arY = new ArrayList<>();
		List<Integer> arZ = new ArrayList<>();

		System.out.println("Ver3 readMagnetometer low pass filter - lap "+ lap  + " info: "+displayInfo);
		List<GyroDataStruc> dataList = new ArrayList<>();
		for(int i = 0;i<lap;i++ ) {
			GyroDataStruc gStruc = new GyroDataStruc();
			//			I2CProtocolHelper.writeToDevice(ak8963,(byte) AK8963_CNTL,(byte) 0b0001_0001); // single measurement and 16 bit output
			//			Thread.sleep(1000);

			int st1 = I2CProtocolHelper.readFromDevice(ak8963,(byte) AK8963_ST1);
			//	        System.out.println("STA_1 :"+ st1+  " " +  Integer.toBinaryString(st1));
			if ((st1&0x01) >0) { // true if last bit of byte is 1 
				byte[] magData = I2CProtocolHelper.readFromDevice(ak8963,AK8963_XOUT_L, 7);
				if (magData.length == 7) {
					//                    System.out.println("STA_2 :"+ magData[6] + " " +  Integer.toBinaryString(magData[6]));
					if (!((magData[6] & 0x08)> 0) ) {
						int m = 0;
						arX.add( (int ) ByteOperations.lsbmsbToInt(magData[m++],magData[m++]));
						arY.add( (int) ByteOperations.lsbmsbToInt(magData[m++],magData[m++]));
						if (displayInfo) arZ.add( (int) ByteOperations.lsbmsbToInt(magData[m++],magData[m++]));

//						gStruc.setMagnX(ByteOperations.lsbmsbToInt(magData[m++],magData[m++]));
//						gStruc.setMagnY(ByteOperations.lsbmsbToInt(magData[m++],magData[m++]));
//						gStruc.setMagnZ(ByteOperations.lsbmsbToInt(magData[m++],magData[m++]));
//						dataList.add(gStruc);
					}
					else {
						System.out.println("overflow: " );
					}
					//					System.out.printf("x- %8d %8d, y- %8d %8d, z- %8d %8d \n", magData[0],magData[1],magData[2],magData[3],magData[4],magData[5]);
					if (displayInfo) System.out.println(i +". " + gStruc.toMagString() + ", head-> " +MagnetometerHelper.magnetTeslaToDegree(gStruc.getMagnX(), gStruc.getMagnY()));
					//					 logger.log(Level.INFO,  gStruc.toString() + ", head-> " +MagnetometerHelper.convertMagnetTeslaToDegre(gStruc.getMagnXScaled(), gStruc.getMagnYScaled()));
				}
				else {
					System.out.println("Magnetometer status data is not received " + magData.length );
				}
			}
			else {
				System.out.println("magnetometer data is not ready.");
			}
			Thread.sleep(180);
		}
//		for (int i1 = 0;i1< dataList.size();i1 ++) {
//			arX.add(dataList.get(i1).magnX);
//			arY.add(dataList.get(i1).magnY);
//			if (displayInfo)	arZ.add(dataList.get(i1).magnZ);
//		}

		if (lap > 1) {
			arX = LowPassFilter.smoother(arX);
			arY = LowPassFilter.smoother(arY);
			if (displayInfo)	arZ = LowPassFilter.smoother(arZ);
//			System.out.println("after low pass filter");
//			for (int i1 =0; i1 < dataList.size();i1++) {
//				System.out.println(i1 +". " + arX.get(i1) + " " + arY.get(i1) + ", head-> " +MagnetometerHelper.magnetTeslaToDegree(arX.get(i1),arY.get(i1)));
//			}
		}
		
		double avgX=0,avgY=0 ,avgZ=0;
		avgX  =  arX.stream().mapToDouble(a-> a).average().getAsDouble();
		avgY =  arY.stream().mapToDouble(a-> a).average().getAsDouble();
		double heading = MagnetometerHelper.magnetTeslaToDegree((int)Math.round(avgX),(int)Math.round(avgY));
		if (displayInfo) {

			double stdX,stdY,stdZ;
			avgZ =  arZ.stream().mapToDouble(a-> a).average().getAsDouble();
			stdX = MagnetometerHelper.standardDeviation(arX);
			stdY = MagnetometerHelper.standardDeviation(arY);
			stdZ = MagnetometerHelper.standardDeviation(arZ);
			System.out.println("Summary of data from this run.");
			System.out.printf("Count:   %5d \n", dataList.size() );
			System.out.printf("average: %8.4f  %8.4f %8.4f head facing %5.2f \n", avgX, avgY, avgZ, heading);
			System.out.printf("std Dev: %6.2f  %6.2f %6.2f\n", stdX, stdY, stdZ);
		}
		return heading;
	}
	
}
