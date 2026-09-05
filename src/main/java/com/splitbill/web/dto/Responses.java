package com.splitbill.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.splitbill.domain.AppUser;
import com.splitbill.domain.PaymentHistory;

public final class Responses {
    private Responses() { }
    public record ApiResponse<T>(Instant timestamp, int status, String message, T data) { }
    public record User(Long id, String username, BigDecimal balance) { }
    public record Auth(User user, Instant tokenExpiration) { }
    public record BillDetail(Long id, Long billHeadId, Long userId, BigDecimal amountToPay, BigDecimal amountPaid, BigDecimal outstanding) { }
    public record BillHead(Long id, Instant creationDate, String description, BigDecimal billDetailsSum, Integer serviceChargePercentage, Long createdById, List<BillDetail> details) { }
    public record Payment(Long id, Long payerId, Long billDetailId, BigDecimal amountPaid, BigDecimal serviceChargeAmount, BigDecimal amountTransferred, Instant paidAt) { }

    public static User user(AppUser u) { return new User(u.getId(), u.getUsername(), u.getBalance()); }
    public static BillDetail detail(com.splitbill.domain.BillDetail d) { return new BillDetail(d.getId(), d.getBillHead().getId(), d.getUser().getId(), d.getAmountToPay(), d.getAmountPaid(), d.outstanding()); }
    public static BillHead head(com.splitbill.domain.BillHead h) { return new BillHead(h.getId(), h.getCreationDate(), h.getDescription(), h.getBillDetailsSum(), h.getServiceChargePercentage(), h.getCreatedBy().getId(), h.getDetails().stream().map(Responses::detail).toList()); }
    public static Payment payment(PaymentHistory p) { return new Payment(p.getId(), p.getPayer().getId(), p.getBillDetail().getId(), p.getAmountPaid(), p.getServiceChargeAmount(), p.getAmountTransferred(), p.getPaidAt()); }
}
