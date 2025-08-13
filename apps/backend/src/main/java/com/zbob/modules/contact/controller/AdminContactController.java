package com.zbob.modules.contact.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.contact.dto.ContactMessageResponse;
import com.zbob.modules.contact.dto.SubscriberResponse;
import com.zbob.modules.contact.service.ContactService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminContactController {

    private final ContactService contact;

    public AdminContactController(ContactService contact) {
        this.contact = contact;
    }

    @GetMapping("/contact-messages")
    PageResponse<ContactMessageResponse> messages(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return contact.listMessages(pageable);
    }

    @PatchMapping("/contact-messages/{id}")
    ContactMessageResponse setHandled(@PathVariable Long id, @RequestParam boolean handled) {
        return contact.setHandled(id, handled);
    }

    @DeleteMapping("/contact-messages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        contact.deleteMessage(id);
    }

    @GetMapping("/newsletter-subscribers")
    PageResponse<SubscriberResponse> subscribers(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return contact.listSubscribers(pageable);
    }
}
