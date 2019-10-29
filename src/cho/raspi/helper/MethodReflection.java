package cho.raspi.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.pi4j.gpio.extension.pca.PCA9685GpioProvider;

public class MethodReflection {
	public MethodReflection() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void getMethodReflection(Object obj) {

		Method[] mths = obj.getClass().getDeclaredMethods();

		System.out.printf("method Name %s \n", obj.getClass().getName() );
		for (Method m : mths) {
			if (m.getName().startsWith("get")) {
				try {
					System.out.printf(m.getName()  + " : ");
					System.out.println(m.invoke(obj));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("?");
				}
			}
		}

		Field[] flds = obj.getClass().getDeclaredFields();

		System.out.printf("Field Name %s \n", obj.getClass().getName() );
		for (Field f : flds) {
			System.out.println(f.getName()  + " : " );

		}



	}


}
