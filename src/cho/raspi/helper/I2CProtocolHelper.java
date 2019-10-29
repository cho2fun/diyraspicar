package cho.raspi.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import testGPIO.ByteOperations;
import testGPIO.GyroDataStruc;

public class I2CProtocolHelper {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	   public static int readFromDevice( I2CDevice device, byte register) {
	    	int result = 0;
	    	try {
				result = device.read( register);
	            Thread.sleep(20);
//				System.out.println("received "+ result);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return result  ;
	    }

	    public static int readFromDevice(I2CDevice device, int register) {
	    	int data = 0;
	    	try {
				data = device.read((byte)register);
	            Thread.sleep(20);
//				System.out.println("Reg "+ register +  "  received "+ data);
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
	    public static byte[] readFromDevice(I2CDevice device,int register, int count) {
	    	byte[] rawData = new byte[count];
	    	try {
	    		device.read(register, rawData, 0, count);
			} catch (IOException  e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    	return rawData;
	    }
	    
	    
	    public static void writeToDevice(I2CDevice device,byte register,byte value) {
	    	
	    	try {
				device.write(register,value );
	            Thread.sleep(200);
//				System.out.println("sent data to register:"+ register);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }

    
	    public static void scanDevice() throws Exception {
	        List<Integer> validAddresses = new ArrayList<Integer>();
	        final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
	        for (int i = 1; i < 128; i++) {
	          try {
	              I2CDevice device  = bus.getDevice(i);
	              device.write((byte)0);
	             validAddresses.add(i);
	          } catch (Exception ignore) { }
	        }

	        System.out.println("Found: ---");
	        for (int a : validAddresses) {
	            System.out.println("Address: " + Integer.toHexString(a));
	        }
	        System.out.println("----------");
	    }
}
