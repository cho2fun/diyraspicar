package cho.raspi.itf;

import cho.raspi.map.MessageEnum;

/**
 * @author: Cheung Ho
 *
 */
public interface MediatorItf {


	public void sendMessage(MessageEnum type, String msg);
	
	
	public void receiveMessage(MessageEnum type, String msg); 
	
}
