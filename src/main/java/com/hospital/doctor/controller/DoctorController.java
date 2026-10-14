package com.hospital.doctor.controller;

import com.hospital.department.entity.Department;

import com.hospital.doctor.dto.DoctorRequest;
import com.hospital.doctor.dto.DoctorResponse;
import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.entity.DoctorStatus;
import com.hospital.doctor.service.DoctorService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.Map;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    // Constructor injection
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // 1. Register a doctor
    @PostMapping
    @Transactional
    public ResponseEntity<DoctorResponse> createDoctor(
            @Valid @RequestBody DoctorRequest request) {

        Doctor doctor = convertToEntity(request);

        Doctor savedDoctor = doctorService.createDoctor(
                doctor,
                request.getDepartmentId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertToResponse(savedDoctor));
    }

    // 2. Retrieve all doctors
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {

        List<DoctorResponse> doctors = doctorService.getAllDoctors()
                .stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(doctors);
    }

    // 3. Retrieve doctor by ID
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<DoctorResponse> getDoctorById(
            @PathVariable Long id) {

        Doctor doctor = doctorService.getDoctorById(id);

        return ResponseEntity.ok(convertToResponse(doctor));
    }

    // 4. Update doctor details
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequest request) {

        Doctor updatedDoctor = doctorService.updateDoctor(
                id,
                convertToEntity(request),
                request.getDepartmentId()
        );

        return ResponseEntity.ok(convertToResponse(updatedDoctor));
    }

    // 5. Search doctors by name
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DoctorResponse>> searchDoctorsByName(
            @RequestParam String name) {

        List<DoctorResponse> doctors =
                doctorService.searchDoctorsByName(name)
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(doctors);
    }

    // 6. Search doctors by specialization
    @GetMapping("/specialization")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DoctorResponse>> searchBySpecialization(
            @RequestParam String name) {

        List<DoctorResponse> doctors =
                doctorService.searchDoctorsBySpecialization(name)
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(doctors);
    }

    // 7. Retrieve doctors by department
    @GetMapping("/department/{departmentId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DoctorResponse>> getDoctorsByDepartment(
            @PathVariable Long departmentId) {

        List<DoctorResponse> doctors =
                doctorService.getDoctorsByDepartment(departmentId)
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(doctors);
    }

    // 8. Update doctor status
    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<DoctorResponse> updateDoctorStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        DoctorStatus doctorStatus;

        try {
            doctorStatus = DoctorStatus.valueOf(
                    status.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid doctor status. Use ACTIVE, INACTIVE, or ON_LEAVE.");
        }

        Doctor doctor = doctorService.updateDoctorStatus(
                id, doctorStatus);

        return ResponseEntity.ok(convertToResponse(doctor));
    }

    // Convert request DTO to entity
    private Doctor convertToEntity(DoctorRequest request) {

        Doctor doctor = new Doctor();

        doctor.setDoctorCode(request.getDoctorCode());
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setEmail(request.getEmail());
        doctor.setContactNumber(request.getContactNumber());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setYearsOfExperience(request.getYearsOfExperience());
        doctor.setConsultationFee(request.getConsultationFee());

        return doctor;
    }

    // Convert entity to response DTO
    private DoctorResponse convertToResponse(Doctor doctor) {

        DoctorResponse response = new DoctorResponse();

        response.setId(doctor.getId());
        response.setDoctorCode(doctor.getDoctorCode());
        response.setFirstName(doctor.getFirstName());
        response.setLastName(doctor.getLastName());
        response.setEmail(doctor.getEmail());
        response.setContactNumber(doctor.getContactNumber());
        response.setSpecialization(doctor.getSpecialization());
        response.setLicenseNumber(doctor.getLicenseNumber());
        response.setYearsOfExperience(doctor.getYearsOfExperience());
        response.setConsultationFee(doctor.getConsultationFee());
        response.setStatus(doctor.getStatus());
        response.setCreatedAt(doctor.getCreatedAt());
        response.setUpdatedAt(doctor.getUpdatedAt());

        Department department = doctor.getDepartment();

        if (department != null) {
            response.setDepartmentId(department.getId());
            response.setDepartmentName(department.getDepartmentName());
        }

        return response;
    }

// 3. Retrieve doctors using pagination and sorting
// 3. Retrieve doctors using pagination, sorting and filters
@GetMapping("/page")
@Transactional(readOnly = true)
public ResponseEntity<Map<String, Object>> getDoctorsWithPagination(

        @RequestParam(defaultValue = "0") int page,

        @RequestParam(defaultValue = "10") int size,

        @RequestParam(defaultValue = "firstName") String sortBy,

        @RequestParam(defaultValue = "asc") String direction,

        @RequestParam(required = false) String status,

        @RequestParam(required = false) Long departmentId) {

    // Convert the status string into the enum
    DoctorStatus doctorStatus = null;

    if (status != null && !status.isBlank()) {

        try {
            doctorStatus = DoctorStatus.valueOf(
                    status.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid doctor status. Use ACTIVE, INACTIVE, or ON_LEAVE."
            );
        }
    }

    // Retrieve filtered doctors
    Page<Doctor> doctorPage = doctorService.getAllDoctors(
            page,
            size,
            sortBy,
            direction,
            doctorStatus,
            departmentId
    );

    // Convert entities to response DTOs
    List<DoctorResponse> doctors = doctorPage.getContent()
            .stream()
            .map(this::convertToResponse)
            .toList();

    // Build response with pagination metadata
    Map<String, Object> response = Map.of(
            "doctors", doctors,
            "currentPage", doctorPage.getNumber(),
            "pageSize", doctorPage.getSize(),
            "totalElements", doctorPage.getTotalElements(),
            "totalPages", doctorPage.getTotalPages(),
            "first", doctorPage.isFirst(),
            "last", doctorPage.isLast()
    );

    return ResponseEntity.ok(response);
}

}