package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("employee")
public class Employee implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    /**
     * 姓名
     */
    private String name;

    /**
     * 角色: SUPERVISOR(主管) / TALLYMAN(理货员)
     */
    private String role;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态: 1正常 0停用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
