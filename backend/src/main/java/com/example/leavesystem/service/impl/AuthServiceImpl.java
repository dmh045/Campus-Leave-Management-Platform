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

// 如果你使用 BCrypt（需要依赖 spring-security-crypto 或 spring-boot-starter-security）
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final StudentMapper studentMapper;
    private final StaffMapper staffMapper;
    private final StaffRoleMapper staffRoleMapper;
    private final LoginTokenMapper loginTokenMapper;

    // private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public LoginResponse login(LoginRequest request) {
        String loginType = request.getLoginType();
        String username  = request.getUsername();
        String password  = request.getPassword();

        if (loginType == null || username == null || password == null) {
            throw new IllegalArgumentException("登录参数不完整");
        }

        loginType = loginType.toUpperCase();
        LocalDateTime now  = LocalDateTime.now();

        if ("STUDENT".equals(loginType)) {
            Student stu = studentMapper.findByStudentNo(username);
            if (stu == null) {
                throw new IllegalArgumentException("学号或密码错误");
            }

            // ===== MOD(1): 校验密码 =====
            if (stu.getPassword() == null || !stu.getPassword().equals(password)) {
                // 如果你存的是 BCrypt hash，用这一句替换：
                // if (stu.getPassword() == null || !encoder.matches(password, stu.getPassword())) {
                throw new IllegalArgumentException("学号或密码错误");
            }

            String token = generateToken();

            LoginToken lt = new LoginToken();
            lt.setUserType("STUDENT");
            lt.setUserId(stu.getStudentId());
            lt.setRoleCode("STUDENT");
            lt.setToken(token);
            lt.setCreatedAt(now);
            lt.setExpireTime(now.plusHours(2));

            loginTokenMapper.insert(lt);

            LoginResponse resp = new LoginResponse();
            resp.setToken(token);
            resp.setUserType("STUDENT");
            resp.setUserId(stu.getStudentId());
            resp.setDisplayName(stu.getName()); // 你这里用的是 getName，确保实体字段一致
            resp.setRoleCode("STUDENT");
            return resp;

        } else if ("STAFF".equals(loginType)) {
            Staff staff = staffMapper.selectByStaffNo(username);
            if (staff == null) {
                throw new IllegalArgumentException("工号或密码错误");
            }

            // ===== MOD(2): 校验密码 =====
            if (staff.getPassword() == null || !staff.getPassword().equals(password)) {
                // 如果你存的是 BCrypt hash，用这一句替换：
                // if (staff.getPassword() == null || !encoder.matches(password, staff.getPassword())) {
                throw new IllegalArgumentException("工号或密码错误");
            }

            // ===== MOD(3): roles null 安全 =====
            List<StaffRole> roles = staffRoleMapper.findByStaffId(staff.getStaffId());
            if (roles == null) roles = Collections.emptyList();

            // 取第一个角色
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

            LoginResponse resp = new LoginResponse();
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
        if (token == null || token.isEmpty()) return;
        loginTokenMapper.deleteByToken(token);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
