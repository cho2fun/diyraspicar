package cho.raspi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;

import net.minidev.json.JSONArray;

/**
 * @author: Cheung Ho
 *
 */

public class AppConfiguration {


	byte[] jsonData = null;


	// create objMapper instance
	//	ObjectMapper objectMapper = new ObjectMapper();

	ObjectMapper mapper = null;


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*
		if ( args.length < 1) {
			System.out.println("Java Configuration config.json");
			//			System.exit(0);
		}
		 */
		//		String configFileName = args[0];
		String configFileName = "D:\\Projects\\RaspberryPi\\resources\\raspicar.json";
//		String configFileName = "D:\\Projects\\RaspberryPi\\resources\\books.json";

		AppConfiguration config = new AppConfiguration(configFileName);
		config.parseBook();
//		config.parseDocument(null);


	}


	public AppConfiguration(String filename) {
		try {

			//		ObjectMapper mapper = new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true);
			jsonData = Files.readAllBytes(Paths.get(filename));

			// create objMapper instance
			//		ObjectMapper objectMapper = new ObjectMapper();

			mapper = new ObjectMapper();
			//.configure(Feature.ALLOW_COMMENTS, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public void parseBook() {
		try {
			StringBuilder buf = new StringBuilder();
			
			DocumentContext jDoc = JsonPath.parse(new String(jsonData));

			String[] queries = {"$.version",
					"$.raspiCar.*",
					"$.raspiCar.frontDeck.*"
			};
			JsonNode dataObject = null;
			
			//		JsonNode rootNode = mapper.readTree(new String(jsonData));
			for (String s: queries) {
				try  {
					System.out.println("query "+ s);
					Object obj = (Object) jDoc.read(s); 
					
//					Configuration conf = Configuration.builder().jsonProvider(new GsonJsonProvider()).build();

//				    JsonNode obj = JsonPath.using(conf).parse(jsonData).read(s);
					
//					JsonNode root = mapper.readTree(jsonData);
					System.out.println("read object type = " + obj.getClass() + " : "+ obj.toString());
					if (obj.getClass() == JsonNode.class) {
						dataObject = (JsonNode) obj;
						System.out.format("%s = %s \n",s, dataObject.fieldNames() , dataObject.toString());
						traverse(dataObject, buf);
					}
					else {
						List<Object> m = JsonPath.read(obj.toString(), "$.*");		
						if (m.size() >  0) {
//							parseDocument(obj.toString());
							m.forEach( n -> {System.out.println( "lambda map value "+ n); 
//							test = n.toString();
//								parseDocument(toString());
							});
						}
						else {
							System.out.println("value: " + obj.toString() )	;
							
						}
					}
				}
				catch (ClassCastException ex) {
					
//					System.out.format("found value ",jDoc.read(s) )	;
				}
			}



			
/*			
			System.out.format("%s = %s \n", jNode.getNodeType(), jNode.asText());
			System.out.format("%s = %s \n", jNode.getNodeType(), jNode.get("pinId"));
		    Iterator<Entry<String, JsonNode>> fields = jNode.fields();
		    
		    while (fields.hasNext()) {
		        Entry<String, JsonNode> jsonField = fields.next();
		        System.out.printf("entry %s ; %s ; %s " , jsonField.getKey(), jsonField.getValue());
		    }
*/


			/*
		//		JsonNode node = rootNode.path("$.store.book[1].author");
		JsonNode versionNode = rootNode.path("$.version");
		System.out.println("version=" + versionNode);

			 */



		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}






	}
	
	
	public void parseDocument (String jsonString) {
		try {
			if (jsonString == null) jsonString = new String(jsonData);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.readTree(jsonString);
		StringBuilder buf = new StringBuilder();
		traverse( root, buf);
		System.out.println("*** result  ");
		System.out.println(buf.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void parse(String str) {
		try {
			StringBuilder buf = new StringBuilder();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(str);
			traverse(root,buf);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	public void traverse(JsonNode n, StringBuilder buf) {
		if (n.isObject()) {
			ObjectNode objectNode = (ObjectNode) n;
	        Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
//	        String pathPrefix = currentPath.isEmpty() ? "" : currentPath + "-";

	        while (iter.hasNext()) {
	            Map.Entry<String, JsonNode> entry = iter.next();
	            System.out.println("traverse Obj "+  entry.getKey() + " : "+ entry.getValue() );
	            traverse(entry.getValue(),buf);
	        }
			
			
		} 
		else if (n.isValueNode()) {
			System.out.println("traverse V:  "+ n.toString());
			
		}
		else if (n.isArray()) {
			for (Object o : n) {
				
				System.out.println("traverse A :  "+ o.toString());
				traverse((JsonNode)o,buf);
				
			}
		}
		
	}
	
	
}
