package net.ohdm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;

/**
 * Main Class containing algorithm for project
 * TODO: rewrite into Methods
 */
public class Main {
	/**
	 * Runs algorithm
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<LineString> lines = new ArrayList<LineString>();
		//GeoJSONReader
		GeoJSONReader jsonReader = new GeoJSONReader();

		//DB Connection
		//PostgreSQLJDBC.run();
		
		//Import der Linien
		try {
			lines = jsonReader.readFile("raw.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
		GeoJSONWriter<LineString> jsonWriter = new GeoJSONWriter<LineString>();
		jsonWriter.writeFile(new File("0.geojson"), false, lines);
		//---------------------1. connect smaller lines to bigger lines------------------------
		System.out.println("Imported " + lines.size() + " LineStrings from raw.json");
		System.out.println("Saving to 0.geojson");
		System.out.println("Connecting smaller lines");
		//Verbindet Linien die nur einen Partner haben miteinander
	
		for(int i = 0; i < lines.size(); i++) {
			Point iHead = lines.get(i).getHead();
			Point iTail = lines.get(i).getTail();
			int headCounter = 0;
			int tailCounter = 0;
			int headIndex = i;
			int tailIndex = i;
			boolean headAppendTail = false;
			boolean tailAppendHead = false;
			for(int j = 0; j < lines.size(); j++) {
				Point jHead = lines.get(j).getHead();
				Point jTail = lines.get(j).getTail();
				if(!(i == j)) {
					if(iHead.equals(jHead)) {
						headCounter++;
						headIndex = j;
					}
					if(iHead.equals(jTail)) {
						headCounter++;
						headAppendTail = true;
						headIndex = j;
					}
					if(iTail.equals(jHead)) {
						tailCounter++;
						tailAppendHead = true;
						tailIndex = j;
					}
					if(iTail.equals(jTail)) {
						tailCounter++;
						tailIndex = j;
					}
				}
			}
			LineString headLine = lines.get(headIndex);
			LineString tailLine = lines.get(tailIndex);
			if(headCounter == 1) {
				if(!headAppendTail) {
					headLine.reverseOrder();
				}
				headLine.getLineString().remove(headLine.getLineString().size() - 1);
				lines.get(i).appendHead(headLine);
			}
			if(tailCounter == 1) {
				if(!tailAppendHead) {
					tailLine.reverseOrder();
				}
				tailLine.getLineString().remove(0);
				lines.get(i).appendTail(tailLine);
			}		
			if(headCounter == 1) {
				lines.remove(headLine);
				i = i -1;
			}
			if(tailCounter == 1) {
				lines.remove(tailLine);
				i = i -1;
			}
			
		}
		System.out.println("Lines connected into " + lines.size() + " bigger lines");
		jsonWriter.writeFile(new File("1.geojson"), false, lines);
		System.out.println("Saving to 1.geojson");
		//-------------------1. END-----------------------------------------------
		
		//-------------------2. Delete repeating values from lines--------------
		//Removes repeating values
		int duplicatePoint = 0;
		for(int i = 0; i < lines.size(); i++) {
			LineString line = lines.get(i);
			ArrayList<Point> result = new ArrayList<Point>();
			HashSet<Point> set = new HashSet<>();
			for(Point p : line.getLineString()) {
				if(!set.contains(p)) {
					result.add(p);
					set.add(p);
				}else {
					duplicatePoint++;
				}
			}
			line.setLine(result);
		}
		System.out.println("Removed " + duplicatePoint + " duplicate Points");	
		jsonWriter.writeFile(new File("2.geojson"), false, lines);
		System.out.println("Saving to 2.geojson");
		duplicatePoint = 0;
		for(int i = 0; i < lines.size(); i++) {
			LineString lineI = lines.get(i);
			for(int j = 0; j < lines.size(); j++) {
				LineString lineJ = lines.get(j);
				if(i != j) {
					if(lineI.equals(lineJ)) {
						lines.remove(j);
						duplicatePoint++;
					}
				}
			}
		}
		System.out.println("Removed " + duplicatePoint + " duplicate LineStrings");	
		jsonWriter.writeFile(new File("3.geojson"), false, lines);
		System.out.println("Saving to 3.geojson");
		
		//runQuery(lines);
		
		System.out.println("Testing Angle");
		boolean[] lineChecked = new boolean[lines.size()];
		ArrayList<LineString> lines2 = new ArrayList<LineString>(lines);
		for(int i = 0; i < lines.size(); i++) {
			lineChecked[i] = true;	
			
			if(i == 1) {
			//	break;
			}
			
			int jIndex = i;
			double angle = 360;
			Point iHead = lines2.get(i).getHead();
			Point iTail = lines2.get(i).getTail();
			int direction = 0;
			for(int j = 0; j < lines.size(); j++) {
				Point jHead = lines.get(j).getHead();
				Point jTail = lines.get(j).getTail();
				if(i != j && !lineChecked[j]) {
					
					double newAngle = 362;
					if(iHead.equals(iTail)) {
						System.out.println("polygon");
						break;
					}
					
					if(iHead.equals(jHead)) {
						Point p1 = lines.get(i).getLineString().get(1);
						Point p2 = lines.get(j).getLineString().get(1);
						newAngle = angleBetweenTwoPointsWithFixedPoint(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude(), iHead.getLatitude(), iHead.getLongitude());							
						if(newAngle < 0) {
							newAngle += 360;
						}
						if(newAngle < angle) {
							direction = 1;
							angle = newAngle;
							jIndex = j;							
						}	
					}
					if(iHead.equals(jTail)) {
						Point p1 = lines.get(i).getLineString().get(1);
						Point p2 = lines.get(j).getLineString().get(lines.get(j).getLineString().size()-2);
						newAngle = angleBetweenTwoPointsWithFixedPoint(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude(), iHead.getLatitude(), iHead.getLongitude());
						if(newAngle < 0) {
							newAngle += 360;
						}
						if(newAngle < angle) {
							direction = 2;
							angle = newAngle;
							jIndex = j;							
						}
					}
				}
			}
			if(i != jIndex) {
				lineChecked[jIndex] = true;
				LineString lineI = lines2.get(i);
				LineString lineJ = lines.get(jIndex);
				if(direction == 1) {
					lineJ.reverseOrder();
					lineI.appendHead(lineJ);
				}else if(direction == 2) {
					lineI.appendHead(lineJ);
				}else if(direction == 3) {
					lineI.appendTail(lineJ);
				}else if(direction == 4) {
					lineJ.reverseOrder();
					lineI.appendTail(lineJ);		
				}
				i--;
			}else {
				System.out.println(i + "/" + lines.size());
				if(!jsonWriter.writeFile(new File("geoms/" + i +".geojson"), false, lines2.get(i))) {
					System.out.println("failed to save geometry");
					break;
				}
				//jsonWriter.writeFile(new File("4.geojson"), true, lines.get(0));
				//lines.remove(lines.get(0));
				Arrays.fill(lineChecked,false);	
				//i--;
			}	
		}
		System.out.println(lines.size());
		//jsonWriter.writeFile(new File("3.geojson"), true, lines);
		System.out.println("Saving to geoms/");
		/* 1. Connect smaller lines to big lines FINISHED
		 * 2. Delete repeating values
		 * 2. check if geojson.io looks the same
		 * ?. Separate Polygons? Keep Lines yes/no
		 * 4. Connect via angle, Problem: some lines are identical
		 * ?. Upload to DB
		 *  
		 * 
		 * 
		 * 
		 */
	}	
	public static double angleBetweenTwoPointsWithFixedPoint(double point1X, double point1Y, 
	        double point2X, double point2Y, 
	        double fixedX, double fixedY) {

	    double angle1 = Math.atan2(point1Y - fixedY, point1X - fixedX);
	    double angle2 = Math.atan2(point2Y - fixedY, point2X - fixedX);

	    return angle1 - angle2; 
	}
	public static boolean runQuery(ArrayList<LineString> lines) {
		Connection c = null;
	      Statement stmt = null;
	      try {
	         Class.forName("org.postgresql.Driver");
	         //ohdm_public DB
	         Properties login = new Properties();
	         try (FileReader in = new FileReader("login.properties")) {
	        	    login.load(in);
	         }
	         String database = login.getProperty("testdb");
	         String username = login.getProperty("username");
	         String password = login.getProperty("password");
	         c = DriverManager
	            .getConnection("jdbc:" + database , username, password);
	         c.setAutoCommit(false);
	         System.out.println("Opened database successfully");
	         System.out.println("Inserting LineStrings");
	         stmt = c.createStatement();
	         for(int i = 0; i < lines.size(); i++) {
	        	 System.out.println(i + "/" + lines.size());
		         stmt.executeUpdate( "INSERT INTO sose2018.lines_admin_2(line) VALUES (ST_GeomFromText("+lines.get(i).toPostGIS()+"));" );     
	         }
	         stmt.close();
	         c.commit();
	         c.close();
	      } catch ( Exception e ) {
	         System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	         return false;
	      }
	      System.out.println("INSERT successful");
	      return true;
	}
}