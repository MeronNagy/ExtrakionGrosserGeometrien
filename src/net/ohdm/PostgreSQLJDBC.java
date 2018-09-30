package net.ohdm;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
/**
 * This class contains a single static method to save the result of a SQL Query
 * @author Eduard Andreev
 *
 */
public class PostgreSQLJDBC {
	/**
	 * Connects to the OHDM Database and executes a SQL Query to receive the LineStrings in GeoJSON format and their names from boundaries_admin_2
	 * The result is saved as "raw.json"
	 */
	public static void run(String table) 
	{
      Connection c = null;
      Statement stmt = null;
      try {
         Class.forName("org.postgresql.Driver");
         //ohdm_public DB
         Properties login = new Properties();
         try (FileReader in = new FileReader("login.properties")) {
        	    login.load(in);
         }
         String database = login.getProperty("database");
         String username = login.getProperty("username");
         String password = login.getProperty("password");
         c = DriverManager
            .getConnection("jdbc:" + database , username, password);
         c.setAutoCommit(false);
         System.out.println("Opened database connection successfully");
         stmt = c.createStatement();
         
         ResultSet rs = stmt.executeQuery( "SELECT ST_AsGeoJSON(ST_Transform(line, 4326)) as line FROM "+ table +";" );
         File newTextFile = new File("LineStrings.geojson");
         FileWriter fw = new FileWriter(newTextFile);
         while ( rs.next() ) {
        	String line;
            //line = rs.getString("line");
        	line = rs.getString("line");
            try {
            	fw.write(line);
                fw.append( System.getProperty("line.separator") );
            } catch (IOException iox) {
                //do stuff with exception
                iox.printStackTrace();
            }/*
            System.out.println( line );
            System.out.println(name);
            */
         }
         fw.close();
         rs.close();
         stmt.close();
         c.close();
      } catch ( Exception e ) {
         System.err.println( e.getClass().getName()+": "+ e.getMessage() );
         System.exit(0);
      }
      System.out.println("Operation done successfully");
      System.out.println("Saved LineStrings as LineStrings.geojson");
   }  
}