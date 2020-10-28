package ir.sahab.rest.common.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ir.sahab.rest.common.audit.CustomAuditingTest.RestServiceSetup;
import ir.sahab.rest.common.security.Authenticator;
import ir.sahab.rest.common.security.EnableCustomSecurity;
import ir.sahab.rest.common.testapp.TestOrderController;
import ir.sahab.rest.common.testapp.TestOrderEntity;
import ir.sahab.rest.common.testapp.TestOrderRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RestServiceSetup.class})
@AutoConfigureMockMvc
public class CustomAuditingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditReader auditReader;
    @Autowired
    private TestOrderRepository testOrderRepository;

    @Test
    public void testAudit() throws Exception {
        // Check insert
        TestOrderEntity entity1 = new TestOrderEntity(1L, "Meat", 1);
        mockMvc.perform(post(TestOrderController.REST_BASE_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header("username", "saeed")
                .contentType(MediaType.APPLICATION_JSON)
                .content(entity1.toJson()))
                .andExpect(status().isOk());
        checkHistory(entity1, 1, true);
        checkAuditingFields(entity1.getId(), 0, "saeed", "saeed", RevisionType.ADD);

        // Check update
        entity1.setName("Meat2");
        mockMvc.perform(put(TestOrderController.REST_BASE_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header("username", "ali")
                .contentType(MediaType.APPLICATION_JSON)
                .content(entity1.toJson()))
                .andExpect(status().isOk());
        checkHistory(entity1, 2, true);
        checkAuditingFields(entity1.getId(), 1, "saeed", "ali", RevisionType.MOD);

        // Check delete
        mockMvc.perform(delete(TestOrderController.REST_BASE_PATH + "/" + entity1.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header("username", "hamid"))
                .andExpect(status().isOk());
        checkHistory(entity1, 3, false);
        checkAuditingFields(entity1.getId(), 1, "saeed", "hamid", RevisionType.DEL);
    }

    private void checkAuditingFields(Long id, long version, String createdBy, String updatedBy,
            RevisionType revisionType) {
        if (revisionType != RevisionType.DEL) {
            Optional<TestOrderEntity> entity = testOrderRepository.findById(id);
            assertTrue(entity.isPresent());
            TestOrderEntity dbOrder = entity.get();
            assertEquals(version, (long) dbOrder.getVersion());
            assertNotNull(dbOrder.getCreatedDate());
            assertEquals(createdBy, dbOrder.getCreatedBy());
            assertNotNull(dbOrder.getModifiedDate());
            assertEquals(updatedBy, dbOrder.getModifiedBy());
        } else {
            AuditQuery q = auditReader.createQuery().forRevisionsOfEntity(TestOrderEntity.class, false, true);
            @SuppressWarnings("unchecked")
            List<Object[]> resultList = q.add(AuditEntity.id().eq(id)).getResultList();
            RevisionEntity revisionEntity = auditReader.findRevision(RevisionEntity.class, resultList.size());
            assertEquals(updatedBy, revisionEntity.getUsername());
        }
    }

    private void checkHistory(TestOrderEntity entity, int historySize, boolean checkEntity) {
        List<TestOrderEntity> historyRecords = getHistoryList(entity.getId());
        assertEquals(historySize, historyRecords.size());
        if (checkEntity) {
            assertEquals(entity, historyRecords.get(historyRecords.size() - 1));
        }
    }

    private List<TestOrderEntity> getHistoryList(Long id) {
        AuditQuery q = auditReader.createQuery().forRevisionsOfEntity(TestOrderEntity.class, true, true);
        q.add(AuditEntity.id().eq(id));
        @SuppressWarnings("unchecked")
        List<TestOrderEntity> result = Collections.unmodifiableList(q.getResultList());
        return result;
    }

    @SpringBootApplication(scanBasePackages = "ir.sahab.rest.common.testapp")
    @PropertySource("classpath:rest-commons-test.properties")
    @EnableJpaRepositories(basePackages = "ir.sahab.rest.common.testapp")
    @EntityScan(basePackages = "ir.sahab.rest.common.testapp")
    @EnableCustomAuditing(schema = TestOrderEntity.SCHEMA)
    // It is not necessary to use the @EnableCustomSecurity. The custom auditing module just needs to find the user in
    // security context to fill the createdBy and modifyBy fields.
    // The @EnableCustomSecurity is just one of the approaches for this reason. If you haven't security mechanism these
    // fields won't fill.
    @EnableCustomSecurity(
            applicationBasePathPattern = "/api/**",
            authenticator = TestAuthenticator.class)
    public static class RestServiceSetup {
    }


    public static class TestAuthenticator implements Authenticator {

        @Override
        public Authentication authenticate(HttpServletRequest request) throws AuthenticationException {
            return new UsernamePasswordAuthenticationToken(request.getHeader("username"), "password");
        }
    }
}
