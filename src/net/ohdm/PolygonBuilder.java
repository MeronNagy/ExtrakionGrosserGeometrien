package net.ohdm;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class PolygonBuilder {
	ArrayList<LineString> lines = new ArrayList<LineString>();
	GeoJSONReader jsonReader = new GeoJSONReader();
	GeoJSONWriter<LineString> jsonWriter = new GeoJSONWriter<LineString>();
	String database = null;
	String username = null;
	String password = null;
	String schemaTable = null;
	
	
	public PolygonBuilder(String database, String username, String password) {
		this.database = database;
		this.username = username;
		this.password = password;
	}
	/**
	 * Sets Schema and Table
	 * @param schemaTable to be used
	 */
	public void setSchemaTable(String schemaTable) {
		this.schemaTable = schemaTable;
	}
	
	public void setDatabase(String database) {
		this.database = database;
	}
	/**
	 * Reads LineStrings from File
	 * @param fileName Name of File
	 */
	public void getLineStrings(String fileName) {
		try { 
			lines = jsonReader.readFile(fileName); 
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}
	
	/**
	 * Builds Polygon from LineStrings
	 */
	public void build() {
		connectLineStrings();
		removeRepeatingPoints();
		removeRepeatingLineStrings();
		uploadTempLineStrings();
		reduceLineStrings();
	
		int distanceMinuend = 0; //Distance
		int maxSize = 600; //Max Anzahl der LineStrings aus dem ein Polygon bestehen darf
		createPolygon(0, distanceMinuend, maxSize);
		for(LineString ls : lines) {
			ls.reverseOrder();
		}
		createPolygon(lines.size(), distanceMinuend, maxSize);
		
	}
	
	/**
	 * Connects LineStrings which only touch 1 other LineString on each end
	 */
	private void connectLineStrings() {
		System.out.println("Connecting LineStrings");
		for(int i = 0; i < lines.size(); i++) { 
			System.out.println(i+ "/" + (lines.size()-1));
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
					} if(iTail.equals(jHead)) { 
						tailCounter++; 
						tailAppendHead = true;
						tailIndex = j; 
					} if(iTail.equals(jTail)) { 
						tailCounter++; 
						tailIndex = j; 
					} 
				}
			} 
			LineString headLine = new LineString(lines.get(headIndex)); 
			LineString tailLine = new LineString(lines.get(tailIndex)); 
			if(headCounter == 1 || tailCounter == 1) {
				if(headCounter == 1) {			
					if(!headAppendTail) { 
						headLine.reverseOrder(); 
					
					}
					headLine.remove(headLine.size() - 1);
					lines.get(i).appendHead(headLine); 
					lines.remove(headIndex);
				}else if(tailCounter == 1) {
					if(!tailAppendHead) { 
						tailLine.reverseOrder(); 
					}
					tailLine.remove(0); 
					lines.get(i).appendTail(tailLine); 
					lines.remove(tailIndex);
				}
				i--;
			}
			
		} 
		System.out.println("LineStrings connected into " + lines.size() + " longer LineStrings"); 	
	}
	
	/**
	 * Removes repeating Points
	 */
	private void removeRepeatingPoints() {
		int repeatingPoints = 0; 
		for(int i = 0; i < lines.size(); i++) { 
			LineString line = lines.get(i); 	
			for(int j = 0; j < line.size(); j++) {
				for(int k = 0; k < line.size(); k++) {
					if(j != k) {
						if(line.get(j).equals(line.get(k))) {
							line.remove(k);
							k--;
							repeatingPoints++;
						}
					}
				}
			}
		}
		System.out.println("Removed " + repeatingPoints + " repeating Points");
	}
	
	/**
	 * Removes repeatingLineStrings
	 */
	private void removeRepeatingLineStrings() {
		int repeatingLineStrings = 0; 
		for(int i = 0; i < lines.size(); i++) { 
			LineString lineI = lines.get(i); 
			for(int j = 0; j < lines.size(); j++) { 
				LineString lineJ = lines.get(j); 
				if(i != j) {
					if(lineI.equals(lineJ)) { 
						lines.remove(j); 
						repeatingLineStrings++; 
					} 
				} 
			}
		}
		System.out.println("Removed " + repeatingLineStrings + " repeating LineStrings");
	}
	
	/**
	 * Uploads LineStrings to Database
	 */
	private void uploadTempLineStrings() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.postgresql.Driver");
			// ohdm_public DB
			Properties login = new Properties();
			try (FileReader in = new FileReader("login.properties")) {
				login.load(in);
			}
			c = DriverManager.getConnection("jdbc:" + database, username, password);
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");
			System.out.println("Inserting LineStrings");
			stmt = c.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS "+ schemaTable + "_pb_temp_line_strings(index integer NOT NULL, line geometry)");
			stmt.execute("TRUNCATE TABLE "+  schemaTable + "_pb_temp_line_strings");
			for (int i = 0; i < lines.size(); i++) {
				System.out.println(i + "/" + lines.size());
				stmt.executeUpdate("INSERT INTO "+ schemaTable + "_pb_temp_line_strings(index, line) VALUES (" + i + ", ST_GeomFromText("
						+ lines.get(i).toPostGIS() + "));");
			}
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		System.out.println("INSERT successful");
	}

	/**
	 * Reduces LineStrings to 4 points (2 at head, 2 at tail)
	 * 
	 */
	private void reduceLineStrings() {
		ArrayList<LineString> tempLines = new ArrayList<LineString>();
		for(int i = 0; i < lines.size(); i++) {
			LineString tempLS = new LineString();
			if(lines.get(i).size() >= 4) {
				tempLS.appendHead(lines.get(i).get(lines.get(i).getSize()-1));
				tempLS.appendHead(lines.get(i).get(lines.get(i).getSize()-2));
				tempLS.appendHead(lines.get(i).get(1));
				tempLS.appendHead(lines.get(i).get(0));
			}else {
				tempLS.appendHead(lines.get(i));
			}	
			tempLines.add(tempLS);
		}
		lines = new ArrayList<LineString>(tempLines);
	}
	
	/**
	 * Creates Polygon
	 * @param dbIndex 
	 * @param distanceMinuend
	 * @param maxSize
	 */
	private void createPolygon(int dbIndex, int distanceMinuend, int maxSize) {
		boolean[] lineChecked = new boolean[lines.size()];
		ArrayList<Integer> linesIndex = new ArrayList<Integer>();
		ArrayList<LineString> newLines = new ArrayList<LineString>();
		LineString line = null;
		if (maxSize == 0) {
			maxSize = lines.size()*2;
		}
		for (int i = 0; i < lines.size(); i++) {
			if (linesIndex.isEmpty()) {
				linesIndex.add(i);
			}
			if (line == null) {
				line = new LineString(lines.get(i));
			}
			boolean isPolygon = false;
			lineChecked[i] = true;
			Point iHead = line.getHead();
			Point iTail = line.getTail();
			int jIndex = i;
			double angle = 361;
			int direction = 0;
			for (int j = 0; j < lines.size(); j++) {
				Point jHead = lines.get(j).getHead();
				Point jTail = lines.get(j).getTail();
				if (iHead.equals(iTail)) {
					i = jIndex;
					isPolygon = true;
					break;
				}
				if (i != j && !lineChecked[j]) {
					double newAngle = 361;
					if (iHead.equals(jHead)) {
						Point p1 = line.get(1);
						Point p2 = lines.get(j).get(1);
						newAngle = angleBetweenTwoPointsWithFixedPoint(p1.getLatitude(), p1.getLongitude(),
								p2.getLatitude(), p2.getLongitude(), iHead.getLatitude(), iHead.getLongitude());
						if (newAngle < 0) {
							newAngle += 360;
						}
						if (newAngle < angle) {
							direction = 1;
							angle = newAngle;
							jIndex = j;
						}
					}
					if (iHead.equals(jTail)) {
						Point p1 = line.get(1);
						Point p2 = lines.get(j).get(lines.get(j).size() - 2);
						newAngle = angleBetweenTwoPointsWithFixedPoint(p1.getLatitude(), p1.getLongitude(),
								p2.getLatitude(), p2.getLongitude(), iHead.getLatitude(), iHead.getLongitude());
						if (newAngle < 0) {
							newAngle += 360;
						}
						if (newAngle < angle) {
							direction = 2;
							angle = newAngle;
							jIndex = j;
						}
					}
				}
			}
			if (i != jIndex) {
				lineChecked[jIndex] = true;
				LineString lineJ = new LineString(lines.get(jIndex));
				if (direction == 1) {
					lineJ.reverseOrder();	
					line.appendHead(lineJ.get(1));
					line.appendHead(lineJ.get(0));
					linesIndex.add(jIndex);
				} else if (direction == 2) {
					line.appendHead(lineJ.get(1));
					line.appendHead(lineJ.get(0));
					linesIndex.add(jIndex);
				}
				i--;
			} else if (!isPolygon) {
				double distanceToSelf = distanceBetween(line.getHead(), line.getTail())-distanceMinuend;
				double distanceBetween = distanceToSelf;
				boolean headCloser = false;
				for (int j = 0; j < lines.size(); j++) {
					if (i != j && !lineChecked[j]) {
						Point jHead = lines.get(j).getHead();
						Point jTail = lines.get(j).getTail();
						double distanceToHead = distanceBetween(line.getHead(), jHead);
						double distanceToTail = distanceBetween(line.getHead(), jTail);

						if (distanceToHead < distanceBetween) {
							distanceBetween = distanceToHead;
							headCloser = true;
							jIndex = j;
						}

						if (distanceToTail < distanceBetween) {
							distanceBetween = distanceToTail;
							headCloser = false;
							jIndex = j;

						}
					}
				}
				if (distanceToSelf == distanceBetween) {
					linesIndex.add(i);
					line.appendHead(line.getTail());
					i--;
				} else {
					LineString lineJ = new LineString(lines.get(jIndex));
					if (headCloser) {
						lineJ.reverseOrder();
					}
					linesIndex.add(-1);
					LineString tmp = new LineString();
					tmp.appendHead(lineJ.getTail());
					tmp.appendTail(line.getHead());
					line.appendHead(lineJ.getTail());
					newLines.add(tmp);
					i--;
				}
			} else {
				System.out.println((dbIndex+i) + "/" + (lines.size()*2 - 1));
				System.out.println(linesIndex.toString());
				if(line.size() <= maxSize) {
					uploadPolygon((dbIndex), linesIndex, newLines);
				}
				
				linesIndex.clear();
				newLines.clear();
				line = null;
				Arrays.fill(lineChecked, false);
			}
		}
	}
	

	private double angleBetweenTwoPointsWithFixedPoint(double point1X, double point1Y, double point2X,
			double point2Y, double fixedX, double fixedY) {
		double angle1 = Math.atan2(point1Y - fixedY, point1X - fixedX);
		double angle2 = Math.atan2(point2Y - fixedY, point2X - fixedX);

		return angle1 - angle2;
	}

	private double distanceBetween(Point p1, Point p2) {
		double lat1 = p1.getLatitude();
		double lat2 = p2.getLatitude();
		double lng1 = p1.getLongitude();
		double lng2 = p2.getLongitude();
		double earthRadius = 6371000;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
		* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return earthRadius * c;
	}

	private boolean uploadPolygon(int n, ArrayList<Integer> linesIndex, ArrayList<LineString> newLines) {	
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.postgresql.Driver");
			// ohdm_public DB
			Properties login = new Properties();
			try (FileReader in = new FileReader("login.properties")) {
				login.load(in);
			}
			c = DriverManager.getConnection("jdbc:" + database, username, password);
			c.setAutoCommit(false);
			stmt = c.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS "+ schemaTable + "_pb_polygons(index integer NOT NULL, polygon geometry)");
			stmt.execute("TRUNCATE TABLE "+  schemaTable + "_pb_polygons");
			String selectPolygon = "(SELECT polygon FROM "+ schemaTable + "_pb_polygons WHERE index = "
					+ (linesIndex.get(0) + n) + ")";
			int j = 0;
			for (int i = 0; i < linesIndex.size(); i++) {
				if (i == 0) {
					String selectLine = "(SELECT line FROM "+ schemaTable + "_pb_temp_line_strings WHERE index = " + linesIndex.get(0)
					+ ")";
					stmt.executeUpdate("INSERT INTO "+ schemaTable + "_pb_polygons(index, polygon) " + "VALUES ("
							+ (linesIndex.get(0) + n) + ", " + selectLine + ");");
				} else {
					String selectLine;
					if (linesIndex.get(0) == linesIndex.get(i)) {
						selectLine = ",(Select ST_MakeLine((SELECT ST_StartPoint(polygon) FROM "+ schemaTable + "_pb_polygons WHERE index = "
								+ linesIndex.get(0)
								+ "),(SELECT ST_EndPoint(polygon) FROM "+ schemaTable + "_pb_polygons WHERE index = "
								+ linesIndex.get(0) + ")))";
					} else {
						if (linesIndex.get(i) == -1) {
							selectLine = ",(SELECT ST_GeomFromText(" + newLines.get(j).toPostGIS() + "))";
							j++;
						} else {
							selectLine = ",(SELECT line FROM "+ schemaTable + "_pb_temp_line_strings WHERE index = " + linesIndex.get(i)	+ ")";
						}
					}
					stmt.executeUpdate(
							"UPDATE "+ schemaTable + "_pb_polygons SET polygon = ST_LineMerge(ST_Multi(St_Collect("
									+ selectPolygon + selectLine + "))) WHERE index = " + (linesIndex.get(0) + n) + ";");
				}
			}
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			return false;
		}
		return true;
	}
}
