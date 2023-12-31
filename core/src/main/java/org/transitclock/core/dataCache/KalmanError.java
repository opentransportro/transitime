/* (C)2023 */
package org.transitclock.core.dataCache;

import java.io.Serializable;

public class KalmanError implements Serializable {
    // This is the error values itself.
    private Double error = Double.NaN;

    // This is the number of times it has been updated.
    private Integer updates = null;

    public KalmanError(Double error) {
        setError(error);
    }

    public KalmanError() {
        // TODO Auto-generated constructor stub
    }

    public Double getError() {
        return error;
    }

    public void setError(Double error) {
        if (this.error.compareTo(error) != 0) {
            this.error = error;
            incrementUpdates();
        }
    }

    public Integer getUpdates() {
        return updates;
    }

    private void incrementUpdates() {
        if (this.updates != null) {
            this.updates = this.updates + 1;
        } else {
            this.updates = 0;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((error == null) ? 0 : error.hashCode());
        result = prime * result + ((updates == null) ? 0 : updates.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        KalmanError other = (KalmanError) obj;
        if (error == null) {
            if (other.error != null) return false;
        } else if (!error.equals(other.error)) return false;
        if (updates == null) {
            return other.updates == null;
        } else return updates.equals(other.updates);
    }

    @Override
    public String toString() {
        return "KalmanError [error=" + error + ", updates=" + updates + "]";
    }
}
