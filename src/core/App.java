package core;

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
		app.compose();
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
		//for (NodeEntity ne : nodes1) System.out.println(ne);
	}
	
	private void compose(){
		System.out.println("working with " + DB_names.NAME);
		List <NmbShotsEntity> shots = SessionAdapter.getInstance().loadNmbShotsEntities();
		System.out.println("loaded ShotsEntity");
		int nmbOfShots = shots.get(shots.size() -1 ).getNmb();
		int maxShotId = nmbOfShots - 1;
		System.out.println("nmbOfShots " + nmbOfShots + " maxShotId " + maxShotId);
		Graph graph = new Graph();
		//iterate tiles
		for (int index = 0; index <= maxShotId; index ++ ){
			Tile tile = new Tile(index);
			System.err.println("INDEX " + index);
			System.err.println("TILE " + tile.toString());
			graph.buildIn(tile);
		}
	}
	
}
