package com.nowcoder.community.model.enums;

/**
 * 私信消息的状态
 *
 * @author mafei007
 * @date 2020/4/18 15:12
 */


public enum MessageStatus implements ValueEnum<Integer> {

	/**
	 * 未读
	 */
	UNREAD(0),

	/**
	 * 以读
	 */
	READED(1),

	/**
	 * 删除
	 */
	DELETED(2);

	private Integer value;

	MessageStatus(Integer value) {
		this.value = value;
	}

	@Override
	public Integer getValue() {
		return value;
	}
}
