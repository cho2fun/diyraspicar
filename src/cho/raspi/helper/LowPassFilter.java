package cho.raspi.helper;

import java.util.List;

/**
 * @author: Cheung Ho
 *
 */

public class LowPassFilter {

	static float smoothing = 0.5f; // must be less than 1
	
	public LowPassFilter() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	static public List<Integer> smoother(List<Integer> list) {
		int value = list.get(0);
		for (int i = 1 ; i< list.size();i++ ) {
				float current = list.get(i);
				value +=(current- value) * smoothing;
				list.set(i, Math.round(value));
		}
		return list;
	}
	
	
}
