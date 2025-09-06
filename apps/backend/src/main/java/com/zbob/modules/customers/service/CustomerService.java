package com.zbob.modules.customers.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.customers.dto.AddressRequest;
import com.zbob.modules.customers.dto.AddressResponse;
import com.zbob.modules.customers.entity.CustomerAddress;
import com.zbob.modules.customers.repository.CustomerAddressRepository;
import com.zbob.modules.users.dto.UserResponse;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.mapper.UserMapper;
import com.zbob.modules.users.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final UserRepository users;
    private final CustomerAddressRepository addresses;

    public CustomerService(UserRepository users, CustomerAddressRepository addresses) {
        this.users = users;
        this.addresses = addresses;
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
    public UserResponse setCustomerActive(Long id, boolean active) {
        User user = users.findById(id)
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        user.setActive(active);
        return UserMapper.toResponse(user);
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
