package com.splitbill.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

//FetchType.LAZY is a configuration strategy that delays the loading of related entities or collections from the database until they are explicitly accessed in the code. 
//This helps optimize application performance and memory footprint by preventing unnecessary database reads

@Entity
public class PaymentHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "payer_id", nullable = false) private AppUser payer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "bill_detail_id", nullable = false) private BillDetail billDetail;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amountPaid;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal serviceChargeAmount;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amountTransferred;
    @Column(nullable = false, updatable = false) private Instant paidAt;

    protected PaymentHistory() { }
    public PaymentHistory(AppUser payer, BillDetail billDetail, BigDecimal amountPaid, BigDecimal serviceChargeAmount, BigDecimal amountTransferred) { this.payer = payer; this.billDetail = billDetail; this.amountPaid = amountPaid; this.serviceChargeAmount = serviceChargeAmount; this.amountTransferred = amountTransferred; this.paidAt = Instant.now(); }
    public Long getId() { return id; }
    public AppUser getPayer() { return payer; }
    public BillDetail getBillDetail() { return billDetail; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public BigDecimal getServiceChargeAmount() { return serviceChargeAmount; }
    public BigDecimal getAmountTransferred() { return amountTransferred; }
    public Instant getPaidAt() { return paidAt; }
}
