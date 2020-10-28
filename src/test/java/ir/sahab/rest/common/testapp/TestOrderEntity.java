package ir.sahab.rest.common.testapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import ir.sahab.rest.common.audit.Auditable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "test_entity", schema = "test_schema")
@Audited
public class TestOrderEntity extends Auditable {

    private static final long serialVersionUID = 1L;

    @VisibleForTesting
    public static final String SCHEMA = "test_schema";

    @Id
    private Long id;

    @Column(length = 15)
    private String name;

    @Min(1)
    @Max(10)
    private Integer count;

    public TestOrderEntity() {
    }

    public TestOrderEntity(Long id, String name, Integer count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new AssertionError("Unable to parse DPI entity to JSON!", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TestOrderEntity that = (TestOrderEntity) o;
        return id.equals(that.id) && name.equals(that.name) && Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, count);
    }
}
