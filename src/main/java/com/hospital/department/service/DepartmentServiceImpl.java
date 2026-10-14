package com.hospital.department.service;

import com.hospital.department.entity.Department;
import com.hospital.department.entity.DepartmentStatus;
import com.hospital.department.repository.DepartmentRepository;
import com.hospital.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    // Constructor injection
    public DepartmentServiceImpl(
            DepartmentRepository departmentRepository) {

        this.departmentRepository = departmentRepository;
    }

    @Override
    public Department createDepartment(Department department) {

        String code = normalizeCode(department.getDepartmentCode());

        if (departmentRepository.existsByDepartmentCode(code)) {
            throw new IllegalArgumentException(
                    "Department code already exists: " + code
            );
        }

        department.setDepartmentCode(code);

        return departmentRepository.save(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {

        return departmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {

        return departmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with ID: " + id
                        )
                );
    }

    @Override
    public Department updateDepartment(
            Long id,
            Department updatedDepartment) {

        Department existingDepartment = getDepartmentById(id);

        String updatedCode =
                normalizeCode(updatedDepartment.getDepartmentCode());

        boolean codeBelongsToAnotherDepartment =
                departmentRepository
                        .findByDepartmentCode(updatedCode)
                        .filter(department ->
                                !department.getId().equals(id))
                        .isPresent();

        if (codeBelongsToAnotherDepartment) {
            throw new IllegalArgumentException(
                    "Department code already exists: " + updatedCode
            );
        }

        existingDepartment.setDepartmentCode(updatedCode);
        existingDepartment.setDepartmentName(
                updatedDepartment.getDepartmentName()
        );
        existingDepartment.setDescription(
                updatedDepartment.getDescription()
        );
        existingDepartment.setLocation(
                updatedDepartment.getLocation()
        );
        existingDepartment.setContactNumber(
                updatedDepartment.getContactNumber()
        );

        return departmentRepository.save(existingDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> searchDepartments(String name) {

        if (name == null || name.isBlank()) {
            return getAllDepartments();
        }

        return departmentRepository
                .findByDepartmentNameContainingIgnoreCase(
                        name.trim()
                );
    }

    @Override
    public Department updateDepartmentStatus(
            Long id,
            String status) {

        Department department = getDepartmentById(id);

        try {
            DepartmentStatus departmentStatus =
                    DepartmentStatus.valueOf(
                            status.trim().toUpperCase(Locale.ROOT)
                    );

            department.setStatus(departmentStatus);

            return departmentRepository.save(department);

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Invalid department status. Use ACTIVE or INACTIVE."
            );
        }
    }

    private String normalizeCode(String code) {

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Department code is required."
            );
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }
}