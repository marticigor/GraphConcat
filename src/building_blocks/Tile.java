package building_blocks;

import java.util.List;

import entity.NodeEntity;
import session.SessionAdapter;

// graph format
// https://www.dropbox.com/s/cpaidvxzisyic4d/2017-12-30%2021.54.47.jpg?dl=0

public class Tile {

	private List<NodeEntity> data;
	private int shotId;

	public Tile(int shotId) {
		data = SessionAdapter.getInstance().loadNodeEntitiesByShotId(shotId);
		this.shotId = shotId;
	}
	
	// tests
	private static long idCount = 1;
	public Tile(int mockShotId, List<NodeEntity> data, boolean verbose) {
		this.data = data;
		this.shotId = mockShotId;
		
		for (NodeEntity ne : data) {
			ne.setShotId((long) mockShotId);
			ne.setId(idCount);
			idCount++;
		}
		if (verbose)
			testDumpData();
	}

	public List<NodeEntity> getData() {
		return data;
	}

	public int getSize() {
		return data.size();
	}

	public int getShotId() {
		return shotId;
	}

	@Override
	public String toString() {
		return "tile by shotId: " + shotId + " |size: " + data.size();
	}

	public void testDumpData() {
		System.out.println("\n\n==========================================");
		System.out.println("==========================================");
		System.out.println("==========================================");
		System.out.println("TEST DUMP DATA TILE SHOT ID: " + shotId);
		for (NodeEntity ne : data)
			System.out.println(ne);
		System.out.println("==========================================");
		System.out.println("==========================================");
		System.out.println("==========================================");
	}
}
