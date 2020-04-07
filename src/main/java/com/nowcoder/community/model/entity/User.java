package com.nowcoder.community.model.entity;

import com.nowcoder.community.model.enums.UserActivationStatus;
import com.nowcoder.community.model.enums.UserType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private UserType type;
    private UserActivationStatus status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;

}
