package com.splitbill.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class BillHead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Instant creationDate;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private Integer serviceChargePercentage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private AppUser createdBy;

    @OneToMany(mappedBy = "billHead", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<BillDetail> details = new ArrayList<>();

    protected BillHead() {
    }

    public BillHead(String description, AppUser createdBy) {
        this.description = description;
        this.serviceChargePercentage = calculateServiceChargePercentage();
        this.createdBy = Objects.requireNonNull(createdBy, "Bill head must have an assigned user");
        this.creationDate = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBillDetailsSum() {
        BigDecimal total = BigDecimal.ZERO;
        for (BillDetail detail : details) {
            total = total.add(detail.getAmountToPay());
        }
        return total;
    }

    public Integer getServiceChargePercentage() {
        return serviceChargePercentage;
    }

    public AppUser getCreatedBy() {
        return createdBy;
    }

    public List<BillDetail> getDetails() {
        return details;
    }

    public void addDetail(BillDetail detail) {
        detail.applyServiceCharge(serviceChargePercentage);
        detail.setBillHead(this);
        details.add(detail);
    }

    public static Integer calculateServiceChargePercentage() {
        return "satriahr".chars().sum() % 10;
    }

    public static BigDecimal amountWithServiceCharge(BigDecimal amount, Integer percentage) {
        BigDecimal multiplier = BigDecimal.ONE.add(BigDecimal.valueOf(percentage).movePointLeft(2));
        return amount.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal amountWithoutServiceCharge(BigDecimal amount, Integer percentage) {
        BigDecimal divisor = BigDecimal.ONE.add(BigDecimal.valueOf(percentage).movePointLeft(2));
        return amount.divide(divisor, 2, RoundingMode.HALF_UP);
    }
}
