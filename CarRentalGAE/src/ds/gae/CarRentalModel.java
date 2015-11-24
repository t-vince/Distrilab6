package ds.gae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;
import ds.gae.entities.ReservationConstraints;
 
public class CarRentalModel {
	
	public Map<String,CarRentalCompany> CRCS = new HashMap<String, CarRentalCompany>();	
	
	private static CarRentalModel instance;
	
	public static CarRentalModel get() {
		if (instance == null)
			instance = new CarRentalModel();
		return instance;
	}
		
	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param 	crcName
	 * 			the car rental company
	 * @return	The list of car types (i.e. name of car type), available
	 * 			in the given car rental company.
	 */
	public Set<String> getCarTypesNames(String crcName) {
		EntityManager em = EMF.get().createEntityManager();
		try{
			Set<String> typeNames = new HashSet<String>();
			Query query = em.createQuery("SELECT crc.cartypes FROM CarRentalCompany crc WHERE crc.name = :name");
			query.setParameter("name", crcName);
			List<HashSet<CarType>> result = query.getResultList();
			Set<CarType> types = result.get(0);
			for (CarType type: types) {
				typeNames.add(type.getName());
			}
			return typeNames;
		} finally {
			em.close();
		}
	}

    /**
     * Get all registered car rental companies
     *
     * @return	the list of car rental companies
     */
    public Collection<String> getAllRentalCompanyNames() {
    	EntityManager em = EMF.get().createEntityManager();
		try{
			Query query = em.createQuery("SELECT crc.name FROM CarRentalCompany crc");
			return query.getResultList();
		} finally {
			em.close();
		}
    }
	
	/**
	 * Create a quote according to the given reservation constraints (tentative reservation).
	 * 
	 * @param	company
	 * 			name of the car renter company
	 * @param	renterName 
	 * 			name of the car renter 
	 * @param 	constraints
	 * 			reservation constraints for the quote
	 * @return	The newly created quote.
	 *  
	 * @throws ReservationException
	 * 			No car available that fits the given constraints.
	 */
    public Quote createQuote(String company, String renterName, ReservationConstraints constraints) throws ReservationException {
    	
    	EntityManager em = EMF.get().createEntityManager();
    	Quote out = null;
    	
		try{
			Query query = em.createQuery("SELECT c FROM CarRentalCompany c WHERE c.name = :crcName")
					.setParameter("crcName", company);
			CarRentalCompany crc = (CarRentalCompany)query.getSingleResult();
			
			if (crc != null) {
				out = crc.createQuote(constraints, renterName);
			} else {
				throw new ReservationException("CarRentalCompany not found.");    	
			}
		}
		finally{
			em.close();
		}
		return out;
    }
    
	/**
	 * Confirm the given quote.
	 *
	 * @param 	q
	 * 			Quote to confirm
	 * 
	 * @throws ReservationException
	 * 			Confirmation of given quote failed.	
	 */
	public Reservation confirmQuote(Quote q) throws ReservationException {
		EntityManager em = EMF.get().createEntityManager();		
		Reservation reservation;
		try{
			Query query = em.createQuery("SELECT c FROM CarRentalCompany c WHERE c.name = :crcName")
					.setParameter("crcName", q.getRentalCompany());
			CarRentalCompany crc = (CarRentalCompany)query.getSingleResult();
			
			reservation = crc.confirmQuote(q);
	    	em.persist(crc);
	    	return reservation;
		}
		finally {
			em.close();
		}
	}
	
    /**
	 * Confirm the given list of quotes
	 * 
	 * @param 	quotes 
	 * 			the quotes to confirm
	 * @return	The list of reservations, resulting from confirming all given quotes.
	 * 
	 * @throws 	ReservationException
	 * 			One of the quotes cannot be confirmed. 
	 * 			Therefore none of the given quotes is confirmed.
	 */
    public List<Reservation> confirmQuotes(List<Quote> quotes) throws ReservationException {    	
    	List<Reservation> reservations = new ArrayList<Reservation>();
		for(Quote quote : quotes){
			reservations.add(confirmQuote(quote));
		}
		return reservations;
    }
	
	/**
	 * Get all reservations made by the given car renter.
	 *
	 * @param 	renter
	 * 			name of the car renter
	 * @return	the list of reservations of the given car renter
	 */
	public List<Reservation> getReservations(String renter) {
		EntityManager em = EMF.get().createEntityManager();
		
		try {			
			Query q = em.createQuery("SELECT c FROM CarRentalCompany c");
			List<CarRentalCompany> companies = q.getResultList();
			
			List<Reservation> out = new ArrayList<Reservation>();
			
	    	for (CarRentalCompany crc : companies) {
	    		for (Car c : crc.getCars()) {
	    			for (Reservation r : c.getReservations()) {
	    				if (r.getCarRenter().equals(renter)) {
	    					out.add(r);
	    				}
	    			}
	    		}
	    	}
	    	
	    	return out;
		}
		finally {
			em.close();
		}
    }

    /**
     * Get the car types available in the given car rental company.
     *
     * @param 	crcName
     * 			the given car rental company
     * @return	The list of car types in the given car rental company.
     */
    public Collection<CarType> getCarTypesOfCarRentalCompany(String crcName) {
    	EntityManager em = EMF.get().createEntityManager();
    	
    	try {
    		Query query = em.createQuery("SELECT c FROM CarRentalCompany c WHERE c.name = :crcName")
					.setParameter("crcName", crcName);
			CarRentalCompany crc = (CarRentalCompany)query.getSingleResult();
			
			Collection<CarType> out = new ArrayList<CarType>(crc.getAllCarTypes());
			return out;
		}
		finally {
			em.close();
		}
    }
	
    /**
     * Get the list of cars of the given car type in the given car rental company.
     *
     * @param	crcName
	 * 			name of the car rental company
     * @param 	carType
     * 			the given car type
     * @return	A list of car IDs of cars with the given car type.
     */
    public Collection<Integer> getCarIdsByCarType(String crcName, CarType carType) {
    	Collection<Integer> out = new ArrayList<Integer>();
    	for (Car c : getCarsByCarType(crcName, carType)) {
    		out.add(c.getId());
    	}
    	return out;
    }
    
    /**
     * Get the amount of cars of the given car type in the given car rental company.
     *
     * @param	crcName
	 * 			name of the car rental company
     * @param 	carType
     * 			the given car type
     * @return	A number, representing the amount of cars of the given car type.
     */
    public int getAmountOfCarsByCarType(String crcName, CarType carType) {
    	return this.getCarsByCarType(crcName, carType).size();
    }

	/**
	 * Get the list of cars of the given car type in the given car rental company.
	 *
	 * @param	crcName
	 * 			name of the car rental company
	 * @param 	carType
	 * 			the given car type
	 * @return	List of cars of the given car type
	 */
	private List<Car> getCarsByCarType(String crcName, CarType carType) {
		EntityManager em = EMF.get().createEntityManager();
		try{
			Query query = em.createQuery("SELECT t.cars FROM CarType t WHERE t.key = :key");
			query.setParameter("key", carType.getKey());
			Set<Car> res = (Set<Car>) query.getSingleResult();
			List<Car> result = new ArrayList<Car>(res);
			return result;
		} finally {
			em.close();
		}
	}

	/**
	 * Check whether the given car renter has reservations.
	 *
	 * @param 	renter
	 * 			the car renter
	 * @return	True if the number of reservations of the given car renter is higher than 0.
	 * 			False otherwise.
	 */
	public boolean hasReservations(String renter) {
		return this.getReservations(renter).size() > 0;		
	}	
}