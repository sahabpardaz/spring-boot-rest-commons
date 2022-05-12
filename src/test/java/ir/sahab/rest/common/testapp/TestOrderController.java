package ir.sahab.rest.common.testapp;

import ir.sahab.rest.common.apierror.ApiException;
import java.io.IOException;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that provides methods for testing each aspects of rest-common module.
 */
@RestController
@RequestMapping(TestOrderController.REST_BASE_PATH)
public class TestOrderController {

    public static final String REST_BASE_PATH = "/api/orders/v1";

    private final TestOrderService testOrderService;

    @Autowired
    public TestOrderController(TestOrderService testOrderService) {
        this.testOrderService = testOrderService;
    }

    @PostMapping
    public TestOrderEntity addOrder(@Valid @RequestBody TestOrderEntity order) {
        return testOrderService.add(order);
    }

    @PutMapping
    public TestOrderEntity updateOrder(@Valid @RequestBody TestOrderEntity order) {
        return testOrderService.update(order);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        testOrderService.delete(id);
    }

    @GetMapping("/order/{id}")
    public TestOrderEntity findOrderById(@PathVariable Long id) {
        return testOrderService.findById(id);
    }

    @GetMapping("/searchByName")
    public List<TestOrderEntity> searchByName(@RequestParam String name) throws ApiException {
        return testOrderService.findByName(name);
    }

    /**
     * A fake API just to simulate IOException.
     */
    @GetMapping("/throwIOException")
    public void throwIoException() throws IOException {
        throw new IOException("Connection refused (Connection refused)");
    }

    @PostMapping("/echo")
    public String echo(@RequestBody String body) {
        return body;
    }

    /**
     * A fake API just to simulate a custom (non-familiar) exception from controller.
     */
    @PostMapping("/throwMyException")
    public void throwMyException(@RequestBody TestOrderEntity order) throws MyException {
        if (order.getName() == null) {
            throw new MyException("Name is empty!");
        }
    }

    /**
     * An API provided to check logging the secure parameters.
     */
    @PostMapping("/secureInsert")
    public TestOrderEntity secureInsert(
            @RequestParam(name = "SECURE_ID") Long id,
            @RequestParam String name,
            @RequestParam(name = "SECURE_COUNT") Integer count) {
        return testOrderService.add(new TestOrderEntity(id, name, count));
    }

    /**
     * A fake API just to check the paths that are ignored according to security configuration.
     */
    @GetMapping("/ignored-path")
    public void ignoredPath() {
    }

    /**
     * A fake API just to check the paths that are protected according to security configuration.
     */
    @GetMapping("/protected-api")
    public void protectedApi() {
    }

    /**
     * A fake API just to check the paths that are protected and require a specific permission.
     */
    @GetMapping("/protected-with-permission")
    @PreAuthorize("hasRole('sample.permission')")
    public void protectedWithPermission() {
    }
}
