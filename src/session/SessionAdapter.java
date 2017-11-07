package session;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import entity.NmbShotsEntity;
import entity.NodeEntity;

public class SessionAdapter {

	private SessionFactory sf = null;
	private Session session = null;
	
	private static SessionAdapter myInstance = new SessionAdapter();
	private SessionAdapter(){
		
        Configuration cf = new Configuration().configure("hibernate.cfg.xml");
        cf.addProperties(getHibernateProperties());
        cf.addAnnotatedClass(entity.NodeEntity.class);
        cf.addAnnotatedClass(entity.NmbShotsEntity.class);

        StandardServiceRegistryBuilder srb = new StandardServiceRegistryBuilder();
        srb.applySettings(cf.getProperties());
        ServiceRegistry sr = srb.build();
        sf = cf.buildSessionFactory(sr);
		
	};
	public static SessionAdapter getInstance(){
		return myInstance;
	}
	
	//TODO ugly
    private Properties getHibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.id.new_generator_mappings","false");
        return properties;
    }
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ useful methods finally
    
    /**
     * 
     * @return
     */
	public List<NmbShotsEntity> loadNmbShotsEntities(){
		List <NmbShotsEntity> data = load(NmbShotsEntity.class);
		return data;
    }
	
	/**
	 * 
	 * @return
	 */
	public List<NodeEntity> loadNodeEntities(){
		List <NodeEntity> data = load(NodeEntity.class);
		return data;
	}
	
	/**
	 * 
	 * @param cl
	 * @return
	 */
	private <T> List<T> load ( Class <T> cl){
		Transaction tx = null;
		List<T> data = new ArrayList<T>();
		try {
	    	session = sf.openSession();
			tx = session.beginTransaction();
			
			//deprecated now
			//Criteria criteria=session.createCriteria(NmbShotsEntity.class);   
			//shots = criteria.list(); 
			
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = builder.createQuery(cl);
            criteriaQuery.from(cl);
            data = session.createQuery(criteriaQuery).getResultList();
            
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return data;
	}
}
