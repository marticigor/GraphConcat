package building_blocks.clustering.utils;

public class ClipToRGBVisible {

	public static int clipToBounds(int value) {
		int upper = 255;
		int lower = 100;
		if (value > upper)
			return upper;
		if (value < lower)
			return lower;
		return value;
	}
	
}
