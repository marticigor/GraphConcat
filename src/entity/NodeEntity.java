package entity;

import java.util.Objects;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name=DB_names.TABLE_NODES)
public class NodeEntity {

    // finally I will want this graph format
    // https://www.dropbox.com/s/8et183ufeskkibi/IMG_20171019_194557.jpg?dl=0
	
	// https://stackoverflow.com/questions/21069687/hibernate-auto-create-database
	// https://stackoverflow.com/questions/43716068/invalid-syntax-error-type-myisam-in-ddl-generated-by-hibernate/43720565
	
    @Id
    // Indicates that the persistence provider
    // must assign primary keys for the entity using a database identity column.
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "shotId")
	private long shotId;
    @Column(name = "lon")
	private double lon;
    @Column(name = "lat")
	private double lat;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name=DB_names.TABLE_ADJACENTS)
	private Set<NodeEntity> adjacents;
	
    //INVARIANT, see hashCode();
    private static final transient double EPSILON = 0.00001;
    public static final transient double MULTIPLICATIVE = 10000.0;
	
	public NodeEntity (){}
	
	public NodeEntity(long shotId, double lon, double lat, Set <NodeEntity> adjacents ){

		this.shotId = shotId;
		this.lon = lon;
		this.lat = lat;
		this.adjacents = adjacents;
	}
	
	public void addToAdj(NodeEntity adj){
		adjacents.add(adj);
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getShotId() {
		return shotId;
	}
	public void setShotId(long shotId) {
		this.shotId = shotId;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double l){
		this.lon = l;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double l){
		this.lat = l;
	}
	public Set<NodeEntity> getAdjacents() {
		return adjacents;
	}
    public void setAdjacents(Set<NodeEntity> adj){
    	this.adjacents = adj;
    }

	@Override
	public int hashCode(){
		//System.out.println("hash call");
		double lonFloored = Math.floor(lon * MULTIPLICATIVE);
		double latFloored = Math.floor(lat * MULTIPLICATIVE);
		return Objects.hash(lonFloored, latFloored);
	}
	//OBJECT!!!
	@Override
	public boolean equals(Object theOther){
		//System.out.println("equals call");
	    // self check
	    if (this == theOther)
	        return true;
	    // null check
	    if (theOther == null)
	    	throw new RuntimeException("equalsMess 1");
	        //return false;
	    // type check
	    if (getClass() != theOther.getClass())
	    	throw new RuntimeException("equalsMess 2");
	        //return false;
	    
	    NodeEntity theOtherNe = (NodeEntity) theOther;
	    return equalsLonLat(theOtherNe);
	}
	
    /**
     * 
     * @param theOther
     * @return
     */
    public boolean equalsLonLat(NodeEntity theOther){
    	boolean lonB = (Math.abs(this.lon - theOther.getLon()) < EPSILON);
    	boolean latB = (Math.abs(this.lat - theOther.getLat()) < EPSILON);
    	return lonB && latB;
    }
	
	@Override
	public String toString(){
		String value = "|id " + id + " |shotId " + shotId +  " |lon " + lon + " |lat " + lat + "\n";
		value += "hashCode: " + hashCode() + "\n";
		value += "adjacents:\n"+ adjacents.size() + "\n";
	    for(NodeEntity n : adjacents){
	    	if(n == this){
	    		System.err.println("reference to this in adjacents in NodeEntity.toString()");
	    		continue;
	    	}
	    	value += ("---------- some NodeEntity - stackOverflowError we do not want here\n");
	    }
		return value;
	}
}
