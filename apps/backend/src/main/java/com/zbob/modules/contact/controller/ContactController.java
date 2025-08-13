package com.zbob.modules.contact.controller;

import com.zbob.modules.contact.dto.ContactRequest;
import com.zbob.modules.contact.dto.NewsletterRequest;
import com.zbob.modules.contact.service.ContactService;
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

    public ContactController(ContactService contact) {
        this.contact = contact;
    }

    @PostMapping("/contact")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void submit(@Valid @RequestBody ContactRequest request) {
        contact.submit(request);
    }

    @PostMapping("/newsletter")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void subscribe(@Valid @RequestBody NewsletterRequest request) {
        contact.subscribe(request);
    }
}
