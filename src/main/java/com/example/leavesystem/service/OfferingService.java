package com.example.leavesystem.service;

import com.example.leavesystem.entity.Offering;
import java.util.List;

public interface OfferingService {
    Offering create(Offering offering);
    Offering update(Offering offering);
    boolean deleteById(Long id);
    Offering findById(Long id);
    List<Offering> listAll();
    List<Offering> findByTermAndClass(Long termId, Long classId);
}
