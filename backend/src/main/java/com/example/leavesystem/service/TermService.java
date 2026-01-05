package com.example.leavesystem.service;

import com.example.leavesystem.entity.Term;
import java.util.List;

public interface TermService {
    Term create(Term term);
    Term update(Term term);
    boolean deleteById(Long id);
    Term findById(Long id);
    List<Term> listAll();
    Term openTerm(Long id);
    Term closeTerm(Long id);
}
