package com.example.leavesystem.mapper;

import com.example.leavesystem.entity.LoginToken;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTokenMapper {

    @Insert("""
        INSERT INTO login_token
          (user_type, user_id, role_code, token, expire_time, created_at)
        VALUES
          (#{userType}, #{userId}, #{roleCode}, #{token}, #{expireTime}, #{createdAt})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "tokenId")
    int insert(LoginToken loginToken);

    @Select("""
        SELECT *
        FROM login_token
        WHERE token = #{token}
        """)
    LoginToken findByToken(String token);

    @Select("""
        SELECT *
        FROM login_token
        WHERE token = #{token} AND expire_time > NOW()
        """)
    LoginToken findValid(String token);

    @Delete("""
        DELETE FROM login_token
        WHERE token = #{token}
        """)
    int deleteByToken(String token);

    @Delete("""
        DELETE FROM login_token
        WHERE expire_time < NOW()
        """)
    int deleteExpired();
}