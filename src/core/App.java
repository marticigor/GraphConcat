package core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import building_blocks.DEMReader;
import building_blocks.DEMTile;
import building_blocks.Graph;
import building_blocks.Tile;
import building_blocks.WriteOutputFile;
import entity.DB_names;
import entity.NmbShotsEntity;
import entity.NodeEntity;
import lib_duke.ImageResource;
import lib_duke.LineMaker;
import lib_duke.Pixel;
import session.SessionAdapter;

public class App {

	// output
	private final static String PATH = "/home/radim/stravaGHMdata/decent/SanFranciscoBaySouth14cycling";
	private final static String NAME = "test1";

	public final static boolean development = false;

	private double minLon = Double.MAX_VALUE, maxLon = Double.MIN_VALUE, minLat = Double.MAX_VALUE,
			maxLat = Double.MIN_VALUE;
	private double deltaLat, deltaLon;
	public static final int PIC_WIDTH_MAX_INDEX = 1999;
	public static final int PIC_HEIGHT_MAX_INDEX = 999;

	public static void main(String args[]) {
		System.out.println(" ---------------------------------------------------------------------- ");
		App app = new App();
		// app.veryBasicTest();
		// app.testHash();
		// app.testEquals();
		app.compose();
	}

	/**
	 * 
	 */
	private void compose() {
		System.out.println("Working with " + DB_names.NAME);
		List<NmbShotsEntity> shots = SessionAdapter.getInstance().loadNmbShotsEntities();
		System.out.println("Loaded ShotsEntity");
		int nmbOfShots = shots.get(shots.size() - 1).getNmb();
		int maxShotId = nmbOfShots - 1;
		System.out.println("NmbOfShots " + nmbOfShots + " maxShotId " + maxShotId);
		Graph graph = new Graph();
		// iterate tiles
		for (int shot = 0; shot <= maxShotId; shot++) {
			Tile tile = new Tile(shot);
			printTileInfo(tile, shot);
			graph.buildIn(tile);
		}

		graph.computeMergedEdgeSize();
		graph.printStats();

		if (development) {
			computeBoundsOfExistingNodes(graph);
			visualTest(graph);
			graph.testOverlap();
			graph.testCompareLeftRightPlay();
		}

		List<NodeEntity> listedDataSet = new ArrayList<NodeEntity>(graph.getRetrievableDataSet().keySet());
		Collections.sort(listedDataSet);
		
		//add elev
		System.out.println("WORKING ON ELEV");
		DEMReader reader = new DEMReader();
		Map<NodeEntity, DEMTile> nodeToDEMTile = new HashMap<NodeEntity, DEMTile>();
		Map<String, DEMTile> nameToDEMTile = new HashMap<String, DEMTile>();

		for(NodeEntity node : listedDataSet){
			
			String neededTile = reader.findNameOfTile(node.getLon(), node.getLat());
			
			if(nameToDEMTile.keySet().contains(neededTile)){
				nodeToDEMTile.put(node, nameToDEMTile.get(neededTile));
			}else{
				nameToDEMTile.put(neededTile, new DEMTile(neededTile));
				nodeToDEMTile.put(node, nameToDEMTile.get(neededTile));
			}
		}
		System.out.println("tiles needed:");
		for(String tile : nameToDEMTile.keySet())System.out.println(tile);
		
		/*
		 * System.out.println("\n\nwriting: " + PATH + File.separator + NAME);
		 * WriteOutputFile wof = new WriteOutputFile(PATH, NAME, listedDataSet,
		 * graph); try { wof.write(); } catch (IOException e) {
		 * e.printStackTrace(); throw new RuntimeException("write"); }
		 */

		System.out.println("FINISHED");
	}

	/**
	 * TODO works globally? No.
	 */
	private void visualTest(Graph graph) {
		ImageResource ir = new ImageResource(PIC_WIDTH_MAX_INDEX + 1, PIC_HEIGHT_MAX_INDEX + 1);
		Set<NodeEntity> needsMergeFromRight = graph.getNeedsMergeFromRight();
		if (!(needsMergeFromRight.size() == (graph.getSizeProblem() + graph.getContainsProblem()))) {
			graph.printMergeProblem();
			System.err.println("Warning: match");
		} else {
			graph.printMergeProblem();
		}
		Pixel current;
		Map<NodeEntity, NodeEntity> dataSet = graph.getRetrievableDataSet();
		for (NodeEntity ne : dataSet.keySet()) {
			current = ir.getPixel(convertLonToPixX(ne.getLon()), convertLatToPixY(ne.getLat()));
			current.setRed(255);
			if (needsMergeFromRight.contains(ne)) {
				current.setGreen(255);
				current.setBlue(255);
			}
		}

		ir.draw();

		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("interupt");
		}

		LineMaker lm = new LineMaker(ir);

		int x1, x2, y1, y2;
		Map<NodeEntity, NodeEntity> dataSet1 = graph.getRetrievableDataSet();
		for (NodeEntity ne : dataSet1.keySet()) {
			x1 = convertLonToPixX(ne.getLon());
			y1 = convertLatToPixY(ne.getLat());
			for (NodeEntity adj : ne.getAdjacents()) {
				x2 = convertLonToPixX(adj.getLon());
				y2 = convertLatToPixY(adj.getLat());
				lm.drawLine(x1, y1, x2, y2, 0, 255, 255);
			}
		}

		ir.draw();
	}

	/**
	 * these are bounds for visualisation, not map bounds
	 * @param graph
	 */
	public void computeBoundsOfExistingNodes(Graph graph) {
		Map<NodeEntity, NodeEntity> dataSet = graph.getRetrievableDataSet();
		double lat, lon;
		for (NodeEntity ne : dataSet.keySet()) {
			lat = ne.getLat();
			lon = ne.getLon();
			minLat = (lat < minLat) ? lat : minLat;
			maxLat = (lat > maxLat) ? lat : maxLat;
			minLon = (lon < minLon) ? lon : minLon;
			maxLon = (lon > maxLon) ? lon : maxLon;
		}
		deltaLat = maxLat - minLat;
		deltaLon = maxLon - minLon;
	}

	/**
	 * TODO works globally? No.
	 * 
	 * @param lat
	 * @return
	 */
	public int convertLatToPixY(double lat) {
		assert (lat <= maxLat);
		double overlapLat = maxLat - lat;
		double ratio = overlapLat / deltaLat;
		int ret = ((int) (ratio * (double) PIC_HEIGHT_MAX_INDEX));
		assert (ret <= PIC_HEIGHT_MAX_INDEX);
		return ret;
	}

	/**
	 * TODO works globally? No.
	 * 
	 * @param lat
	 * @return
	 */
	public int convertLonToPixX(double lon) {
		assert (lon >= minLon);
		double overlapLon = lon - minLon;
		double ratio = overlapLon / deltaLon;
		int ret = ((int) (ratio * (double) PIC_WIDTH_MAX_INDEX));
		assert (ret <= PIC_WIDTH_MAX_INDEX);
		return ret;
	}

	/**
	 * 
	 */
	private void printBounds() {
		System.out.println(" --------------------- BOUNDS:");
		System.out.println("minLat " + minLat);
		System.out.println("maxLat " + maxLat);
		System.out.println("minLon " + minLon);
		System.out.println("maxLon " + maxLon);
		System.out.println("deltaLat " + deltaLat);
		System.out.println("deltaLon " + deltaLon);
	}

	/**
	 * 
	 * @param tile
	 * @param shot
	 */
	private void printTileInfo(Tile tile, int shot) {
		System.out.println("=========================================================================");
		System.err.println("\nSHOT " + shot);
		System.err.println("TILE " + tile.toString());
		System.out.println("=========================================================================");
	}

	@SuppressWarnings("unused")
	private void testHash() {
		NodeEntity a = new NodeEntity(0, 50.1234567891, 14.12345678921, (short) -1, new HashSet<NodeEntity>());
		NodeEntity b = new NodeEntity(0, 50.1234567891, 14.12345678921, (short) -1, new HashSet<NodeEntity>());
		System.out.println(a.hashCode() + " - " + b.hashCode());
		System.out.println(a.equals(b) + " - " + b.equals(a));
		System.out.println(a == b);
		HashSet<NodeEntity> test = new HashSet<NodeEntity>();
		test.add(a);
		System.out.println("set contains a already in " + test.contains(a));
		System.out.println("set contains b which equals a " + test.contains(b));
	}

	@SuppressWarnings("unused")
	private void testEquals() {
		System.out.println("test equals");
		Tile tile1 = new Tile(0);
		System.out.println("tile1 returned");
		tile1.testDumpData();
		Tile tile2 = new Tile(1);
		System.out.println("tile2 returned");
		tile2.testDumpData();
		System.out.println("sizes " + tile1.getSize() + " " + tile2.getSize());
		int overlap = 0;
		for (NodeEntity ne1 : tile1.getData()) {
			for (NodeEntity ne2 : tile2.getData()) {
				if (ne1.equals(ne2)) {
					overlap++;
					System.out.println("EQUALITY");
					System.out.println(ne1 + "\nEQUALS\n" + ne2);
				}
			}
		}
		System.out.println("overlap " + overlap);
	}

	@SuppressWarnings("unused")
	private void veryBasicTest() {
		List<NmbShotsEntity> shots = SessionAdapter.getInstance().loadNmbShotsEntities();
		System.out.println("loadShotsEntity");
		System.err.println("shots.size() " + shots.size());
		for (NmbShotsEntity shot : shots)
			System.out.println(shot.getNmb());
		List<NodeEntity> nodes = SessionAdapter.getInstance().loadNodeEntities();
		System.err.println("all nodes.size() " + nodes.size());
		List<NodeEntity> nodes1 = SessionAdapter.getInstance().loadNodeEntitiesByShotId(0);
		System.err.println("nodes1.size() " + nodes1.size());
		for (NodeEntity ne : nodes1)
			System.out.println(ne);
	}

}
