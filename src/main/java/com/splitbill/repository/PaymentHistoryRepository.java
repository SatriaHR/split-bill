package com.splitbill.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.splitbill.domain.PaymentHistory;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
	List<PaymentHistory> findByPayerIdOrderByPaidAtDesc(Long payerId);

	@Query("select p from PaymentHistory p where p.id = :id and p.payer.id = :payerId")
	Optional<PaymentHistory> findByIdAndPayerId(@Param("id") Long id, @Param("payerId") Long payerId);
}
