package com.example.leavesystem.service;

import com.example.leavesystem.entity.Clazz;
import java.util.List;

public interface ClassService {
    Clazz create(Clazz clazz);
    Clazz update(Clazz clazz);
    boolean deleteById(Long id);
    Clazz findById(Long id);
    List<Clazz> listAll();
    Clazz setCounselor(Long classId, Long counselorId);
}
