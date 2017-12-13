package core;

import java.util.HashSet;
import java.util.List;

import building_blocks.Graph;
import building_blocks.Tile;
import entity.DB_names;
import entity.NmbShotsEntity;
import entity.NodeEntity;
import session.SessionAdapter;

public class App {

	public static void main (String args[]){
		System.out.println(" --------------------- ");
		System.out.println(" --------------------- ");
		System.out.println(" --------------------- ");
		System.out.println(" --------------------- ");
		App app = new App();
		//app.veryBasicTest();
		//app.testHash();
		//app.testEquals();
		app.compose();
	}
	/**
	 * 
	 * TODO do not forget to normaize edge weights (corner case)
	 */
	private void compose(){
		System.out.println("working with " + DB_names.NAME);
		List <NmbShotsEntity> shots = SessionAdapter.getInstance().loadNmbShotsEntities();
		System.out.println("loaded ShotsEntity");
		int nmbOfShots = shots.get(shots.size() -1 ).getNmb();
		int maxShotId = nmbOfShots - 1;
		System.out.println("nmbOfShots " + nmbOfShots + " maxShotId " + maxShotId);
		Graph graph = new Graph();
		//iterate tiles
		for (int shot = 0; shot <= maxShotId; shot ++ ){
			Tile tile = new Tile(shot);
			System.out.println("-------------------------------");
			System.err.println("SHOT " + shot);
			System.err.println("TILE " + tile.toString());
			System.out.println("-------------------------------");
			//tile.testDumpData();
			graph.buildIn(tile);
		}
		System.out.println("FINISHED");
		System.out.println("raw number " + graph.getRawSize());//528361
		System.out.println("final number " + graph.getMergedSize());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@SuppressWarnings("unused")
	private void testHash(){
		//public NodeEntity(long shotId, double lon, double lat, Set <NodeEntity> adjacents )
		NodeEntity a = new NodeEntity(0,50.1234567891,14.12345678921, new HashSet<NodeEntity>());
		NodeEntity b = new NodeEntity(0,50.1234567891,14.12345678921, new HashSet<NodeEntity>());
		System.out.println(a.hashCode() + " - " + b.hashCode());
		System.out.println(a.equals(b) + " - " +b.equals(a));
		System.out.println(a == b);
		HashSet <NodeEntity> test = new HashSet<NodeEntity>();
		test.add(a);
		System.out.println("set contains a already in " + test.contains(a));
		System.out.println("set contains b which equals a " + test.contains(b));
		
	}
	
	@SuppressWarnings("unused")
	private void testEquals(){
		System.out.println("test equals");
		Tile tile1 = new Tile(0);
		System.out.println("tile1 returned");
		tile1.testDumpData();
		Tile tile2 = new Tile(1);
		System.out.println("tile2 returned");
		tile2.testDumpData();
		System.out.println("sizes " + tile1.getSize() + " " + tile2.getSize());
		int overlap = 0;
		for (NodeEntity ne1 : tile1.getData()){
			for (NodeEntity ne2 : tile2.getData()){
				if(ne1.equals(ne2)){
					overlap ++;
					System.out.println("EQUALITY");
					System.out.println(ne1 + "\nEQUALS\n" + ne2);
				}
			}
		}
		System.out.println("overlap " + overlap);
	}
	
	@SuppressWarnings("unused")
	private void veryBasicTest(){
		List <NmbShotsEntity> shots = SessionAdapter.getInstance().loadNmbShotsEntities();
		System.out.println("loadShotsEntity");
		System.err.println("shots.size() " + shots.size());
		for (NmbShotsEntity shot : shots) System.out.println(shot.getNmb());
		List <NodeEntity> nodes = SessionAdapter.getInstance().loadNodeEntities();
		System.err.println("all nodes.size() " + nodes.size());
		//for (NodeEntity ne : nodes) System.out.println(ne);
		List <NodeEntity> nodes1 = SessionAdapter.getInstance().loadNodeEntitiesByShotId(0);
		System.err.println("nodes1.size() " + nodes1.size());
		for (NodeEntity ne : nodes1) System.out.println(ne);
	}
	
}
