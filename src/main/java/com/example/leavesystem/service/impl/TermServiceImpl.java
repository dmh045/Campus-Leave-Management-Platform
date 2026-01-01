package com.example.leavesystem.service.impl;

import com.example.leavesystem.entity.Term;
import com.example.leavesystem.mapper.TermMapper;
import com.example.leavesystem.service.TermService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TermServiceImpl implements TermService {

    @Autowired
    private TermMapper termMapper;

    @Override
    public Term create(Term term) {
        termMapper.insert(term);
        return term;
    }

    @Override
    public Term update(Term term) {
        termMapper.updateById(term);
        return term;
    }

    @Override
    public boolean deleteById(Long id) {
        return termMapper.deleteById(id) > 0;
    }

    @Override
    public Term findById(Long id) {
        return termMapper.selectById(id);
    }

    @Override
    public List<Term> listAll() {
        return termMapper.selectAll();
    }

    @Override
    @Transactional
    public Term openTerm(Long id) {
        Term t = termMapper.selectById(id);
        if (t == null) return null;

        termMapper.setOnlyCurrent(id);
        return termMapper.selectById(id);
    }

    @Override
    @Transactional
    public Term closeTerm(Long id) {
        Term t = termMapper.selectById(id);
        if (t == null) return null;

        termMapper.closeById(id);
        return termMapper.selectById(id);
    }
}