package utils.geospatial;

public class Bearing {

    public static double getBearing(double sLat, double sLon, double eLat, double eLon) {

        //credits to Mercy Viking at
        //http://gis.stackexchange.com/questions/29239/calculate-bearing-between-two-decimal-gps-coordinates

        double startLat = radians(sLat);
        double startLon = radians(sLon);
        double endLat = radians(eLat);
        double endLon = radians(eLon);

        double dLon = endLon - startLon;

        double dPhi = Math.log(Math.tan(endLat / 2.0 + Math.PI / 4.0) / Math.tan(startLat / 2.0 + Math.PI / 4.0));
        if (Math.abs(dLon) > Math.PI) {
            if (dLon > 0.0)
                dLon = -(2.0 * Math.PI - dLon);
            else
                dLon = (2.0 * Math.PI + dLon);
        }
        return (degrees(Math.atan2(dLon, dPhi)) + 360.0) % 360.0;
    }
    
    public static double radians(double n) {
        return n * (Math.PI / 180);
    }
    public static double degrees(double n) {
        return n * (180 / Math.PI);
    }
}
