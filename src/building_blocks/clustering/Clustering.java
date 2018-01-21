package building_blocks.clustering;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import building_blocks.Graph;
import entity.NodeEntity;

public class Clustering {

	private Graph graph;
	private int numberClusters;
	private static final int CLUSTER_SIZE_DIVISOR = 100;

	private double[] lon;
	private double[] lat;
	private Set<IdWrapper> forest = new HashSet<IdWrapper>();
	private List<Point> unprocesedPoints = new LinkedList<Point>();
	private List<Edge> edges = new LinkedList<Edge>();

	public Clustering(Graph graph) {
		this.graph = graph;
		this.numberClusters = graph.getDatasetSize() / CLUSTER_SIZE_DIVISOR;
	}

	public void doInit() {
		System.out.println("doInitStart");
		int size = graph.getDatasetSize();
		lon = new double[size];
		lat = new double[size];
		int index = 0;
		for (NodeEntity n : graph.getRetrievableDataSet().keySet()) {
			lon[index] = n.getLon();
			lat[index] = n.getLat();
			index++;
		}
		System.out.println("doInitArraysFilled");

		long idRepresentative = 1;
		for (int i = 0; i < lat.length; i++) {
			Point current = new Point();
			current.lon = lon[i];
			current.lat = lat[i];
			unprocesedPoints.add(current);
			Set<Point> disjointSet = new HashSet<Point>();
			disjointSet.add(current);
			IdWrapper wrapper = new IdWrapper();
			wrapper.disjointSet = disjointSet;
			wrapper.idRepresentative = idRepresentative;
			current.wrapper = wrapper;
			forest.add(wrapper);
			idRepresentative++;
			if (idRepresentative % 1000 == 0)
				System.out.println("doInitIter " + idRepresentative);
		}

		int n = unprocesedPoints.size();
		int count = 0;
		for (int i = 0; i < n; i++) {
			Point current = unprocesedPoints.get(0);
			unprocesedPoints.remove(0);
			for (Point iterated : unprocesedPoints) {
				Edge newOne = new Edge();
				newOne.p1 = current;
				newOne.p2 = iterated;
				newOne.distance = distanceBtw(current, iterated);
				edges.add(newOne);
				// EDGE CONSTRUCTED
				if (count % 100000 == 0)
					System.out.println("edgeConstructed " + count);
				count ++;
			}
		}
		Collections.sort(edges);
	}

	// now What To Do?
	// Think about ways of adopting the Kruskal’s algorithm for solving this
	// problem.

	public void clusterize() {

		int currentForrestSize = forest.size();

		while (currentForrestSize > numberClusters) {
			// retrieve shortest edge
			Edge currentEdge = edges.get(0);

			// System.out.println("EDGE CONSIDERED "+edges.get(0).toString());

			// check if connection between points of this edge produces a cycle:
			// this means wrappers of both points have same id -- unnecessary,
			// wrapper == wrapper2 is better approach
			Point p1 = currentEdge.p1;
			long idP1 = p1.wrapper.idRepresentative;
			Point p2 = currentEdge.p2;
			long idP2 = p2.wrapper.idRepresentative;
			// if true, remove this edge from list of edges and continue
			if (idP1 == idP2) {
				// System.out.println("EDGE REMOVED !");
				edges.remove(0);
				continue;
			} // else union wrappers and sets of both points
			else {
				// System.out.println("---------- UNION OPERATION");
				IdWrapper w1 = p1.wrapper;
				Set<Point> s1 = w1.disjointSet;

				// System.out.println("-----------------------------------");
				// for (Point point1 : s1)
				// System.out.println("----- set 1 " + point1.toString());

				IdWrapper w2 = p2.wrapper;
				Set<Point> s2 = w2.disjointSet;

				// System.out.println("-----------------------------------");
				// for (Point point2 : s2)
				// System.out.println("----- set 2 " + point2.toString());

				for (Point p : s2) {
					p.wrapper = w1;
					s1.add(p);
				}

				forest.remove(w2);
				edges.remove(0);
				s2 = null;
				currentForrestSize--;

				// System.out.println("-- resulting id of this union " +
				// w1.idRepresentative);
				System.out.println("-- currentForrestSize " + currentForrestSize);

			}
		}
	}

	// --------------------------------------------------------------------------------------------------

	private class Point {
		double lon;
		double lat;
		IdWrapper wrapper;

		public String toString() {
			return "< lon: " + lon + ", lat: " + lat + " >" + " wrapper id: " + wrapper.idRepresentative;
		}
	}

	private class Edge implements Comparable<Edge> {
		Point p1;
		Point p2;
		double distance;

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

	private class IdWrapper {
		Set<Point> disjointSet;
		long idRepresentative;

		public String toString() {
			String returnVal = "----------------- idWrapper: idRepresentative: " + idRepresentative + " | size: "
					+ disjointSet.size() + "\n";
			for (Point p : disjointSet)
				returnVal += "--------------------- " + p.toString();
			return returnVal;
		}

	}

	//TODO safety
	//TODO haversine in m better?
	private double distanceBtw(Point p1, Point p2) {
		double deltaLon = (p1.lon - p2.lon);
		double deltaLat = (p1.lat - p2.lat);
		return Math.sqrt((deltaLon * deltaLon) + (deltaLat * deltaLat));
	}

}
