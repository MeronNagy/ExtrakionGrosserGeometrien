package net.ohdm;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Reads data from a file made from {@link net.ohdm.PostgreSQLJDBC PostgreSQLJDBC Class}
 * @author Meron Nagy 
 * @author Eduard Andreev
 */
public class GeoJSONReader {
	/**
	 * Reads data from file and tries to convert it into named LineStrings
	 * @param fileName Name of file to be used
	 * @return named LineStrings
	 * @throws IOException failed to read File
	 */
	ArrayList<LineString> readFile(String fileName) throws IOException {
		ArrayList<LineString> lines = new ArrayList<LineString>();
	    BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    String in;
	    while ((in = r.readLine()) != null) {
	    	in = in.replaceAll("[^\\.,0123456789]","");
	    	in = in.substring(1, in.length());
	    	String[] strings = in.trim().split(",");
	    	LineString line = new LineString();
	    	for(int i = 0; i<strings.length; i = i+2) {
	    		line.appendHead(new Point(Double.parseDouble(strings[i]),Double.parseDouble(strings[i+1])));
	    	}
	    	lines.add(line);
	    }
	    r.close();
		return lines;
	}
}
