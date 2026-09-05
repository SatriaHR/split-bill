package com.splitbill.service;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.splitbill.domain.BillDetail;
import com.splitbill.domain.BillHead;
import com.splitbill.repository.BillDetailRepository;
import com.splitbill.web.ApiException;
import com.splitbill.web.dto.Requests;

@Service
public class BillDetailService {
    private final BillDetailRepository details;
    private final BillHeadService heads;
    public BillDetailService(BillDetailRepository details, BillHeadService heads) { this.details = details; this.heads = heads; }
    @Transactional
    public BillDetail create(Long headId, Requests.BillDetailCreate request) {
        BillHead billHead = heads.get(headId);
        BillDetail detail = new BillDetail(request.amountToPay(), heads.user(request.userId()));
        billHead.addDetail(detail);
        return details.save(detail);
    }

    @Transactional(readOnly = true)
    public BillDetail get(Long id) {
        return details.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bill detail not found"));
    }

    @Transactional(readOnly = true)
    public Iterable<BillDetail> all() {
        return details.findAll();
    }

    @Transactional(readOnly = true)
    public Iterable<BillDetail> unpaidForUser(Long userId) {
        return details.findUnpaidByUserId(userId);
    }

    @Transactional
    public BillDetail update(Long id, Requests.BillDetailCreate request) {
        BillDetail detail = get(id);
        BigDecimal adjustedAmount = BillHead.amountWithServiceCharge(
                request.amountToPay(), detail.getBillHead().getServiceChargePercentage());

        if (detail.getAmountPaid().compareTo(adjustedAmount) > 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Amount to pay cannot be less than amount already paid");
        }

        detail.setAmountToPay(adjustedAmount);
        detail.setUser(heads.user(request.userId()));
        return detail;
    }

    @Transactional
    public void delete(Long id) {
        BillDetail detail = get(id);
        if (detail.getAmountPaid().signum() > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Paid bill details cannot be deleted");
        }
        details.delete(detail);
    }

    public BillDetail locked(Long id) {
        return details.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bill detail not found"));
    }
}
