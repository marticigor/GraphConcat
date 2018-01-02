package utils;

import java.text.DecimalFormat;

public class FormatDouble {

	private static DecimalFormat df10 = new DecimalFormat("##############.##########");
	private static DecimalFormat df2 = new DecimalFormat("##############.##");
	
	public static String formatDouble10(double d){
		return df10.format(d);
	}
	public static String formatDouble2(double d){
		return df2.format(d);
	}
	
	public static void tests(){
		
		double [] cases = new double [] { 12354854.21212, - 12.12123141556, 12.12123141556, 12, 0, 1d, 1.2E-7, -132.0120012, 123.00000000000};
		for (double d : cases){
			System.out.println("case " + d + " formatted: " + formatDouble10(d));
		}
	}
	
	public static void main(String[] args){
		tests();
	}
	
}
