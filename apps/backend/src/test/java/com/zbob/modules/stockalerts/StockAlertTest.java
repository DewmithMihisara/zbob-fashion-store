package com.zbob.modules.stockalerts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.products.repository.ProductRepository;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import com.zbob.security.JwtService;
import com.zbob.support.RecordingEmailSender;
import com.zbob.support.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Not transactional, because emails go out after a commit. Removes what it creates. */
@SpringBootTest
@AutoConfigureMockMvc
@Import({RecordingEmailSender.Config.class, TestData.class})
class StockAlertTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private RecordingEmailSender emails;

    @Autowired
    private ProductVariantRepository variants;

    @Autowired
    private ProductRepository products;

    @Autowired
    private StockAlertRepository alerts;

    @Autowired
    private UserRepository users;

    @Autowired
    private JwtService jwt;

    private ProductVariant soldOut;
    private User staff;
    private String email;

    @BeforeEach
    void setUp() {
        long n = System.nanoTime();
        soldOut = data.variant("Alert Tee " + n, 1000, 0);
        staff = users.save(new User("alert-staff-" + n + "@zbob.test", "hash", "Staff", Role.STAFF));
        email = "waiting-" + n + "@example.com";
    }

    @AfterEach
    void tearDown() {
        alerts.findAll().stream().filter(a -> a.getEmail().equals(email)).forEach(alerts::delete);
        products.deleteById(soldOut.getProduct().getId());
        users.delete(staff);
    }

    private ResultActions subscribe(String address) throws Exception {
        return mvc.perform(post("/api/v1/stock-alerts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":%d,\"email\":\"%s\"}".formatted(soldOut.getId(), address)));
    }

    private ResultActions setStock(int quantity) throws Exception {
        return mvc.perform(patch("/api/v1/admin/variants/" + soldOut.getId() + "/stock")
                .header("Authorization", "Bearer " + jwt.issue(staff.getId(), staff.getRole()))
                .contentType(MediaType.APPLICATION_JSON).content("{\"stockQuantity\":%d}".formatted(quantity)));
    }

    @Test
    void restockEmailsEachWaitingShopperOnce() throws Exception {
        subscribe(email).andExpect(status().isAccepted());
        subscribe(email.toUpperCase()).andExpect(status().isAccepted());
        assertThat(alerts.countByVariantIdAndNotifiedAtIsNull(soldOut.getId())).isEqualTo(1);

        setStock(4).andExpect(status().isOk());

        assertThat(emails.to(email)).singleElement().satisfies(m -> {
            assertThat(m.subject()).endsWith("is back in stock");
            assertThat(m.body()).contains("/shop/" + soldOut.getProduct().getSlug());
        });

        setStock(0);
        setStock(5);
        assertThat(emails.to(email)).hasSize(1);
    }

    @Test
    void anInStockItemCanNotBeWaitedFor() throws Exception {
        setStock(3);

        subscribe(email).andExpect(status().isConflict());
    }

    @Test
    void anInvalidEmailIsRefused() throws Exception {
        subscribe("not-an-email").andExpect(status().isBadRequest());
    }
}
