package cho.raspi.map;

/**
 * @author: Cheung Ho
 *
 */

public interface MPU9250_Map {

	
	/**
	* Just wakes the device up, because it sets the 
	* sleep bit to 0. Also sets
	* the clock source to internal.
	*/
	public final byte MPU9250_RA_PWR_MGMT_1 = 107; 	// (hex 0x6B ) 

	public final byte MPU9250_RA_WHO_AM_I = 117; 	// (hex 0x75 ) 

	
	public final byte MPU9250_RA_INFORMATION = 1; 	// (hex 0x01 ) 
	
	public final byte MPU9250_RA_STATUS_1 = 2; 	// (hex 0x02 ) 

	public final byte MPU9250_RA_STATUS_2 = 9; 	// (hex 0x09 ) 
	
	
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
	public final byte MPU9250_RA_SMPLRT_DIV = 25; 	// (hex 0x19 )

	
	/**
	* Setting the digital low pass filter to
	* Acc Bandwidth (Hz) = 184
	* Acc Delay (ms) = 2.0
	* Gyro Bandwidth (Hz) = 188
	* Gyro Delay (ms) = 1.9
	* Fs (kHz) = 1
	*
	*/
	public final byte MPU9250_RA_CONFIG = 26; 		// (hex 0x1A )

	/**
	* Sets the full scale range of the gyroscopes to ± 2000 °/s
	*/
	public final byte MPU9250_RA_GYRO_CONFIG = 27; 	// (hex 0x1B )
	
	/**
	* Setting accelerometer sensitivity to ± 2g
	*/
	public final byte MPU9250_RA_ACCEL_CONFIG = 28;	// (hex 0x1C )

	/**
	* Disabling interrupts
	*/
	public final byte MPU9250_RA_INT_ENABLE = 56;	// (hex 0x38 )

	/**
	* Disabling standby modes
	*/	
	public final byte MPU9250_RA_PWR_MGMT_2 = 108; 	// (hex 0x6C )
	
	/**
	* Disabling FIFO buffer
	*/
	public final byte MPU9250_RA_FIFO_EN = 35; // (HEX 0X23)
	
	
	
	
	//// Test reading from these registers
	
	
	/**
	* 16-bit 2’s complement value. Stores the most recent X axis accelerometer
	* measurement.
	*/
	public static final byte MPU9250_RA_ACCEL_XOUT_H = 59; // (HEX 0X2B)
	/**
	* 16-bit 2’s complement value. Stores the most recent X axis accelerometer
	* measurement.
	*/
	public static final byte MPU9250_RA_ACCEL_XOUT_L = 60; // (HEX 0X2C)
	/**
	* 16-bit 2’s complement value. Stores the most recent Y axis accelerometer
	* measurement.
	*/
	public static final byte MPU9250_RA_ACCEL_YOUT_H = 61;
	/**
	* 16-bit 2’s complement value. Stores the most recent Y axis accelerometer
	* measurement.
	*/
	public static final byte MPU9250_RA_ACCEL_YOUT_L = 62;
	/**
	* 16-bit 2’s complement value. Stores the most recent Z axis accelerometer
	* measurement.
	*/
	public static final byte MPU9250_RA_ACCEL_ZOUT_H = 63;
	/**
	* 16-bit 2’s complement value. Stores the most recent Z axis accelerometer
	* measurement.
	*/
	public static final byte MPU9250_RA_ACCEL_ZOUT_L = 64;
	/**
	* 16-bit signed value. Stores the most recent temperature sensor
	* measurement.
	*/
	public static final byte MPU9250_RA_TEMP_OUT_H = 65;
	/**
	* 16-bit signed value. Stores the most recent temperature sensor
	* measurement.
	*/
	public static final byte MPU9250_RA_TEMP_OUT_L = 66;
	/**
	* 16-bit 2’s complement value. Stores the most recent X axis gyroscope
	* measurement.
	*/
	public static final byte MPU9250_RA_GYRO_XOUT_H = 67;
	/**
	* 16-bit 2’s complement value. Stores the most recent X axis gyroscope
	* measurement.
	*/
	public static final byte MPU9250_RA_GYRO_XOUT_L = 68;
	/**
	* 16-bit 2’s complement value. Stores the most recent Y axis gyroscope
	* measurement.
	*/
	public static final byte MPU9250_RA_GYRO_YOUT_H = 69;
	/**
	* 16-bit 2’s complement value. Stores the most recent Y axis gyroscope
	* measurement.
	*/
	public static final byte MPU9250_RA_GYRO_YOUT_L = 70;
	/**
	* 16-bit 2’s complement value. Stores the most recent Z axis gyroscope
	* measurement.
	*/
	public static final byte MPU9250_RA_GYRO_ZOUT_H = 71;
	/**
	* 16-bit 2’s complement value. Stores the most recent Z axis gyroscope
	* measurement.
	*/
	public static final byte MPU9250_RA_GYRO_ZOUT_L = 72;
	

	/* following registers are magnetometer output 
	 * 
	 */

/*	
	// the last bit of this register indicates whether the data is ready to be read.
	// 0 = process started, 1 = data is ready
	public static final byte MPU9250_RA_MAGNET_ST1 = 2;
	
	
	public static final byte MPU9250_RA_MAGNET_HXL = 3;
	
	public static final byte MPU9250_RA_MAGNET_HXH = 4;
	
	public static final byte MPU9250_RA_MAGNET_HYL = 5;
	
	public static final byte MPU9250_RA_MAGNET_HYH = 6;

	public static final byte MPU9250_RA_MAGNET_HZL = 7;
	
	public static final byte MPU9250_RA_MAGNET_HZH = 8;
*/
	
	
/* below data are for MPU9250
 * 
 * 	
 */
	
	
	
/*
* //Magnetometer
	public static final byte  MAG_AD 0xC
	public static final byte  WIA_AD 0x00
	public static final byte  INFO 0x01
	public static final byte  STATUS_1_AD 0x02
	public static final byte  HXL_AD 0x03    //X-axis measurement data lower 8bit
	public static final byte  HXH_AD 0x04    //X-axis measurement data higher 8bit
	public static final byte  HYL_AD 0x05    //Y-axis measurement data lower 8bit
	public static final byte  HYH_AD 0x06    //Y-axis measurement data higher 8bit
	public static final byte  HZL_AD 0x07    //Z-axis measurement data lower 8bit
	public static final byte  HZH_AD 0x08    //Z-axis measurement data higher 8bit
	public static final byte  STATUS_2_AD 0x09
	public static final byte  CNTL1_AD 0x0A   //control 1
	public static final byte  CNTL2_AD 0x0B   //control 2
	public static final byte  ASTC_AD 0x0C    //Self-Test Control
	public static final byte  TS1_AD 0x0D    //test 1
	public static final byte  TS2_AD 0x0E   //test 2
	public static final byte  I2CDIS_AD 0x0F    //I2C disable
	public static final byte  ASAX_AD 0x10    //Magnetic sensor X-axis sensitivity adjustment value
	public static final byte  ASAY_AD 0x11    //Magnetic sensor Y-axis sensitivity adjustment value
	public static final byte  ASAZ_AD 0x12    //Magnetic sensor Z-axis sensitivity adjustment value
	public static final byte  MAGNE_SENS 6.67f
	public static final byte  SCALE 0.1499f  // 4912/32760 uT/tick
	public static final byte  DATA_READY 0x01
	public static final byte  MAGIC_OVERFLOW 0x8
*/	
	
}
