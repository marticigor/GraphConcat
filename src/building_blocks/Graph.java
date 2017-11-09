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
		
		long count = 0;
		for (NodeEntity ne : tileData){
			if (!dataSet.contains(ne)){
				dataSet.add(ne);
				System.out.print("put ");
			} else {
				System.out.print("in ");
			}
		count ++;
		if(count % 50 == 0) System.out.println("\n");
		}
	}
}
