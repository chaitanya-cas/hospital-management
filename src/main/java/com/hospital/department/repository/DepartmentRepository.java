package com.hospital.department.repository;

import com.hospital.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface DepartmentRepository  extends JpaRepository<Department,Long> {

    //Find a department using its unique code
    Optional<Department> findByDepartmentCode(String departmentCode);

    //find department by name, ignoring uppercase/ lowercase
    List<Department> findByDepartmentNameContainingIgnoreCase(String departmentName);

    //Check whether a department code already exists
    boolean existsByDepartmentCode(String departmentCode);

}
