package cho.raspi.map;

/**
 * @author: Cheung Ho
 *
 */

public interface AK8963_Map {

	
	// this address will need to retrieve from this ak8963ID  
	public static final byte  AK8963_ADDRESS  = 0x0C; 

	public static final byte  WHO_AM_I_AK8963 = 0x00 ;// should return 0x48
	public static final byte  AK8963_INFO     = 0x01;
	public static final byte  AK8963_ST1      = 0x02  ;// data ready status bit 0
	public static final byte  AK8963_XOUT_L   = 0x03 ; // data
	public static final byte  AK8963_XOUT_H   = 0x04;
	public static final byte  AK8963_YOUT_L   = 0x05;
	public static final byte  AK8963_YOUT_H   = 0x06;
	public static final byte  AK8963_ZOUT_L   = 0x07;
	public static final byte  AK8963_ZOUT_H   = 0x08;
	public static final byte  AK8963_ST2      = 0x09 ; // Data overflow bit 3 and data read error status bit 2
	public static final byte  AK8963_CNTL     = 0x0A;  // Power down (0000), single-measurement (0001), 
	// continuous measure 1 (0010), continuous measure 2 (0110), self-test (1000) and Fuse ROM (1111) modes on bits 3:0
	
	public static final byte  AK8963_CNTL2    = 0x0B;  // Reset
	public static final byte  AK8963_ASTC     = 0x0C;  // Self test control
	public static final byte  AK8963_I2CDIS   = 0x0F;  // I2C disable
	public static final byte  AK8963_ASAX     = 0x10  ;// Fuse ROM x-axis sensitivity adjustment value
	public static final byte  AK8963_ASAY     = 0x11 ; // Fuse ROM y-axis sensitivity adjustment value
	public static final byte  AK8963_ASAZ     = 0x12;  // Fuse ROM z-axis sensitivity adjustment value

	public static final byte  SELF_TEST_X_GYRO = 0x00  ;                
	public static final byte  SELF_TEST_Y_GYRO = 0x01 ;                                                                         
	public static final byte  SELF_TEST_Z_GYRO = 0x02;

/*	public static final byte  X_FINE_GAIN      0x03 // [7:0] fine gain
	public static final byte  Y_FINE_GAIN      0x04
	public static final byte  Z_FINE_GAIN      0x05
	public static final byte  XA_OFFSET_H      0x06 // User-defined trim values for accelerometer
	public static final byte  XA_OFFSET_L_TC   0x07
	public static final byte  YA_OFFSET_H      0x08
	public static final byte  YA_OFFSET_L_TC   0x09
	public static final byte  ZA_OFFSET_H      0x0A
	public static final byte  ZA_OFFSET_L_TC   0x0B */

	public static final byte  SELF_TEST_X_ACCEL = 0x0D;
	public static final byte  SELF_TEST_Y_ACCEL =0x0E ;   
	public static final byte  SELF_TEST_Z_ACCEL =0x0F;

	public static final byte  SELF_TEST_A      =0x10;

	public static final byte  XG_OFFSET_H      =0x13 ; // User-defined trim values for gyroscope
	public static final byte  XG_OFFSET_L      =0x14;
	public static final byte  YG_OFFSET_H      =0x15;
	public static final byte  YG_OFFSET_L      =0x16;
	public static final byte  ZG_OFFSET_H      =0x17;
	public static final byte  ZG_OFFSET_L      =0x18;
	public static final byte  SMPLRT_DIV       =0x19;
	public static final byte  CONFIG           =0x1A;
	public static final byte  GYRO_CONFIG      =0x1B;
	public static final byte  ACCEL_CONFIG     =0x1C;
	public static final byte  ACCEL_CONFIG2    =0x1D;
	public static final byte  LP_ACCEL_ODR     =0x1E ;  
	public static final byte  WOM_THR          =0x1F;   

	public static final byte  MOT_DUR         = 0x20  ;// Duration counter threshold for motion interrupt generation, 1 kHz rate, LSB = 1 ms
	public static final byte  ZMOT_THR        = 0x21 ; // Zero-motion detection threshold bits [7:0]
	public static final byte  ZRMOT_DUR       = 0x22;  // Duration counter threshold for zero motion interrupt generation, 16 Hz rate, LSB = 64 ms

	public static final byte  FIFO_EN         = 0x23;
	public static final byte  I2C_MST_CTRL    = 0x24 ;  
	public static final byte  I2C_SLV0_ADDR   = 0x25;
	public static final byte  I2C_SLV0_REG    = 0x26;
	public static final byte  I2C_SLV0_CTRL   = 0x27;
	public static final byte  I2C_SLV1_ADDR   = 0x28;
	public static final byte  I2C_SLV1_REG    = 0x29;
	public static final byte  I2C_SLV1_CTRL   = 0x2A;
	public static final byte  I2C_SLV2_ADDR   = 0x2B;
	public static final byte  I2C_SLV2_REG    = 0x2C;
	public static final byte  I2C_SLV2_CTRL   = 0x2D;
	public static final byte  I2C_SLV3_ADDR   = 0x2E;
	public static final byte  I2C_SLV3_REG    = 0x2F;
	public static final byte  I2C_SLV3_CTRL   = 0x30;
	public static final byte  I2C_SLV4_ADDR   = 0x31;
	public static final byte  I2C_SLV4_REG    = 0x32;
	public static final byte  I2C_SLV4_DO     = 0x33;
	public static final byte  I2C_SLV4_CTRL   = 0x34;
	public static final byte  I2C_SLV4_DI     = 0x35;
	public static final byte  I2C_MST_STATUS  = 0x36;
	public static final byte  INT_PIN_CFG     = 0x37;
	public static final byte  INT_ENABLE      = 0x38;
	public static final byte  DMP_INT_STATUS  = 0x39 ; // Check DMP interrupt
	public static final byte  INT_STATUS      = 0x3A;
	public static final byte  ACCEL_XOUT_H    = 0x3B;
	public static final byte  ACCEL_XOUT_L    = 0x3C;
	public static final byte  ACCEL_YOUT_H    = 0x3D;
	public static final byte  ACCEL_YOUT_L    = 0x3E;
	public static final byte  ACCEL_ZOUT_H    = 0x3F;
	public static final byte  ACCEL_ZOUT_L    = 0x40;
	public static final byte  TEMP_OUT_H      = 0x41;
	public static final byte  TEMP_OUT_L      = 0x42;
	public static final byte  GYRO_XOUT_H     = 0x43;
	public static final byte  GYRO_XOUT_L     = 0x44;
	public static final byte  GYRO_YOUT_H     = 0x45;
	public static final byte  GYRO_YOUT_L     = 0x46;
	public static final byte  GYRO_ZOUT_H     = 0x47;
	public static final byte  GYRO_ZOUT_L     = 0x48;
	public static final byte  EXT_SENS_DATA_00= 0x49;
	public static final byte  EXT_SENS_DATA_01 =0x4A;
	public static final byte  EXT_SENS_DATA_02 =0x4B;
	public static final byte  EXT_SENS_DATA_03 =0x4C;
	public static final byte  EXT_SENS_DATA_04 =0x4D;
	public static final byte  EXT_SENS_DATA_05 =0x4E;
	public static final byte  EXT_SENS_DATA_06 =0x4F;
	public static final byte  EXT_SENS_DATA_07 =0x50;
	public static final byte  EXT_SENS_DATA_08 =0x51;
	public static final byte  EXT_SENS_DATA_09 =0x52;
	public static final byte  EXT_SENS_DATA_10 =0x53;
	public static final byte  EXT_SENS_DATA_11 =0x54;
	public static final byte  EXT_SENS_DATA_12 =0x55;
	public static final byte  EXT_SENS_DATA_13 =0x56;
	public static final byte  EXT_SENS_DATA_14 =0x57;
	public static final byte  EXT_SENS_DATA_15 =0x58;
	public static final byte  EXT_SENS_DATA_16 =0x59;
	public static final byte  EXT_SENS_DATA_17 =0x5A;
	public static final byte  EXT_SENS_DATA_18 =0x5B;
	public static final byte  EXT_SENS_DATA_19 =0x5C;
	public static final byte  EXT_SENS_DATA_20 =0x5D;
	public static final byte  EXT_SENS_DATA_21 =0x5E;
	public static final byte  EXT_SENS_DATA_22 =0x5F;
	public static final byte  EXT_SENS_DATA_23 =0x60;
	public static final byte  MOT_DETECT_STATUS= 0x61;
	public static final byte  I2C_SLV0_DO      =0x63;
	public static final byte  I2C_SLV1_DO      =0x64;
	public static final byte  I2C_SLV2_DO      =0x65;
	public static final byte  I2C_SLV3_DO      =0x66;
	public static final byte  I2C_MST_DELAY_CTRL =0x67;
	public static final byte  SIGNAL_PATH_RESET  =0x68;
	public static final byte  MOT_DETECT_CTRL = 0x69;
	public static final byte  USER_CTRL       = 0x6A;  // Bit 7 enable DMP, bit 3 reset DMP
	public static final byte  PWR_MGMT_1      = 0x6B; // Device defaults to the SLEEP mode
	public static final byte  PWR_MGMT_2      = 0x6C;
	public static final byte  DMP_BANK        = 0x6D ; // Activates a specific bank in the DMP
	public static final byte  DMP_RW_PNT      = 0x6E ; // Set read/write pointer to a specific start address in specified DMP bank
	public static final byte  DMP_REG         = 0x6F ; // Register in DMP from which to read or to which to write
	public static final byte  DMP_REG_1       = 0x70;
	public static final byte  DMP_REG_2       = 0x71 ;
	public static final byte  FIFO_COUNTH     = 0x72;
	public static final byte  FIFO_COUNTL     = 0x73;
	public static final byte  FIFO_R_W        = 0x74;
	public static final byte  WHO_AM_I_MPU9250 = 0x75 ;// Should return 0x71
	public static final byte  XA_OFFSET_H      = 0x77;
	public static final byte  XA_OFFSET_L      =0x78;
	public static final byte  YA_OFFSET_H      =0x7A;
	public static final byte  YA_OFFSET_L      =0x7B;
	public static final byte  ZA_OFFSET_H      =0x7D;
	public static final byte  ZA_OFFSET_L      =0x7E;

// Set initial input parameters
	public static final byte   AFS_2G = 0;
	public static final byte   AFS_4G = 1;
	public static final byte   AFS_8G = 2;
	public static final byte   AFS_16G= 3;

	public static final byte   GFS_250DPS = 0;
	public static final byte   GFS_500DPS = 1;
	public static final byte   GFS_1000DPS= 2;
	public static final byte   GFS_2000DPS= 3;

	public static final byte   MFS_14BITS = 0; // 0.6 mG per LSB
	public static final byte   MFS_16BITS = 1;    // 0.15 mG per LSB

	public static final byte  M_8Hz  = 0x02;
	public static final byte  M_100Hz= 0x06;
	
}
