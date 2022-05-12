package ir.sahab.rest.common.testapp;

import ir.sahab.rest.common.apierror.ApiException;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class to do CRUD for {@link TestOrderEntity}.
 */
@Service
public class TestOrderService {

    private final TestOrderRepository testOrderRepository;

    @Autowired
    public TestOrderService(TestOrderRepository testOrderRepository) {
        this.testOrderRepository = testOrderRepository;
    }

    public TestOrderEntity add(TestOrderEntity order) {
        return testOrderRepository.save(order);
    }

    public TestOrderEntity update(TestOrderEntity order) {
        TestOrderEntity dbOrder = findById(order.getId());
        dbOrder.setName(order.getName());
        dbOrder.setCount(order.getCount());
        return testOrderRepository.save(dbOrder);
    }

    public void delete(long id) {
        testOrderRepository.delete(findById(id));
    }

    public TestOrderEntity findById(long id) {
        return testOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order doesn't exist with ID: " + id));
    }

    public List<TestOrderEntity> findByName(String name) throws ApiException {
        List<TestOrderEntity> result = testOrderRepository.findByName(name);
        if (result.isEmpty()) {
            throw new ApiException(OrderErrorCode.NOT_AVAILABLE_IN_STORE, name);
        }
        return result;
    }
}
