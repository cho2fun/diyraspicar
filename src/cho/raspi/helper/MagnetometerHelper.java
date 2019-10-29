package cho.raspi.helper;

import java.util.List;
import java.util.OptionalDouble;

/**
 * @author: Cheung Ho
 *
 */

// this class has collections of helper methods converting 
// raw data reading  to useful information.
public class MagnetometerHelper {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int x = 16;
		int y = 13;
		
		double degree = MagnetometerHelper.magnetTeslaToDegree(x, y);
		System.out.printf("degree of %6d and %6d = %6.2f \n", x,y,degree );
	}
	
	
	/*
	 * https://github.com/torvalds/linux/blob/master/drivers/iio/magnetometer/ak8975.c
	 * For AK8963 and AK09911, same calculation, but the device is less sensitive:
	 *
	 * H is in the range of +-8190.  The magnetometer has a range of
	 * +-4912uT.  To go from the raw value to uT is:
	 *
	 * HuT = H * 4912/8190, or roughly, 6/10, instead of 3/10.
	 */
	
	private static long ak8963_raw_to_gauss(int data) {
		return (((long)data + 128) * 6000) / 256;
	}

	/*
	 * Convert raw data of x and Y from magnetometer to degree  
	 */
	static public double magnetTeslaToDegree (int teslaX, int teslaY ) {
		int degree = -1;
		// from micro-tesla to milliGauss
		
		double gaussX = (double) teslaX  * 1.499389 *173; //* 0.48828125;// 0.48828125 mg
		double gaussY = (double) teslaY  * 1.499389 *175 ;// see calculation below       * 0.48828125;  

		
/*		void GetMres()
		{
		switch (AK8963_MAGNET_RES)
		{
		// Possible magnetometer scales (and their register bit settings) are:
		// 14 bit resolution (0) and 16 bit resolution (1)
		case 0:
		mRes = 10.*4912./8190.; // Proper scale to return milliGauss
		mRes = 5.997558
		break;
		case 1:
		mRes = 10.*4912./32760.0; // Proper scale to return milliGauss
		mRes = 1.499389
		break;
*/
		
//		System.out.printf("gauss %8.4f %8.4f \n", gaussX, gaussY);
			// convert from polar coordinates to degrees
			int d = (int) Math.round(Math.atan2(gaussY, gaussX) *(180/Math.PI ));
			if (teslaY < 0) degree = d * -1;
			else {
				degree = 180 + (180 - d);
			}
			
			
		return   degree  ; // 5 is declination
	}
	
	
/*  convert lsb raw data Magnetometer to degree shown on compass.
	0 degree	= points to north
 * 	90 		 	= points to east
 *  180 		= south
 *  270			= west
 *  
 *  input	X = 
 *  		Y = 
 */
	@Deprecated
	static private double _convertMagnetTeslaToDegre (int teslaX, int teslaY ) {
		int degree = -1;
//		System.out.printf("tesla %8d %8d \n", teslaX, teslaY);
		// from micro-tesla to Gauss
		double gaussX = (double) teslaX ; //* 0.48828125;// 0.48828125 mg
		double gaussY = (double) teslaY ;//* 0.48828125;
//		System.out.printf("gauss %8.4f %8.4f \n", gaussX, gaussY);
		if (gaussX == 0 ) {
			if (gaussY < 0) {
				degree = 90;
			}
			else  {
				degree = 0;
			}
		}
		else {
			//arctan(yGaussData/xGaussData)∗(180/π)
			// convert from polar coordinates to degrees
			int d = (int) Math.round(Math.atan(gaussY/gaussX) *(180/Math.PI ));
//			d+=5;
//			System.out.println("arctan " + d);
			if (d < 0) {
				degree = d + 360;
			}
			else  if (d> 360 ) {
				degree = d - 360;
			}
			else degree =  d;
		}
		return   degree  ; // 5 is declination
//		return  ((double) degree / 276) *360  ; // 5 is declination
	}

	
	
	static public double standardDeviation(List<Integer> list  ) {
		double stdDev = 0;

		int count = list.size();
		OptionalDouble average = list.stream().mapToDouble(a -> a).average();
		double sum = 0;
		for (int i : list) {
			sum = Math.pow((i - average.getAsDouble()), 2);
		}
		stdDev = sum / count;
		
		return stdDev;
	}
	
	
}
