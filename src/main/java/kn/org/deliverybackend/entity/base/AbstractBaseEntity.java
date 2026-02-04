package kn.org.deliverybackend.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@MappedSuperclass
public abstract class AbstractBaseEntity<T>
    implements AuditableInterface, SoftDeletableInterface, Identifiable<T>, Serializable {

    private Long createdBy;

    @Column(columnDefinition = "timestamp")
    @CreationTimestamp
    private Date createdAt;

    private Long updatedBy;

    @Column(columnDefinition = "timestamp")
    @UpdateTimestamp
    private Date updatedAt;

    private Long deletedBy;

    @Column(columnDefinition = "timestamp")
    private Date deletedAt;

    @ColumnDefault("false")
    private Boolean deleted = Boolean.FALSE;

    @ColumnDefault("0")
    @Version
    private Long version = 0L;

    public Boolean getDeleted() {
        return deleted != null && deleted;
    }

    @PrePersist
    public void onCreate() {
        this.setDeleted(Boolean.FALSE);
        this.setVersion(0L);
    }

    @PreUpdate
    protected void onUpdate() {
    }
}
