package com.amrapari.nhcsbackend.repository;

import com.amrapari.nhcsbackend.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByPatientIdOrderByDateDesc(Long patientId);
}
