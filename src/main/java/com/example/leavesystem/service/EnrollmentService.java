package com.example.leavesystem.service;

import com.example.leavesystem.entity.Enrollment;
import java.util.List;

public interface EnrollmentService {
    Enrollment create(Enrollment enrollment);
    boolean updateStatus(Long enrollmentId, String status);
    boolean deleteById(Long id);
    Enrollment findById(Long id);
    List<Enrollment> listAll();
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByOfferingId(Long offeringId);
    List<Enrollment> batchCreate(List<Enrollment> enrollments);
}