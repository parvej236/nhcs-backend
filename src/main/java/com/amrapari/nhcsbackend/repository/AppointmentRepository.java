package com.amrapari.nhcsbackend.repository;

import com.amrapari.nhcsbackend.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByPatientIdOrderByDateDesc(Long patientId);

    List<Appointment> findByApprovalStatus(String approvalStatus);
    List<Appointment> findByApprovalStatusAndDate(String approvalStatus, LocalDate date);
    List<Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date);

    @Query("SELECT a.doctor.id, COUNT(a) FROM Appointment a WHERE a.date = :date AND a.status = 'Upcoming' GROUP BY a.doctor.id")
    List<Object[]> countUpcomingAppointmentsByDoctorForDate(@Param("date") LocalDate date);
}
