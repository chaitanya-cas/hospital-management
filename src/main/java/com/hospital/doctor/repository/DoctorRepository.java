
package com.hospital.doctor.repository;

import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.entity.DoctorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByDoctorCode(String doctorCode);

    Optional<Doctor> findByEmailIgnoreCase(String email);

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    // Search doctors by first name or last name
    @EntityGraph(attributePaths = "department")
    List<Doctor> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName
    );

    // Search doctors by specialization
    @EntityGraph(attributePaths = "department")
    List<Doctor> findBySpecializationContainingIgnoreCase(
            String specialization
    );

    // Find doctors by department
    @EntityGraph(attributePaths = "department")
    List<Doctor> findByDepartment_Id(Long departmentId);

    // Find doctors by status
    @EntityGraph(attributePaths = "department")
    List<Doctor> findByStatus(DoctorStatus status);

    // Check uniqueness
    boolean existsByDoctorCode(String doctorCode);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByLicenseNumber(String licenseNumber);

    // Retrieve all doctors with department information
    @Override
    @EntityGraph(attributePaths = "department")
    List<Doctor> findAll();

    // Retrieve a doctor by ID with department information
    @Override
    @EntityGraph(attributePaths = "department")
    Optional<Doctor> findById(Long id);

    // Retrieve doctors using pagination and sorting
    @Override
    @EntityGraph(attributePaths = "department")
    Page<Doctor> findAll(Pageable pageable);


    // Filter doctors by status and department with pagination
    @Query("""
        SELECT d
        FROM Doctor d
        WHERE (:status IS NULL OR d.status = :status)
        AND (:departmentId IS NULL OR d.department.id = :departmentId)
        """)
    @EntityGraph(attributePaths = "department")
    Page<Doctor> findDoctorsByFilters(
            @Param("status") DoctorStatus status,
            @Param("departmentId") Long departmentId,
            Pageable pageable
    );
}
