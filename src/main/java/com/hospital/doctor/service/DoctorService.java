
package com.hospital.doctor.service;

import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.entity.DoctorStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DoctorService {

    // 1. Register a doctor
    Doctor createDoctor(Doctor doctor, Long departmentId);

    // 2. Retrieve all doctors
    List<Doctor> getAllDoctors();

    // 3. Retrieve doctors using pagination and sorting
    Page<Doctor> getAllDoctors(
            int page,
            int size,
            String sortBy,
            String direction
    );

    Page<Doctor> getAllDoctors(
            int page,
            int size,
            String sortBy,
            String direction,
            DoctorStatus status,
            Long departmentId
    );

    // 4. Retrieve a doctor by ID
    Doctor getDoctorById(Long id);

    // 5. Update doctor details
    Doctor updateDoctor(
            Long id,
            Doctor updatedDoctor,
            Long departmentId
    );

    // 6. Search doctors by name
    List<Doctor> searchDoctorsByName(String name);

    // 7. Search doctors by specialization
    List<Doctor> searchDoctorsBySpecialization(String specialization);

    // 8. Retrieve doctors by department
    List<Doctor> getDoctorsByDepartment(Long departmentId);

    // 9. Update doctor status
    Doctor updateDoctorStatus(Long id, DoctorStatus status);
}

