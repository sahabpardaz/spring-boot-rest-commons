package ir.sahab.rest.common.audit;

import static javax.persistence.TemporalType.TIMESTAMP;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.Version;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * This class provides some basic auditing features for its inherited entities. These features are as follows:
 * <ul>
 *  <li> Adds several audit fields (i.e., created date, modified date, created by and modified by) to the entity which
 *  is done by annotation {@link MappedSuperclass} and the defined fields here.
 *  <li> Fills audit fields automatically which is done by assigned {@link AuditingEntityListener}.
 *  The {@link AuditingEntityListener} uses the registered {@link AuditorAware} component to find user name to fill the
 *  fields annotated by @CreatedBy and @LastModifiedBy.
 *  <li> Persists historical changes to the entity in a separate table automatically which is done by annotation
 *  {@link Audited}.
 *  <li> Adds an extra field named 'version' which is used
 *   for hibernate optimistic locking mechanism.
 * </ul>
 */
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Audited
public class Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @CreatedDate
    @Column(name = "created_date")
    @Temporal(TIMESTAMP)
    @NotAudited
    private Date createdDate;

    @Column(name = "created_by")
    @CreatedBy
    @NotAudited
    private String createdBy;

    @Column(name = "modified_date")
    @LastModifiedDate
    @Temporal(TIMESTAMP)
    @NotAudited
    private Date modifiedDate;

    @Column(name = "modified_by")
    @LastModifiedBy
    @NotAudited
    private String modifiedBy;

    /**
     * This property is used for hibernate optimistic locking mechanism.
     */
    @JsonIgnore
    @Column(name = "version", nullable = false)
    @Version
    private Long version;


    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
