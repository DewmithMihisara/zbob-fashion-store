package com.zbob.modules.contact.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.contact.dto.AdminMessageRequest;
import com.zbob.modules.contact.dto.ContactMessageResponse;
import com.zbob.modules.contact.dto.ContactRequest;
import com.zbob.modules.contact.dto.NewsletterRequest;
import com.zbob.modules.contact.dto.SubscriberResponse;
import com.zbob.modules.contact.entity.ContactMessage;
import com.zbob.modules.contact.entity.NewsletterSubscriber;
import com.zbob.modules.contact.repository.ContactMessageRepository;
import com.zbob.modules.contact.repository.NewsletterSubscriberRepository;
import java.util.Locale;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ContactService {

    private final ContactMessageRepository messages;
    private final NewsletterSubscriberRepository subscribers;

    public ContactService(ContactMessageRepository messages, NewsletterSubscriberRepository subscribers) {
        this.messages = messages;
        this.subscribers = subscribers;
    }

    /** Bot submissions, spotted by the hidden field, get the normal response but are not saved. */
    @Transactional
    public void submit(ContactRequest request) {
        if (StringUtils.hasText(request.website())) {
            return;
        }
        messages.save(new ContactMessage(request.name().trim(), request.email().trim(), request.subject(), request.message()));
    }

    /** Subscribing twice succeeds quietly, so the response does not reveal who is already on the list. */
    @Transactional
    public void subscribe(NewsletterRequest request) {
        String email = normalize(request.email());
        if (!subscribers.existsByEmail(email)) {
            subscribers.save(new NewsletterSubscriber(email));
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ContactMessageResponse> listMessages(Pageable pageable) {
        return PageResponse.of(messages.findAll(pageable), ContactService::toResponse);
    }

    @Transactional
    public ContactMessageResponse createMessage(AdminMessageRequest request) {
        ContactMessage message = new ContactMessage(request.name().trim(), request.email().trim(), request.subject(), request.message());
        message.setHandled(request.handled());
        return toResponse(messages.save(message));
    }

    @Transactional
    public ContactMessageResponse updateMessage(Long id, AdminMessageRequest request) {
        ContactMessage message = find(id);
        message.setDetails(request.name().trim(), request.email().trim(), request.subject(), request.message());
        message.setHandled(request.handled());
        return toResponse(message);
    }

    @Transactional
    public ContactMessageResponse setHandled(Long id, boolean handled) {
        ContactMessage message = find(id);
        message.setHandled(handled);
        return toResponse(message);
    }

    @Transactional
    public void deleteMessage(Long id) {
        messages.delete(find(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<SubscriberResponse> listSubscribers(Pageable pageable) {
        return PageResponse.of(subscribers.findAll(pageable), ContactService::toResponse);
    }

    @Transactional
    public SubscriberResponse createSubscriber(NewsletterRequest request) {
        String email = normalize(request.email());
        if (subscribers.existsByEmail(email)) {
            throw new ConflictException("This email is already subscribed");
        }
        return toResponse(subscribers.save(new NewsletterSubscriber(email)));
    }

    @Transactional
    public SubscriberResponse updateSubscriber(Long id, NewsletterRequest request) {
        NewsletterSubscriber subscriber = findSubscriber(id);
        String email = normalize(request.email());
        if (subscribers.existsByEmailAndIdNot(email, id)) {
            throw new ConflictException("This email is already subscribed");
        }
        subscriber.setEmail(email);
        return toResponse(subscriber);
    }

    @Transactional
    public void deleteSubscriber(Long id) {
        subscribers.delete(findSubscriber(id));
    }

    private NewsletterSubscriber findSubscriber(Long id) {
        return subscribers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Subscriber", id));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static SubscriberResponse toResponse(NewsletterSubscriber s) {
        return new SubscriberResponse(s.getId(), s.getEmail(), s.getCreatedAt());
    }

    private ContactMessage find(Long id) {
        return messages.findById(id).orElseThrow(() -> new ResourceNotFoundException("Message", id));
    }

    private static ContactMessageResponse toResponse(ContactMessage m) {
        return new ContactMessageResponse(m.getId(), m.getName(), m.getEmail(), m.getSubject(), m.getMessage(),
                m.isHandled(), m.getCreatedAt());
    }
}
