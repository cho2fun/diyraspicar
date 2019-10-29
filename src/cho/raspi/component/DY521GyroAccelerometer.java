package cho.raspi.component;

import cho.raspi.GyroDataStruc;
import cho.raspi.map.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.OptionalDouble;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * @author: Cheung Ho
 *
 */
public class DY521GyroAccelerometer extends Thread implements MPU6050_Map, MPU6050_MapValues {
	
	

	static int DEVICE_ADDR = 0X68;  // device address on i2c bus
	
	
	 private static I2CBus bus = null;
	 private static I2CDevice mpu6050 = null;
	 private GyroDataStruc gStruc = null;
	 private ArrayList<Double> dataList = new ArrayList<>();
	 
	 
    public static void main(String[] args) {
    	
    	DY521GyroAccelerometer gyro = new DY521GyroAccelerometer();
    	if ("R".equals(args[0])) {
    		gyro.readFromDevice();
    	}
    	else if ("W".equals(args[0])) {
    		gyro.writeToDevice();
    	}
    	else if ("T".equals(args[0])) {
    		int lap = -1;
    		gyro.selfTest();
    		if (args.length == 2) lap = Integer.parseInt(args[1]);
    		gyro.testDevice(lap);
    		gyro.testDevice(10);
    	}
    	else if ("BR".equals(args[0])) {
    		int lap = -1;
    		if (args.length == 2) lap = Integer.parseInt(args[1]);
    		gyro.burstReadFromDevice(lap);
    	}
    }

    
    public void run() {
    	
    	long startTime = System.currentTimeMillis();   	
		burstReadFromDevice(-1);
    	long stopTime = System.currentTimeMillis();   	
		OptionalDouble average = getAverageTurnInDegree();
		double degreeTurned = this.getTurnInDegree(startTime, stopTime, average.getAsDouble()); 
    	System.out.printf("degrees turned  %8.3f \n",  degreeTurned );
		
    	
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

    
    public void readFromDevice() {
    	
    	try {
			int data = mpu6050.read((byte)0x0d);
            Thread.sleep(500);
			System.out.println("received "+ data);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

    public int readFromDevice(int register) {
    	int data = 0;
    	try {
			data = mpu6050.read((byte)register);
            Thread.sleep(20);
//			System.out.println("Reg "+ register +  "  received "+ data);
		} catch (IOException | InterruptedException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return data;
    }


    /*
     * parameter: Starting register address
     * 			: number of consecutive address
     * return : byte array correspondence to parameter of count
     */
    public byte[] readFromDevice(int register, int count) {
    	byte[] rawData = new byte[count];
//    	int data = 0;
    	try {
//    		for (int i = 0;i< count;i++ ) {
    		
    		mpu6050.read(register, rawData, 0, count);
//			System.out.println("Reg "+ register +  "  received "+ data);
		} catch (IOException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return rawData;
    }
    
    
    public void writeToDevice() {
    	
    	try {
			mpu6050.write((byte)0x0d, (byte)0xff);
            Thread.sleep(500);
			System.out.println("sent data");
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    

    public void burstReadFromDevice(int cycle) {
    	System.out.println(" Burst Read test started!");
    	int position = 0;
    	int lap = 0;
    	while (true) {
    		
        	byte[] rawData = this.readFromDevice(MPU6050_RA_ACCEL_XOUT_H, 14);
        	int i = 0;
        	gStruc = new GyroDataStruc();
        	if (rawData.length == 14) {
//        		System.out.println("RA_ACCEL ");
        		gStruc.setAcclX(this.msblsbToInt(rawData[i++],rawData[i++]));
        		gStruc.setAcclY(this.msblsbToInt(rawData[i++],rawData[i++]));
        		gStruc.setAcclZ(this.msblsbToInt(rawData[i++],rawData[i++]));
//        		System.out.println("RA_TEMP ");
        		gStruc.setTemperature(this.msblsbToInt(rawData[i++], rawData[i++]));
//        		System.out.println("RA_GYRO ");
        		gStruc.setGyroX(this.msblsbToInt(rawData[i++],rawData[i++]));
        		gStruc.setGyroY(this.msblsbToInt(rawData[i++],rawData[i++]));
        		gStruc.setGyroZ(this.msblsbToInt(rawData[i++],rawData[i++]));
        		System.out.println("P " + ( position += gStruc.getGyroZScaled()) + " " + gStruc.toString());
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
    		msb = this.readFromDevice(MPU6050_RA_ACCEL_XOUT_H);
    		lsb = this.readFromDevice(MPU6050_RA_ACCEL_XOUT_L);
    		gStruc.setAcclX(this.msblsbToInt(msb, lsb));
    		msb = this.readFromDevice(MPU6050_RA_ACCEL_YOUT_H);
    		lsb = this.readFromDevice(MPU6050_RA_ACCEL_YOUT_L);
    		gStruc.setAcclY(this.msblsbToInt(msb, lsb));
    		msb = this.readFromDevice(MPU6050_RA_ACCEL_ZOUT_H);
    		lsb = this.readFromDevice(MPU6050_RA_ACCEL_ZOUT_L);
    		gStruc.setAcclZ(this.msblsbToInt(msb, lsb));
//    		System.out.println("RA_TEMP ");
    		msb = this.readFromDevice(MPU6050_RA_TEMP_OUT_H);
    		lsb = this.readFromDevice(MPU6050_RA_TEMP_OUT_L);
    		gStruc.setTemperature(this.msblsbToInt(msb, lsb));
//    		System.out.println("RA_GYRO ");

    		msb = this.readFromDevice(MPU6050_RA_GYRO_XOUT_H);
    		lsb = this.readFromDevice(MPU6050_RA_GYRO_XOUT_L);
    		gStruc.setGyroX(this.msblsbToInt(msb, lsb));
    		msb = this.readFromDevice(MPU6050_RA_GYRO_YOUT_H);
    		lsb = this.readFromDevice(MPU6050_RA_GYRO_YOUT_L);
    		gStruc.setGyroY(this.msblsbToInt(msb, lsb));
    		msb = this.readFromDevice(MPU6050_RA_GYRO_ZOUT_H);
    		lsb = this.readFromDevice(MPU6050_RA_GYRO_ZOUT_L);
    		gStruc.setGyroZ(this.msblsbToInt(msb, lsb));

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
			mpu6050.write(MPU6050_RA_PWR_MGMT_1_VALUE, pwr_sleep);
			Thread.sleep(5000);
	    	pwr_sleep = 0b00001000; // wake up and disable temperature
			mpu6050.write(MPU6050_RA_PWR_MGMT_1_VALUE, pwr_sleep);
	    	System.out.println("set gyroscope to wake up vis PWR_MGMT_1" );

    	} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
    public int msblsbToInt(byte msb, byte lsb) {
    	
   	    int result = (short)(  msb  << 8 ) +  lsb ;
//   	    System.out.println(msb + " | " + lsb + " = "+ result);
   	    return result;
   }

    public int msblsbToInt(int msb, int lsb) {
    	
//    	 int msb = 0xFF;
//    	    int lsb = 0xFF;
    	    int result = (short)(  msb  << 8 ) +  lsb ;
//    	    System.out.println(msb + " | " + lsb + " = "+ result);
    	    return result;
    }
    
    // configure device
	public DY521GyroAccelerometer() {
		
		try {
			
			bus = I2CFactory.getInstance(I2CBus.BUS_1);
			mpu6050 = bus.getDevice(DEVICE_ADDR);
			
			System.out.println("I2C Bus Initialization completed. "+ mpu6050.toString());
			
			this.configuraton();
			
		} catch (UnsupportedBusNumberException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	

    // configure device
	public DY521GyroAccelerometer(Deque _deque) {
		super();
	}
	
	
	public void configuraton() {
		
		try {
			mpu6050.write(MPU6050_RA_PWR_MGMT_1,MPU6050_RA_PWR_MGMT_1_VALUE);
			mpu6050.write(MPU6050_RA_GYRO_CONFIG,MPU6050_RA_GYRO_CONFIG_VALUE );
			mpu6050.write(MPU6050_RA_FIFO_EN,MPU6050_RA_INT_ENABLE_VALUE);
			mpu6050.write(MPU6050_RA_ACCEL_CONFIG,MPU6050_RA_ACCEL_CONFIG_VALUE);
			mpu6050.write(MPU6050_RA_INT_ENABLE,MPU6050_RA_INT_ENABLE_VALUE);
			mpu6050.write(MPU6050_RA_PWR_MGMT_2,MPU6050_RA_PWR_MGMT_2_VALUE);
			mpu6050.write(MPU6050_RA_SMPLRT_DIV,MPU6050_RA_SMPLRT_DIV_VALUE);
			mpu6050.write(MPU6050_RA_CONFIG,MPU6050_RA_CONFIG_VALUE);
			System.out.println("configuraitons completed. ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
	
	public boolean selfTest() {
		boolean ok = false;
		System.out.println("self test started.");
		try {
			byte MPU6050_RA_GYRO_CONFIG_SELFTEST = (byte) 0b10011000;

			mpu6050.write(MPU6050_RA_GYRO_CONFIG,MPU6050_RA_GYRO_CONFIG_SELFTEST );
            int cnt = 0;
            while (true) {
                int dataRead = mpu6050.read();
                System.out.println(++cnt  +". Read " + dataRead + " via I2C");
//                System.out.println("Waiting .5 seconds");
                Thread.sleep(500);
            }
		} catch (IOException | InterruptedException e) {
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

    
}
