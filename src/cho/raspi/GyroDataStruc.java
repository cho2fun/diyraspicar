package cho.raspi;

import cho.raspi.map.MPU9250_MapValues;



/*
 * 9DOF - 9 degree of freedom 
 *  Data structure for each 3-axis of magnetometer, gyro, accelerometer.
 */
public class GyroDataStruc implements MPU9250_MapValues{

	int gyroX, gyroY, gyroZ = 0;
	int acclX, acclY, acclZ = 0;
	int magnX=0, magnY=0, magnZ = 0;
	int temperature = 0;
	
	public int getMagnXScaled() {
		return magnX + MAGNT_X_OFFSET;
	}
	public int getMagnYScaled() {
		return magnX + MAGNT_Y_OFFSET;
	}
	public int getMagnZScaled() {
		return magnX + MAGNT_Z_OFFSET;
	}
	public int getMagnX() {
		return magnX;
	}
	public void setMagnX(int magnX) {
		this.magnX = magnX;
	}
	public int getMagnY() {
		return magnY;
	}
	public void setMagnY(int magnY) {
		this.magnY = magnY;
	}
	public int getMagnZ() {
		return magnZ;
	}
	public void setMagnZ(int magnZ) {
		this.magnZ = magnZ;
	}
	public int getTemperature() {
		return temperature;
	}
	public int getGyroX() {
		return gyroX;
	}
	public double getGyroXScaled() {
		return gyroX/GYRO_DIVISOR + GYRO_X_OFFSET;
	}
	public void setGyroX(int gyroX) {
		this.gyroX = gyroX;
	}
	public int getGyroY() {
		return gyroY;
	}
	public double getGyroYScaled() {
		return gyroY/GYRO_DIVISOR + GYRO_Y_OFFSET;
	}
	public void setGyroY(int gyroY) {
		this.gyroY = gyroY;
	}
	public int getGyroZ() {
		return gyroZ;
	}
	public double getGyroZScaled() {
		return gyroZ/GYRO_DIVISOR + GYRO_Z_OFFSET;
	}
	public void setGyroZ(int gyroZ) {
		this.gyroZ = gyroZ;
	}
	public int getAcclX() {
		return acclX;
	}
	public double getAcclXScaled() {
		return acclX/ACCEL_DIVISOR + ACCEL_X_OFFSET;
	}
	public void setAcclX(int acclX) {
		this.acclX = acclX;
	}
	public int getAcclY() {
		return acclY;
	}
	public double getAcclYScaled() {
		return acclY/ACCEL_DIVISOR + ACCEL_Y_OFFSET;
	}
	public void setAcclY(int acclY) {
		this.acclY = acclY;
	}
	public int getAcclZ() {
		return acclZ;
	}
	public double getAcclZScaled() {
		return acclZ/ACCEL_DIVISOR + ACCEL_Z_OFFSET;
	}
	public void setAcclZ(int acclZ) {
		this.acclZ = acclZ;
	}
	public double getTemperatureCelsius() {
		
		double d = (temperature / 340.0) + 36.53;
		
		return d;
	}
	public double getTemperatureFahrenheit() {
		
		double fhrenheit =  (this.getTemperatureCelsius() * 9/5) + 32;
		
		return fhrenheit;
	}
	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}
	
	public String  toMagString() {
		return String.format("M %6d %6d %6d ,T %5d = %5.2f ",
				this.getMagnX(), this.getMagnY(), this.getMagnZ(), this.temperature, this.getTemperatureFahrenheit() );
	}
	
	public String  toString() {
		return String.format("G %7.2f %7.2f %7.2f , A %7.2f %7.2f %7.2f , M %6d %6d %6d ,T %5d = %5.2f ",
				this.getGyroXScaled(), this.getGyroYScaled(), this.getGyroZScaled(), this.getAcclXScaled(), this.getAcclYScaled(), this.getAcclZScaled(),
				this.getMagnX(), this.getMagnY(), this.getMagnZ(), this.temperature, this.getTemperatureFahrenheit() );
	}
	
}
