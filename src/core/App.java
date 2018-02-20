package core;

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
import building_blocks.WriteOutputFiles;
import building_blocks.clustering.Clustering;
import entity.DB_names;
import entity.NmbShotsEntity;
import entity.NodeEntity;
import lib_duke.ImageResource;
import lib_duke.LineMaker;
import lib_duke.Pixel;
import session.SessionAdapter;
import test_mocks.MockTiles;
import test_mocks.TileTester;
//more data
//http://www.dis.uniroma1.it/challenge9/download.shtml

//metadata file format:
//  528|174|305|49.8890337397|50.1461788264|14.2016829361|14.7505701889
//  222203|716776


public class App {

	// elev bounds
	private static final short UPPER_BOUND = 9999;
	private static final short LOWER_BOUND = -333;
	public static final short MOCK_ELEV = 333;

	// output
	//===========================================================================
	private final static String PATH = "/home/radim/shutter_shots/okoliPrahy";
	private final static String NAME = "jih_od_prahy";
	public static final int TYPE = WriteOutputFiles.VALUE_RTE_TYPE_CYCLE;
	public static final String DESRIPTION = "testing basic cycling routing where I live";
	//===========================================================================
	
	public final static boolean DEVELOPMENT = false;
	public final static boolean VERBOSE = false;
	public final static boolean MOCKS = false;

	public double minLon = 1000.0, maxLon = -1000.0, minLat = 1000.0, maxLat = -1000.0;
	private double deltaLat, deltaLon;
	public static final int PIC_WIDTH_MAX_INDEX = 1279;//1280/720 resolution
	public static final int PIC_HEIGHT_MAX_INDEX = 719;
	
	public static void main(String args[]) {
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
	Graph graph = new Graph(this);

	private void compose() {

		System.out.println("Working with " + DB_names.NAME);
		List<NmbShotsEntity> shots = SessionAdapter.getInstance().loadNmbShotsEntities();
		System.out.println("Loaded ShotsEntity");
		int nmbOfShots = shots.get(shots.size() - 1).getNmb();
		int maxShotId = nmbOfShots - 1;
		System.out.println("NmbOfShots " + nmbOfShots + " maxShotId " + maxShotId);

		if (MOCKS) {
			MockTiles mt = new MockTiles();
			for (Tile tile : mt.getTiles()) {
				printTileInfo(tile, tile.getShotId());
				// if smth goes wrong, throw exception, no attempt to fix
				performTestsOnTile("mocks", tile);
				graph.buildIn(tile);
			}

		} else {
			// iterate regular tiles
			for (int shot = 0; shot <= maxShotId; shot++) {
				Tile tile = new Tile(shot);
				printTileInfo(tile, shot);
				// if smth goes wrong, throw exception, no attempt to fix
				performTestsOnTile("regular tile", tile);
				graph.buildIn(tile);
			}
		}

		fixDataset("<after build in loop >");
		graph.rebuildDataSet();
		fixDataset("< after rebuild DataSet completed >");
		graph.computeEdgeSizeAfterMerge();
		graph.prune();
		graph.computeEdgeSizeAfterPrune();
		fixDataset("< after prune DataSet completed >");
		
		//perform aligned nodes not necessary transform n-times
		//int n = 20;
		//for(int i = 0; i < n; i++) {
			//graph.cutUnnecesarryAlignedNodes();
			//graph.resetCutAvailability();
		//}
		//fixDataset("< after cut DataSet completed >");
		
		graph.computeEdgeSizeAfterCut();
		
		computeBoundsOfExistingNodes(graph);
		
		List<NodeEntity> listedDataSet = new ArrayList<NodeEntity>(graph.getRetrievableDataSet().keySet());
		
		compareListToSet(listedDataSet, graph.getRetrievableDataSet().keySet());

		Collections.sort(listedDataSet); // by id

		long renumberedId = 0;
		for (NodeEntity ne : listedDataSet) {
			ne.setId(renumberedId);
			ne.setRenumbered(true);
			renumberedId++;
		}

		graph.printStats();

		// now clustering

		//Clustering clustering = new Clustering(graph, this);
		//long clusteringStart = System.currentTimeMillis();
		//clustering.doInit();
		//clustering.clusterize();
		//long clusteringFinish = System.currentTimeMillis();
		//System.out.println("\n\nClustering time: " + clusteringFinish +
		//clusteringStart);

		if (DEVELOPMENT) {
			computeBoundsOfExistingNodes(graph);
			visualTest(graph, 1000, null);
			graph.testOverlap();
			graph.testCompareLeftRightPlay();
		}

		if (!MOCKS) {
			// add elev to regular nodes
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
				if(node == null){
					System.err.println("App: NULL in listedDataSet");
				}
				DEMTile tile = nodeToDEMTile.get(node);
				try {
					elev = tile.getElev(node.getLat(), node.getLon());
					if (!isWithinBounds(elev)) {
						System.err.println("ELEV CORRECTION NEEDED on:\n" + node.hashCode());
						node.setNeedsElevCorr(true);
						voidCounter++;
					}
				} catch (Exception e) {
					e.printStackTrace();
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
		} // if( ! mocks)

		if (MOCKS) {
			maxElev = minElev = elevAvg = MOCK_ELEV;
		}
		
		visualTest(graph, maxElev, null);
		
		WriteOutputFiles wof = new WriteOutputFiles(PATH, NAME, listedDataSet, graph, this);
		
		try {
			wof.write();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("write");
		}
		System.out.println("FINISHED");
	}// compose

	private void fixDataset(String stageOfAlgo) {
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("fixDataset, stage: " + stageOfAlgo);
		TileTester tester = new TileTester();
		boolean testZero = tester.mapAdapterTestZeroAdj(graph.getRetrievableDataSet());
		if (testZero == false) {
			int culpritsSize = tester.getCulprits().size();
			assert (culpritsSize > 0);
			for (NodeEntity culprit : tester.getCulprits()) {
				graph.removeNodeEntity(culprit);
			}
			System.out.println("zeroAdj nodes removed: " + culpritsSize);
		}

		tester.resetCulprits();

		boolean testMutualVis = tester.mapAdapterTestMutualVisibility(graph.getRetrievableDataSet());
		if (testMutualVis == false) {
			int culpritsSize = tester.getCulprits().size();
			assert (culpritsSize > 0);
			int newEdges  = graph.fixMutualVisibility();
			System.out.println("newEdges by fixMutualVisibility: " + newEdges);
		}
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");

	}

	private void performTestsOnTile(String stageOfAlgo, Tile tile) {
		TileTester tt = new TileTester();
		boolean testsZero = tt.testZeroAdj(tile);
		boolean testsMutualVis = tt.testMutualVisibility(tile);
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("performTestsOnTile, stage: " + stageOfAlgo);
		System.out.println("testsZero: " + testsZero);
		System.out.println("testsMutualVis: " + testsMutualVis);
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
		if ((testsZero == true && testsMutualVis == true) == false)
			throw new RuntimeException("performTestsOnTile");
	}
	
	private void compareListToSet(List<NodeEntity> list, Set<NodeEntity> set){
		for(NodeEntity ne : list)
			if(set.contains(ne) == false){
				System.err.println("/n/n/ncompareListToSet: from list :/n" + ne + "/n/nset does not contain it");
			}
	}
	
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
		// TODO All around world
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
			Thread.sleep(10000);
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

	@SuppressWarnings("unused")
	private void visualizeListCulprits(List<NodeEntity> culprits) {
		ImageResource image = new ImageResource(PIC_WIDTH_MAX_INDEX + 1, PIC_HEIGHT_MAX_INDEX + 1);
		for (NodeEntity n : culprits) {
			Pixel p = image.getPixel(convertLonToPixX(n.getLon()), convertLatToPixY(n.getLat()));
			p.setRed(255);
		}
		image.draw();
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
		if (!(lat >= minLat && lat <= maxLat)) {
			System.out.println("my lat: " + lat);
			printBounds();
			throw new RuntimeException();
		}
		assert (lat >= minLat && lat <= maxLat);
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
		if (!(lon >= minLon && lon <= maxLon)) {
			System.out.println("my lon: " + lon);
			printBounds();
			throw new RuntimeException();
		}
		assert (lon >= minLon && lon <= maxLon);
		double overlapLon = lon - minLon;
		double ratio = overlapLon / deltaLon;
		int ret = ((int) (ratio * (double) PIC_WIDTH_MAX_INDEX));
		assert (ret <= PIC_WIDTH_MAX_INDEX);
		return ret;
	}

	private void printBounds() {
		System.out.println("\n\n================================================================== BOUNDS");
		System.out.println("minLat " + minLat);
		System.out.println("maxLat " + maxLat);
		System.out.println("minLon " + minLon);
		System.out.println("maxLon " + maxLon);
		System.out.println("deltaLat " + deltaLat);
		System.out.println("deltaLon " + deltaLon);
		System.out.println("=========================================================================\n");
	}

	private void printTileInfo(Tile tile, int shot) {
		System.out.println("\n\n=========================================================================");
		System.out.println("SHOT " + shot);
		System.out.println("TILE " + tile.toString());
		if (VERBOSE)
			tile.testDumpData();
		System.out.println("=========================================================================\n");
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
