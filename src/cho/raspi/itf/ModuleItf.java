package cho.raspi.itf;

/**
 * @author: Cheung Ho
 *
 */

public interface ModuleItf {
	

	// stop operation until further notice
	public void execute();

	
	// stop operation until further notice
	public void pause();
	
	// continue operation
	public void unpause();

	//return whether the module is paused or not.
	// return true when it is paused
	// return false when it is not paused.
	public boolean isPaused();

}
