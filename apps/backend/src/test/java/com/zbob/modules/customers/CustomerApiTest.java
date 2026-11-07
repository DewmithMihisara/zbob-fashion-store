package com.zbob.modules.customers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.customers.entity.CustomerAddress;
import com.zbob.modules.customers.repository.CustomerAddressRepository;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import com.zbob.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerApiTest {

    private static final String ADDRESS = """
            {"recipient":"Sam","line1":"1 Main St","city":"Town","postalCode":"1000","country":"LK"}""";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private CustomerAddressRepository addresses;

    @Autowired
    private JwtService jwt;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = users.save(new User("alice@zbob.test", "hash", "Alice", Role.CUSTOMER));
        bob = users.save(new User("bob@zbob.test", "hash", "Bob", Role.CUSTOMER));
    }

    private String bearer(User user) {
        return "Bearer " + jwt.issue(user.getId(), user.getRole());
    }

    private CustomerAddress saveAddress(User owner) {
        CustomerAddress address = new CustomerAddress(owner.getId());
        address.setRecipient("Owner");
        address.setLine1("1 Main St");
        address.setCity("Town");
        address.setPostalCode("1000");
        address.setCountry("LK");
        return addresses.save(address);
    }

    @Test
    void customerManagesOwnProfileAndAddresses() throws Exception {
        mvc.perform(get("/api/v1/me").header("Authorization", bearer(alice)))
                .andExpect(jsonPath("$.email").value("alice@zbob.test"));
        mvc.perform(post("/api/v1/me/addresses").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(ADDRESS))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/v1/me/addresses").header("Authorization", bearer(alice)))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void customerCannotTouchAnotherCustomersAddress() throws Exception {
        CustomerAddress bobs = saveAddress(bob);
        String url = "/api/v1/me/addresses/" + bobs.getId();

        mvc.perform(put(url).header("Authorization", bearer(alice)).contentType(MediaType.APPLICATION_JSON).content(ADDRESS))
                .andExpect(status().isNotFound());
        mvc.perform(delete(url).header("Authorization", bearer(alice))).andExpect(status().isNotFound());
        assertThat(addresses.findById(bobs.getId())).isPresent();
    }

    @Test
    void invalidCountryIsRejected() throws Exception {
        mvc.perform(post("/api/v1/me/addresses").header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON).content(ADDRESS.replace("LK", "Sri Lanka")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousIsRejected() throws Exception {
        mvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void staffCanListAndDisableCustomers() throws Exception {
        User staff = users.save(new User("s@zbob.test", "hash", "Staff", Role.STAFF));

        mvc.perform(get("/api/v1/admin/customers").header("Authorization", bearer(staff)))
                .andExpect(jsonPath("$.content", hasSize(2)));
        mvc.perform(patch("/api/v1/admin/customers/" + alice.getId()).param("active", "false")
                        .header("Authorization", bearer(staff)))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void customerCannotReachAdminCustomerList() throws Exception {
        mvc.perform(get("/api/v1/admin/customers").header("Authorization", bearer(alice)))
                .andExpect(status().isForbidden());
    }
}
