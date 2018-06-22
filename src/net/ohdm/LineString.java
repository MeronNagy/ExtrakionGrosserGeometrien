package net.ohdm;
import java.util.ArrayList;
import java.util.Collections;

/**
 * The LineString class represents path between Points.
 * A LineString consists of an ordered series of Points.
 * The first Point in a LineString is called Head
 * The last Point in a LineString is called Tail
 * @author Meron Nagy
 */
public class LineString {
	private ArrayList<Point> line;
	private Point head;
	private Point tail;
//	private String name;
	
	/**
	 * Constructs a LineString from an ArrayList of Points 
	 * If the ArrayList is empty {@link net.ohdm.LineString#LineString() LineString()} will be called
	 * @param line LineString
	 */
	public LineString(ArrayList<Point> line) {
		if(line.size() > 0) {
			this.line = new ArrayList<Point>(line);
			setHeadTail();
		}else {
			this.line = new ArrayList<Point>();
		}
	}
	/**
	 * Constructs an empty named LineString
	 * @param name Name to be given
	 */
	public LineString(String name) {
		//this.name = name;
		this.line = new ArrayList<Point>();
	}
	/**
	 * Constructs an empty and unnamed LineString
	 */
	public LineString() {
		this.line = new ArrayList<Point>();
		head = null;
		tail = null;
		//this.name = null;
	}
	
	/**
	 * @return the ArrayList of Points
	 */
	public ArrayList<Point> getLineString(){
		return this.line;
	}
	/**
	 * @param points to be used as LineString
	 */
	public void setLine(ArrayList<Point> points) {
		this.line = new ArrayList<Point>(points);
		setHeadTail();
		
	}
	/*
	/**
	 * @return name of LineString
	 */
	/*
	public String getName() {
		return name;
	}
	
	/**
	 * @param name to be given to LineString
	 */
	/*
	public void setName(String name) {
		this.name = name;
	}
	*/
	/**
	 * @return first Point of LineString
	 */
	public Point getHead() {
		return this.head;
	}
	/**
	 * @return last Point of LineString
	 */
	public Point getTail() {
		return this.tail;
	}
	//I recently learned the word prepend which means attach as prefix but I can't be bothered to change it right now :(
	//But we learned the terms head & tail to describe the first and last element of a list in Algorithem & Datenstrukturen
	//so it might be easier to understand this way?
	/**
	 * @param p prepends p to the LineString
	 */
	public void appendHead(Point p) {
		this.line.add(0, p);
		setHeadTail();
	}
	/**
	 * @param line prepends line to the LineString
	 */
	public void appendHead(ArrayList<Point> line) {
		this.line.addAll(0, line);
		setHeadTail();
	}
	/**
	 * @param prepends line to the LineString
	 */
	public void appendHead(LineString line) {
		appendHead(line.line);
	}
	/**
	 * @param appends p to the LineString
	 */
	public void appendTail(Point p) {
		this.line.add(p);
		setHeadTail();
	}
	/**
	 * @param line appends line to the LineString
	 */	
	public void appendTail(ArrayList<Point> line) {
		this.line.addAll(this.line.size(), line);	
		setHeadTail();	
	}
	/**
	 * @param line appends line to the LineString
	 */
	public void appendTail(LineString line) {
		appendTail(line.line);
	}
	/**
	 * Makes sure Head and Tail are the first and last Element of the LineString, respectively.
	 */
	private void setHeadTail() {
		this.tail = this.line.get(this.line.size()-1);
		this.head = this.line.get(0);
	}
	/**
	 * Reverses the Order of the LineString
	 */
	public void reverseOrder() {
		Collections.reverse(this.line);
		setHeadTail();
	}
	/**
	 * Returns all Points of the LineString contained in square brackets and separated by commas.
	 * @return a string representation of the LineString
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String str = "";
		for(int i = 0; i<line.size(); i++) {
			str += ("[" + line.get(i).toString() + "]" + ",");
		}
		//str = sb.toString();
		if(str.length() >= 1) {
			str = str.substring(0, str.length()-1);
		}
		return str;
	}
	
	public boolean equals(LineString ls) {
		if(this.getLineString().size() == ls.getLineString().size()) {
			for(int i = 0; i < this.getLineString().size(); i++) {
				Point p1 = this.getLineString().get(i);
				Point p2 = ls.getLineString().get(i);
				if(!p1.equals(p2)) {
					return false;
				}
			}
		}else {
			return false;
		}
		return true;
		
	}
	
}
