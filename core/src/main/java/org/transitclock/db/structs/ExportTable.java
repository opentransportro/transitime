/* (C)2023 */
package org.transitclock.db.structs;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.DynamicUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitclock.applications.Core;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * For storing static configuration for vehicle in block.
 *
 * @author Hubert GoEuropa
 */
@Entity
@DynamicUpdate
@Table(name = "ExportTable")
public class ExportTable implements Serializable {

    // ID of vehicle
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    @Temporal(TemporalType.DATE)
    private Date dataDate;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date exportDate;

    @Column
    private int exportType;

    @Column
    private int exportStatus;

    @Column
    private String fileName;

    @Column
    private byte[] file;

    private static final Logger logger = LoggerFactory.getLogger(ExportTable.class);

    /**
     * @param vehicleId vehicle ID * @param blockId block ID * @param tripId trip ID * @param
     *     assignmentDate time * * @param validFrom time * * @param validTo time
     */
    public static ExportTable create(Date dataDate, int exportType, String fileName) {
        ExportTable exportElement = new ExportTable(dataDate, exportType, fileName);

        // Log VehicleToBlockConfig in log file
        logger.info(exportElement.toString());

        // Queue to write object to database
        Core.getInstance().getDbLogger().add(exportElement);

        // Return new VehicleToBlockConfig
        return exportElement;
    }

    public ExportTable(Date dataDate, int exportType, String fileName) {
        this.dataDate = dataDate;
        this.exportType = exportType;
        this.fileName = fileName;
        this.exportDate = new Date();
        this.exportStatus = 1;
    }

    public ExportTable(long id, Date dataDate, Date exportDate, int exportType, int exportStatus, String fileName) {
        this.id = id;
        this.dataDate = dataDate;
        this.exportDate = exportDate;
        this.exportType = exportType;
        this.exportStatus = exportStatus;
        this.fileName = fileName;
    }

    /** Needed because Hibernate requires no-arg constructor */
    @SuppressWarnings("unused")
    protected ExportTable() {
        dataDate = null;
        exportDate = null;
        exportType = 0;
        fileName = null;
        file = null;
    }

    /**
     * Reads List of VehicleConfig objects from database
     *
     * @param session
     * @return List of VehicleConfig objects
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<ExportTable> getExportTable(Session session) throws HibernateException {
        // String hql = "FROM ExportTable";
        var query = session.createQuery("SELECT id, dataDate, exportDate, exportType, exportStatus, fileName FROM ExportTable order by exportDate desc");
        return query.list();
    }

    public static void deleteExportTableRecord(long id, Session session) throws HibernateException {
        Transaction transaction = session.beginTransaction();
        try {
            var q = session
                    .createQuery("delete from ExportTable where id = :id")
                    .setParameter("id", id);
            q.executeUpdate();

            transaction.commit();
        } catch (Throwable t) {
            transaction.rollback();
            throw t;
        }
    }

    @SuppressWarnings("unchecked")
    public static List<ExportTable> getExportFile(Session session, long id) throws HibernateException {
        return session.createQuery("FROM ExportTable WHERE id = :id")
                .setParameter("id", id)
                .list();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

    public Date getExportDate() {
        return exportDate;
    }

    public void setExportDate(Date exportDate) {
        this.exportDate = exportDate;
    }

    public int getExportType() {
        return exportType;
    }

    public void setExportType(int exportType) {
        this.exportType = exportType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public int getExportStatus() {
        return exportStatus;
    }

    public void setExportStatus(int exportStatus) {
        this.exportStatus = exportStatus;
    }
}
