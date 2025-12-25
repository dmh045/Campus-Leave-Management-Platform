package com.example.leavesystem.service.impl;

import com.example.leavesystem.entity.Offering;
import com.example.leavesystem.mapper.OfferingMapper;
import com.example.leavesystem.service.OfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferingServiceImpl implements OfferingService {

    private final OfferingMapper offeringMapper;

    @Override
    public Offering create(Offering offering) {
        offeringMapper.insert(offering);
        return offering;
    }

    @Override
    public Offering update(Offering offering) {
        offeringMapper.update(offering);
        return offering;
    }

    @Override
    public boolean deleteById(Long id) {
        return offeringMapper.delete(id) > 0;
    }

    @Override
    public Offering findById(Long id) {
        return offeringMapper.findById(id);
    }

    @Override
    public List<Offering> listAll() {
        return offeringMapper.findAll();
    }

    @Override
    public List<Offering> findByTermAndClass(Long termId, Long classId) {
        return offeringMapper.findByTermAndClass(termId, classId);
    }
}