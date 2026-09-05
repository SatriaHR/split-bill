package com.splitbill.service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.splitbill.domain.AppUser;
import com.splitbill.domain.BillDetail;
import com.splitbill.domain.BillHead;
import com.splitbill.repository.AppUserRepository;
import com.splitbill.repository.BillHeadRepository;
import com.splitbill.web.ApiException;
import com.splitbill.web.dto.Requests;
import com.splitbill.web.dto.Responses;

@SpringBootTest
@Transactional
@Rollback
class BalanceServiceTest {
    @Autowired private AppUserRepository users;
    @Autowired private BillHeadRepository heads;
    @Autowired private BillHeadService billHeads;
    @Autowired private BillDetailService billDetails;
    @Autowired private PaymentService payments;
    @Autowired private UserService usersService;

    @BeforeEach
    void cleanDatabase() {
        heads.deleteAll();
        users.deleteAll();
    }

    @Test
    void billHeadCanExistWithoutDetails() {
        AppUser creator = saveUser("creator");
        creator.setBalance(new BigDecimal("100.00"));

        Requests.BillHeadCreate request = new Requests.BillHeadCreate("Dinner");
        BillHead billHead = billHeads.create(request, creator.getId());
        System.out.println("REQUEST " + request + " RESPONSE " + Responses.head(billHead));

        assertEquals(new BigDecimal("100.00"), creator.getBalance());
        assertEquals(0, billHead.getDetails().size());
    }

    @Test
    void paymentTransfersPrincipalAndReportsServiceCharge() {
        AppUser creator = saveUser("creator");
        creator.setBalance(new BigDecimal("200.00"));
        BillHead billHead = billHeads.create(new Requests.BillHeadCreate("Dinner"), creator.getId());
        AppUser payer = saveUser("payer");
        payer.setBalance(new BigDecimal("31.00"));
        Requests.BillDetailCreate request = new Requests.BillDetailCreate(new BigDecimal("30.00"), payer.getId());
        BillDetail detail = billDetails.create(billHead.getId(), request);

        var payment = payments.pay(payer.getId(), detail.getId());
        System.out.println("REQUEST " + request + " RESPONSE " + Responses.payment(payment));

        assertEquals(new BigDecimal("0.40"), payer.getBalance());
        assertEquals(new BigDecimal("230.00"), creator.getBalance());
        assertEquals(new BigDecimal("30.60"), detail.getAmountPaid());
        assertEquals(new BigDecimal("0.60"), payment.getServiceChargeAmount());
        assertEquals(new BigDecimal("30.00"), payment.getAmountTransferred());
    }

    @Test
    void paymentRequiresEnoughBalanceAndTheAssignedUser() {
        AppUser creator = saveUser("creator");
        creator.setBalance(new BigDecimal("100.00"));
        BillHead billHead = billHeads.create(new Requests.BillHeadCreate("Dinner"), creator.getId());
        AppUser payer = saveUser("payer");
        payer.setBalance(new BigDecimal("30.00"));
        AppUser other = saveUser("other");
        other.setBalance(new BigDecimal("100.00"));
        BillDetail detail = billDetails.create(billHead.getId(), new Requests.BillDetailCreate(new BigDecimal("50.00"), payer.getId()));

        ApiException exception = assertThrows(ApiException.class, () -> payments.pay(payer.getId(), detail.getId()));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(new BigDecimal("30.00"), payer.getBalance());

        ApiException wrongUser = assertThrows(ApiException.class, () -> payments.pay(other.getId(), detail.getId()));
        assertEquals(HttpStatus.FORBIDDEN, wrongUser.getStatus());
        System.out.println("REQUEST payment payer=" + payer.getId() + " RESPONSE errors=" + exception.getMessage() + "; " + wrongUser.getMessage());
    }

    @Test
    void userCanTopUpAndWithdrawOnlyWithinTheirBalance() {
        AppUser user = saveUser("wallet");

        usersService.topUp(user.getId(), new BigDecimal("50.00"));
        usersService.withdraw(user.getId(), new BigDecimal("12.50"));

        assertEquals(new BigDecimal("37.50"), usersService.get(user.getId()).getBalance());
        ApiException exception = assertThrows(ApiException.class,
                () -> usersService.withdraw(user.getId(), new BigDecimal("40.00")));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void userCanViewTheirUnpaidDetailsAndPaymentHistory() {
        AppUser creator = saveUser("creator");
        AppUser payer = saveUser("payer");
        payer.setBalance(new BigDecimal("20.40"));
        BillHead billHead = billHeads.create(new Requests.BillHeadCreate("Dinner"), creator.getId());
        BillDetail detail = billDetails.create(billHead.getId(), new Requests.BillDetailCreate(new BigDecimal("10.00"), payer.getId()));

        assertEquals(1, ((java.util.List<?>) billDetails.unpaidForUser(payer.getId())).size());
        payments.pay(payer.getId(), detail.getId());
        assertEquals(1, ((java.util.List<?>) payments.forUser(payer.getId())).size());
        assertEquals(0, ((java.util.List<?>) billDetails.unpaidForUser(payer.getId())).size());
    }

    private AppUser saveUser(String username) {
        return users.saveAndFlush(new AppUser(username, "password"));
    }
}