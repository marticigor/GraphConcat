package building_blocks.clustering;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import building_blocks.Graph;
import building_blocks.clustering.utils.ClipToRGBVisible;
import core.App;
import entity.NodeEntity;
import ifaces.Clusterizer;
import lib_duke.ImageResource;
import lib_duke.Pixel;

public class kClusters implements Clusterizer{

	private Graph graph;
	private App app;
	private int numberClusters;

	private List<NodeEntity> entities;
	private List<Point> centroids;
	private long idRepresentative = 1;

	public kClusters(Graph graph, App app) {
		this.graph = graph;
		this.app = app;
		numberClusters = graph.getDatasetSize() / IdWrapper.CLUSTER_SIZE_DIVISOR;
		entities = new LinkedList<NodeEntity>(this.graph.getRetrievableDataSet().keySet());
		centroids = new LinkedList<Point>();
	}
	
	@Override
	public void doInit(){
		//do nothing
	}

	/**
	 * k-Cluster aproximation algo, farthest first traversal algo
	 */
	@Override
	public void clusterize() {
		System.out.println("\n\nk-Clusters start");
		System.out.println("Number of clusters expected: " + numberClusters);
		
		NodeEntity firstEntity = entities.get(entities.size() / 2);
		IdWrapper firstWrapper = new IdWrapper();
		firstWrapper.disjointSet = new HashSet<Point>();
		firstWrapper.idRepresentative = this.idRepresentative;
		this.idRepresentative ++;
		Point firstCentroid = new Point(firstWrapper, firstEntity);
		firstCentroid.setNodeEntityIsCentroid();
		centroids.add(firstCentroid);
		
		Point regularCentroid = firstCentroid;
		Point newCentroid;
		IdWrapper regularWrapper;
		NodeEntity regularFarthest;
		

	    // Scan the list of not-yet-selected points to find a point p that
		// has the maximum distance from the selected points.
	    
		// Remove p from the not-yet-selected points and add it to the end
		// of the sequence of selected points.
	    
		// For each remaining not-yet-selected point q, replace the distance stored
		// for q by the minimum of its old value and the distance from p to q.

		
		for(int i = 0; i < numberClusters - 1; i ++){
			regularWrapper = new IdWrapper();
			regularWrapper.disjointSet = new HashSet<Point>();
			regularWrapper.idRepresentative = this.idRepresentative;
			this.idRepresentative ++;
			regularFarthest = getFarthestNodeEntity(regularCentroid);
			newCentroid = new Point(regularWrapper, regularFarthest);
			regularCentroid = newCentroid;
			newCentroid.setNodeEntityIsCentroid();
			if(i % 100 == 0)System.out.println("(reduced print) Adding new centroid: " + newCentroid.singleLineToString());
			centroids.add(newCentroid);
		}
		
		System.out.println("Setting id start");
		Point closestCentroid;
		for(NodeEntity n : entities){
			
			closestCentroid = getClosestCentroid(n);
			n.setIdCLuster(closestCentroid.wrapper.idRepresentative);
			closestCentroid.wrapper.disjointSet.add(new Point(null, n));
			
		}
		System.out.println("Setting id finish");
		visualizeClusters();
	}
	
	/*
	 *
	 */
	private NodeEntity getFarthestNodeEntity(Point latestCentroid){
		NodeEntity farthest = null;
		double maxDist = 0.0;
		double oldDist = 0.0;
		for(NodeEntity n : entities){
			if (n.isCentroid) continue;
			
			// first update (farthest first traversal)
			// For each remaining not-yet-selected NodeEntity n, replace the distance stored
			// for n by the minimum of its oldDist and the distance from latestCentroid to n.
			oldDist = n.cartesianDistFromSelectedPoints;
			n.cartesianDistFromSelectedPoints = Math.min(oldDist, cartesianDist(latestCentroid, n));
			
			// Scan the list of not-yet-selected NodeEntities to find a NodeEntity farthest, that
			// has the maximum distance from the selected Centroids (Points)
			if(n.cartesianDistFromSelectedPoints > maxDist){
				maxDist = n.cartesianDistFromSelectedPoints;
				farthest = n;
			}
		}
		return farthest;
	}
	
	/*
	 * 
	 */
	private Point getClosestCentroid(NodeEntity n){
		Point closest = null;
		double minDist = Double.MAX_VALUE;
		double dist = 0.0;
		for (Point centroid: centroids){
			dist = cartesianDist(centroid, n);
			if(dist < minDist){
				closest = centroid;
				minDist = dist;
			}
		}
		return closest;
	}
	
	private double cartesianDist(Point p, NodeEntity n){
		double dLat = p.lat - n.getLat();
		double dLon = p.lon - n.getLon();
		return Math.sqrt((dLat * dLat) + (dLon * dLon));
	}

	@Override
	public void visualizeClusters() {
		ImageResource ir = new ImageResource(App.PIC_WIDTH_MAX_INDEX + 1, App.PIC_HEIGHT_MAX_INDEX + 1);
		Random random = new Random();
		ir.draw();
		for (Point centroid : centroids) {
			int R = ClipToRGBVisible.clipToBounds(random.nextInt(255));
			int G = ClipToRGBVisible.clipToBounds(random.nextInt(255));
			int B = ClipToRGBVisible.clipToBounds(random.nextInt(255));

			Pixel pix = ir.getPixel(app.convertLonToPixX(centroid.lon), app.convertLatToPixY(centroid.lat));
			pix.setRed(R);
			pix.setGreen(G);
			pix.setBlue(B);
			
		}
		ir.draw();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("HALT, INTERUPTED ?!");
		}
		ir.blacken();
		
		for (Point centroid : centroids) {
			int R = ClipToRGBVisible.clipToBounds(random.nextInt(255));
			int G = ClipToRGBVisible.clipToBounds(random.nextInt(255));
			int B = ClipToRGBVisible.clipToBounds(random.nextInt(255));
			for (Point p : centroid.wrapper.disjointSet) {
				Pixel pix = ir.getPixel(app.convertLonToPixX(p.lon), app.convertLatToPixY(p.lat));
				pix.setRed(R);
				pix.setGreen(G);
				pix.setBlue(B);
			}
		}
		ir.draw();
	}
}
