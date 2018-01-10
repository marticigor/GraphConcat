package test_mocks;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import building_blocks.Tile;
import entity.NodeEntity;

public class TestTile {

	List<NodeEntity> culprits;

	public TestTile() {
		culprits = new LinkedList<NodeEntity>();
	}

	// must return true
	public boolean testTile(Tile tile) {
		return testZeroAdj(tile) && testMutualVisibility(tile);
	}

	public boolean mapAdapter(Map<NodeEntity, NodeEntity> retrievableDataSet) {
		List<NodeEntity> listedDataSet = new LinkedList<NodeEntity>();
		for (NodeEntity n : retrievableDataSet.keySet()) {
			listedDataSet.add(n);
		}
		Tile testTile = new Tile(1, listedDataSet, false);
		return testTile(testTile);
	}

	private boolean testZeroAdj(Tile tile) {
		System.out.println("TEST TILE, testing: " + tile.toString());
		for (NodeEntity n : tile.getData()) {
			if (n.getAdjacents() == null || n.getAdjacents().size() == 0) {
				System.err.println("testZeroAdj: zero or null adj:\n" + n);
				return false;
			}
		}
		System.out.println("testZeroAdj: OK (returning true)");
		return true;
	}

	private boolean testMutualVisibility(Tile tile) {
		Set<NodeEntity> adj;
		Set<NodeEntity> adjDeep;
		int errors = 0;
		for (NodeEntity n : tile.getData()) {
			adj = n.getAdjacents();
			for (NodeEntity adjNode : adj) {
				adjDeep = adjNode.getAdjacents();
				if (adjDeep.contains(n) == false){
					errors++;
					culprits.add(n);
				}
			}

		}
		System.out.println("testMutualVisibility: errors: " + errors);
		if (errors == 0) {
			return true;
		} else
			return false;
	}

	public List<NodeEntity> getCulprits() {
		return culprits;
	}

}
