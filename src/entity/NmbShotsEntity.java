package entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name=DB_names.TABLE_SHOTS)
public class NmbShotsEntity {

	public NmbShotsEntity(){}
	public NmbShotsEntity(int nmb){ this.nmb = nmb; }
	
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;
	
	@Column(name = "nmb")
	private int nmb;
	
	public int getNmb() {
		return nmb;
	}	
}
