package building_blocks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import entity.NodeEntity;

public class Graph {
    private Set <NodeEntity> dataSet = new HashSet<NodeEntity>();
    
    /**
     * 
     * @param tile
     */
	public void buildIn(Tile tile){
		List<NodeEntity> tileData = tile.getData();
		
		for (Object o : tileData){
			System.out.println(o.getClass());
			System.out.println(o.toString());
		}
		
		for (NodeEntity ne : tileData){
			if (!dataSet.contains(ne)){
				dataSet.add(ne);
				System.out.println(" - not in");
			} else {
				System.out.println(" ----------- already in");
			}
		}
	}
}
