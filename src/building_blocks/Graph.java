package building_blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import entity.NodeEntity;

public class Graph {
    private Set <NodeEntity> dataSet = new HashSet<NodeEntity>();
    private Map <NodeEntity, Short> entityInToWeight = new HashMap<NodeEntity, Short>(); 
    private int rawSize;
    private int edgeSize;
    
    /**
     * 
     * @param tile
     */
	public void buildIn(Tile tile){
		
		List<NodeEntity> tileData = tile.getData();
		
		//long count = 0;
		short maxWeight, weightIn, weightOut;
		
		for (NodeEntity ne : tileData){
			rawSize ++;
			if (!dataSet.contains(ne)){
				//PUT IT IN
				
				//System.out.print("|put");
				dataSet.add(ne);
				entityInToWeight.put(ne, ne.getWeight());
				
			} else {
				//IS ALREADY IN
				
				//System.out.print("|in already");
				
				//compare weights(this is necessary as some nodes may be corner cases,
				//diferent pans differs in weight)
				
				//NodeEntity overwrites hashCode() and equals();
				weightIn = entityInToWeight.get(ne);
				weightOut = ne.getWeight();
				maxWeight = (short) Math.max((int) weightIn, (int) weightOut);
				if(maxWeight > weightIn){
					//System.out.println("OVERWRITING corner case");
					//System.out.println("in before: " + entityInToWeight.get(ne));
					entityInToWeight.put(ne, maxWeight);
					//System.out.println("in after: " + entityInToWeight.get(ne));
				}
			}
		//count ++;
		//if(count % 10 == 0) System.out.println("\n");
			edgeSize += ne.getAdjacents().size();
		}//for
		//System.out.print("\n");
		
		//iterate dataSet and correct weights affected by corner cases
		System.out.println("correcting weights affected by corner cases");
		short before,fromMap;
		for(NodeEntity ne : dataSet){
			before = ne.getWeight();
			fromMap = entityInToWeight.get(ne);
			if(fromMap > before){
				ne.setWeight(fromMap);
				//System.out.println("updating NodeEntity weight");
			}
		}
		testDatasetIntegrity();
	}

	/**
	 * 
	 */
	private void testDatasetIntegrity(){
		
		int errorCount = 0;
		Set<NodeEntity> adj;
		
		for(NodeEntity left : dataSet){
			adj = left.getAdjacents();
			for(NodeEntity right : adj){
				if(!dataSet.contains(right))errorCount ++;
			}
		}
		
		if(errorCount != 0){
			throw new RuntimeException("inconsistency detected");
		}
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
	
	/**
	 * @return
	 * if we call after all work done.
	 */
	public int getEdgeSize(){return edgeSize;}
}
