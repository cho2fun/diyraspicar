package cho.raspi.map;

/**
 * @author: Cheung Ho
 *
 */

public interface MPU6050_Map {

	
	/**
	* Just wakes the device up, because it sets the 
	* sleep bit to 0. Also sets
	* the clock source to internal.
	*/
	public final byte MPU6050_RA_PWR_MGMT_1 = 107; 	// (hex 0x6B ) 

	
	/**
	* Sets the smaple rate divider for the gyroscopes and 
	* accelerometers. This
	* means
	* acc-rate = 1kHz / 1+ sample-rate
	* and
	* gyro-rate = 8kHz /
	* 1+ sample-rate.
	* The concrete value 0 leaves the sample rate on
	* default, which means 1kHz for acc-rate and 
	* 8kHz for gyr-rate.
	*/
	public final byte MPU6050_RA_SMPLRT_DIV = 25; 	// (hex 0x19 )

	
	/**
	* Setting the digital low pass filter to
	* Acc Bandwidth (Hz) = 184
	* Acc Delay (ms) = 2.0
	* Gyro Bandwidth (Hz) = 188
	* Gyro Delay (ms) = 1.9
	* Fs (kHz) = 1
	*
	*/
	public final byte MPU6050_RA_CONFIG = 26; 		// (hex 0x1A )

	/**
	* Sets the full scale range of the gyroscopes to ± 2000 °/s
	*/
	public final byte MPU6050_RA_GYRO_CONFIG = 27; 	// (hex 0x1B )
	
	/**
	* Setting accelerometer sensitivity to ± 2g
	*/
	public final byte MPU6050_RA_ACCEL_CONFIG = 28;	// (hex 0x1C )

	/**
	* Disabling interrupts
	*/
	public final byte MPU6050_RA_INT_ENABLE = 56;	// (hex 0x38 )

	/**
	* Disabling standby modes
	*/	
	public final byte MPU6050_RA_PWR_MGMT_2 = 108; 	// (hex 0x6C )
	
	/**
	* Disabling FIFO buffer
	*/
	public final byte MPU6050_RA_FIFO_EN = 35; // (HEX 0X23)
	
	
	
	
	//// Test reading from these registers
	
	
	/**
	* 16-bit 2’s complement value. Stores the most recent X axis accelerometer
	* measurement.
	*/
	public static final byte MPU6050_RA_ACCEL_XOUT_H = 59;
	/**
	* 16-bit 2’s complement value. Stores the most recent X axis accelerometer
	* measurement.
	*/
	public static final byte MPU6050_RA_ACCEL_XOUT_L = 60;
	/**
	* 16-bit 2’s complement value. Stores the most recent Y axis accelerometer
	* measurement.
	*/
	public static final byte MPU6050_RA_ACCEL_YOUT_H = 61;
	/**
	* 16-bit 2’s complement value. Stores the most recent Y axis accelerometer
	* measurement.
	*/
	public static final byte MPU6050_RA_ACCEL_YOUT_L = 62;
	/**
	* 16-bit 2’s complement value. Stores the most recent Z axis accelerometer
	* measurement.
	*/
	public static final byte MPU6050_RA_ACCEL_ZOUT_H = 63;
	/**
	* 16-bit 2’s complement value. Stores the most recent Z axis accelerometer
	* measurement.
	*/
	public static final byte MPU6050_RA_ACCEL_ZOUT_L = 64;
	/**
	* 16-bit signed value. Stores the most recent temperature sensor
	* measurement.
	*/
	public static final byte MPU6050_RA_TEMP_OUT_H = 65;
	/**
	* 16-bit signed value. Stores the most recent temperature sensor
	* measurement.
	*/
	public static final byte MPU6050_RA_TEMP_OUT_L = 66;
	/**
	* 16-bit 2’s complement value. Stores the most recent X axis gyroscope
	* measurement.
	*/
	public static final byte MPU6050_RA_GYRO_XOUT_H = 67;
	/**
	* 16-bit 2’s complement value. Stores the most recent X axis gyroscope
	* measurement.
	*/
	public static final byte MPU6050_RA_GYRO_XOUT_L = 68;
	/**
	* 16-bit 2’s complement value. Stores the most recent Y axis gyroscope
	* measurement.
	*/
	public static final byte MPU6050_RA_GYRO_YOUT_H = 69;
	/**
	* 16-bit 2’s complement value. Stores the most recent Y axis gyroscope
	* measurement.
	*/
	public static final byte MPU6050_RA_GYRO_YOUT_L = 70;
	/**
	* 16-bit 2’s complement value. Stores the most recent Z axis gyroscope
	* measurement.
	*/
	public static final byte MPU6050_RA_GYRO_ZOUT_H = 71;
	/**
	* 16-bit 2’s complement value. Stores the most recent Z axis gyroscope
	* measurement.
	*/
	public static final byte MPU6050_RA_GYRO_ZOUT_L = 72;
	
	
	
}
