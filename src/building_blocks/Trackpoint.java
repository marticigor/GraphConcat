package building_blocks;

import java.text.DecimalFormat;

public class Trackpoint {
	private double lon;
	private double lat;
	private short elev;
	private static DecimalFormat df10 = new DecimalFormat("######.##########");
	private static DecimalFormat df2 = new DecimalFormat("######.##");
	
	public Trackpoint(double lat, double lon, short elev) {
		this.lat = lat;
		this.lon = lon;
		this.elev = elev;
	}

	public String getLon() {
		return df10.format(lon);
	}

	public String getLat() {
		return df10.format(lat);
	}
	
	public String getElev() {
		return df2.format(elev);
	}
	
}
