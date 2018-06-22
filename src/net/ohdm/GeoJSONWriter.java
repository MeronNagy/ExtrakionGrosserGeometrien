package net.ohdm;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Creates a GeoJSON File from Polygon and LineString objects.
 * @author Meron Nagy
 * @author Eduard Andreev
 * @param <T> ArrayList of Polygons or LineStrings
 */
public class GeoJSONWriter<T> {
	/**
	 * Creates a GeoJSON File containing Polygons or LineStrings.
	 * @param file GeoJSON File
	 * @param list ArrayList of Polygons or LineStrings
	 * @return true if writing successful, false if writing unsuccessful or size of list &lt; 1
	 */
	boolean writeFile(File file, boolean append, ArrayList<T> list) {
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(file, append), 32768);
			if(list.size() >= 1) {
				if(list.get(0).getClass().getName().contains("Polygon")) {
					out.write("{\"type\": \"FeatureCollection\",\"features\": [");
					out.newLine();
					for(int i = 0; i < list.size(); i++) {
						out.write("{\"type\": \"Feature\",\"properties\": {},\"geometry\": {\"type\": \"Polygon\",\"coordinates\": [[");
						out.write(list.get(i).toString());
						out.write("]]}}");
						if(i != list.size()-1) {
							out.write(',');
							out.newLine();
						}
					}
					out.newLine();
					out.write("]}");
				}
				if(list.get(0).getClass().getName().contains("LineString")) {
					out.write("{\"type\": \"FeatureCollection\",\"features\": [");
					for(int i = 0; i < list.size(); i++) {
						out.write("{\"type\": \"Feature\",\"properties\": {},\"geometry\": {\"type\": \"LineString\",\"coordinates\": [");
						out.write(list.get(i).toString());
						out.write("]}}");
						if(i != list.size()-1) {
							out.write(",\n");
						}
					}
					out.write("]}");
				}		
			}else {
				out.close();
				return false;
			}
	        out.close();
	        return true;
	    } catch (IOException e) {
	    	return false;
	    }
	}

	public boolean writeFile(File file, boolean append, LineString lineString) {
		// TODO Auto-generated method stub
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter(file, append), 32768);
				if(lineString.getLineString().size() >= 1) {
					out.write("{\"type\": \"FeatureCollection\",\"features\": [");
						out.write("{\"type\": \"Feature\",\"properties\": {},\"geometry\": {\"type\": \"LineString\",\"coordinates\": [");
						out.write(lineString.toString());
						out.write("]}}");
						out.write("]}");
						out.newLine();
				} else {
					out.close();
					return false;
				}
	        out.close();
	        return true;
	    } catch (IOException e) {
	    	return false;
	    }
	}
	
}
