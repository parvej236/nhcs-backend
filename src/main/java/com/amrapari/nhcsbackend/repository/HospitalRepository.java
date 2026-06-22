package com.amrapari.nhcsbackend.repository;

import com.amrapari.nhcsbackend.domain.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
}
