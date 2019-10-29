package cho.raspi.map;

/**
 * @author: Cheung Ho
 *
 */

public interface MPU9250_MapValues {

	 /**
	 * Just wakes the device up, because it sets the 
	 * sleep bit to 0. Also sets
	 * the clock source to internal.
	 */
	 public static final byte MPU9250_RA_PWR_MGMT_1_VALUE = 0b00000000;
	 /**
	 * Sets the full scale range of the gyroscopes to ± 2000 °/s
	 */
	 public static final byte MPU9250_RA_GYRO_CONFIG_VALUE = 0b00011000;
	 /**
	 * Sets the sample rate divider for the gyroscopes and 
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
	 public static final byte MPU9250_RA_SMPLRT_DIV_VALUE = 0b00000000;
	 /**
	  * refer to document : MPU-6000-Register-Map.pdf
	 * Setting the digital low pass filter (DLPF) to
	 * Acc Bandwidth (Hz) = 184
	 * Acc Delay (ms) = 2.0
	 * Gyro Bandwidth (Hz) = 188
	 * Gyro Delay (ms) = 1.9
	 * Fs (kHz) = 1
	 *
	 */
	 public static final byte MPU9250_RA_CONFIG_VALUE = 0b00000001;
	 /**
	 * Setting accelerometer sensitivity to ± 2g
	 */
	 public static final byte MPU9250_RA_ACCEL_CONFIG_VALUE = 0b00000000;
	 /**
	 * Disabling FIFO buffer
	 */
	 public static final byte MPU9250_RA_FIFO_EN_VALUE = 0b00000000;
	 /**
	 * Disabling interrupts
	 */
	 public static final byte MPU9250_RA_INT_ENABLE_VALUE = 0b00000001;
	 /**
	 * Disabling standby modes
	 */
	 public static final byte MPU9250_RA_PWR_MGMT_2_VALUE = 0b00000000;	 
	
	 
	 public static final byte GYRO_X_OFFSET  = 0;
	 public static final byte GYRO_Y_OFFSET  = 0;
	 public static final byte GYRO_Z_OFFSET  = 0;
	 
	 public static final byte ACCEL_X_OFFSET  = 0;
	 public static final byte ACCEL_Y_OFFSET  = 0;
	 public static final byte ACCEL_Z_OFFSET  = 0;
	 
	 public static final byte MAGNT_X_OFFSET  = 0;
	 public static final byte MAGNT_Y_OFFSET  = 0;
	 public static final byte MAGNT_Z_OFFSET  = 0;

	 
	 
	 public static final int GYRO_DIVISOR = 131;
	 public static final int ACCEL_DIVISOR = 16384;
	 
}
