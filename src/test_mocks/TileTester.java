package test_mocks;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import building_blocks.Tile;
import entity.NodeEntity;

public class TileTester {

	List<NodeEntity> culprits;

	public TileTester() {
		culprits = new LinkedList<NodeEntity>();
	}

	public boolean mapAdapterTestZeroAdj(Map<NodeEntity, NodeEntity> retrievableDataSet) {
		List<NodeEntity> listedDataSet = new LinkedList<NodeEntity>(retrievableDataSet.keySet());
		// mockId, data, verbose
		Tile testTile = new Tile(1, listedDataSet, false);
		return testZeroAdj(testTile);
	}

	public boolean mapAdapterTestMutualVisibility(Map<NodeEntity, NodeEntity> retrievableDataSet) {
		List<NodeEntity> listedDataSet = new LinkedList<NodeEntity>(retrievableDataSet.keySet());
		// mockId, data, verbose
		Tile testTile = new Tile(1, listedDataSet, false);
		return testMutualVisibility(testTile);
	}

	public boolean testZeroAdj(Tile tile) {
		for (NodeEntity n : tile.getData()) {
			if (n.getAdjacents() == null) {
				throw new RuntimeException("testZeroAdj: NULL adj:\n" + n);
			} else if (n.getAdjacents().size() == 0) {
				System.err.println("testZeroAdj: ZERO SIZE adj:\n" + n);
				culprits.add(n);
			}
		}
		if (culprits.size() == 0) {
			return true;
		} else {
			System.err.println("testZeroAdj: NOT OK (returning false)");
			return false;
		}
	}

	public boolean testMutualVisibility(Tile tile) {
		Set<NodeEntity> adj;
		Set<NodeEntity> adjDeep;
		int errors = 0;
		for (NodeEntity n : tile.getData()) {
			adj = n.getAdjacents();
			for (NodeEntity adjNode : adj) {
				adjDeep = adjNode.getAdjacents();
				if (adjDeep.contains(n) == false) {
					errors++;
					culprits.add(n);
				}
			}

		}
		if (errors == 0) {
			return true;
		} else {
			System.out.println("testMutualVisibility: errors: " + errors);
			return false;
		}
	}

	public List<NodeEntity> getCulprits() {
		return culprits;
	}

	public void resetCulprits() {
		culprits.clear();
	}

}
