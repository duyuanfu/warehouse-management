package com.warehouse.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.dto.LoginDTO;
import com.warehouse.entity.Employee;
import com.warehouse.vo.LoginVO;

public interface EmployeeService extends IService<Employee> {
    LoginVO login(LoginDTO loginDTO);
}
