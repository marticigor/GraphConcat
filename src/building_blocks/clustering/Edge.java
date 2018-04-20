package building_blocks.clustering;

public class Edge implements Comparable<Edge> {
	public final Point p1;
	public final Point p2;
	public final double distance;

	public Edge(Point p1, Point p2, double dist) {
		this.p1 = p1;
		this.p2 = p2;
		this.distance = dist;
	}

	public int compareTo(Edge theOther) {
		if (this.distance > theOther.distance)
			return 1;
		else if (this.distance < theOther.distance)
			return -1;
		else
			return 0;
	}

	public String toString() {
		return p1.toString() + " | " + p2.toString() + " | dist: " + this.distance;
	}
}