package com.example.leavesystem.service.impl;

import com.example.leavesystem.entity.Enrollment;
import com.example.leavesystem.mapper.EnrollmentMapper;
import com.example.leavesystem.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentMapper enrollmentMapper;

    @Override
    public Enrollment create(Enrollment enrollment) {
        enrollmentMapper.insert(enrollment);
        return enrollment;
    }

    @Override
    public boolean updateStatus(Long enrollmentId, String status) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(enrollmentId);
        enrollment.setStatus(status);
        return enrollmentMapper.updateStatus(enrollment) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return enrollmentMapper.delete(id) > 0;
    }

    @Override
    public Enrollment findById(Long id) {
        return enrollmentMapper.findById(id);
    }

    @Override
    public List<Enrollment> listAll() {
        // 修改为调用findAll方法
        return enrollmentMapper.findAll();
    }

    @Override
    public List<Enrollment> findByStudentId(Long studentId) {
        return enrollmentMapper.findByStudentId(studentId);
    }

    @Override
    public List<Enrollment> findByOfferingId(Long offeringId) {
        return enrollmentMapper.findByOfferingId(offeringId);
    }

    @Override
    @Transactional
    public List<Enrollment> batchCreate(List<Enrollment> enrollments) {
        enrollments.forEach(enrollmentMapper::insert);
        return enrollments;
    }
}