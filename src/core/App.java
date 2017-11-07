package core;

import java.util.List;

import entity.NmbShotsEntity;
import session.SessionAdapter;

public class App {

	public static void main (String args[]){
		System.out.println(" --------------------- test");
		App app = new App();
		app.test();

	}
	
	private void test(){
		List <NmbShotsEntity> shots = SessionAdapter.getInstance().loadShotsEntity();
		System.out.println("loadShotsEntity");
		System.out.println("shots.size() " + shots.size());
		for (NmbShotsEntity shot : shots) System.out.println(shot.getNmb());
	}
	
}
