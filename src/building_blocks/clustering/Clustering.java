package building_blocks.clustering;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import building_blocks.Graph;
import core.App;
import entity.NodeEntity;
import lib_duke.ImageResource;
import lib_duke.Pixel;
import utils.Haversine;

public class Clustering {

	private Graph graph;
	private App app;
	private int numberClusters;
	private static final int EXCLUDE_FROM_COMPARISON_IMMEDIATELLY_DIVISOR = 16;
	private final double excludeThreshold;
	private static final int CLUSTER_SIZE_DIVISOR = 15;
	// to prevent out of memory error for these extremely dense graphs;
	private static final double MAX_EDGE_DISTANCE = 500.0;

	private double[] lon;
	private double[] lat;
	private final Set<IdWrapper> forest = new HashSet<IdWrapper>();
	private final List<Point> points = new LinkedList<Point>();
	private final List<Edge> edges = new LinkedList<Edge>();
	int countWrappers = 0;

	public Clustering(Graph graph, App app) {
		this.graph = graph;
		this.app = app;
		double spanLat = app.maxLat - app.minLat;
		double spanLon = app.maxLon - app.minLon;
		double span = (spanLat + spanLon) / 2;
		this.excludeThreshold = span / EXCLUDE_FROM_COMPARISON_IMMEDIATELLY_DIVISOR;
		this.numberClusters = graph.getDatasetSize() / CLUSTER_SIZE_DIVISOR;
	}

	public void doInit() {

		System.out.println("\n\nCLUSTERING\n -- doInitStart");
		int size = graph.getDatasetSize();
		lon = new double[size];
		lat = new double[size];
		int index = 0;
		for (NodeEntity n : graph.getRetrievableDataSet().keySet()) {
			lon[index] = n.getLon();
			lat[index] = n.getLat();
			index++;
		}
		System.out.println(" -- doInitArraysFilled\n -- iD wrappers creation\n");

		// points
		int printedInit1 = 0;
		long idRepresentative = 1;
		for (int i = 0; i < lat.length; i++) {
			IdWrapper wrapper = new IdWrapper();
			Set<Point> disjointSet = new HashSet<Point>();
			wrapper.disjointSet = disjointSet;
			wrapper.idRepresentative = idRepresentative;
			final Point current = new Point(lat[i], lon[i], wrapper);
			disjointSet.add(current);
			points.add(current);
			forest.add(wrapper);
			idRepresentative++;
			if (idRepresentative % 1000 == 0) {
				System.out.print(", " + idRepresentative);
				printedInit1++;
				if (printedInit1 % 20 == 0)
					System.out.println();

			}
		}

		// edges
		final int n = points.size();
		final int howMany = 8;
		final int chunk = n / howMany;
		final int[] chunkBounds = new int[howMany + 1];
		chunkBounds[0] = 0;
		chunkBounds[howMany] = n;
		for (int i = 1; i < howMany; i++) {
			chunkBounds[i] = i * chunk;
		}
		//from now on chunkBounds treat as immutable
		
		System.out.println("\n\nn: " + n);
		System.out.println("chunk: " + chunk);
		System.out.println("chunkBounds: " + chunkBounds.toString());
		for (int i : chunkBounds) System.out.println(i);

		long count = 0;
		long skipped = 0;
		double dist = 0;
		int printedInit2 = 0;
		double lonDelta = 0.0;
		double latDelta = 0.0;
		System.out.println("\n\n -- edges creation\n");

		for (int i = 0; i < n; i++) {
			Point current = points.get(i);
			for (Point iterated : points) {
				// to prevent out of memory error for these extremely dense
				// graphs;
				lonDelta = Math.abs(current.lon - iterated.lon);
				latDelta = Math.abs(current.lat - iterated.lat);
				if (lonDelta > excludeThreshold || latDelta > excludeThreshold)
					continue;
				dist = distanceBtw(current, iterated);
				if (dist > MAX_EDGE_DISTANCE) {
					skipped++;
					continue;
				}
				final Edge newOne = new Edge(current, iterated, dist);
				edges.add(newOne);
				if (count % 100000 == 0) {
					System.out.print(", created: " + count + " | skipped: " + skipped);
					printedInit2++;
					if (printedInit2 % 5 == 0)
						System.out.println();
				}
				count++;
			}
		}

		Collections.sort(edges);
		System.out.println("\n\nedges.size : " + edges.size());
	}

	/**
	 * Kruskal
	 */
	public void clusterize() {
		System.out.println("\n\nKruskal start");
		System.out.println("Number of clusters expected: " + numberClusters);
		System.out.println("Forest sizes:\n");
		int printed = 0;

		while (forest.size() > numberClusters && edges.size() > 0) {
			// retrieve shortest edge
			Edge currentEdge = edges.get(0);

			Point p1 = currentEdge.p1;
			Point p2 = currentEdge.p2;

			if (p1.wrapper.idRepresentative == p2.wrapper.idRepresentative) {
				edges.remove(0);
				continue;
			}

			// UNION OPERATION
			IdWrapper w1 = p1.wrapper;
			Set<Point> s1 = w1.disjointSet;

			IdWrapper w2 = p2.wrapper;
			Set<Point> s2 = w2.disjointSet;

			for (Point p : s2) {
				p.setWrapper(w1);
				s1.add(p);
			}

			forest.remove(w2);
			edges.remove(0);

			s2 = null;
			w2 = null;

			if (forest.size() % 10000 == 0) {
				System.out.print(", " + forest.size());
				printed++;
				if (printed % 20 == 0)
					System.out.println();
			}
		}

		for (IdWrapper w : forest) {
			countWrappers += w.disjointSet.size();
		}
		if (countWrappers != graph.getDatasetSize()) {
			System.err.println("NODES MISSED");
			// throw new RuntimeException("NODES MISSED");
		}
		printForestDisjointTreesStats();
		visualizeClusters();
	}

	/**
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	private double distanceBtw(Point p1, Point p2) {
		return Haversine.haversineInM(p1.lat, p1.lon, p2.lat, p2.lon);
	}

	private void printForestDisjointTreesStats() {
		System.out.println("\n\n=============================================");
		System.out.println("ForestDisjointTrees size: " + forest.size());
		int count = 0;
		int printed = 0;
		System.out.println("Sizes:\n");
		for (IdWrapper w : forest) {
			System.out.print(", " + w.disjointSet.size());
			printed++;
			if (printed % 20 == 0)
				System.out.println();
			count += w.disjointSet.size();
		}
		System.err.println("\nDIFF: " + (graph.getDatasetSize() - count));
		System.out.println("\n=============================================");
	}

	private void visualizeClusters() {
		ImageResource ir = new ImageResource(App.PIC_WIDTH_MAX_INDEX + 1, App.PIC_HEIGHT_MAX_INDEX + 1);
		Random random = new Random();
		ir.draw();
		for (IdWrapper wrapper : forest) {
			int R = clipToBounds(random.nextInt(255));
			int G = clipToBounds(random.nextInt(255));
			int B = clipToBounds(random.nextInt(255));
			for (Point p : wrapper.disjointSet) {
				Pixel pix = ir.getPixel(app.convertLonToPixX(p.lon), app.convertLatToPixY(p.lat));
				pix.setRed(R);
				pix.setGreen(G);
				pix.setBlue(B);
			}
		}
		ir.draw();
	}

	private int clipToBounds(int value) {
		int upper = 255;
		int lower = 100;
		if (value > upper)
			return upper;
		if (value < lower)
			return lower;
		return value;
	}

	// --------------------------------------------------------------------------------------------------

	private class Point {
		private final double lon;
		private final double lat;
		private IdWrapper wrapper;

		private Point(double lat, double lon, IdWrapper wrapper) {
			this.lat = lat;
			this.lon = lon;
			this.wrapper = wrapper;
		}

		private synchronized void setWrapper(IdWrapper w) {
			this.wrapper = w;
		}

		public synchronized String toString() {
			return "< lon: " + lon + ", lat: " + lat + " >" + " wrapper id: " + wrapper.idRepresentative;
		}
	}

	private class Edge implements Comparable<Edge> {
		private final Point p1;
		private final Point p2;
		private final double distance;

		private Edge(Point p1, Point p2, double dist) {
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
}
