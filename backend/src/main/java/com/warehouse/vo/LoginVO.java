package com.warehouse.vo;

import lombok.Data;

@Data
public class LoginVO {
    private Long id;
    private String account;
    private String name;
    private String role;
    private String roleName;
    private String token;
}
