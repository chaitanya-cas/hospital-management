
package com.hospital.doctor.service;

import com.hospital.department.entity.Department;
import com.hospital.department.repository.DepartmentRepository;

import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.entity.DoctorStatus;
import com.hospital.doctor.repository.DoctorRepository;

import com.hospital.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
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
    @Transactional
    public Doctor createDoctor(Doctor doctor, Long departmentId) {

        if (doctor == null) {
            throw new IllegalArgumentException(
                    "Doctor details are required"
            );
        }

        String doctorCode = normalizeRequired(
                doctor.getDoctorCode(), "Doctor code"
        );

        String email = normalizeRequired(
                doctor.getEmail(), "Email"
        ).toLowerCase(Locale.ROOT);

        String licenseNumber = normalizeRequired(
                doctor.getLicenseNumber(), "License number"
        );

        validateDoctor(doctor);

        if (doctorRepository.existsByDoctorCode(doctorCode)) {
            throw new IllegalArgumentException(
                    "Doctor code already exists: " + doctorCode
            );
        }

        if (doctorRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Email already registered: " + email
            );
        }

        if (doctorRepository.existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException(
                    "License number already registered"
            );
        }

        Department department = getDepartmentOrThrow(departmentId);

        doctor.setDoctorCode(doctorCode);
        doctor.setEmail(email);
        doctor.setLicenseNumber(licenseNumber);

        doctor.setFirstName(
                normalizeRequired(doctor.getFirstName(), "First name")
        );

        doctor.setLastName(
                normalizeRequired(doctor.getLastName(), "Last name")
        );

        doctor.setContactNumber(
                normalizeRequired(
                        doctor.getContactNumber(), "Contact number"
                )
        );

        doctor.setSpecialization(
                normalizeRequired(
                        doctor.getSpecialization(), "Specialization"
                )
        );

        doctor.setDepartment(department);

        return doctorRepository.save(doctor);
    }

    // 2. Retrieve all doctors
    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // 3. Retrieve all doctors with pagination and sorting
    @Override
    public Page<Doctor> getAllDoctors(
            int page,
            int size,
            String sortBy,
            String direction) {

        return getAllDoctors(
                page,
                size,
                sortBy,
                direction,
                null,
                null
        );
    }

    // 4. Retrieve doctors with pagination, sorting and filters
    @Override
    public Page<Doctor> getAllDoctors(
            int page,
            int size,
            String sortBy,
            String direction,
            DoctorStatus status,
            Long departmentId) {

        // Validate page number
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        // Validate page size
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        // Validate department ID if provided
        if (departmentId != null) {

            if (departmentId <= 0) {
                throw new IllegalArgumentException(
                        "Department ID must be greater than zero"
                );
            }

            getDepartmentOrThrow(departmentId);
        }

        // Set default sorting field
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "firstName";
        }

        sortBy = sortBy.trim();

        // Validate allowed sorting fields
        List<String> allowedSortFields = List.of(
                "id",
                "firstName",
                "lastName",
                "email",
                "specialization",
                "yearsOfExperience",
                "consultationFee",
                "createdAt"
        );

        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sortBy
            );
        }

        // Validate sorting direction
        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(
                    direction == null || direction.isBlank()
                            ? "asc"
                            : direction.trim()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Direction must be asc or desc"
            );
        }

        // Create pageable configuration
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        // Retrieve doctors with filters
        return doctorRepository.findDoctorsByFilters(
                status,
                departmentId,
                pageable
        );
    }

    // 5. Retrieve doctor by ID
    @Override
    public Doctor getDoctorById(Long id) {

        validateId(id, "Doctor ID");

        return doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with ID: " + id
                        )
                );
    }

    // 6. Update doctor details
    @Override
    @Transactional
    public Doctor updateDoctor(
            Long id,
            Doctor updatedDoctor,
            Long departmentId) {

        validateId(id, "Doctor ID");

        if (updatedDoctor == null) {
            throw new IllegalArgumentException(
                    "Updated doctor details are required"
            );
        }

        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with ID: " + id
                        )
                );

        String doctorCode = normalizeRequired(
                updatedDoctor.getDoctorCode(), "Doctor code"
        );

        String email = normalizeRequired(
                updatedDoctor.getEmail(), "Email"
        ).toLowerCase(Locale.ROOT);

        String licenseNumber = normalizeRequired(
                updatedDoctor.getLicenseNumber(), "License number"
        );

        validateDoctor(updatedDoctor);

        // Check duplicate doctor code
        doctorRepository.findByDoctorCode(doctorCode)
                .filter(doctor -> !doctor.getId().equals(id))
                .ifPresent(doctor -> {
                    throw new IllegalArgumentException(
                            "Doctor code already exists: " + doctorCode
                    );
                });

        // Check duplicate email
        doctorRepository.findByEmailIgnoreCase(email)
                .filter(doctor -> !doctor.getId().equals(id))
                .ifPresent(doctor -> {
                    throw new IllegalArgumentException(
                            "Email already registered: " + email
                    );
                });

        // Check duplicate license number
        doctorRepository.findByLicenseNumber(licenseNumber)
                .filter(doctor -> !doctor.getId().equals(id))
                .ifPresent(doctor -> {
                    throw new IllegalArgumentException(
                            "License number already registered"
                    );
                });

        Department department = getDepartmentOrThrow(departmentId);

        // Update doctor fields
        existingDoctor.setDoctorCode(doctorCode);

        existingDoctor.setFirstName(
                normalizeRequired(
                        updatedDoctor.getFirstName(), "First name"
                )
        );

        existingDoctor.setLastName(
                normalizeRequired(
                        updatedDoctor.getLastName(), "Last name"
                )
        );

        existingDoctor.setEmail(email);

        existingDoctor.setContactNumber(
                normalizeRequired(
                        updatedDoctor.getContactNumber(), "Contact number"
                )
        );

        existingDoctor.setSpecialization(
                normalizeRequired(
                        updatedDoctor.getSpecialization(), "Specialization"
                )
        );

        existingDoctor.setLicenseNumber(licenseNumber);

        existingDoctor.setYearsOfExperience(
                updatedDoctor.getYearsOfExperience()
        );

        existingDoctor.setConsultationFee(
                updatedDoctor.getConsultationFee()
        );

        existingDoctor.setDepartment(department);

        return doctorRepository.save(existingDoctor);
    }

    // 7. Search doctors by name
    @Override
    public List<Doctor> searchDoctorsByName(String name) {

        if (name == null || name.isBlank()) {
            return getAllDoctors();
        }

        String keyword = name.trim();

        return doctorRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        keyword,
                        keyword
                );
    }

    // 8. Search doctors by specialization
    @Override
    public List<Doctor> searchDoctorsBySpecialization(
            String specialization) {

        if (specialization == null || specialization.isBlank()) {
            return getAllDoctors();
        }

        return doctorRepository
                .findBySpecializationContainingIgnoreCase(
                        specialization.trim()
                );
    }

    // 9. Retrieve doctors by department
    @Override
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {

        validateId(departmentId, "Department ID");
        getDepartmentOrThrow(departmentId);

        return doctorRepository.findByDepartment_Id(departmentId);
    }

    // 10. Update doctor status
    @Override
    @Transactional
    public Doctor updateDoctorStatus(Long id, DoctorStatus status) {

        validateId(id, "Doctor ID");

        if (status == null) {
            throw new IllegalArgumentException(
                    "Doctor status is required"
            );
        }

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with ID: " + id
                        )
                );

        doctor.setStatus(status);

        return doctorRepository.save(doctor);
    }

    // Validate doctor fields
    private void validateDoctor(Doctor doctor) {

        if (doctor == null) {
            throw new IllegalArgumentException(
                    "Doctor details are required"
            );
        }

        normalizeRequired(
                doctor.getFirstName(), "First name"
        );

        normalizeRequired(
                doctor.getLastName(), "Last name"
        );

        normalizeRequired(
                doctor.getContactNumber(), "Contact number"
        );

        normalizeRequired(
                doctor.getSpecialization(), "Specialization"
        );

        if (doctor.getYearsOfExperience() == null
                || doctor.getYearsOfExperience() < 0) {

            throw new IllegalArgumentException(
                    "Years of experience must be zero or greater"
            );
        }

        if (doctor.getConsultationFee() == null
                || !Double.isFinite(doctor.getConsultationFee())
                || doctor.getConsultationFee() < 0) {

            throw new IllegalArgumentException(
                    "Consultation fee must be a valid non-negative number"
            );
        }
    }

    // Validate IDs
    private void validateId(Long id, String fieldName) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero"
            );
        }
    }

    // Find department or throw exception
    private Department getDepartmentOrThrow(Long departmentId) {

        validateId(departmentId, "Department ID");

        return departmentRepository.findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with ID: "
                                        + departmentId
                        )
                );
    }

    // Normalize required text fields
    private String normalizeRequired(
            String value,
            String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return value.trim();
    }
}

