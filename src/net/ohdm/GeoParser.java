package net.ohdm;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GeoParser {

	public static void main(String[] args) throws IOException 
	{
		FileReader fr = new FileReader("raw.json");
	    BufferedReader br = new BufferedReader(fr);
	    List<String> lines = new ArrayList<String>();
	    
	    //Schema
	    lines.add("{\r\n" + 
	    		"  \"type\": \"FeatureCollection\",\r\n" + 
	    		"  \"features\": [");
	    
	    
	    
	    // read the file into lines
	    BufferedReader r = new BufferedReader(new FileReader("thetextfile.json"));
	    String in;
	    while ((in = r.readLine()) != null) {
	    	//Schema
	    	lines.add("{\r\n" + 
	    			"      \"type\": \"Feature\",\r\n" + 
	    			"      \"properties\": {},\r\n" + 
	    			"      \"geometry\":");
	    	
	    	lines.add(in);
	    }
	        
	    r.close();
	    
	    String letzte = lines.get(lines.size() - 1);
	    String replacedStr = letzte.replaceAll("}},", "}}]}");
	    lines.remove(lines.size() - 1);
	    lines.add(replacedStr);
	    
	    Path out = Paths.get("final.json");
	    Files.write(out,lines,Charset.defaultCharset());
        
	}
	

}
