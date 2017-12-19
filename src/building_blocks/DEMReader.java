package building_blocks;

import java.io.File;

public class DEMReader {

	private static final String PATH = "/home/radim/data/DEMData";
	
	
	
	
	
	
	
	
	 /**
	  * 
	  * @param args
	  */
	 public String findNameOfTile(double longitude, double latitude) {

	   String zeroesLon = null;
	   String zeroesLat = null;

	   String prefixLon = longitude >= 0 ? "E" : "W";
	   if (Math.abs(longitude) < 10) zeroesLon = "00";
	   else if (Math.abs(longitude) >= 10 && Math.abs(longitude) < 100) zeroesLon = "0";
	   else zeroesLon = "";

	   String prefixLat = latitude >= 0 ? "N" : "S";
	   if (Math.abs(latitude) < 10) zeroesLat = "0";
	   else zeroesLat = "";

	   String latitudeS = String.valueOf((int)(Math.abs(Math.floor(latitude))));
	   String longitudeS = String.valueOf((int)(Math.abs(Math.floor(longitude))));

	   StringBuilder sb = new StringBuilder();
	   
	   sb.append(prefixLat);
	   sb.append(prefixLon);
	   sb.append(File.separator);
	   sb.append(prefixLat);
	   sb.append(zeroesLat);
	   sb.append(latitudeS);
	   sb.append(prefixLon);
	   sb.append(zeroesLon);
	   sb.append(longitudeS);
	   sb.append(".hgt");

	   return sb.toString();
	   
	  }
	
}
