package com.hospital.doctor.service;

import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.entity.DoctorStatus;

import java.util.List;

public interface DoctorService {

    // Register a new doctor
    Doctor createDoctor(Doctor doctor, Long departmentId);

    // Retrieve all doctors
    List<Doctor> getAllDoctors();

    // Retrieve a doctor by ID
    Doctor getDoctorById(Long id);

    // Update doctor details
    Doctor updateDoctor(Long id, Doctor updatedDoctor, Long departmentId);

    // Search doctors by name
    List<Doctor> searchDoctorsByName(String name);

    // Search doctors by specialization
    List<Doctor> searchDoctorsBySpecialization(String specialization);

    // Retrieve doctors belonging to a department
    List<Doctor> getDoctorsByDepartment(Long departmentId);

    // Update doctor status
    Doctor updateDoctorStatus(Long id, DoctorStatus status);
}