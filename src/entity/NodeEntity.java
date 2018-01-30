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

import core.App;

@Entity
@Table(name = DB_names.TABLE_NODES)
public class NodeEntity implements Comparable<NodeEntity> {

	// graph format
	// https://www.dropbox.com/s/cpaidvxzisyic4d/2017-12-30%2021.54.47.jpg?dl=0

	// https://stackoverflow.com/questions/21069687/hibernate-auto-create-database
	// https://stackoverflow.com/questions/43716068/invalid-syntax-error-type-myisam-in-ddl-generated-by-hibernate/43720565

	@Id
	// Indicates that the persistence provider
	// must assign primary keys for the entity using a database identity column.
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "shotId")
	private long shotId;
	@Column(name = "lon")
	private double lon;
	@Column(name = "lat")
	private double lat;
	@Column(name = "weight")
	private short weight;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = DB_names.TABLE_ADJACENTS)
	private Set<NodeEntity> adjacents;

	private transient short elev = App.MOCK_ELEV;
	// Redundant. But verbose maybe better than (not so obvious) elev =
	// Short.MIN_VALUE
	// not used so far
	private transient boolean needsElevCorr = false;

	// very very important
	private static final transient int HASHCODE_MULTIPLICATION_LON_LAT = 10000;

	private transient boolean renumbered = false;

	public transient VisitedStatus visitedStatus = VisitedStatus.UNVISITED;

	private transient long idCLuster;

	public NodeEntity() {
	}

	public NodeEntity(long shotId, double lon, double lat, short weight, Set<NodeEntity> adjacents) {
		this.weight = weight;
		this.shotId = shotId;
		this.lon = lon;
		this.lat = lat;
		this.adjacents = adjacents;
	}

	public void addToAdj(NodeEntity adj) {
		adjacents.add(adj);
	}

	public void removeFromAdjacents(NodeEntity ne) {
		if (!this.adjacents.contains(ne))
			throw new RuntimeException("removeFromAdjacents: this NodeEntity does not contain NE asked to remove");
		else this.adjacents.remove(ne);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getIdCLuster() {
		return idCLuster;
	}

	public void setIdCLuster(long idRepresentative) {
		this.idCLuster = idRepresentative;
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

	public void setLon(double l) {
		this.lon = l;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double l) {
		this.lat = l;
	}

	public short getWeight() {
		return weight;
	}

	public void setWeight(short weight) {
		this.weight = weight;
	}

	public short getElev() {
		return elev;
	}

	public void setElev(short elev) {
		this.elev = elev;
	}

	public boolean needsElevCorr() {
		return needsElevCorr;
	}

	public void setNeedsElevCorr(boolean needsElevCorr) {
		this.needsElevCorr = needsElevCorr;
	}

	public Set<NodeEntity> getAdjacents() {
		return adjacents;
	}

	public void setAdjacents(Set<NodeEntity> adj) {
		this.adjacents = adj;
	}

	@Override
	public int hashCode() {
		int lonFloored = (int) (lon * HASHCODE_MULTIPLICATION_LON_LAT);
		int latFloored = (int) (lat * HASHCODE_MULTIPLICATION_LON_LAT);
		return Objects.hash(lonFloored, latFloored);
	}

	// OBJECT!!!
	@Override
	public boolean equals(Object theOther) {
		// self check
		if (this == theOther)
			return true;
		// null check
		if (theOther == null)
			throw new RuntimeException("equalsMess 1");
		// return false;
		// type check
		if (getClass() != theOther.getClass())
			throw new RuntimeException("equalsMess 2");
		// return false;

		NodeEntity theOtherNe = (NodeEntity) theOther;
		return equalsLonLat(theOtherNe);
	}

	/**
	 * 
	 * @param theOther
	 * @return
	 */
	public boolean equalsLonLat(NodeEntity theOther) {
		boolean lonB = (((int) (this.lon * HASHCODE_MULTIPLICATION_LON_LAT)) == ((int) (theOther.lon
				* HASHCODE_MULTIPLICATION_LON_LAT)));
		boolean latB = (((int) (this.lat * HASHCODE_MULTIPLICATION_LON_LAT)) == ((int) (theOther.lat
				* HASHCODE_MULTIPLICATION_LON_LAT)));
		return lonB && latB;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nN O D E    E N T I T Y\n");
		sb.append("|id ").append(id).append(" |shotId ").append(shotId).append(" |lon ").append(lon).append(" |lat ")
				.append(lat).append("\n");
		sb.append("|weight ").append(weight).append(" |elev ").append(elev);
		sb.append("\n|hashCode(): ").append(hashCode());
		sb.append("\n RENUMBERED = " + renumbered);
		sb.append("\n\tadjacents:").append(adjacents.size()).append("\n");

		for (NodeEntity n : adjacents) {
			if (n == this) {
				System.err.println("!!! reference to this in adjacents in NodeEntity.toString()");
				continue;
			}
			sb.append("\n\t" + n.getId() + "---------------------------------");
			sb.append("\n\thash: ").append(n.hashCode());
			sb.append("\n\tshot: ").append(n.getShotId());
			sb.append("\n\tlat: ").append(n.getLat());
			sb.append("\n\tlon:").append(n.getLon());
			sb.append("\n\tweight: ").append(n.getWeight());
			sb.append("\n\telev: ").append(n.getElev());
			sb.append("\n\trenumbered: ").append(n.isRenumbered());
		}
		return sb.toString();
	}

	@Override
	public int compareTo(NodeEntity theOther) {

		Long myId = new Long(this.id);
		Long hisId = new Long(theOther.getId());

		return myId.compareTo(hisId);
	}

	public boolean isRenumbered() {
		return renumbered;
	}

	public void setRenumbered(boolean renumbered) {
		this.renumbered = renumbered;
	}
}
