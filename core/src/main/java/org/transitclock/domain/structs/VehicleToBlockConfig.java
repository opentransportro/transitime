/* (C)2023 */
package org.transitclock.domain.structs;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.DynamicUpdate;
import org.transitclock.SingletonContainer;
import org.transitclock.domain.hibernate.DataDbLogger;

/**
 * For storing static configuration for vehicle in block.
 *
 * @author Hubert GoEuropa
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
@DynamicUpdate
@Table(name = "VehicleToBlockConfigs")
public class VehicleToBlockConfig implements Serializable {

    // ID of vehicle
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Id
    @Column(length = 60)
    private final String vehicleId;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private final Date assignmentDate;

    @Column(length = 60)
    private String blockId;

    @Column(length = 60)
    private String tripId;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date validFrom;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date validTo;

    public VehicleToBlockConfig(
            String vehicleId, String blockId, String tripId, Date assignmentDate, Date validFrom, Date validTo) {
        this.vehicleId = vehicleId;
        this.blockId = blockId;
        this.tripId = tripId;
        this.assignmentDate = assignmentDate;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    /**
     * @param vehicleId vehicle ID * @param blockId block ID * @param tripId trip ID * @param
     *     assignmentDate time * * @param validFrom time * * @param validTo time
     */
    public static VehicleToBlockConfig create(
            String vehicleId, String blockId, String tripId, Date assignmentDate, Date validFrom, Date validTo) {
        VehicleToBlockConfig vehicleToBlockConfig =
                new VehicleToBlockConfig(vehicleId, blockId, tripId, assignmentDate, validFrom, validTo);

        // Queue to write object to database
        SingletonContainer.getInstance(DataDbLogger.class)
                .add(vehicleToBlockConfig);

        // Return new VehicleToBlockConfig
        return vehicleToBlockConfig;
    }

    /** Needed because Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected VehicleToBlockConfig() {
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
    public static List<VehicleToBlockConfig> getVehicleToBlockConfigs(Session session) throws HibernateException {
        return session
                .createQuery("FROM VehicleToBlockConfig", VehicleToBlockConfig.class)
                .list();
    }

    /**
     * Reads List of VehicleConfig objects from database
     *
     * @param vehicleToBlockConfig, session
     * @throws HibernateException
     */
    public static void updateVehicleToBlockConfig(VehicleToBlockConfig vehicleToBlockConfig, Session session)
            throws HibernateException {
        session.merge(vehicleToBlockConfig);
    }

    public static void deleteVehicleToBlockConfig(long id, Session session) throws HibernateException {
        Transaction transaction = session.beginTransaction();
        try {
            session
                    .createMutationQuery("delete from VehicleToBlockConfig where id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            transaction.commit();
        } catch (Throwable t) {
            transaction.rollback();
            throw t;
        }
    }

    public static List<VehicleToBlockConfig> getVehicleToBlockConfigsByBlockId(Session session, String blockId) throws HibernateException {
        return session
                .createQuery("FROM VehicleToBlockConfig WHERE blockId = :blockId ORDER BY assignmentDate DESC", VehicleToBlockConfig.class)
                .setParameter("blockId", blockId)
                .list();
    }

    public static List<VehicleToBlockConfig> getVehicleToBlockConfigsByVehicleId(Session session, String vehicleId) throws HibernateException {
        return session.createQuery("FROM VehicleToBlockConfig WHERE vehicleId = :vehicleId ORDER BY assignmentDate DESC", VehicleToBlockConfig.class)
                .setParameter("vehicleId", vehicleId)
                .list();
    }
}
