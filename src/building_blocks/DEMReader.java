package building_blocks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DEMReader {

	private static final String PATH = "/home/radim/data/DEMData/";
	public static final int SIZE = 1201;

	public short[] getData(String name) {

		short[] dataset = new short[SIZE * SIZE];

		// stream
		FileInputStream in = null;
		BufferedInputStream bin = null;

		try {

			in = new FileInputStream(PATH + name);
			bin = new BufferedInputStream(in, 16 * 1024);

			int aI = 0;
			int bI = 0;
			byte a = 0;
			byte b = 0;

			for (int i = 0; i < dataset.length; i++) {

				aI = bin.read();
				bI = bin.read();

				if (aI == -1 || bI == -1)
					throw new RuntimeException("something is wrong here 1");

				a = (byte) aI;
				b = (byte) bI;

				dataset[i] = (short) (((a & 0xFF) << 8) | (b & 0xFF));
			}

			int stop = bin.read();
			if (stop != -1)
				throw new RuntimeException("something is wrong here 2");

			/*
			 * The read() method of a FileInputStream returns an int which
			 * contains the byte value of the byte read. If the read() method
			 * returns -1, there is no more data to read in the FileInputStream,
			 * and it can be closed. That is, -1 as int value, not -1 as byte
			 * value. There is a difference here!
			 */

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("HALT while reading");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("HALT while closing in");
				}
			}
			if (bin != null) {
				try {
					bin.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("HALT while closing bin");
				}
			}
		}
		return dataset;
	}

	/**
	 * 
	 * @param args
	 */
	public String findNameOfTile(double longitude, double latitude) {

		String zeroesLon = null;
		String zeroesLat = null;

		String prefixLon = longitude >= 0 ? "E" : "W";
		if (Math.abs(longitude) < 10)
			zeroesLon = "00";
		else if (Math.abs(longitude) >= 10 && Math.abs(longitude) < 100)
			zeroesLon = "0";
		else
			zeroesLon = "";

		String prefixLat = latitude >= 0 ? "N" : "S";
		if (Math.abs(latitude) < 10)
			zeroesLat = "0";
		else
			zeroesLat = "";

		String latitudeS = String.valueOf((int) (Math.abs(Math.floor(latitude))));
		String longitudeS = String.valueOf((int) (Math.abs(Math.floor(longitude))));

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
