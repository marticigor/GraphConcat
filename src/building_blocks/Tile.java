package building_blocks;

import java.util.List;

import entity.NodeEntity;
import session.SessionAdapter;

// graph format
// https://www.dropbox.com/s/cpaidvxzisyic4d/2017-12-30%2021.54.47.jpg?dl=0

public class Tile {

	private List <NodeEntity> data;
	private int shotId;
	
	public Tile (int shotId){
		data = SessionAdapter.getInstance().loadNodeEntitiesByShotId(shotId);
		this.shotId = shotId;
	}
	
	public List <NodeEntity> getData() {
		return data;
	}
	
	public int getSize(){
		return data.size();
	}
	
	@Override
	public String toString(){
		return "tile by shotId: " + shotId + " |size: " + data.size(); 
	}
	public void testDumpData(){
		for (NodeEntity ne : data) System.out.println(ne);
	}
}
