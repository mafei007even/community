package com.nowcoder.community.model.enums;

/**
 * 不同事件在 kafka 中的主题
 *
 * @author mafei007
 * @date 2020/5/5 19:12
 */


public enum Topic implements ValueEnum<String> {

	/**
	 * 评论事件的 topic
	 */
	Comment("comment"),

	/**
	 * 点赞事件的 topic
	 */
	Like("like"),

	/**
	 * 关注事件的 topic
	 */
	Follow("follow"),

	/**
	 * 发布帖子
	 */
	Publish("publish"),

	/**
	 * 删帖事件
	 */
	Delete("delete");

	private final String value;

	Topic(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}
}
