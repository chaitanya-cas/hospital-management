package com.hospital.department.controller;

import com.hospital.department.dto.DepartmentRequest;
import com.hospital.department.dto.DepartmentResponse;
import com.hospital.department.entity.Department;
import com.hospital.department.service.DepartmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    // Constructor injection
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // 1. Create department
    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {

        Department department = new Department();

        department.setDepartmentCode(request.getDepartmentCode());
        department.setDepartmentName(request.getDepartmentName());
        department.setDescription(request.getDescription());
        department.setLocation(request.getLocation());
        department.setContactNumber(request.getContactNumber());

        Department savedDepartment =
                departmentService.createDepartment(department);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(convertToResponse(savedDepartment));
    }

    // 2. Get all departments
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {

        List<DepartmentResponse> departments =
                departmentService.getAllDepartments()
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(departments);
    }

    // 3. Get department by ID
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @PathVariable Long id) {

        Department department =
                departmentService.getDepartmentById(id);

        return ResponseEntity.ok(convertToResponse(department));
    }

    // 4. Update department
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {

        Department department = new Department();

        department.setDepartmentCode(request.getDepartmentCode());
        department.setDepartmentName(request.getDepartmentName());
        department.setDescription(request.getDescription());
        department.setLocation(request.getLocation());
        department.setContactNumber(request.getContactNumber());

        Department updatedDepartment =
                departmentService.updateDepartment(id, department);

        return ResponseEntity.ok(convertToResponse(updatedDepartment));
    }

    // 5. Search departments by name
    @GetMapping("/search")
    public ResponseEntity<List<DepartmentResponse>> searchDepartments(
            @RequestParam(required = false) String name) {

        List<DepartmentResponse> departments =
                departmentService.searchDepartments(name)
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(departments);
    }

    // 6. Update department status
    @PatchMapping("/{id}/status")
    public ResponseEntity<DepartmentResponse> updateDepartmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Department department =
                departmentService.updateDepartmentStatus(id, status);

        return ResponseEntity.ok(convertToResponse(department));
    }

    // Convert Entity to Response DTO
    private DepartmentResponse convertToResponse(Department department) {

        DepartmentResponse response = new DepartmentResponse();

        response.setId(department.getId());
        response.setDepartmentCode(department.getDepartmentCode());
        response.setDepartmentName(department.getDepartmentName());
        response.setDescription(department.getDescription());
        response.setLocation(department.getLocation());
        response.setContactNumber(department.getContactNumber());
        response.setStatus(department.getStatus());
        response.setCreatedAt(department.getCreatedAt());
        response.setUpdatedAt(department.getUpdatedAt());

        return response;
    }
}