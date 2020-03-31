package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Service;

/**
 * @author mafei007
 * @date 2020/3/30 19:11
 */

@Service
public class UserService {

    private UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    public User findUserById(Integer id){
        return userMapper.selectByPrimaryKey(id);
    }

}
