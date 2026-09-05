package com.splitbill.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.splitbill.domain.BillDetail;

import jakarta.persistence.LockModeType;

public interface BillDetailRepository extends JpaRepository<BillDetail, Long> {
    @Query("select d from BillDetail d where d.user.id = :userId and d.amountPaid < d.amountToPay order by d.id")
    Iterable<BillDetail> findUnpaidByUserId(@Param("userId") Long userId);

    //PESSIMISTIC_WRITE is a database locking mode that blocks all other transactions from reading, updating, or deleting a locked row until your transaction finishes.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from BillDetail d where d.id = :id")
    Optional<BillDetail> findByIdForUpdate(@Param("id") Long id);
}
