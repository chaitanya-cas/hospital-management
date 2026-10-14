package com.hospital.doctor.repository;

import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.entity.DoctorStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByDoctorCode(String doctorCode);

    Optional<Doctor> findByEmailIgnoreCase(String email);

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    List<Doctor> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName
    );

    List<Doctor> findBySpecializationContainingIgnoreCase(
            String specialization
    );

    List<Doctor> findByDepartment_Id(Long departmentId);

    List<Doctor> findByStatus(DoctorStatus status);

    boolean existsByDoctorCode(String doctorCode);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByLicenseNumber(String licenseNumber);
}