package ir.sahab.rest.common.testapp;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring JPA repository for {@link TestOrderEntity} entity.
 */
public interface TestOrderRepository extends JpaRepository<TestOrderEntity, Long> {

    List<TestOrderEntity> findByName(String name);
}
