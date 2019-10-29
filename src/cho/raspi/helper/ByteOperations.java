package cho.raspi.helper;

/**
 * @author: Cheung Ho
 *
 */

public class ByteOperations {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		byte lsb = (byte) 0b1111_1111;
		byte msb = (byte) 0b1111_1111;
		msb = (byte) 0b1111_1111;
		lsb = (byte) 0b1111_1111;
		int test = msblsbToInt(msb,lsb);
		System.out.println(Integer.toBinaryString(test));
		
		System.out.println("result msblsb "+ msblsbToInt(msb,lsb));
		System.out.println("result lsbmsb "+ lsbmsbToInt(lsb,msb));
	}

    public static short msblsbToInt(byte msb, byte lsb) {
    	short result = (short)(((msb & 0xFF) << 8) | (lsb & 0xFF));
   	    
   	    return result;
   }

    public static short msblsbToInt(int msb, int lsb) {
    	short result = (short)(((msb & 0xFF) << 8) | (lsb & 0xFF));
    	
    	    return result;
    }

    public static short lsbmsbToInt(byte lsb, byte msb) {
    	short result = (short)(((msb & 0xFF) << 8) | (lsb & 0xFF));
    	
   	    return result;
   }

    
    public static short lsbmsbToInt(int lsb, int msb) {
    	short result = (short)(((msb & 0xFF) << 8) | (lsb & 0xFF));
    	
   	    return result;
   }

	public static String intToString(int i) {
		 return Integer.toBinaryString(i);
	}
	
	public static String byteToString(byte b) {
		 return Byte.toString(b);
	}
}
