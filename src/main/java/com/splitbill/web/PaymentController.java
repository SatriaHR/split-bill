package com.splitbill.web;

import java.util.stream.StreamSupport;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.splitbill.domain.AppUser;
import com.splitbill.service.PaymentService;
import com.splitbill.web.dto.Responses;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService payments;

    public PaymentController(PaymentService payments) { this.payments = payments; }

    @PostMapping("/bill-details/{detailId}") 
    public ResponseEntity<Responses.ApiResponse<Responses.Payment>> pay(@PathVariable Long detailId, @AuthenticationPrincipal AppUser user) { 
        return ResponseHandler.response(HttpStatus.OK, "Payment completed successfully", Responses.payment(payments.pay(user.getId(), detailId))); 
    }

    @GetMapping 
    public ResponseEntity<?> all(@AuthenticationPrincipal AppUser user) { 
        return ResponseHandler.response(HttpStatus.OK, "Payments retrieved successfully", StreamSupport.stream(payments.forUser(user.getId()).spliterator(), false).map(Responses::payment).toList()); 
    }
    
    @GetMapping("/{id}") 
    public ResponseEntity<Responses.ApiResponse<Responses.Payment>> get(@PathVariable Long id, @AuthenticationPrincipal AppUser user) { return ResponseHandler.response(HttpStatus.OK, "Payment retrieved successfully", Responses.payment(payments.getForUser(id, user.getId()))); }
}
