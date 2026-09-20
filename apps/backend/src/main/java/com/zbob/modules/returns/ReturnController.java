package com.zbob.modules.returns;

import com.zbob.common.dto.PageResponse;
import com.zbob.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReturnController {

    public record CreateReturnRequest(@NotBlank @Size(max = 1000) String reason) {
    }

    public record DecideReturnRequest(@NotNull ReturnStatus status, @Size(max = 1000) String staffNote) {
    }

    private final ReturnService returns;

    public ReturnController(ReturnService returns) {
        this.returns = returns;
    }

    @PostMapping("/api/v1/orders/{orderId}/returns")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    ReturnService.ReturnResponse request(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long orderId,
            @Valid @RequestBody CreateReturnRequest request) {
        return returns.request(user.id(), orderId, request.reason());
    }

    @GetMapping("/api/v1/orders/{orderId}/returns")
    @PreAuthorize("hasRole('CUSTOMER')")
    List<ReturnService.ReturnResponse> forOrder(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long orderId) {
        return returns.forCustomerOrder(user.id(), orderId);
    }

    @GetMapping("/api/v1/admin/returns")
    PageResponse<ReturnService.ReturnResponse> list(@RequestParam(required = false) ReturnStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return returns.list(status, pageable);
    }

    @PatchMapping("/api/v1/admin/returns/{id}")
    ReturnService.ReturnResponse decide(@PathVariable Long id, @Valid @RequestBody DecideReturnRequest request) {
        return returns.decide(id, request.status(), request.staffNote());
    }
}
