package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.Term;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TermMapper {

    @Select("SELECT * FROM term WHERE term_id = #{id}")
    Term findById(Long id);

    @Select("SELECT * FROM term WHERE term_code = #{code}")
    Term findByCode(String code);

    @Select("SELECT * FROM term")
    List<Term> findAll();

    @Insert("""
        INSERT INTO term
          (term_code, term_name, start_date, end_date, is_current, created_at, updated_at)
        VALUES
          (#{termCode}, #{termName}, #{startDate}, #{endDate}, #{isCurrent}, NOW(), NOW())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "termId")
    int insert(Term term);

    @Update("""
        UPDATE term SET
          term_code  = #{termCode},
          term_name  = #{termName},
          start_date = #{startDate},
          end_date   = #{endDate},
          is_current = #{isCurrent},
          updated_at = NOW()
        WHERE term_id = #{termId}
        """)
    int update(Term term);

    @Delete("DELETE FROM term WHERE term_id = #{id}")
    int delete(Long id);

    List<Term> selectAll();

    Term selectById(Long id);

    int deleteById(Long id);

    void updateById(Term term);
}
