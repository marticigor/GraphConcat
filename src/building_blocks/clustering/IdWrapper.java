package building_blocks.clustering;

import java.util.Set;

class IdWrapper {
	Set<Point> disjointSet;
	long idRepresentative;
	public static final int CLUSTER_SIZE_DIVISOR = 20;//15

	public String toString() {
		String returnVal = "----------------- idWrapper: idRepresentative: " + idRepresentative + " | size: "
				+ disjointSet.size() + "\n";
		for (Point p : disjointSet)
			returnVal += "--------------------- " + p.toString();
		return returnVal;
	}
}
