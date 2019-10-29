package cho.raspi.helper;

import cho.raspi.itf.ModuleItf;

/**
 * @author: Cheung Ho
 *
 */
// this helperThread execute shared module that requiring runtime conditions.
// allow main controller place a halt on this operation 
// by calling pause method
public class ThreadHelper extends Thread implements ModuleItf {
	private String name = "default";
	private boolean continuous = false;
	ModuleItf module = null;
	
	public volatile boolean paused = false;
	
	public ThreadHelper(String threadName, boolean _continuous) {
		this.name = threadName;
		this.continuous = _continuous;
	}
	

	public void setModuleInterface(final ModuleItf _module) {
		module = _module;
	}
	
	public void run() {
		int i = 0;
		while(true) {
//			System.out.printf("%s HelpterThread iter %d\n",this.name,i++ );
			if (this.paused) {
				synchronized (this) {	
					try {
//						System.out.printf("%s HelpterThread iter sleep %d\n",this.name,i );
						this.sleep(5000);
						this.wait();
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else {
//				System.out.printf("%s HelperThread run- execute()  %d\n",this.name,i);
				module.execute();
//				System.out.printf("%s HelperThread run- execute() done %d \n",this.name, i);
			}
//			System.out.printf("%s HelpterThread done iter %d\n",this.name,i );
			if (!continuous ) break;
		}
	}
	
	
	public void pause() {
		this.paused = true;
	}

	public void unpause() {
        synchronized (this) {
			this.paused = false;
			this.notify();
        }
	}

	@Override
	public boolean isPaused() {
		// TODO Auto-generated method stub
		return this.paused;
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		
	}
}
