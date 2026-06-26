package com.amrapari.nhcsbackend.repository;

import com.amrapari.nhcsbackend.domain.LabReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, String> {
    List<LabReport> findByPatientId(Long patientId);
    List<LabReport> findByPatientIdOrderByDateDesc(Long patientId);
}
