package building_blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.App;
import entity.NodeEntity;
import lib_duke.ImageResource;
import lib_duke.LineMaker;
import lib_duke.Pixel;

public class Graph {
	private Map<NodeEntity, NodeEntity> retrievableDataSet = new HashMap<NodeEntity, NodeEntity>();
	private Set<NodeEntity> needsMergeFromRight = new HashSet<NodeEntity>();
	private int rawSize;
	private int edgeSizeNoMerge;
	private int sizeProblem, containsProblem;
	private ImageResource visual = new ImageResource(App.PIC_WIDTH_MAX_INDEX + 1, App.PIC_HEIGHT_MAX_INDEX + 1);
	private LineMaker line = new LineMaker(visual);
	private App app = new App();
	private List<NodeEntity> playLeft = new ArrayList<NodeEntity>();
	private List<NodeEntity> playRight = new ArrayList<NodeEntity>();
	private List<NodeEntity> matchFound = new ArrayList<NodeEntity>(); 

	/**
	 * 
	 * @param tile
	 */
	public void buildIn(Tile tile) {

		List<NodeEntity> tileData = tile.getData();
		int needsMergeThisRound = 0;
		Set<NodeEntity> left, right;

		for (NodeEntity nodeEntityRight : tileData) {
			rawSize++;
			if (!retrievableDataSet.keySet().contains(nodeEntityRight)) {
				// PUT IT IN
				retrievableDataSet.put(nodeEntityRight, nodeEntityRight);
				edgeSizeNoMerge += nodeEntityRight.getAdjacents().size();

			} else {
				// IS ALREADY IN
				matchFound.add(nodeEntityRight);
				// merge adjacents if they are not identical
				right = nodeEntityRight.getAdjacents();
				left = retrievableDataSet.get(nodeEntityRight).getAdjacents();
				if (!needsMergeFromRight.contains(nodeEntityRight) && !areIdentical(left, right)) {
					needsMergeFromRight.add(nodeEntityRight);
					needsMergeThisRound ++;
				}
			}
		} // for
		printDataMergeThisRound(needsMergeThisRound);
		testDatasetIntegrity();
	}

	/**
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	private boolean areIdentical(Set<NodeEntity> left, Set<NodeEntity> right) {

		if (left.size() != right.size()) {
			sizeProblem++;
			return false;
		}
		for (NodeEntity leftNodeEntity : left) {
			if (!right.contains(leftNodeEntity)) {
				containsProblem++;
				return false;
			}
		}
		return true;
	}

	/**
	 * called regularly
	 */
	private void testDatasetIntegrity() {

		int errorCount = 0;
		Set<NodeEntity> adj;

		for (NodeEntity left : retrievableDataSet.keySet()) {
			adj = left.getAdjacents();
			for (NodeEntity right : adj) {
				if (!retrievableDataSet.keySet().contains(right))
					errorCount++;
			}
		}
		if (errorCount != 0) {
			String msg = "GRAPH INCONSISTENCY detected, error count: " + errorCount;
			System.err.println(msg);
			//throw new RuntimeException(msg);
		}
	}

	/**
	 * @return the dataSet
	 */
	public Map<NodeEntity, NodeEntity> getRetrievableDataSet() {
		return retrievableDataSet;
	}

	/**
	 * 
	 * @return
	 */
	public Set<NodeEntity> getNeedsMergeFromRight() {
		return needsMergeFromRight;
	}

	/**
	 * @return original number of nodes read from DB if we call after all work
	 *         done.
	 */
	public int getRawSize() {
		return rawSize;
	}

	/**
	 * @return final number of nodes if we call after all work done.
	 */
	public int getMergedSize() {
		return retrievableDataSet.size();
	}

	/**
	 * @return if we call after all work done.
	 */
	public int getEdgeSizeNoMerge() {
		return edgeSizeNoMerge;
	}

	/**
	 * @return the sizeProblem
	 */
	public int getSizeProblem() {
		return sizeProblem;
	}

	/**
	 * @return the containsProblem
	 */
	public int getContainsProblem() {
		return containsProblem;
	}

	/**
	 * 
	 * @param right
	 * @param left
	 */
	@SuppressWarnings("unused")
	private void printMergeState(NodeEntity right, Set<NodeEntity> left) {
		System.out.println("=========================================================================");
		System.out.println("MISMATCH EITHER SIZE OR IDENTITY");
		System.out.println("RIGHT already IN left dataset, no MERGE NOW:");
		System.out.println(right);
		System.out.println("\nCURRENT STATE of adjacents in 'entityInToItsAdjacents'");
		for (NodeEntity leftNo : left)
			System.out.println(leftNo);
		System.out.println("=========================================================================");
	}

	/**
	 * 
	 * @param left
	 * @param right
	 */
	@SuppressWarnings("unused")
	private void printCompare(NodeEntity left, NodeEntity right) {
		System.out.println("=========================================================================");
		System.out.println("FROM my DATABASE has just arrived:");
		System.out.println(right);
		System.out.println("WHAT I ALREADY HAVE in my DATASET:");
		System.out.println(left);
		System.out.println("=========================================================================");
	}

	/**
	 * 
	 */
	public void printStats(){
		System.out.println("=========================================================================");
		System.out.println("RAW number " + this.getRawSize());
		System.out.println("FINAL number " + this.getMergedSize());
		System.out.println("\nEdgeSize before merge " + this.getEdgeSizeNoMerge());
		System.out.println("=========================================================================");
	}
	
	/**
	 * 
	 * @param needsMergeThisRound
	 */
	private void printDataMergeThisRound(int needsMergeThisRound){
		System.out.println("=========================================================================");
		System.out.println("Needs merge THIS ROUND: " + needsMergeThisRound);
		System.out.println("TOTAL needs merge: " + needsMergeFromRight.size());
		System.out.println("TOTAL EDGE size no merge: " + edgeSizeNoMerge);
		System.out.println("=========================================================================");
	} 
	
	/**
	 * 
	 * @param left
	 * @param right
	 */
	@SuppressWarnings("unused")
	private void testCompareLeftRightStore(NodeEntity left, NodeEntity right) {
		playLeft.add(left);
		playRight.add(right);
	}

	/**
	 * call when graph done
	 */
	public void testCompareLeftRightPlay() {
		app.computeBounds(this);
		int size = playLeft.size();
		int [] reds = new int []{255,0};
		int [] blues = new int []{0,255};
		for (int i = 0; i < size; i++) {
			NodeEntity left = playLeft.get(i);
			NodeEntity right = playRight.get(i);
			NodeEntity[] entities = new NodeEntity[] { left, right };
			for (int j = 0; j < 2; j++) {
				int anchorX = app.convertLonToPixX(entities[j].getLon());
				int anchorY = app.convertLatToPixY(entities[j].getLat());
				for (NodeEntity adj : entities[j].getAdjacents()) {
					int adjX = app.convertLonToPixX(adj.getLon());
					int adjY = app.convertLatToPixY(adj.getLat());
					line.drawLine(anchorX, anchorY, adjX, adjY, reds[j], 0, blues[j]);
				}
				visual.draw();
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * call when graph done
	 */
	public void testOverlap(){
		app.computeBounds(this);
		for(NodeEntity entity : matchFound){
			Pixel p = visual.getPixel(
					app.convertLonToPixX(entity.getLon()),
					app.convertLatToPixY(entity.getLat()));
			p.setRed(255);
			p.setBlue(255);
		}
		for(NodeEntity entity : retrievableDataSet.keySet()){
			Pixel p = visual.getPixel(
					app.convertLonToPixX(entity.getLon()),
					app.convertLatToPixY(entity.getLat()));
			p.setGreen(200);
		}
		visual.draw();
	}
}
