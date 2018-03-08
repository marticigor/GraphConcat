package test_mocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import building_blocks.Tile;
import entity.NodeEntity;

public class MockTiles {
	
	public MockTiles(){
		build();
	}
	
	/*
	public NodeEntity(long shotId, double lon, double lat, short weight, Set<NodeEntity> adjacents) {
		this.weight = weight;
		this.shotId = shotId;
		this.lon = lon;
		this.lat = lat;
		this.adjacents = adjacents;
	}
	*/
	
	//case1 ok
	//NodeEntity n1 = new NodeEntity(-1l, 4d, 1d, (short)0, new HashSet<NodeEntity>());
	//NodeEntity n2 = new NodeEntity(-1l, 6d, 1d, (short)0, new HashSet<NodeEntity>());
	//NodeEntity n3 = new NodeEntity(-1l, 7d, 1d, (short)0, new HashSet<NodeEntity>());
	
	//NodeEntity n4 = new NodeEntity(-1l, 6d, 1.00000009d, (short)0, new HashSet<NodeEntity>());
	//NodeEntity n5 = new NodeEntity(-1l, 7.00000009d, 1d, (short)0, new HashSet<NodeEntity>());
	
	//case2
	NodeEntity n1 = new NodeEntity(-1l, 6d, 8d, (short)0, new HashSet<NodeEntity>());
	NodeEntity n2 = new NodeEntity(-1l, 4d, 7d, (short)0, new HashSet<NodeEntity>());
	NodeEntity n3 = new NodeEntity(-1l, 6d, 7d, (short)0, new HashSet<NodeEntity>());
	
	NodeEntity n4 = new NodeEntity(-1l, 6d, 7.00000009d, (short)0, new HashSet<NodeEntity>());
	NodeEntity n5 = new NodeEntity(-1l, 6.00000009d, 8d, (short)0, new HashSet<NodeEntity>());
	
	Tile left,right;
	
	//https://www.dropbox.com/s/wkgk3hb49uvbdaa/2018-01-03%2009.48.19.jpg?dl=0
	private void build(){
		
		n1.addToAdj(n2);
		n1.addToAdj(n3);
		n3.addToAdj(n2);
		
		n4.addToAdj(n5);
		n5.addToAdj(n4);
		
		List<NodeEntity> forTile1left = new LinkedList<NodeEntity>();
		forTile1left.add(n1);
		forTile1left.add(n2);
		forTile1left.add(n3);
		List<NodeEntity> forTile2right = new LinkedList<NodeEntity>();
		forTile2right.add(n4);
		forTile2right.add(n5);
		
		left = new Tile(1, forTile1left, false);
		right = new Tile(2, forTile2right, false);
		
	}
	
	public List <Tile> getTiles(){
		List<Tile> tiles = new LinkedList<Tile>();
		tiles.add(left);
		tiles.add(right);
		return tiles;
	}
}
