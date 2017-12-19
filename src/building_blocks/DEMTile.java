package building_blocks;

public class DEMTile {

	String name;
	Object data;
	
	public DEMTile(String name){
		this.name = name;
		System.out.println("Loading tile: " + name);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String toString(){
		return"TILE: " + name;
	}
	
}
