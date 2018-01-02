package core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
//more data
//http://www.dis.uniroma1.it/challenge9/download.shtml

// graph format
// https://www.dropbox.com/s/cpaidvxzisyic4d/2017-12-30%2021.54.47.jpg?dl=0

public class App {

	// elev bounds
	private static final short UPPER_BOUND = 9999;
	private static final short LOWER_BOUND = -333;

	// output /smallTest
	private final static String PATH = "/home/radim/stravaGHMdata/decent/SanFranciscoBaySouth14cycling/smallTest";
	private final static String NAME = "test1";

	public final static boolean development = false;

	private double minLon = 1000.0, maxLon = -1000.0, minLat = 1000.0, maxLat = -1000.0;
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

	//
	//
	public short maxElev = Short.MIN_VALUE;
	public short minElev = Short.MAX_VALUE;
	public short elevAvg = 0;

	private short elev;
	private long elevSum = 0;
	private int voidCounter = 0;

	//
	//
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

		List<NodeEntity> listedDataSet = new ArrayList<NodeEntity>(graph.getRetrievableDataSet().keySet());

		Collections.sort(listedDataSet); // by id

		long renumberedId = 1;
		for (NodeEntity ne : listedDataSet) {
			ne.setId(renumberedId);
			ne.setRenumbered(true);
			renumberedId++;
		}

		// DATASET CONSISTENCY CHECK
		int containsProblemListed = 0;
		int containsProblemAdj = 0;
		int notRenumberedListed = 0;
		int notRenumberedAdj = 0;
		Map<NodeEntity, NodeEntity> notRenumberedCulpritsParentToChild = new HashMap<NodeEntity, NodeEntity>();
		for (NodeEntity ne : graph.getRetrievableDataSet().keySet()) {
			if (!graph.getRetrievableDataSet().keySet().contains(ne)) {
				containsProblemListed++;
			}
			if (ne.isRenumbered() == false) {
				notRenumberedListed++;
			}
			for (NodeEntity neAdj : ne.getAdjacents()) {
				if (!graph.getRetrievableDataSet().keySet().contains(neAdj)) {
					containsProblemAdj++;
				}
				if (neAdj.isRenumbered() == false) {
					notRenumberedAdj++;
					notRenumberedCulpritsParentToChild.put(ne, neAdj);
				}
			}
		}
		
		graph.computeMergedEdgeSize();
		graph.printStats();

		printCheckDatasetConsistency(containsProblemListed, containsProblemAdj, notRenumberedListed, notRenumberedAdj);

		if (development) {
			computeBoundsOfExistingNodes(graph);
			visualTest(graph, 1000, null);
			graph.testOverlap();
			graph.testCompareLeftRightPlay();
		}

		// add elev
		System.out.println("WORKING ON ELEV");
		DEMReader reader = new DEMReader();
		Map<NodeEntity, DEMTile> nodeToDEMTile = new HashMap<NodeEntity, DEMTile>();
		Map<String, DEMTile> nameToDEMTile = new HashMap<String, DEMTile>();

		for (NodeEntity node : listedDataSet) {
			String neededTile = reader.findNameOfTile(node.getLon(), node.getLat());

			if (nameToDEMTile.keySet().contains(neededTile)) {
				nodeToDEMTile.put(node, nameToDEMTile.get(neededTile));
			} else {
				nameToDEMTile.put(neededTile, new DEMTile(neededTile, reader));
				nodeToDEMTile.put(node, nameToDEMTile.get(neededTile));
			}
		}

		System.out.println("Tiles needed:");
		for (String tile : nameToDEMTile.keySet())
			System.out.println(tile);

		System.out.println("Reading elevs");

		for (NodeEntity node : listedDataSet) {
			DEMTile tile = nodeToDEMTile.get(node);

			elev = tile.getElev(node.getLat(), node.getLon());
			if (!isWithinBounds(elev)) {
				System.err.println("ELEV CORRECTION NEEDED on:\n" + node.hashCode());
				node.setNeedsElevCorr(true);
				voidCounter++;
			}

			// raw elev set for all nodes, filtering is much better done in
			// client app
			node.setElev(elev);

			if (elev > maxElev)
				maxElev = elev;
			if (elev < minElev)
				minElev = elev;
			elevSum += (long) elev;
		}

		elevAvg = (short) (elevSum / (long) (listedDataSet.size()));
		System.out.println("Max elev: " + maxElev);
		System.out.println("Min elev: " + minElev);
		System.out.println("Elev avg: " + elevAvg);
		System.err.println("Voids: " + voidCounter);

		computeBoundsOfExistingNodes(graph);
		visualTest(graph, maxElev, notRenumberedCulpritsParentToChild);

		System.out.println("\n\nWriting: " + PATH + File.separator + NAME + WriteOutputFile.EXTENSION);
		WriteOutputFile wof = new WriteOutputFile(PATH, NAME, listedDataSet, graph, this);
		try {
			wof.write();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("write");
		}
		System.out.println("FINISHED");
	}// compose

	/**
	 * @param rawElev
	 * @return
	 */
	private boolean isWithinBounds(short elev) {
		if (elev > UPPER_BOUND || elev < LOWER_BOUND)
			return false;
		else
			return true;
	}

	/**
	 * these are bounds for visualisation, not map bounds
	 * 
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
		printBounds();
	}

	/**
	 * culprits allowed null
	 */
	private void visualTest(Graph graph, int maxAlt, Map<NodeEntity, NodeEntity> culprits) {
		ImageResource ir = new ImageResource(PIC_WIDTH_MAX_INDEX + 1, PIC_HEIGHT_MAX_INDEX + 1);
		Pixel currentParent, currentCulprit;
		Map<NodeEntity, NodeEntity> dataSet = graph.getRetrievableDataSet();
		NodeEntity culprit;
		if (culprits != null) {
			for (NodeEntity parent : culprits.keySet()) {
				culprit = culprits.get(parent);
				currentParent = ir.getPixel(convertLonToPixX(parent.getLon()), convertLatToPixY(parent.getLat()));
				currentParent.setRed(0);
				currentParent.setGreen(255);
				currentParent.setBlue(0);
				currentCulprit = ir.getPixel(convertLonToPixX(culprit.getLon()), convertLatToPixY(culprit.getLat()));
				currentCulprit.setRed(255);
				currentCulprit.setGreen(0);
				currentCulprit.setBlue(255);
			}
		}

		ir.draw();

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			throw new RuntimeException("interupt1");
		}

		for (NodeEntity ne : dataSet.keySet()) {
			currentParent = ir.getPixel(convertLonToPixX(ne.getLon()), convertLatToPixY(ne.getLat()));
			currentParent.setRed(255);
			currentParent.setGreen(255);
			currentParent.setBlue(255);
		}

		ir.draw();

		try {
			Thread.sleep(8000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException("interupt2");
		}

		LineMaker lm = new LineMaker(ir);

		int x1, x2, y1, y2;
		short elev1, elev2;
		Map<NodeEntity, NodeEntity> dataSet1 = graph.getRetrievableDataSet();
		for (NodeEntity ne : dataSet1.keySet()) {
			x1 = convertLonToPixX(ne.getLon());
			y1 = convertLatToPixY(ne.getLat());
			elev1 = ne.getElev();
			int colorValue;
			for (NodeEntity adj : ne.getAdjacents()) {
				x2 = convertLonToPixX(adj.getLon());
				y2 = convertLatToPixY(adj.getLat());
				elev2 = adj.getElev();
				colorValue = interpolateColorToMaxAlt((int) ((elev1 + elev2) / 2), (int) maxAlt);
				lm.drawLine(x1, y1, x2, y2, colorValue, 255 - colorValue, 0);
			}
		}

		ir.draw();
	}

	/**
	 * @param alt
	 * @return
	 */
	private int interpolateColorToMaxAlt(int alt, int maxAlt) {
		assert (alt <= maxAlt);
		double step = (double) maxAlt / 255.0;
		int value = (int) ((double) alt / step);
		return value;
	}

	/**
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

	private void printBounds() {
		System.out.println(" --------------------- BOUNDS:");
		System.out.println("minLat " + minLat);
		System.out.println("maxLat " + maxLat);
		System.out.println("minLon " + minLon);
		System.out.println("maxLon " + maxLon);
		System.out.println("deltaLat " + deltaLat);
		System.out.println("deltaLon " + deltaLon);
	}

	private void printCheckDatasetConsistency(int listed, int adj, int notRenumberedL, int notRenumberedAdj) {
		System.out.println("=========================================================================");
		System.out.println("PRINT CHECK Dataset Consistency");
		System.out.println("listedContainsProblem " + listed);
		System.out.println("adjacentsContainsProblem " + adj);
		System.out.println("notRenumberedProblemInListed " + notRenumberedL);
		System.out.println("notRenumberedProblemInAdj " + notRenumberedAdj);
	}

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
