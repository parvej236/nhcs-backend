package com.amrapari.nhcsbackend.repository;

import com.amrapari.nhcsbackend.domain.ImagingReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImagingReportRepository extends JpaRepository<ImagingReport, String> {
    List<ImagingReport> findByPatientId(Long patientId);
    List<ImagingReport> findByPatientIdOrderByDateDesc(Long patientId);
}
