package building_blocks.clustering;

import entity.NodeEntity;

public class Point {
	public final double lon;
	public final double lat;
	public IdWrapper wrapper;
	public NodeEntity n;

	public Point(IdWrapper wrapper, NodeEntity n) {
		this.lat = n.getLat();
		this.lon = n.getLon();
		this.wrapper = wrapper;
		this.n = n;
	}

	void setWrapper(IdWrapper w) {
		this.wrapper = w;
	}
	void setNodeEntityIsCentroid(){
		n.isCentroid = true;
	}

	public String toString() {
		return "< lon: " + lon + ", lat: " + lat + " >" + " wrapper id: " + wrapper.idRepresentative + "NodeEntity "
				+ n.toString();
	}
	
	public String singleLineToString(){
		return " point: " + n.getLat()+ " | " + n.getLon() + " | id: " + wrapper.idRepresentative;
	}
}
