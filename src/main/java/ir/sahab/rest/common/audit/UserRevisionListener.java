package ir.sahab.rest.common.audit;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The revision listener is the class where you need to specify how to populate the additional data into the revision.
 * This class needs to implement the {@link RevisionListener} interface which has only one method called {@link
 * RevisionListener#newRevision}. In this listener we add current user to revision entity.
 */
public class UserRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        RevisionEntity entity = (RevisionEntity) revisionEntity;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            entity.setUsername(authentication.getName());
        }
    }
}