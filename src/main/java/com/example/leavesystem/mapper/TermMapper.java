package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.Term;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TermMapper {

    // ===== 已有注解方法（保留） =====

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

    // ===== 兼容旧 Service/Controller 的方法（补齐注解，避免 BindingException） =====

    @Select("SELECT * FROM term ORDER BY term_id DESC")
    List<Term> selectAll();

    @Select("SELECT * FROM term WHERE term_id = #{id}")
    Term selectById(Long id);

    @Delete("DELETE FROM term WHERE term_id = #{id}")
    int deleteById(Long id);

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
    int updateById(Term term);

    @Update("""
    UPDATE term
    SET
      is_current = CASE WHEN term_id = #{termId} THEN 1 ELSE 0 END,
      updated_at = NOW()
    """)
    int setOnlyCurrent(@Param("termId") Long termId);

    @Update("""
    UPDATE term
    SET is_current = 0,
        updated_at = NOW()
    WHERE term_id = #{termId}
    """)
    int closeById(@Param("termId") Long termId);
}
