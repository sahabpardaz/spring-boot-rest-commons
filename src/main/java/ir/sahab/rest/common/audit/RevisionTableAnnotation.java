package ir.sahab.rest.common.audit;

import java.lang.annotation.Annotation;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

public class RevisionTableAnnotation implements Table {

    private final String schema;

    public RevisionTableAnnotation(String schema) {
        this.schema = schema;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return RevisionTableAnnotation.class;
    }

    @Override
    public String name() {
        return "revision_entity";
    }

    @Override
    public String catalog() {
        return "";
    }

    @Override
    public String schema() {
        return schema;
    }

    @Override
    public UniqueConstraint[] uniqueConstraints() {
        return new UniqueConstraint[]{};
    }

    @Override
    public Index[] indexes() {
        return new Index[] {};
    }
}