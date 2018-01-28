package building_blocks.clustering;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

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
	private static final int EXCLUDE_FROM_COMPARISON_IMMEDIATELLY_DIVISOR = 256;
	private final double excludeThreshold;
	private static final int CLUSTER_SIZE_DIVISOR = 15;
	// to prevent out of memory error for these extremely dense an big graphs;
	private static final double MAX_EDGE_DISTANCE = 120.0;

	private List<NodeEntity> entities;
	private final Set<IdWrapper> forest = new HashSet<IdWrapper>();
	private final List<Point> points = new LinkedList<Point>();
	private List<Edge> edges;
	private final Map<Edge, Edge> edgesConcurrentRetrievableSet = new ConcurrentHashMap<Edge, Edge>();
	private int countWrappers = 0;
	private CyclicBarrier barrier;
	private Thread main = Thread.currentThread();
	private volatile boolean barrierReached = false;

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
		entities = new LinkedList<NodeEntity>(graph.getRetrievableDataSet().keySet());
		System.out.println(" -- doInitArraysFilled\n -- iD wrappers creation\n");

		// points
		int printed = 0;
		long idRepresentative = 1;
		for (NodeEntity n : entities) {
			IdWrapper wrapper = new IdWrapper();
			Set<Point> disjointSet = new HashSet<Point>();
			wrapper.disjointSet = disjointSet;
			wrapper.idRepresentative = idRepresentative;
			final Point current = new Point(wrapper, n);
			disjointSet.add(current);
			points.add(current);
			forest.add(wrapper);
			idRepresentative++;
			if (idRepresentative % 1000 == 0) {
				System.out.print(", " + idRepresentative);
				printed++;
				if (printed % 20 == 0)
					System.out.println();

			}
		}

		// edges
		final int howMany = 8;
		final int chunk = points.size() / howMany;
		final int[] chunkBounds = new int[howMany + 1];
		chunkBounds[0] = 0;
		chunkBounds[howMany] = points.size();
		// left inclusive right exclusive
		for (int i = 1; i < howMany; i++) {
			chunkBounds[i] = i * chunk;
		}
		// from now on treat chunkBounds as immutable
		// from now on treat points as immutable
		System.out.println("\n\nn: " + points.size());
		System.out.println("chunk: " + chunk);
		System.out.println("chunkBounds: " + chunkBounds.toString());
		for (int i : chunkBounds)
			System.out.println(" -- " + i);

		barrier = new CyclicBarrier(howMany, new Runnable() {
			@Override
			public void run() {
				synchronized (printSync) {
					System.out.println("\n====== Barrier reached ======\nNOTIFYING main\n");
				}
				// spurious wake up prevention
				barrierReached = true;
				synchronized (main) {
					main.notify();
				}
			}
		});

		// fork workers
		for (int positionChunk = 1; positionChunk < chunkBounds.length; positionChunk++) {

			int fromIncl = chunkBounds[positionChunk - 1];
			int toExcl = chunkBounds[positionChunk];
			synchronized (printSync) {
				System.out.println("\nCreating an edge worker, portion given to him (incl, excl): " + fromIncl + ", "
						+ toExcl + "\n");
			}
			EdgesInKartesianWorker worker = new EdgesInKartesianWorker(fromIncl, toExcl);
			Thread workerThread = new Thread(worker);
			workerThread.start();

		}
		// join workers(barrier)
		synchronized (main) {
			try {
				while (barrierReached == false) {
					synchronized (printSync) {
						System.out.println("\nPUTTING main ON WAIT\n");
					}
					main.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException("HALT IN CLUSTERING - WAITING FOR BARRIER TO INVOKE notify()");
			}
		}

		System.out.println("main CARIES ON");
		edges = new LinkedList<Edge>(edgesConcurrentRetrievableSet.keySet());
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
			for (Point p : w.disjointSet) {
				p.n.setIdCLuster(w.idRepresentative);
			}
		}
		if (countWrappers != graph.getDatasetSize()) {
			System.err.println("NODES MISSED");
			throw new RuntimeException("NODES MISSED");
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
		int reducingFaktor = determinePrintReducingFactor(forest.size());
		int iteratedAlready = 0;
		System.out.println("SIZES");
		System.out.println("reducingFaktor(print only): " + reducingFaktor);
		for (IdWrapper w : forest) {
			if (iteratedAlready % reducingFaktor == 0) {
				System.out.print(", " + w.disjointSet.size());
				printed++;
				if (printed % 60 == 0)
					System.out.println();
			}
			count += w.disjointSet.size();
			iteratedAlready++;
		}
		System.err.println("\nDIFF: " + (graph.getDatasetSize() - count));
		System.out.println("\n=============================================");
	}

	private int determinePrintReducingFactor(int dataStructureSize) {
		int faktor = 1;
		while (true) {
			if (dataStructureSize / faktor <= 100) {
				return faktor;
			} else {
				faktor++;
			}
		}
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

	private final Object printSync = new Object();
	private AtomicInteger printed = new AtomicInteger(0);

	private class EdgesInKartesianWorker implements Runnable {

		private final int fromInclusive;
		private final int toExclusive;

		private EdgesInKartesianWorker(int from, int to) {
			this.fromInclusive = from;
			this.toExclusive = to;
		}

		@Override
		public void run() {

			long count = 0;
			long skipped = 0;
			double dist = 0;
			double lonDelta = 0.0;
			double latDelta = 0.0;

			for (int i = fromInclusive; i < toExclusive; i++) {
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
					edgesConcurrentRetrievableSet.put(newOne, newOne);
					if (count % 100000 == 0) {
						synchronized (printSync) {
							System.out.print(", created: " + count + " | skipped: " + skipped);
						}
						printed.incrementAndGet();
						if (printed.get() % 5 == 0)
							synchronized (printSync) {
								System.out.println();
							}
					}
					count++;
				}
			}
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
				throw new RuntimeException("HALT IN CLUSTERING - calling barrier.await()");
			}
		}
	}

	// --------------------------------------------------------------------------------------------------

	private class Point {
		private final double lon;
		private final double lat;
		private IdWrapper wrapper;
		private NodeEntity n;

		private Point(IdWrapper wrapper, NodeEntity n) {
			this.lat = n.getLat();
			this.lon = n.getLon();
			this.wrapper = wrapper;
			this.n = n;
		}

		private void setWrapper(IdWrapper w) {
			this.wrapper = w;
		}

		public String toString() {
			return "< lon: " + lon + ", lat: " + lat + " >" + " wrapper id: " + wrapper.idRepresentative + "NodeEntity "
					+ n.toString();
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
		private Set<Point> disjointSet;
		private long idRepresentative;

		public String toString() {
			String returnVal = "----------------- idWrapper: idRepresentative: " + idRepresentative + " | size: "
					+ disjointSet.size() + "\n";
			for (Point p : disjointSet)
				returnVal += "--------------------- " + p.toString();
			return returnVal;
		}
	}
}
