package core;

import java.util.List;

import entity.NmbShotsEntity;
import entity.NodeEntity;
import session.SessionAdapter;

public class App {

	public static void main (String args[]){
		System.out.println(" --------------------- test");
		App app = new App();
		app.test();

	}
	
	private void test(){
		List <NmbShotsEntity> shots = SessionAdapter.getInstance().loadNmbShotsEntities();
		System.out.println("loadShotsEntity");
		System.err.println("shots.size() " + shots.size());
		for (NmbShotsEntity shot : shots) System.out.println(shot.getNmb());
		List <NodeEntity> nodes = SessionAdapter.getInstance().loadNodeEntities();
		System.err.println("nodes.size() " + nodes.size());
		//for (NodeEntity ne : nodes) System.out.println(ne);
	}
	
}
