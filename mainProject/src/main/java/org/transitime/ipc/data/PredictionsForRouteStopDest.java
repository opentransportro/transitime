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

package org.transitime.ipc.data;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.transitime.applications.Core;
import org.transitime.db.structs.Trip;

/**
 * Contains list of predictions for a route/stop/destination. 
 *
 * @author SkiBu Smith
 *
 */
public class PredictionsForRouteStopDest implements Serializable {

	private final String routeId;
	// routeShortName needed because routeId is sometimes not consistent over
	// schedule changes but routeShortName usually is.
	private final String routeShortName;
	private final String routeName;
	
	private final String stopId;
	private final String stopName;
	
	private final String destination;
	private final String directionId;
	
	// The predictions associated with the route/stop
	private final List<Prediction> predictionsForRouteStop;

	// How big the prediction arrays for the route/stops can be. Really doesn't
	// need to be all that large. Might generate predictions further into the
	// future but when a user requests predictions they really just need 
	// a few.
	private final static int MAX_PREDICTIONS = 5;

	private static final long serialVersionUID = 5875028328864504842L;

	/********************** Member Functions **************************/

	/**
	 * Constructor for creating a PredictionsForRouteStopDest on the server
	 * side.
	 * 
	 * @param trip
	 * @param stopId
	 */
	public PredictionsForRouteStopDest(Trip trip, String stopId) {
		this.routeId = trip != null ? trip.getRouteId() : null;
		this.routeShortName = trip != null ? trip.getRouteShortName() : null;
		this.routeName = trip != null ? trip.getRouteName() : null;
		this.stopId = stopId;
		this.stopName = Core.getInstance().getDbConfig().getStop(stopId).getName();
		this.destination = trip != null ? trip.getName() : null;
		this.directionId = trip != null ? trip.getDirectionId() : null;
		
		this.predictionsForRouteStop = new ArrayList<Prediction>(MAX_PREDICTIONS);
	}
	
	/**
	 * Constructor for cloning a PredictionsForRouteStop object.
	 * 
	 * @param toClone
	 * @param maxPredictionsPerStop
	 * @param maxSystemTimeForPrediction
	 *            Max point in future want predictions for. This way can limit
	 *            predictions when requesting a large number of them.
	 */
	private PredictionsForRouteStopDest(PredictionsForRouteStopDest toClone,
			int maxPredictionsPerStop, long maxSystemTimeForPrediction) {
		this.routeId = toClone.routeId;
		this.routeShortName = toClone.routeShortName;
		this.routeName = toClone.routeName;
		this.stopId = toClone.stopId;
		this.stopName = toClone.stopName;
		this.destination = toClone.destination;
		this.directionId = toClone.directionId;
		
		// Copy all the prediction info. Do while synchronized since another
		// thread could otherwise be changing the data.
		synchronized (toClone) {	
			int size = Math.min(toClone.predictionsForRouteStop.size(),
					maxPredictionsPerStop);
			this.predictionsForRouteStop = new ArrayList<Prediction>(size);			
			for (int i=0; i<size; ++i) {
				Prediction prediction = toClone.predictionsForRouteStop.get(i);
				// If prediction exceeds max time then done
				if (prediction.getTime() > maxSystemTimeForPrediction)
					break;
				this.predictionsForRouteStop.add(i, prediction);
			}
		}
	}
	
	/**
	 * Constructor used for when deserializing a proxy object. Declared private
	 * because only used internally by the proxy class.
	 * 
	 * @param routeId
	 * @param routeShortName
	 * @param routeName
	 * @param stopId
	 * @param stopName
	 * @param destination
	 * @param directionId
	 * @param predictions
	 */
	private PredictionsForRouteStopDest(String routeId, String routeShortName,
			String routeName, String stopId, String stopName,
			String destination, String directionId, List<Prediction> predictions) {
		this.routeId = routeId;
		this.routeShortName = routeShortName;
		this.routeName = routeName;
		this.stopId = stopId;
		this.stopName = stopName;
		this.destination = destination;
		this.directionId = directionId;
		
		this.predictionsForRouteStop = predictions;
	}
	
	/**
	 * SerializationProxy is used so that this class can be immutable and so
	 * that can do versioning of objects.
	 */
	private static class SerializationProxy implements Serializable {
		private String routeId;
		private String routeShortName;
		private String routeName;
		private String stopId;
		private String stopName;
		private String destination;
		private String directionId;
		private List<Prediction> predictionsForRouteStop;

		private static final short serializationVersion = 0;
		private static final long serialVersionUID = -2312925771271829358L;

		/*
		 * Only to be used within this class.
		 */
		private SerializationProxy(PredictionsForRouteStopDest p) {
			this.routeId = p.routeId;
			this.routeShortName = p.routeShortName;
			this.routeName = p.routeName;
			this.stopId = p.stopId;
			this.stopName = p.stopName;
			this.destination = p.destination;
			this.directionId = p.directionId;
			this.predictionsForRouteStop = p.predictionsForRouteStop;
		}

		/*
		 * When object is serialized writeReplace() causes this
		 * SerializationProxy object to be written. Write it in a custom way
		 * that includes a version ID so that clients and servers can have two
		 * different versions of code.
		 */
		private void writeObject(java.io.ObjectOutputStream stream)
				throws IOException {
			stream.writeShort(serializationVersion);
			
			stream.writeObject(routeId);
			stream.writeObject(routeShortName);
			stream.writeObject(routeName);
			stream.writeObject(stopId);
			stream.writeObject(stopName);
			stream.writeObject(destination);
			stream.writeObject(directionId);
			stream.writeObject(predictionsForRouteStop);
		}
		
		/*
		 * Custom method of deserializing a SerializationProy object.
		 */
		@SuppressWarnings("unchecked")
		private void readObject(java.io.ObjectInputStream stream)
				throws IOException, ClassNotFoundException {
			short readVersion = stream.readShort();
			if (serializationVersion != readVersion) {
				throw new IOException("Serialization error when reading "
						+ getClass().getSimpleName()
						+ " object. Read serializationVersion=" + readVersion);
			}

			// serialization version is OK so read in object
			routeId = (String) stream.readObject();
			routeShortName = (String) stream.readObject();
			routeName = (String) stream.readObject();
			stopId = (String) stream.readObject();
			stopName = (String) stream.readObject();
			destination = (String) stream.readObject();
			directionId = (String) stream.readObject();
			predictionsForRouteStop = (List<Prediction>) stream.readObject();
		}

		/*
		 * When an object is read in it will be a SerializatProxy object due to
		 * writeReplace() being used by the enclosing class. When such an object
		 * is deserialized this method will be called and the SerializationProxy
		 * object is converted to an enclosing class object.
		 */
		private Object readResolve() {
			return new PredictionsForRouteStopDest(routeId, routeShortName,
					routeName, stopId, stopName, destination, directionId,
					predictionsForRouteStop);
		}
	} /* End of SerializationProxy inner class */
	
	/*
	 * Needed as part of using a SerializationProxy. When Vehicle object is
	 * serialized the SerializationProxy will instead be used.
	 */
	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	/*
	 * Needed as part of using a SerializationProxy. Makes sure that Vehicle
	 * object cannot be deserialized without using proxy, thereby eliminating
	 * possibility of such an attack as described in "Effective Java".
	 */
	private void readObject(ObjectInputStream stream)
			throws InvalidObjectException {
		throw new InvalidObjectException("Must use proxy instead");
	}

	/**
	 * Gets a copy of this object. This is done with the object being
	 * copied synchronized so that the predictions remain coherent. Limits
	 * number of predictions to maxPredictionsPerStop.
	 * 
	 * @param maxPredictionsPerStop
	 * @return
	 */
	public PredictionsForRouteStopDest getClone(int maxPredictionsPerStop) {
		// Get copy of predictions. Don't limit by how far predictions
		// are into the future. Therefore maxPredictionTime set to
		// Integer.MAX_VALUE and currentTime set to 0L because it 
		// doesn't matter.
		PredictionsForRouteStopDest clone = new PredictionsForRouteStopDest(this,
				maxPredictionsPerStop, Long.MAX_VALUE);
		return clone;
	}
	
	/**
	 * Gets a copy of this object. This is done with the object being copied
	 * synchronized so that the predictions remain coherent. Limits number of
	 * predictions to maxPredictionsPerStop.
	 * 
	 * @param maxPredictionsPerStop
	 *            Won't copy more then this number of predictions
	 * @param maxSystemTimeForPrediction
	 *            Max point in future want predictions for. This way can limit
	 *            predictions when requesting a large number of them.
	 * @return
	 */
	public PredictionsForRouteStopDest getClone(int maxPredictionsPerStop,
			long maxSystemTimeForPrediction) {
		PredictionsForRouteStopDest clone = new PredictionsForRouteStopDest(
				this, maxPredictionsPerStop, maxSystemTimeForPrediction);
		return clone;
	}
	
	/**
	 * Removes a prediction.
	 * <p>
	 * Not sure if really need to synchronize removal of predictions
	 * from list since it is only a single operation. But synching
	 * it is certainly the safe thing to do.
	 * 
	 * @param oldPrediction
	 */
	public synchronized void removePrediction(Prediction oldPrediction) {
		predictionsForRouteStop.remove(oldPrediction);
	}

	/**
	 * Updates the predictions for this object with the new predictions for a
	 * vehicle.
	 * <p>
	 * Synchronized because there are multiple steps in removing old predictions
	 * and creating new ones.
	 * 
	 * @param newPredsForRouteStopDest
	 *            The new predictions for the vehicle
	 * @param currentTime
	 *            So can get rid of predictions that have expired.
	 */
	public synchronized void updatePredictionsForVehicle(
			List<Prediction> newPredsForRouteStopDest,
			long currentTime) {
		// If no predictions then nothing to do so return.
		if (newPredsForRouteStopDest == null
				|| newPredsForRouteStopDest.isEmpty())
			return;
	
		// Determine which vehicle we are updating predictions for
		String vehicleId = newPredsForRouteStopDest.get(0).getVehicleId();
		
		// Go through current predictions and get rid of existing ones for
		// this vehicle or ones that have expired
		Iterator<Prediction> iterator = predictionsForRouteStop.iterator();
		while (iterator.hasNext()) {
			Prediction currentPrediction = iterator.next();

			// Remove existing predictions for this vehicle
			if (currentPrediction.getVehicleId().equals(vehicleId)) {
				iterator.remove();
				continue;
			}
			
			// Remove predictions that are expired. It makes sense to do this 
			// here when adding predictions since only need to take out 
			// predictions if more are being added.
			if (currentPrediction.getTime() < currentTime) {
				iterator.remove();
				continue;
			}
		}

		// Go through list and insert the new predictions into the 
		// appropriate places
		for (Prediction newPredForRouteStop : newPredsForRouteStopDest) {
			boolean insertedPrediction = false;
			for (int i=0; i<predictionsForRouteStop.size(); ++i) {
				// If the new prediction is before the previous prediction
				// in currentPredsForRouteStop then insert it.
				if (newPredForRouteStop.getTime() < 
						predictionsForRouteStop.get(i).getTime()) {
					// Add the new prediction to the list. If the list already
					// has the max number of predictions then first remove the
					// last one so that the array doesn't need to grow to 
					// accommodate the new one.
					int arraySize = predictionsForRouteStop.size();
					if (arraySize == MAX_PREDICTIONS)
						predictionsForRouteStop.remove(arraySize-1);
					
					// Now that definitely have room, actually add the 
					// prediction to the list
					predictionsForRouteStop.add(i, newPredForRouteStop);
					insertedPrediction = true;
					
					// Done with the inner for loop so break out of loop
					// and continue with next prediction.
					break;
				}
			}
			
			// If didn't find that the prediction was before one of the 
			// existing ones then insert it onto the end if there is still
			// some space in the array.
			if (!insertedPrediction) {
				if (predictionsForRouteStop.size() < MAX_PREDICTIONS) {
					predictionsForRouteStop.add(predictionsForRouteStop.size(), 
							newPredForRouteStop);
				} else {
					// Didn't insert prediction because it was greater than
					// the others but there is no space at end. This means that
					// done with the new predictions. Don't need to look at 
					// anymore because the remaining ones will have an even
					// higher prediction time and therefore also don't need
					// to be added.
					break;
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return "PredictionsForRouteStopDest [" 
				+ "routeId=" + routeId
				+ ", routeShortName=" + routeShortName 
				+ ", routeName=" + routeName 
				+ ", stopId=" + stopId
				+ ", stopName=" + stopName 
				+ ", destination=" + destination
				+ ", directionId=" + directionId 
				+ ", predictionsForRouteStop=" + predictionsForRouteStop 
				+ "]";
	}

	public String getRouteId() {
		return routeId;
	}

	public String getRouteShortName() {
		return routeShortName;
	}

	public String getRouteName() {
		return routeName;
	}
	
	public String getStopId() {
		return stopId;
	}

	public String getStopName() {
		return stopName;
	}

	public String getDestination() {
		return destination;
	}

	public String getDirectionId() {
		return directionId;
	}

	public List<Prediction> getPredictionsForRouteStop() {
		return predictionsForRouteStop;
	}
}