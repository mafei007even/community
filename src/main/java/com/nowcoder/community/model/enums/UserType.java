package com.nowcoder.community.model.enums;

/**
 * @author mafei007
 * @date 2020/4/7 22:53
 */


public enum UserType implements ValueEnum<Integer> {

    /**
     * 普通用户
     */
    ORDINARY(0),

    /**
     * 超级管理员
     */
    ADMIN(1),

    /**
     * 版主
     */
    POSTER(2);


    private final Integer value;

    UserType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }

}
