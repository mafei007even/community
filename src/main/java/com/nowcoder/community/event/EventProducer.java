package com.nowcoder.community.event;

import com.nowcoder.community.model.event.Event;
import com.nowcoder.community.utils.JsonUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author mafei007
 * @date 2020/5/5 18:55
 */

@Component
public class EventProducer {

	private final KafkaTemplate kafkaTemplate;

	public EventProducer(KafkaTemplate kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	/**
	 * 点赞、评论、关注时发送消息，异步处理
	 * 发帖时通知 es 建立索引
	 * @param event
	 */
	public void fireEvent(Event event){
		// 将事件发布到指定的主题
		kafkaTemplate.send(event.getTopic().getValue(), JsonUtils.objectToJson(event));
	}

}
