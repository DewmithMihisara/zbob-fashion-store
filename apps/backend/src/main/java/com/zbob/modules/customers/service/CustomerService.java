package com.zbob.modules.customers.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.exception.BadRequestException;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.auth.repository.PasswordResetTokenRepository;
import com.zbob.modules.customers.dto.AddressRequest;
import com.zbob.modules.customers.dto.AddressResponse;
import com.zbob.modules.customers.dto.ChangePasswordRequest;
import com.zbob.modules.customers.dto.CreateCustomerRequest;
import com.zbob.modules.customers.dto.UpdateCustomerRequest;
import com.zbob.modules.customers.entity.CustomerAddress;
import com.zbob.modules.customers.repository.CustomerAddressRepository;
import com.zbob.modules.notifications.EmailService;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.users.dto.UserResponse;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.mapper.UserMapper;
import com.zbob.modules.users.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final UserRepository users;
    private final CustomerAddressRepository addresses;
    private final OrderRepository orders;
    private final PasswordEncoder encoder;
    private final PasswordResetTokenRepository resetTokens;
    private final EmailService emails;

    public CustomerService(UserRepository users, CustomerAddressRepository addresses, OrderRepository orders,
            PasswordEncoder encoder, PasswordResetTokenRepository resetTokens, EmailService emails) {
        this.users = users;
        this.addresses = addresses;
        this.orders = orders;
        this.encoder = encoder;
        this.resetTokens = resetTokens;
        this.emails = emails;
    }

    @Transactional(readOnly = true)
    public UserResponse profile(Long userId) {
        return UserMapper.toResponse(findUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, String fullName) {
        User user = findUser(userId);
        user.setFullName(fullName.trim());
        return UserMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUser(userId);
        if (!encoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Your current password is not correct");
        }
        user.setPasswordHash(encoder.encode(request.newPassword()));
        resetTokens.invalidateForUser(userId, Instant.now());
        emails.passwordChanged(user);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> addresses(Long userId) {
        return addresses.findByUserIdOrderByIdAsc(userId).stream().map(CustomerService::toResponse).toList();
    }

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        CustomerAddress address = new CustomerAddress(userId);
        apply(address, request);
        return toResponse(addresses.save(address));
    }

    /** Looked up by owner as well as id, so one customer can never reach another's address. */
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        CustomerAddress address = findAddress(userId, addressId);
        apply(address, request);
        return toResponse(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        addresses.delete(findAddress(userId, addressId));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listCustomers(Pageable pageable) {
        return PageResponse.of(users.findByRoleIn(List.of(Role.CUSTOMER), pageable), UserMapper::toResponse);
    }

    @Transactional
    public UserResponse createCustomer(CreateCustomerRequest request) {
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User(email, encoder.encode(request.password()), request.fullName().trim(), Role.CUSTOMER);
        return UserMapper.toResponse(users.save(user));
    }

    @Transactional
    public UserResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        User user = findCustomer(id);
        String email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictException("An account with this email already exists");
        }
        user.setEmail(email);
        user.setFullName(request.fullName().trim());
        user.setActive(request.active());
        return UserMapper.toResponse(user);
    }

    @Transactional
    public UserResponse setCustomerActive(Long id, boolean active) {
        User user = findCustomer(id);
        user.setActive(active);
        return UserMapper.toResponse(user);
    }

    /** Orders must stay on record, so a customer who has any can only be disabled. */
    @Transactional
    public void deleteCustomer(Long id) {
        User user = findCustomer(id);
        if (orders.existsByUserId(id)) {
            throw new ConflictException("This customer has orders, so the account can only be disabled");
        }
        users.delete(user);
    }

    private User findCustomer(Long id) {
        return users.findById(id)
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private User findUser(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private CustomerAddress findAddress(Long userId, Long addressId) {
        return addresses.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
    }

    private static void apply(CustomerAddress a, AddressRequest r) {
        a.setRecipient(r.recipient());
        a.setLine1(r.line1());
        a.setLine2(r.line2());
        a.setCity(r.city());
        a.setRegion(r.region());
        a.setPostalCode(r.postalCode());
        a.setCountry(r.country());
        a.setPhone(r.phone());
    }

    private static AddressResponse toResponse(CustomerAddress a) {
        return new AddressResponse(a.getId(), a.getRecipient(), a.getLine1(), a.getLine2(), a.getCity(), a.getRegion(),
                a.getPostalCode(), a.getCountry(), a.getPhone());
    }
}
