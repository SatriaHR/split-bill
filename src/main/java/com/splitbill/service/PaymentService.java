package com.splitbill.service;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.splitbill.domain.AppUser;
import com.splitbill.domain.BillDetail;
import com.splitbill.domain.BillHead;
import com.splitbill.domain.PaymentHistory;
import com.splitbill.repository.PaymentHistoryRepository;
import com.splitbill.web.ApiException;

@Service
public class PaymentService {
    private final PaymentHistoryRepository history;
    private final UserService users;
    private final BillDetailService details;
    
    public PaymentService(PaymentHistoryRepository history, UserService users, BillDetailService details) { this.history = history; this.users = users; this.details = details; }
    
    @Transactional
    public PaymentHistory pay(Long payerId, Long detailId) {
        AppUser payer = users.locked(payerId);
        BillDetail detail = details.locked(detailId);
        if (!detail.getUser().getId().equals(payerId)) throw new ApiException(HttpStatus.FORBIDDEN, "Only the bill detail user can pay this detail");
        BigDecimal amount = detail.outstanding();
        if (amount.signum() <= 0) throw new ApiException(HttpStatus.CONFLICT, "Bill detail is already paid");
        if (payer.getBalance().compareTo(amount) < 0) throw new ApiException(HttpStatus.CONFLICT, "Insufficient balance");
        AppUser creator = users.locked(detail.getBillHead().getCreatedBy().getId());
        BigDecimal transferred = BillHead.amountWithoutServiceCharge(amount, detail.getBillHead().getServiceChargePercentage());
        BigDecimal serviceCharge = amount.subtract(transferred);
        payer.setBalance(payer.getBalance().subtract(amount));
        detail.setAmountPaid(detail.getAmountPaid().add(amount));
        creator.setBalance(creator.getBalance().add(transferred));
        return history.save(new PaymentHistory(payer, detail, amount, serviceCharge, transferred));
    }
    @Transactional(readOnly = true) public Iterable<PaymentHistory> all() { return history.findAll(); }
    @Transactional(readOnly = true) public PaymentHistory get(Long id) { return history.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found")); }
    @Transactional(readOnly = true) public Iterable<PaymentHistory> forUser(Long userId) { return history.findByPayerIdOrderByPaidAtDesc(userId); }
    @Transactional(readOnly = true) public PaymentHistory getForUser(Long id, Long userId) {
        return history.findByIdAndPayerId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));
    }
}
