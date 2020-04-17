package com.nowcoder.community.model.enums;

/**
 * comment 评论表中 entity_type 字段
 * 就是当前一条记录是属于帖子下的直接评论，还是评论的评论
 * 可以扩展出更多别的类型的评论
 * : 课程的评论...
 *
 * @author mafei007
 * @date 2020/4/9 16:29
 */


public enum CommentEntityType implements ValueEnum<Integer> {

    /**
     * 帖子的评论
     */
    POST(1),

    /**
     * 评论的评论
     */
    COMMENT(2);


    private final Integer value;

    CommentEntityType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }


}
