package net.ohdm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

/**
 * Main Class containing Menu for Geometry Extraction
 */
public class Main {
	private static String userInput = "0";
	private static String database = null;
	private static String username = null;
	private static String password = null;
	private static String schema = null;
	private static String table = null;
	private static Scanner sc = new Scanner(System.in);
	private static PolygonBuilder polygonBuilder;
	/**
	 * Loads LoginProperites
	 * If LoginProperties are wrong user input will be required
	 */
	private static void getLoginProperties() {
		File loginProperties = new File("login.properties");
		do {
			boolean propertiesLoaded = false;
			if(loginProperties.exists()) {
				propertiesLoaded = true;
				database = getProperty("database");
				username = getProperty("username");
				password = getProperty("password");
				if(database == null || username == null || password == null) {
					propertiesLoaded = false;
				}else {
					System.out.println("login.properties Loaded");
					System.out.println("Database: " + database);
				}
			}
			if(!propertiesLoaded) {
				propertiesLoaded = true;
				System.out.println("No login.properties File found");
				createLoginProperties();
			}
		}while(database == null || username == null || password == null);
		polygonBuilder = new PolygonBuilder(database, username, password);
	}
	
	/**
	 * Reads Properties from login.properties
	 * @param key key of property
	 * @return value
	 */
	private static String getProperty(String key) {
		Properties login = new Properties();
		try (FileReader in = new FileReader("login.properties")) {
			login.load(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return login.getProperty(key);
	}
	
	/**
	 * Creates login.properties File
	 */
	private static void createLoginProperties() {
		System.out.print("Database: ");
		database = sc.next();
		System.out.print("Username: ");
		username = sc.next();
		System.out.print("Password: ");
		password = sc.next();
		try (FileWriter out = new FileWriter("login.properties")) {
			out.write("database="+database);
			out.append( System.getProperty("line.separator") );
			out.write("username="+username);
			out.append( System.getProperty("line.separator") );
			out.write("password="+password);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		polygonBuilder = new PolygonBuilder(database, username, password);
	}
	
	/**
	 * Prints Menu into Console
	 */
	private static void printMenu() {
		System.out.println("[1] Get LineStrings from SQL Database");
		System.out.println("[2] Build Polygons");
		System.out.println("[9] Switch Database");
		System.out.println("[0] Exit");
	}
	
	/**
	 * Loads LineStrings from specified Table
	 */
	private static void getLineStrings() {
		
		System.out.print("Input Schema: ");
		schema = sc.next();
		System.out.print("Input Table: ");
		table = sc.next();
		PostgreSQLJDBC.run(schema + "." + table);
		polygonBuilder.getLineStrings("LineStrings.geojson");
		System.out.println("Imported " + polygonBuilder.lines.size() +" LineStrings from LineStrings.geojson");
	}
	
	/**
	 * Calls build() method from PolygonBuilder
	 */
	private static void buildPolygon() {
		polygonBuilder.setDatabase("postgresql://ohm.f4.htw-berlin.de:5432/ohdm_test");
		polygonBuilder.setSchemaTable("sose2018." + table);
		polygonBuilder.build();
	}
	
	/**
	 * Runs program
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("OHDM Polygon Builder");
		boolean lineStringsLoaded = false;
		getLoginProperties();
		do {
			printMenu();
			System.out.print("Input: ");
			userInput = sc.next();
			switch(userInput) {
			case "0":
				break;
			case "1":
				getLineStrings();
				lineStringsLoaded = true;
				break;
			case "2":
				if(lineStringsLoaded) {
					buildPolygon();
				}else {
					System.out.println("Get LineStrings before you build the polygon");
				}
				
				break;
			case "9":
				createLoginProperties();
				break;
			}
		}while(userInput != "0");
	}
}