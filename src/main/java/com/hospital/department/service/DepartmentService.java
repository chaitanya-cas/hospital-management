package com.hospital.department.service;

import com.hospital.department.entity.Department;

import java.util.List;

public interface DepartmentService {

    // Create a department
    Department createDepartment(Department department);

    // Retrieve all departments
    List<Department> getAllDepartments();

    // Retrieve one department by ID
    Department getDepartmentById(Long id);

    // Update an existing department
    Department updateDepartment(Long id, Department department);

    // Search departments by name
    List<Department> searchDepartments(String name);

    // Activate or deactivate a department
    Department updateDepartmentStatus(
            Long id,
            String status
    );
}