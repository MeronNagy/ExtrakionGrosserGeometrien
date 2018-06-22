package net.ohdm;
import java.util.ArrayList;
/**
 * The Polygon class represents an area.
 * Polygons are closed LineStrings.
 * Currently no support for holes within the Polygon
 * @author Meron Nagy
 *
 */
public class Polygon extends LineString{
	/**
	 * Constructs a Polygon from a LineString.
	 * If the LineString is not closed the ends of the LineString will be connected.
	 * @param line LineString
	 */
	public Polygon(LineString line) {
		super(new ArrayList<Point>(line.getLineString()));
		if(this.getTail() != this.getHead()) {
			this.appendTail(this.getHead());
		}

	}
	
}
