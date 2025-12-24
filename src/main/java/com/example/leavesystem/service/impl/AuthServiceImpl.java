package com.example.leavesystem.service.impl;

import com.example.leavesystem.dto.LoginRequest;
import com.example.leavesystem.dto.LoginResponse;
import com.example.leavesystem.entity.LoginToken;
import com.example.leavesystem.entity.StaffRole;
import com.example.leavesystem.entity.Student;
import com.example.leavesystem.entity.Staff;
import com.example.leavesystem.mapper.LoginTokenMapper;
import com.example.leavesystem.mapper.StaffMapper;
import com.example.leavesystem.mapper.StaffRoleMapper;
import com.example.leavesystem.mapper.StudentMapper;
import com.example.leavesystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StudentMapper studentMapper;
    private final StaffMapper staffMapper;
    private final StaffRoleMapper staffRoleMapper;
    private final LoginTokenMapper loginTokenMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        String loginType = request.getLoginType();
        String username  = request.getUsername();
        String password  = request.getPassword();

        if (loginType == null || username == null || password == null) {
            throw new IllegalArgumentException("登录参数不完整");
        }

        loginType = loginType.toUpperCase();

        LoginResponse resp = new LoginResponse();
        LocalDateTime now  = LocalDateTime.now();

        if ("STUDENT".equals(loginType)) {
            // 用学号 + 密码查学生
            Student stu = studentMapper.findByStudentNo(username);
            if (stu == null) {
                throw new IllegalArgumentException("学号或密码错误");
            }

            String token = generateToken();

            LoginToken lt = new LoginToken();
            lt.setUserType("STUDENT");
            lt.setUserId(stu.getStudentId());
            lt.setRoleCode("STUDENT");
            lt.setToken(token);
            lt.setCreatedAt(now);
            lt.setExpireTime(now.plusHours(2));  // token 有效期 2 小时

            loginTokenMapper.insert(lt);

            resp.setToken(token);
            resp.setUserType("STUDENT");
            resp.setUserId(stu.getStudentId());
            resp.setDisplayName(stu.getName());
            resp.setRoleCode("STUDENT");
            return resp;

        } else if ("STAFF".equals(loginType)) {
            // 用工号 + 密码查教职工
            Staff staff = staffMapper.selectByStaffNo(username);
            if (staff == null) {
                throw new IllegalArgumentException("工号或密码错误");
            }

            // 查一下该 staff 的角色（取第一个）
            List<StaffRole> roles = staffRoleMapper.findByStaffId(staff.getStaffId());
            String roleCode = roles.isEmpty() ? "STAFF" : roles.get(0).getRoleCode();

            String token = generateToken();

            LoginToken lt = new LoginToken();
            lt.setUserType("STAFF");
            lt.setUserId(staff.getStaffId());
            lt.setRoleCode(roleCode);
            lt.setToken(token);
            lt.setCreatedAt(now);
            lt.setExpireTime(now.plusHours(2));

            loginTokenMapper.insert(lt);

            resp.setToken(token);
            resp.setUserType("STAFF");
            resp.setUserId(staff.getStaffId());
            resp.setDisplayName(staff.getName());
            resp.setRoleCode(roleCode);
            return resp;

        } else {
            throw new IllegalArgumentException("不支持的登录类型: " + loginType);
        }
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        loginTokenMapper.deleteByToken(token);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
