package building_blocks.clustering;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import building_blocks.Graph;
import core.App;
import entity.NodeEntity;
import utils.Haversine;

public class Clustering {

	private Graph graph;
	@SuppressWarnings("unused")
	private App app;
	private int numberClusters;
	private static final int CLUSTER_SIZE_DIVISOR = 20;
	// to prevent out of memory error for these extremely dense graphs;
	private static final double MAX_EDGE_DISTANCE = 500.0;

	private double[] lon;
	private double[] lat;
	private Set<IdWrapper> forest = new HashSet<IdWrapper>();
	private List<Point> unprocesedPoints = new LinkedList<Point>();
	private List<Edge> edges = new LinkedList<Edge>();
	int countWrappers = 0;

	public Clustering(Graph graph, App app) {
		this.graph = graph;
		this.app = app;
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
		double dist = 0;
		for (int i = 0; i < n; i++) {
			Point current = unprocesedPoints.get(0);
			unprocesedPoints.remove(0);
			for (Point iterated : unprocesedPoints) {
				dist = distanceBtw(current, iterated);
				// to prevent out of memory error for these extremely dense graphs;
				if (dist > MAX_EDGE_DISTANCE)
					continue;
				Edge newOne = new Edge();
				newOne.p1 = current;
				newOne.p2 = iterated;
				newOne.distance = dist;
				edges.add(newOne);
				// EDGE CONSTRUCTED
				if (count % 100000 == 0)
					System.out.println("edges: " + count);
				count++;
			}
		}
		Collections.sort(edges);
		System.out.println("edges.size : " + edges.size());
	}

	/**
	 * Kruskal
	 */
	public void clusterize() {
		System.out.println("\n\nKruskal start");
		System.out.println("Number clusters expected: " + numberClusters);

		while (forest.size() > numberClusters && edges.size() > 0) {
			// retrieve shortest edge
			Edge currentEdge = edges.get(0);

			Point p1 = currentEdge.p1;
			Point p2 = currentEdge.p2;
			
			if(p1.wrapper.idRepresentative == p2.wrapper.idRepresentative) {
				edges.remove(0);
				continue;
			}
			
			// System.out.println("---------- UNION OPERATION");
			IdWrapper w1 = p1.wrapper;
			Set<Point> s1 = w1.disjointSet;

			IdWrapper w2 = p2.wrapper;
			Set<Point> s2 = w2.disjointSet;

			for (Point p : s2) {
				p.wrapper = w1;
				s1.add(p);
			}

			forest.remove(w2);
			edges.remove(0);

			s2 = null;
			w2 = null;

			if (forest.size() % 10000 == 0)
				System.out.println("-- currentForrestSize " + forest.size());
		}

		for (IdWrapper w : forest) {
			countWrappers += w.disjointSet.size();
		}
		if (countWrappers != graph.getDatasetSize()) {
			System.err.println("NODES MISSED");
			// throw new RuntimeException("NODES MISSED");
		}
		printForestDisjointTreesStats();
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
		for (IdWrapper w : forest) {
			System.out.println("size: " + w.disjointSet.size());
			count += w.disjointSet.size();
		}
		System.err.println("DIFF: " + (graph.getDatasetSize() - count));
		System.out.println("=============================================");
	}
}
