package cho.raspi.map;

/**
 * @author: Cheung Ho
 *
 */
public enum OperationEnum {

	HALT("HALT"),
	WIP("WIP"),
	NORMAL("Normal");
	
	 // declaring private variable for getting values 
    private String action; 
  
    // getter method 
    public String getAction() 
    { 
        return this.action; 
    } 
  
    // enum constructor - cannot be public or protected 
    private OperationEnum(String action) 
    { 
        this.action = action; 
    } 
	
}
