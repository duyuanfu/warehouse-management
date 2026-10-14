package com.warehouse.controller;

import com.warehouse.common.Result;
import com.warehouse.dto.LoginDTO;
import com.warehouse.service.EmployeeService;
import com.warehouse.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        LoginVO vo = employeeService.login(loginDTO);
        return Result.success("登录成功", vo);
    }
}
