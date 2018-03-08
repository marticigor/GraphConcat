package building_blocks;

public class DEMTile {

	private String name;
	private short [] data;
	
	public DEMTile(String name, DEMReader reader){
		this.name = name;
		System.out.println("Loading tile: " + name);
		data = reader.getData(name);
		//veryBasicTest();
	}

	/**
	 * TODO works globally?
	 * @param lat
	 * @param lon
	 * @return
	 */
	public short getElev(double lat, double lon){
		
		double lonDec = lon - Math.floor(lon);
		double latDec = lat - Math.floor(lat);
		
		int col = (int) (Math.round(((double) DEMReader.SIZE - 1) * lonDec));
		int rowSN = (int) (Math.round(((double) DEMReader.SIZE - 1) * latDec));
		int row = DEMReader.SIZE - 1 - rowSN;
		
		//System.out.println("row " + row + " col " + col);
		
		return data[(DEMReader.SIZE * row) + col];
	}
	
	public String toString(){
		return"TILE: " + name;
	}
	
	@SuppressWarnings("unused")
	private void veryBasicTest(){
		System.out.println("veryBasicTest: " + name);
		for(int i = 0; i < data.length; i++){
			if(i % 10000 == 0)System.out.println("a few values: " + data[i]);
		}
	}
}
