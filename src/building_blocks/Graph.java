package building_blocks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import entity.NodeEntity;

public class Graph {
    private Set <NodeEntity> dataSet = new HashSet<NodeEntity>();
    private int rawSize;
    
    /**
     * 
     * @param tile
     */
	public void buildIn(Tile tile){
		
		List<NodeEntity> tileData = tile.getData();
		
		//long count = 0;
		for (NodeEntity ne : tileData){
			rawSize ++;
			if (!dataSet.contains(ne)){
				dataSet.add(ne);
				//System.out.print("|put");
			} else {
				//System.out.print("|in already");
			}
		//count ++;
		//if(count % 10 == 0) System.out.println("\n");
		}
		//System.out.print("\n");
	}

	/**
	 * @return the dataSet
	 */
	public Set <NodeEntity> getDataSet() {
		return dataSet;
	}
	
	/**
	 * @return original number of nodes read from DB
	 * if we call after all work done.
	 */
	public int getRawSize(){return rawSize;}
	
	/**
	 * @return final number of nodes
	 * if we call after all work done.
	 */
	public int getMergedSize(){return dataSet.size();}
}
