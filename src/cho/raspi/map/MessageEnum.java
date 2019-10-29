package cho.raspi.map;

/**
 * @author: Cheung Ho
 *
 */
public enum MessageEnum {

	ALERT("Alert"),
	WARN("Warn"),
    CLEAR("Clear"),
    STOP("Stop"),
    GO("Go");
	
	 // declaring private variable for getting values 
    private String action; 
  
    // getter method 
    public String getAction() 
    { 
        return this.action; 
    } 
  
    // enum constructor - cannot be public or protected 
    private MessageEnum(String action) 
    { 
        this.action = action; 
    } 
	
}
