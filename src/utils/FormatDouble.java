package utils;

import java.text.DecimalFormat;

public class FormatDouble {

	private static DecimalFormat df8 = new DecimalFormat("##############.##########");
	
	public static String formatDouble(double d){
		return df8.format(d);
	}
	
	public static void tests(){
		
		double [] cases = new double [] { 12354854.21212, - 12.12123141556, 12.12123141556, 12, 0, 1d, 1.2E-7, -132.0120012, 123.00000000000};
		for (double d : cases){
			System.out.println("case " + d + " formatted: " + formatDouble(d));
		}
	}
	
	public static void main(String[] args){
		tests();
	}
	
}
