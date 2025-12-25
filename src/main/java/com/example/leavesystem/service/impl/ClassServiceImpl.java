package com.example.leavesystem.service.impl;

import com.example.leavesystem.entity.Clazz;
import com.example.leavesystem.mapper.ClazzMapper;
import com.example.leavesystem.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClazzMapper clazzMapper;

    @Override
    public Clazz create(Clazz clazz) {
        clazzMapper.insert(clazz);
        return clazz;
    }

    @Override
    public Clazz update(Clazz clazz) {
        clazzMapper.update(clazz);
        return clazz;
    }

    @Override
    public boolean deleteById(Long id) {
        return clazzMapper.delete(id) > 0;
    }

    @Override
    public Clazz findById(Long id) {
        return clazzMapper.findById(id);
    }

    @Override
    public List<Clazz> listAll() {
        return clazzMapper.findAll();
    }

    @Override
    public Clazz setCounselor(Long classId, Long counselorId) {
        Clazz clazz = clazzMapper.findById(classId);
        if (clazz != null) {
            clazz.setCounselorId(counselorId);
            clazzMapper.update(clazz);
        }
        return clazz;
    }
}