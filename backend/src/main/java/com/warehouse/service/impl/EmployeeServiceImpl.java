package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.dto.LoginDTO;
import com.warehouse.entity.Employee;
import com.warehouse.mapper.EmployeeMapper;
import com.warehouse.service.EmployeeService;
import com.warehouse.vo.LoginVO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        Employee employee = this.getOne(new LambdaQueryWrapper<Employee>()
                .eq(Employee::getAccount, loginDTO.getAccount()));

        if (employee == null) {
            throw new BusinessException("账号不存在");
        }

        if (!employee.getPassword().equals(loginDTO.getPassword())) {
            throw new BusinessException("密码错误");
        }

        if (employee.getStatus() == null || employee.getStatus() == 0) {
            throw new BusinessException("该员工账号已被停用，请联系主管");
        }

        LoginVO vo = new LoginVO();
        vo.setId(employee.getId());
        vo.setAccount(employee.getAccount());
        vo.setName(employee.getName());
        vo.setRole(employee.getRole());
        vo.setRoleName("SUPERVISOR".equalsIgnoreCase(employee.getRole()) ? "主管" : "理货员");
        // 生成模拟 Token
        vo.setToken(UUID.randomUUID().toString().replace("-", ""));
        return vo;
    }
}
