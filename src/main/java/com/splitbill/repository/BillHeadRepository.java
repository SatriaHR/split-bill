package com.splitbill.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.splitbill.domain.BillHead;

public interface BillHeadRepository extends JpaRepository<BillHead, Long> { }
