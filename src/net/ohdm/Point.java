package net.ohdm;
/**
 * The Point class represents a single geographical location.
 * A Point consists of a latitude and longitude
 * @author Meron Nagy
 *
 */
public class Point {
	private double latitude;
	private double longitude;
	/**
	 * Creates a spatial point with the given params
	 * @param latitude
	 * @param longitude
	 */
	Point(double latitude, double longitude){
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * @return latitude
	 */
	public double getLatitude() {
		return latitude;
	}
	
	/**
	 * @return longitude
	 */
	public double getLongitude() {
		return longitude;
	}
	/**
	 * Returns latitude and longitude seperated with a comma as String
	 * @return a string representation of the Point
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(latitude + "," + longitude);
		return sb.toString();
	}
	public String toPostGIS() {
		StringBuilder sb = new StringBuilder();
		sb.append(latitude + " " + longitude);
		return sb.toString();
	}
	/**
	 * Indicates whether some other Point is "equal to" this one.
	 * Returns true only if the longitudes and latitudes of both Points are equal.
	 * @param p the reference Point with which to compare.
	 * @return true if this Point is the same as the p argument; false otherwise.
	 */
	public boolean equals (Point p) {
		if(this.longitude == p.getLongitude() && this.latitude == p.getLatitude()) {
			return true;
		}
		return false;
	}
}
