package com.splitbill.web;

import java.util.stream.StreamSupport;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.splitbill.domain.AppUser;
import com.splitbill.service.BillDetailService;
import com.splitbill.service.PaymentService;
import com.splitbill.service.UserService;
import com.splitbill.web.dto.Requests;
import com.splitbill.web.dto.Responses;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;
    private final BillDetailService details;
    private final PaymentService payments;
    public UserController(UserService users, BillDetailService details, PaymentService payments) {
        this.users = users;
        this.details = details;
        this.payments = payments;
    }
    @GetMapping public ResponseEntity<?> all() { return ResponseHandler.response(HttpStatus.OK, "Users retrieved successfully", StreamSupport.stream(users.all().spliterator(), false).map(Responses::user).toList()); }
    @GetMapping("/{id}") public ResponseEntity<Responses.ApiResponse<Responses.User>> get(@PathVariable Long id) { return ResponseHandler.response(HttpStatus.OK, "User retrieved successfully", Responses.user(users.get(id))); }
    @PostMapping("/{id}/top-up") public ResponseEntity<Responses.ApiResponse<Responses.User>> topUp(@PathVariable Long id, @Valid @RequestBody Requests.Amount r, @AuthenticationPrincipal AppUser current) { requireSelf(id, current); return ResponseHandler.response(HttpStatus.OK, "Account topped up successfully", Responses.user(users.topUp(id, r.amount()))); }
    @PostMapping("/{id}/withdraw") public ResponseEntity<Responses.ApiResponse<Responses.User>> withdraw(@PathVariable Long id, @Valid @RequestBody Requests.Amount r, @AuthenticationPrincipal AppUser current) { requireSelf(id, current); return ResponseHandler.response(HttpStatus.OK, "Withdrawal completed successfully", Responses.user(users.withdraw(id, r.amount()))); }
    @GetMapping("/{id}/bill-details/unpaid") public ResponseEntity<?> unpaidBillDetails(@PathVariable Long id, @AuthenticationPrincipal AppUser current) { requireSelf(id, current); return ResponseHandler.response(HttpStatus.OK, "Unpaid bill details retrieved successfully", StreamSupport.stream(details.unpaidForUser(id).spliterator(), false).map(Responses::detail).toList()); }
    @GetMapping("/{id}/payments") public ResponseEntity<?> paymentHistory(@PathVariable Long id, @AuthenticationPrincipal AppUser current) { requireSelf(id, current); return ResponseHandler.response(HttpStatus.OK, "Payment history retrieved successfully", StreamSupport.stream(payments.forUser(id).spliterator(), false).map(Responses::payment).toList()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUser current) { requireSelf(id, current); users.delete(id); }
    private void requireSelf(Long id, AppUser current) { if (!id.equals(current.getId())) throw new ApiException(HttpStatus.FORBIDDEN, "Users may only modify their own account"); }
}
