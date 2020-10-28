package ir.sahab.rest.common.audit;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.envers.DefaultRevisionEntity;

/**
 * The default revision entity for historical changes on an entity does not contain the user name that applies a change.
 * This class adds this extra field. Also the assigned listener fills the user name field automatically. Schema of the
 * table will be added to annotation automatically when enabling custom editing.
 *
 * @see EnableCustomAuditing
 */
@Entity
@org.hibernate.envers.RevisionEntity(UserRevisionListener.class)
@Table(name = "revision_entity")
public class RevisionEntity extends DefaultRevisionEntity {

    private static final long serialVersionUID = 1L;

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
