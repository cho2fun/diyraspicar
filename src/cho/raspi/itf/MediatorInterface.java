package cho.raspi.itf;

@Deprecated
public interface MediatorInterface {

	
	// this allows modules to alert main controller to stop cars immediately and
	// survey surrounding for objects and obstacle.
	public void alert (String trigger);
	
	/* this allows modules to communicate to main controller.
	*	message will be trigger for actions taken by controller. 
	*/
	public void sendMessage (String message);
	
}
