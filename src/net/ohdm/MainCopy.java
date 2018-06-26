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
public class MainCopy {
	/**
	 * Runs algorithm
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<LineString> lines = new ArrayList<LineString>();
		//GeoJSONReader
		GeoJSONReader jsonReader = new GeoJSONReader();
		GeoJSONWriter<LineString> jsonWriter = new GeoJSONWriter<LineString>();
		//DB Connection
		//PostgreSQLJDBC.run();
		
		//Import der Linien
		
		/*
		try {
			lines = jsonReader.readFile("raw.json");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		
		runQuery(lines);
		*/
		
		
		lines.clear();
		getShortLines(lines);
		
		System.out.println("Testing Angle");
		boolean[] lineChecked = new boolean[lines.size()];
		ArrayList<Integer> linesIndex = new ArrayList<Integer>();
		LineString line = null;
		for(int i = 0; i < lines.size(); i++) {
			if(linesIndex.isEmpty()) {
				linesIndex.add(i);
			}
			if(line == null) {
				line = new LineString(lines.get(i).getLineString());
			}
			lineChecked[i] = true;	
			
			int jIndex = i;
			double angle = 360;
			Point iHead = line.getHead();
			Point iTail = line.getTail();
			int direction = 0;
			boolean isPolygon = false;
			for(int j = 0; j < lines.size(); j++) {
				Point jHead = lines.get(j).getHead();
				Point jTail = lines.get(j).getTail();
				if(iHead.equals(iTail)) {
					System.out.println("polygon");
					isPolygon = true;
					break;
				}
				if(i != j && !lineChecked[j]) {
					
					double newAngle = 362;
					
					
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
				LineString lineJ = lines.get(jIndex);
				if(direction == 1) {
					lineJ.reverseOrder();
					line.appendHead(lineJ.getLineString().get(1));
					line.appendHead(lineJ.getLineString().get(0));
					linesIndex.add(jIndex);
				}else if(direction == 2) {
					line.appendHead(lineJ.getLineString().get(1));
					line.appendHead(lineJ.getLineString().get(0));
					linesIndex.add(jIndex);
				}else {
					System.out.println("ok?");
					i++;
				}
				
				i--;
			
			}
			/*else if(!isPolygon){
				boolean headCloser = false;
				double distanceToSelf = distanceBetween(line.getHead(), line.getTail());
				double distanceBetween = distanceToSelf;
			
				for(int j = 0; j < lines.size(); j++) {
					LineString lineJ = lines.get(j);
					if(i != j && !lineChecked[j]) {
						double distanceToHead = distanceBetween(line.getHead(), lineJ.getHead());
						if(distanceToHead < distanceBetween) {
							distanceBetween = distanceToHead;
							headCloser = true;
							jIndex = j;
						}
						double distanceToTail = distanceBetween(line.getHead(), lineJ.getTail());
						if(distanceToTail < distanceBetween) {
							distanceBetween = distanceToHead;
							headCloser = false;
							jIndex = j;
						}		
					}
				}
				if(distanceToSelf != distanceBetween) {
					LineString lineJ = lines.get(jIndex);
					lineChecked[jIndex] = true;
					if(headCloser) {
						lineJ.reverseOrder();
					} 
					line.appendHead(lineJ.getLineString().get(1));
					line.appendHead(lineJ.getLineString().get(0));
					linesIndex.add(jIndex);
					i--;
				}else{
					System.out.println("Ok");

					line.appendHead(line.getTail());
					linesIndex.add(i);
					i--;
				}
			}*/else {
		
				System.out.println(i+1 + "/" + lines.size());
				System.out.println(line.toString());
				//uploadPolygon(linesIndex, lines);
				linesIndex.clear();
				line = null;
				isPolygon = false;
				Arrays.fill(lineChecked,false);	
			}	
		}
		System.out.println("Saving to DB");
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
	private static boolean uploadPolygon(ArrayList<Integer> linesIndex, ArrayList<LineString> lines) {
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
	         String selectPolygon = "(SELECT polygon FROM sose2018.polygons_admin_2 WHERE index = " + linesIndex.get(0) + ")";
	         for(int i = 0; i < linesIndex.size(); i++) {	 
	        	 if(i == 0) {
	        		 String selectLine = "(SELECT line FROM sose2018.lines_admin_2 WHERE index = " + linesIndex.get(0) + ")";
	        		 stmt.executeUpdate( "INSERT INTO sose2018.polygons_admin_2(index, polygon) "
	 	        	 		+ "VALUES (" + 	linesIndex.get(0) + ", " + selectLine + ");" );     
	        	 }else {
	        		 //linesIndex.get(0) == linesIndex.get(i)
	        		 if(linesIndex.get(0) == linesIndex.get(i)) {
	        			 String tail =  ",(SELECT ST_EndPoint(line) FROM sose2018.lines_admin_2 WHERE index = " + linesIndex.get(0) + ")";
		        		 stmt.executeUpdate( "UPDATE sose2018.polygons_admin_2 SET polygon = ST_AddPoint(" 
		        				 + selectPolygon + tail +") WHERE index = " + linesIndex.get(0) + ";"); 
	        		 }else {
	        			 if(!lines.get(linesIndex.get(i-1)).getHead().equals(lines.get(linesIndex.get(i)).getTail())) {
	        				 String p1 = "ST_Point(" + lines.get(linesIndex.get(i-1)).getHead().getLongitude() + "," + lines.get(linesIndex.get(i-1)).getHead().getLatitude() + ")";
	        				 String p2 = "ST_Point(" + lines.get(linesIndex.get(i)).getTail().getLongitude() + "," + lines.get(linesIndex.get(i)).getTail().getLatitude() + ")";
	        				 String selectLine = ",(ST_MakeLine(" + p1 + "," + p2 + "))";
			        		 stmt.executeUpdate( "UPDATE sose2018.polygons_admin_2 SET polygon = ST_LineMerge(ST_Collect( "
			        				 + selectPolygon + selectLine + ")) WHERE index = " + linesIndex.get(0) + ";"); 
	        			 }else {
	        				  String selectLine = ",(SELECT line FROM sose2018.lines_admin_2 WHERE index = " + linesIndex.get(i) + ")";
	        				  stmt.executeUpdate( "UPDATE sose2018.polygons_admin_2 SET polygon = ST_LineMerge(ST_Collect( "
		        				 + selectPolygon + selectLine + ")) WHERE index = " + linesIndex.get(0) + ";"); 
	        			 }
	        			
	        		 }
	        		 
	        	 }
	         }
	         System.out.println();
	         /*
	         stmt.executeUpdate( "UPDATE sose2018.polygons_admin_2 SET polygon = ST_BuildArea(" 
    				 + selectPolygon + ") WHERE index = " + linesIndex.get(0) + ";");
	         */
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
	private static boolean getShortLines(ArrayList<LineString> lines) {
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
	         System.out.println("Getting row count");
	         stmt = c.createStatement();
	         ResultSet count = stmt.executeQuery( "SELECT count(index) as rows FROM sose2018.lines_admin_2;");
	         count.next();
	         int rows = Integer.parseInt(count.getString("rows"));
	         count.close();
	         for(int i = 0; i < rows; i++) {
	        	 System.out.println(i + "/" + rows);
	        	 ResultSet head = stmt.executeQuery( "SELECT substring(ST_AsText(line), '-?[0-9]*\\.?[0-9]*?\\s-?[0-9]*\\.?[0-9]*?,-?[0-9]*\\.?[0-9]*?\\s-?[0-9]*\\.?[0-9]*?') as line FROM sose2018.lines_admin_2 WHERE index = " + i + ";");
	        	 LineString  ls = new LineString();
	        	 while ( head.next() ) {
	        		 String line;
	        		 line = head.getString("line");
	        		 String[] strings = line.trim().split(",");
	        		 for(String s : strings) {
	        			 Point p = new Point(Double.parseDouble(s.substring(0,s.indexOf(' '))), Double.parseDouble(s.substring(s.indexOf(' ')+1,s.length())));
	        			 ls.appendTail(p);
	        		 }	 	                
	 	         }
	        	 head.close();
	        	 ResultSet tail = stmt.executeQuery( "SELECT substring(ST_AsText(line), '-?[0-9]*\\.?[0-9]*?\\s-?[0-9]*\\.?[0-9]*?,-?[0-9]*\\.?[0-9]*?\\s-?[0-9]*\\.?[0-9]*?\\)$') as line FROM sose2018.lines_admin_2 WHERE index = " + i + ";" );	         
	        	 while ( tail.next() ) {
	        		 String line;
	        		 line = tail.getString("line");
	        		 String[] strings = line.trim().split(",");
	        		 for(String s : strings) {
	        			 Point p = new Point(Double.parseDouble(s.substring(0,s.indexOf(' '))), Double.parseDouble(s.substring(s.indexOf(' ')+1,s.length()-1)));
	        			 ls.appendTail(p);
	        		 }		 	                
	 	         }
	        	 tail.close();
	        	 lines.add(ls);
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
	public static double angleBetweenTwoPointsWithFixedPoint(double point1X, double point1Y, 
	        double point2X, double point2Y, 
	        double fixedX, double fixedY) {

	    double angle1 = Math.atan2(point1Y - fixedY, point1X - fixedX);
	    double angle2 = Math.atan2(point2Y - fixedY, point2X - fixedX);

	    return angle1 - angle2; 
	}
	public static double distanceBetween(Point p1, Point p2) {
		double lat1 = p1.getLatitude();
		double lat2 = p2.getLatitude();
		double lng1 = p1.getLongitude();
		double lng2 = p2.getLongitude();
		double earthRadius = 6371000;
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + 
				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		        Math.sin(dLng/2) * Math.sin(dLng/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		
		return earthRadius * c;
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
		         stmt.executeUpdate( "INSERT INTO sose2018.lines_admin_2(index, line) VALUES (" + i + ", ST_GeomFromText("+lines.get(i).toPostGIS()+"));" );     
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