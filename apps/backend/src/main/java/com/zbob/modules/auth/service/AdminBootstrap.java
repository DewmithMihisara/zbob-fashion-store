package com.zbob.modules.auth.service;

import com.zbob.config.AppProperties;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Creates the admin named by ADMIN_EMAIL and ADMIN_PASSWORD if that account does not exist yet. */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppProperties.Admin admin;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminBootstrap(AppProperties properties, UserRepository users, PasswordEncoder encoder) {
        this.admin = properties.admin();
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(admin.email()) || !StringUtils.hasText(admin.password())) {
            return;
        }
        if (users.existsByEmailIgnoreCase(admin.email())) {
            return;
        }
        users.save(new User(admin.email().toLowerCase(), encoder.encode(admin.password()), "Administrator", Role.ADMIN));
        log.info("Created first admin account");
    }
}
