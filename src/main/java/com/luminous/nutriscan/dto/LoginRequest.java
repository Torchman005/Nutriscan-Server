package com.luminous.nutriscan.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String phoneNumber;
    private String password;
    private String loginType; // "PHONE" or "WECHAT"
    private String wechatOpenId;
}
