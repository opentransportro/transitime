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


import org.hibernate.CallbackException;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.classic.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;
import org.transitclock.configData.CoreConfig;
import org.transitclock.db.hibernate.HibernateUtils;
import org.transitclock.utils.Geo;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * A StopPath is a set of points that defines how a vehicle gets from one stop
 * to another. The stops do not necessarily lie directly on the segments since
 * the segments are likely to be street center line data while the stops are
 * usually on the sidewalk.
 * 
 * @author SkiBu Smith
 * 
 */
@Entity 
@DynamicUpdate 
@Table(name="StopPaths")
public class StopPath implements Serializable, Lifecycle {

	@Column 
	@Id
	private final int configRev;
	
	// Using size of
	// 2 * DEFAULT_ID_SIZE since stop path names are stop1_to_stop2 so can
	// be twice as long as other IDs. And when using GTFS Editor the IDs
	// are quite long, a bit longer than 40 characters.
	@Column(length=2*HibernateUtils.DEFAULT_ID_SIZE) 
	@Id
	private final String stopPathId;
	
	@Column(length=TripPattern.TRIP_PATTERN_ID_LENGTH) 
	@Id
	private String tripPatternId;
	
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String stopId;
	
	// The stop_sequence for the trip from the GTFS stop_times.txt file 
	@Column
	private final int gtfsStopSeq;
	
	// Needed for schedule adherence so can return scheduled departure time
	// for most stops but the scheduled arrival time for the last stop for
	// a trip.
	@Column
	private final boolean lastStopInTrip;
	
	// route ID from GTFS data
	@Column(length=HibernateUtils.DEFAULT_ID_SIZE)
	private final String routeId;
	
	// Indicates that vehicle can leave route path before departing this stop
	// since the driver is taking a break.
	@Column
	private final boolean layoverStop;
	
	// Indicates that vehicle is not supposed to depart the stop until the
	// scheduled departure time.
	@Column
	private final boolean waitStop;
	
	// If should generate special ScheduleAdherence data for this stop
	@Column
	private final boolean scheduleAdherenceStop;

	// How long a break driver is entitled to have at the stop, even if it
	// means that the driver should depart after the scheduled departure time.
	@Column
	private final Integer breakTime;
	

  // sacrifice performance for reportability -- use a child table instead of java serialization 
	@ElementCollection
  @OrderColumn
	private List<Location> locations;


	// Having the path length readily accessible via the database is handy
	// since that way can easily do queries to determine travel speeds and
	// such. 
	@Column
	private double pathLength;
	
	
	@Column 
	private Double maxDistance;
	
	@Column
	private Double maxSpeed;
	// So can have easy access to vectors representing the segments so
	// can easily determine heading. Declared transient because this
	// info is generated by other members after the object has been 
	// loaded from the database.
	@Transient
	private List<VectorWithHeading> vectors = null;
	
	/**
	 * This is used just to get better location of the bustop
	 * where bestmatch is done.
	 */
	@Transient
	private Double shapeDistanceTraveled;

	// Because Hibernate requires objects with composite IDs to be Serializable
	private static final long serialVersionUID = 8170734640228933095L;

	private static final Logger logger = 
			LoggerFactory.getLogger(StopPath.class);

	/********************** Member Functions **************************/

	/**
	 * Simple constructor
	 * 
	 * @param configRev
	 * @param pathId
	 * @param stopId
	 * @param gtfsStopSeq
	 * @param lastStopInTrip
	 * @param routeId
	 * @param layoverStop
	 * @param waitStop
	 * @param scheduleAdherenceStop
	 * @param breakTime
	 * @param shapeDistanceTraveled 
	 */
	public StopPath(int configRev,
			String pathId, 
			String stopId,
			int gtfsStopSeq,
			boolean lastStopInTrip,
			String routeId, 
			boolean layoverStop,
			boolean waitStop,
			boolean scheduleAdherenceStop,
			Integer breakTime,
			Double maxDistance,
			Double maxSpeed, Double shapeDistanceTraveled) {
		this.configRev = configRev;
		this.stopPathId = pathId;
		this.stopId = stopId;
		this.gtfsStopSeq = gtfsStopSeq;
		this.lastStopInTrip = lastStopInTrip;
		this.routeId = routeId;
		this.locations = null;
		this.layoverStop = layoverStop;
		this.waitStop = waitStop;
		this.scheduleAdherenceStop = scheduleAdherenceStop;
		// If valid break time was passed in then use it. Otherwise
		// use the default value.
		this.breakTime = breakTime;
		this.maxDistance = maxDistance;
		this.maxSpeed = maxSpeed;
		this.shapeDistanceTraveled=shapeDistanceTraveled;
	}

	/**
	 * Needed because Hibernate requires no-arg constructor
	 */
	@SuppressWarnings("unused")
	private StopPath() {
		this.configRev = -1;
		this.stopPathId = null;
		this.stopId = null;
		this.gtfsStopSeq = -1;
		this.lastStopInTrip = false;
		this.routeId = null;
		this.tripPatternId = null;
		this.locations = null;
		this.layoverStop = false;
		this.waitStop = false;
		this.scheduleAdherenceStop = false;
		this.breakTime = null;
		this.maxDistance = null;
		this.maxSpeed = null;
		
	}
	
	/**
	 * Returns List of StopPath objects for the specified database revision.
	 * 
	 * @param session
	 * @param configRev
	 * @return
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static List<StopPath> getPaths(Session session, int configRev) 
			throws HibernateException {
		String hql = "FROM StopPath " +
				"    WHERE configRev = :configRev";
		Query query = session.createQuery(hql);
		query.setInteger("configRev", configRev);
		return query.list();
	}

	/**
	 * For consistently naming the path Id. It is based on the current
	 * stop ID and the previous stop Id. If previousStopId is null
	 * then will return "to_" + stopId. If not null will return
	 * previousStopId + "_to_" + stopId.
	 * @param previousStopId
	 * @param stopId
	 * @return
	 */
	public static String determinePathId(String previousStopId, String stopId) {
		if (previousStopId == null) {
			return "to_" + stopId;
		} else {
			return previousStopId + "_to_" + stopId;
		}
	}
	
	public Double getShapeDistanceTraveled() {
		return shapeDistanceTraveled;
	}

	public void setShapeDistanceTraveled(Double shapeDistanceTraveled) {
		this.shapeDistanceTraveled = shapeDistanceTraveled;
	}
		
	/**
	 * Returns the distance to travel along the path. Summation of 
	 * all of the path segments.
	 * @return
	 */
	public double length() {
		// Make sure locations were set before trying to access them
		if (locations == null) {
			logger.error("For stopPathId={} trying to access locations when " +
					"they have not been set.", stopPathId);
			return Double.NaN;
		}
		
		double totalLength = 0.0;
		for (int i=0; i<locations.size()-1; ++i) {
			totalLength += (new Vector(locations.get(i), locations.get(i+1))).length();
		}
		return totalLength;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StopPath ["
				+ "configRev=" + configRev
				+ ", stopPathId=" + stopPathId 
				+ ", stopId=" + stopId 				
				+ ", gtfsStopSeq=" + gtfsStopSeq 
				+ ", lastStopInTrip=" + lastStopInTrip
				+ ", routeId=" + routeId 
				+ ", tripPatternId=" + tripPatternId
				+ ", locations=" + locations 
				+ ", pathLength=" + Geo.distanceFormat(pathLength)
				+ ", layoverStop=" + layoverStop 
				+ ", waitStop=" + waitStop 
				+ ", scheduleAdherenceStop=" + scheduleAdherenceStop
				+ ", maxDistance=" + maxDistance
				+ ", maxSpeed=" + maxSpeed
				+ (breakTime == null ? "" : ", breakTime=" + breakTime)
				+ "]";
	}

	

	public Double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(Double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public Double getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(Double maxDistance) {
		this.maxDistance = maxDistance;
	}

	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + configRev;
		result = prime * result + (layoverStop ? 1231 : 1237);
		result = prime * result
				+ ((locations == null) ? 0 : locations.hashCode());
		result = prime * result + ((stopPathId == null) ? 0 : stopPathId.hashCode());
		long temp;
		temp = Double.doubleToLongBits(pathLength);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime * result + (scheduleAdherenceStop ? 1231 : 1237);
		result = prime * result + (lastStopInTrip ? 1231 : 1237);
		result = prime * result + ((breakTime == null) ? 0 : breakTime.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		result = prime * result
				+ ((tripPatternId == null) ? 0 : tripPatternId.hashCode());
		result = prime * result + ((vectors == null) ? 0 : vectors.hashCode());
		result = prime * result + (waitStop ? 1231 : 1237);
		return result;
	}

	/**
	 * For when seeing if TripPatternBase is unique in a collection. When
	 * this is done the StopPath isn't yet fully complete so only compare
	 * the key members that signify if a StopPath is unique.
	 */
	public int basicHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + configRev;
		result = prime * result + ((stopPathId == null) ? 0 : stopPathId.hashCode());
		result = prime * result + ((routeId == null) ? 0 : routeId.hashCode());
		result = prime * result + ((stopId == null) ? 0 : stopId.hashCode());
		return result;
	}

	
	/**
	 * Needed because have a composite ID for Hibernate storage
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StopPath other = (StopPath) obj;
		if (configRev != other.configRev)
			return false;
		if (layoverStop != other.layoverStop)
			return false;
		if (locations == null) {
			if (other.locations != null)
				return false;
		} else if (!locations.equals(other.locations))
			return false;
		if (stopPathId == null) {
			if (other.stopPathId != null)
				return false;
		} else if (!stopPathId.equals(other.stopPathId))
			return false;
		if (Double.doubleToLongBits(pathLength) != Double
				.doubleToLongBits(other.pathLength))
			return false;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (scheduleAdherenceStop != other.scheduleAdherenceStop)
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (gtfsStopSeq != other.gtfsStopSeq)
			return false;
		if (lastStopInTrip != other.lastStopInTrip)
			return false;
		if (breakTime == null) {
			if (other.breakTime != null)
				return false;
		} else if (!breakTime.equals(other.breakTime))
			return false;
		if (tripPatternId == null) {
			if (other.tripPatternId != null)
				return false;
		} else if (!tripPatternId.equals(other.tripPatternId))
			return false;
		if (vectors == null) {
			if (other.vectors != null)
				return false;
		} else if (!vectors.equals(other.vectors))
			return false;
		if (waitStop != other.waitStop)
			return false;
		return true;
	}

	/**
	 * For when seeing if TripPatternBase is unique in a collection. When
	 * this is done the StopPath isn't yet fully complete so only compare
	 * the key members that signify if a StopPath is unique.
	 */
	public boolean basicEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StopPath other = (StopPath) obj;
		if (configRev != other.configRev)
			return false;
		if (stopPathId == null) {
			if (other.stopPathId != null)
				return false;
		} else if (!stopPathId.equals(other.stopPathId))
			return false;
		if (routeId == null) {
			if (other.routeId != null)
				return false;
		} else if (!routeId.equals(other.routeId))
			return false;
		if (stopId == null) {
			if (other.stopId != null)
				return false;
		} else if (!stopId.equals(other.stopId))
			return false;
		if (gtfsStopSeq != other.gtfsStopSeq)
			return false;
		return true;
	}
	
	/***************************** Setter/Getter Methods *********************/
	
	/**
	 * @return the configRev
	 */
	public int getConfigRev() {
		return configRev;
	}
	
	/**
	 * @return the stopPathId
	 */
	public String getId() {
		return stopPathId;
	}

	/**
	 * @return the stopId
	 */
	public String getStopId() {
		return stopId;
	}
	
	/**
	 * The stop_sequence for the trip from the GTFS stop_times.txt file
	 * 
	 * @return the gtfsStopSeq
	 */
	public int getGtfsStopSeq() {
		return gtfsStopSeq;
	}
	
	/**
	 * @return the lastStopInTrip
	 */
	public boolean isLastStopInTrip() {
		return lastStopInTrip;
	}
	
	/**
	 * @return the stopPathId
	 */
	public String getStopPathId() {
		return stopPathId;
	}
	
	/**
	 * Provides the name of the stop as obtained by a Core predictor. Cannot be
	 * used with other applications.
	 * 
	 * @return the name of the stop
	 */
	public String getStopName() {
		return Core.getInstance().getDbConfig().getStop(stopId).getName();
	}
	
	/**
	 * @return the tripPatternId
	 */
	public String getTripPatternId() {
		return tripPatternId;
	}

	/**
	 * @return the routeId
	 */
	public String getRouteId() {
		return routeId;
	}
	
	/**
	 * Locations are not available when StopPath is first created so
	 * need to be able to set them after construction. Sets the
	 * locations member and also determines the pathLength.
	 * @param locations
	 */
	public void setLocations(ArrayList<Location> locations) {
		this.locations = locations;
		
		pathLength = 0.0;
		for (int i=0; i<locations.size()-1; ++i) {
			Location l1 = locations.get(i);
			Location l2 = locations.get(i+1);
			pathLength += l1.distance(l2);
		}
			
	}
	
	/**
	 * Returns the location of the stop at the end of the path.
	 * 
	 * @return Location of stop
	 */
	public Location getStopLocation() {
		// Simply return the last location of the path, since it
		// corresponds to the stop associated with the path.
		return locations.get(locations.size()-1);
	}
	
	/**
	 * So can set the tripPatternId once it is known (which happens after 
	 * StopPath is constructed).
	 * @param tripPatternId
	 */
	public void setTripPatternId(String tripPatternId) {
		this.tripPatternId = tripPatternId;
	}
	
	/**
	 * @return List of Locations of the segments that make up the path
	 */
	public List<Location> getLocations() {
		return locations;
	}
	
	/**
	 * @return Number of segments in path.
	 */
	public int getNumberSegments() {
		return locations.size()-1;
	}
	
	/**
	 * Returns the end of the path, which is where the stop is. Note that
	 * it is not exactly the stop location because stops do not need to be
	 * on the path.
	 * 
	 * @return
	 */
	public Location getEndOfPathLocation() {
		return locations.get(locations.size()-1);
	}
	
	/**
	 * @return Combined length of all the path segments 
	 */
	public double getLength() {
		return pathLength;
	}
	
	/**
	 * @return List of VectorWithHeadings of the segments that make up the path
	 */
	public List<VectorWithHeading> getSegmentVectors() {
		return vectors;
	}
	
	/**
	 * Returns the vector for the specified segment.
	 * 
	 * @param segmentIndex
	 * @return The vector, or null if segmentIndex not valid
	 */
	public VectorWithHeading getSegmentVector(int segmentIndex) {
		// If index out of range return null
		if (segmentIndex < 0 || segmentIndex >= getSegmentVectors().size())  {
			return null;
		}

		return getSegmentVectors().get(segmentIndex);
	}
	
	/**
	 * @param index
	 * @return Location for the specified index along the StopPath
	 */
	public Location getLocation(int index) {
		return locations.get(index);
	}

	/**
	 * Indicates that vehicle can leave route path before departing this stop
	 * since the driver is taking a break.
	 * 
	 * @return true if a layover stop
	 */
	public boolean isLayoverStop() {
		return layoverStop;
	}

	/**
	 * Returns how long driver is expected to have a break for at this stop.
	 * 
	 * @return Layover time in seconds if layover stop, otherwise, 0.
	 */
	public int getBreakTimeSec() {
		if (layoverStop) {
			if (breakTime != null)
				return breakTime;
			else
				return CoreConfig.getDefaultBreakTimeSec();
		} else {
			return 0;
		}
	}

	/**
	 * Indicates that vehicle is not supposed to depart the stop until the
	 * scheduled departure time.
	 * 
	 * @return true if a wait stop
	 */
	public boolean isWaitStop() {
		return waitStop;
	}

	public boolean isScheduleAdherenceStop() {
		return scheduleAdherenceStop;
	}
	
	/**
	 * How far a vehicle can be ahead of a stop and be considered to have 
	 * arrived.
	 * 
	 * @return
	 */
	public double getBeforeStopDistance() {
		return CoreConfig.getBeforeStopDistance();
	}
	
	/**
	 * How far a vehicle can be past a stop and still be considered at the stop.
	 * @return
	 */
	public double getAfterStopDistance() {
		return CoreConfig.getAfterStopDistance();
	}

	/* (non-Javadoc)
	 * @see org.hibernate.classic.Lifecycle#onDelete(org.hibernate.Session)
	 */
	@Override
	public boolean onDelete(Session arg0) throws CallbackException {
		// Don't veto delete
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernate.classic.Lifecycle#onLoad(org.hibernate.Session,
	 * java.io.Serializable)
	 * 
	 * When the vector is read in from db this method is automatically called to
	 * set the transient vector array. This way it is simpler to go through the
	 * path segments to determine matches.
	 */
	@Override
	public void onLoad(Session arg0, Serializable arg1) {
		vectors = new ArrayList<VectorWithHeading>(locations.size()-1);
		for (int segmentIndex=0; segmentIndex<locations.size()-1; ++segmentIndex) {
			VectorWithHeading v = 
					new VectorWithHeading(nullSafeLocation(locations.get(segmentIndex)), 
					              		  nullSafeLocation(locations.get(segmentIndex+1)));
			vectors.add(v);
		}
	}

	private Location nullSafeLocation(Location location) {
	  if (location == null) {
	    location = new Location(0.0, 0.0);
	  }
    return location;
  }

  /* (non-Javadoc)
	 * @see org.hibernate.classic.Lifecycle#onSave(org.hibernate.Session)
	 */
	@Override
	public boolean onSave(Session arg0) throws CallbackException {
		// Don't veto save
		return false;
	}

	/* (non-Javadoc)
	 * @see org.hibernate.classic.Lifecycle#onUpdate(org.hibernate.Session)
	 */
	@Override
	public boolean onUpdate(Session arg0) throws CallbackException {
		// Don't veto update
		return false;
	}

}
