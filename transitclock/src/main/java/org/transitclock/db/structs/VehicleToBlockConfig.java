/*
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transitclock.db.structs;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.db.hibernate.HibernateUtils;

/**
 * For storing static configuration for vehicle in block.
 *
 * @author Hubert GoEuropa
 *
 */
@Entity @DynamicUpdate @Table(name="VehicleToBlockConfigs")
public class VehicleToBlockConfig implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// ID of vehicle
	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@Id
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String vehicleId;
	
	@Column(nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private final Date assignmentDate;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String blockId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private String tripId;
	
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private Date validFrom;
	
	@Column	
	@Temporal(TemporalType.TIMESTAMP)
	private Date validTo;
	
	
	
	
	private static final Logger logger = 
			LoggerFactory.getLogger(VehicleEvent.class);
	/********************** Member Functions **************************/

	/**
	 * 
	 * @param vehicleId vehicle ID
	 * * @param blockId block ID
	 * * @param tripId trip ID
	 * * @param time time
	 */
	public VehicleToBlockConfig(String vehicleId, String blockId, String tripId, Date assignmentDate, Date validFrom, Date validTo) {
		this.vehicleId = vehicleId;
		this.blockId = blockId;
		this.tripId = tripId;
		this.assignmentDate = assignmentDate;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}
	
	/**
	 * 
	 * @param vehicleId vehicle ID
	 * * @param blockId block ID
	 * * @param tripId trip ID
	 * * @param assignmentDate time
	 * 	 * * @param validFrom time
	 * 	 * * @param validTo time
	 */
	public static VehicleToBlockConfig create(String vehicleId, String blockId, String tripId, Date assignmentDate, Date validFrom, Date validTo) {
		VehicleToBlockConfig vehicleToBlockConfig =
				new VehicleToBlockConfig(vehicleId, blockId, tripId, assignmentDate, validFrom, validTo);

		// Log VehicleToBlockConfig in log file
		logger.info(vehicleToBlockConfig.toString());

		// Queue to write object to database
		Core.getInstance().getDbLogger().add(vehicleToBlockConfig);

		// Return new VehicleToBlockConfig
		return vehicleToBlockConfig;
	}

	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private VehicleToBlockConfig() {
		vehicleId = null;
		blockId = null;
		tripId = null;
		assignmentDate = null;
		validFrom = null;
		validTo = null;
	}

	/**
	 * Reads List of VehicleConfig objects from database
	 * 
	 * @param session
	 * @return List of VehicleConfig objects
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<VehicleToBlockConfig> getVehicleToBlockConfigs(Session session) 
			throws HibernateException {
		String hql = "FROM VehicleToBlockConfig";
		Query query = session.createQuery(hql);
		return query.list();
	}
	
	/**
	 * Reads List of VehicleConfig objects from database
	 * 
	 * @param VehicleConfig, session
	 * @throws HibernateException
	 */
	public static void updateVehicleToBlockConfig(VehicleToBlockConfig vehicleToBlockConfig, Session session) 
			throws HibernateException {
		session.update(vehicleToBlockConfig);
	}
	
	
	public static void deleteVehicleToBlockConfig(long id, Session session) 
			throws HibernateException {
		Transaction transaction = session.beginTransaction();
		try {
		  String hql = "delete from VehicleToBlockConfig where id = :id";
		  Query q = session.createQuery(hql).setParameter("id", id);
		  q.executeUpdate();

		  transaction.commit();
		} catch (Throwable t) {
		  transaction.rollback();
		  throw t;
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static List<VehicleToBlockConfig> getVehicleToBlockConfigsByBlockId(Session session, String blockId) 
			throws HibernateException {
		String hql = "FROM VehicleToBlockConfig WHERE blockid = '" + blockId + "' ORDER BY assignmentDate DESC";
		Query query = session.createQuery(hql);
		return query.list();
	}
	
	@SuppressWarnings("unchecked")
	public static List<VehicleToBlockConfig> getVehicleToBlockConfigsByVehicleId(Session session, String vehicleId) 
			throws HibernateException {
		String hql = "FROM VehicleToBlockConfig WHERE vehicleid = '" + vehicleId + "' ORDER BY assignmentDate DESC";
		Query query = session.createQuery(hql);
		return query.list();
	}
	
	

	/************* Getter Methods ****************************/
	
	public long getId() {
		return id;
	}

	public String getBlockId() {
		return blockId;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public Date getAssignmentDate() {
		return assignmentDate;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	

}
