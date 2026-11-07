package com.zbob.modules.contact.controller;

import com.zbob.common.ratelimit.RateLimits;
import com.zbob.modules.contact.dto.ContactRequest;
import com.zbob.modules.contact.dto.NewsletterRequest;
import com.zbob.modules.contact.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ContactController {

    private final ContactService contact;
    private final RateLimits limits;

    public ContactController(ContactService contact, RateLimits limits) {
        this.contact = contact;
        this.limits = limits;
    }

    @PostMapping("/contact")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void submit(@Valid @RequestBody ContactRequest request, HttpServletRequest http) {
        RateLimits.enforce(limits.formsPerIp().tryAcquire("forms:" + http.getRemoteAddr()));
        contact.submit(request);
    }

    @PostMapping("/newsletter")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void subscribe(@Valid @RequestBody NewsletterRequest request, HttpServletRequest http) {
        RateLimits.enforce(limits.formsPerIp().tryAcquire("forms:" + http.getRemoteAddr()));
        contact.subscribe(request);
    }
}
