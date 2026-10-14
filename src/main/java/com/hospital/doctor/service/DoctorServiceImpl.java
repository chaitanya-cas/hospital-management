package com.hospital.doctor.service;

import com.hospital.department.entity.Department;
import com.hospital.department.repository.DepartmentRepository;
import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.entity.DoctorStatus;
import com.hospital.doctor.repository.DoctorRepository;
import com.hospital.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    // Constructor injection
    public DoctorServiceImpl(
            DoctorRepository doctorRepository,
            DepartmentRepository departmentRepository) {

        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
    }

    // 1. Register a doctor
    @Override
    public Doctor createDoctor(Doctor doctor, Long departmentId) {

        String doctorCode = normalizeRequired(
                doctor.getDoctorCode(), "Doctor code");

        String email = normalizeRequired(
                doctor.getEmail(), "Email").toLowerCase(Locale.ROOT);

        String licenseNumber = normalizeRequired(
                doctor.getLicenseNumber(), "License number");

        if (doctorRepository.existsByDoctorCode(doctorCode)) {
            throw new IllegalArgumentException(
                    "Doctor code already exists: " + doctorCode);
        }

        if (doctorRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Email already registered: " + email);
        }

        if (doctorRepository.existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException(
                    "License number already registered");
        }

        validateDoctor(doctor);

        Department department = getDepartmentOrThrow(departmentId);

        doctor.setDoctorCode(doctorCode);
        doctor.setEmail(email);
        doctor.setLicenseNumber(licenseNumber);
        doctor.setDepartment(department);

        return doctorRepository.save(doctor);
    }

    // 2. Get all doctors
    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {

        return doctorRepository.findAll();
    }

    // 3. Get doctor by ID
    @Override
    @Transactional(readOnly = true)
    public Doctor getDoctorById(Long id) {

        return doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with ID: " + id));
    }

    // 4. Update doctor
    @Override
    public Doctor updateDoctor(
            Long id,
            Doctor updatedDoctor,
            Long departmentId) {

        Doctor existingDoctor = getDoctorById(id);

        String doctorCode = normalizeRequired(
                updatedDoctor.getDoctorCode(), "Doctor code");

        String email = normalizeRequired(
                updatedDoctor.getEmail(), "Email").toLowerCase(Locale.ROOT);

        String licenseNumber = normalizeRequired(
                updatedDoctor.getLicenseNumber(), "License number");

        doctorRepository.findByDoctorCode(doctorCode)
                .filter(doctor -> !doctor.getId().equals(id))
                .ifPresent(doctor -> {
                    throw new IllegalArgumentException(
                            "Doctor code already exists: " + doctorCode);
                });

        doctorRepository.findByEmailIgnoreCase(email)
                .filter(doctor -> !doctor.getId().equals(id))
                .ifPresent(doctor -> {
                    throw new IllegalArgumentException(
                            "Email already registered: " + email);
                });

        doctorRepository.findByLicenseNumber(licenseNumber)
                .filter(doctor -> !doctor.getId().equals(id))
                .ifPresent(doctor -> {
                    throw new IllegalArgumentException(
                            "License number already registered");
                });

        validateDoctor(updatedDoctor);

        Department department = getDepartmentOrThrow(departmentId);

        existingDoctor.setDoctorCode(doctorCode);
        existingDoctor.setFirstName(updatedDoctor.getFirstName().trim());
        existingDoctor.setLastName(updatedDoctor.getLastName().trim());
        existingDoctor.setEmail(email);
        existingDoctor.setContactNumber(
                updatedDoctor.getContactNumber().trim());
        existingDoctor.setSpecialization(
                updatedDoctor.getSpecialization().trim());
        existingDoctor.setLicenseNumber(licenseNumber);
        existingDoctor.setYearsOfExperience(
                updatedDoctor.getYearsOfExperience());
        existingDoctor.setConsultationFee(
                updatedDoctor.getConsultationFee());
        existingDoctor.setDepartment(department);

        return doctorRepository.save(existingDoctor);
    }

    // 5. Search doctors by name
    @Override
    @Transactional(readOnly = true)
    public List<Doctor> searchDoctorsByName(String name) {

        if (name == null || name.isBlank()) {
            return getAllDoctors();
        }

        String keyword = name.trim();

        return doctorRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        keyword, keyword);
    }

    // 6. Search doctors by specialization
    @Override
    @Transactional(readOnly = true)
    public List<Doctor> searchDoctorsBySpecialization(
            String specialization) {

        if (specialization == null || specialization.isBlank()) {
            return getAllDoctors();
        }

        return doctorRepository.findBySpecializationContainingIgnoreCase(
                specialization.trim());
    }

    // 7. Get doctors by department
    @Override
    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {

        getDepartmentOrThrow(departmentId);

        return doctorRepository.findByDepartment_Id(departmentId);
    }

    // 8. Update doctor status
    @Override
    public Doctor updateDoctorStatus(Long id, DoctorStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Doctor status is required");
        }

        Doctor doctor = getDoctorById(id);
        doctor.setStatus(status);

        return doctorRepository.save(doctor);
    }

    // Validate required fields and numeric values
    private void validateDoctor(Doctor doctor) {

        normalizeRequired(doctor.getFirstName(), "First name");
        normalizeRequired(doctor.getLastName(), "Last name");
        normalizeRequired(doctor.getContactNumber(), "Contact number");
        normalizeRequired(doctor.getSpecialization(), "Specialization");

        if (doctor.getYearsOfExperience() == null
                || doctor.getYearsOfExperience() < 0) {
            throw new IllegalArgumentException(
                    "Years of experience cannot be negative");
        }

        if (doctor.getConsultationFee() == null
                || !Double.isFinite(doctor.getConsultationFee())
                || doctor.getConsultationFee() < 0) {
            throw new IllegalArgumentException(
                    "Consultation fee must be a valid non-negative number");
        }
    }

    // Find a department or throw 404
    private Department getDepartmentOrThrow(Long departmentId) {

        if (departmentId == null) {
            throw new IllegalArgumentException(
                    "Department ID is required");
        }

        return departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with ID: "
                                        + departmentId));
    }

    // Normalize required text fields
    private String normalizeRequired(String value, String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required");
        }

        return value.trim();
    }
}