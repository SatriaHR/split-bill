package com.splitbill.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class BillDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "bill_head_id", nullable = false) private BillHead billHead;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private AppUser user;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amountToPay;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amountPaid = BigDecimal.ZERO;

    protected BillDetail() { }
    public BillDetail(BigDecimal amountToPay, AppUser user) { this.amountToPay = amountToPay; this.user = user; }
    public Long getId() { return id; }
    public BillHead getBillHead() { return billHead; }
    public void setBillHead(BillHead billHead) { this.billHead = billHead; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public BigDecimal getAmountToPay() { return amountToPay; }
    public void setAmountToPay(BigDecimal amountToPay) { this.amountToPay = amountToPay; }
    public void applyServiceCharge(Integer percentage) {
        this.amountToPay = BillHead.amountWithServiceCharge(amountToPay, percentage);
    }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public BigDecimal outstanding() { return amountToPay.subtract(amountPaid).max(BigDecimal.ZERO); }
}
