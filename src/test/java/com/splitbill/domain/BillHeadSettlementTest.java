package com.splitbill.domain;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class BillHeadSettlementTest {
    private final AppUser user = new AppUser("payer", "password");

    @Test
    void computesSettlementFromAllDetailsAndDerivedServiceCharge() {
        BillHead billHead = new BillHead("Dinner", user);
        billHead.addDetail(new BillDetail(new BigDecimal("10.00"), user));
        billHead.addDetail(new BillDetail(new BigDecimal("12.50"), user));

        assertEquals(2, billHead.getServiceChargePercentage());
        assertEquals(new BigDecimal("22.95"), billHead.getBillDetailsSum());
        assertEquals(new BigDecimal("10.20"), billHead.getDetails().get(0).getAmountToPay());
        assertEquals(new BigDecimal("12.75"), billHead.getDetails().get(1).getAmountToPay());
    }

    @Test
    void getterReflectsDetailAmountChangesWithoutRecalculationCall() {
        BillHead billHead = new BillHead("Dinner", user);
        BillDetail detail = new BillDetail(new BigDecimal("10.00"), user);
        billHead.addDetail(detail);

        detail.setAmountToPay(new BigDecimal("15.25"));

        assertEquals(new BigDecimal("15.25"), billHead.getBillDetailsSum());
    }

    @Test
    void requiresAnAssignedUser() {
        assertThrows(NullPointerException.class, () -> new BillHead("Dinner", null));
    }
}
