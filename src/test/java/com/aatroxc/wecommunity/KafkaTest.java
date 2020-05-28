package com.aatroxc.wecommunity;

import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author mafei007
 * @date 2020/5/5 18:04
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTest {

	@Autowired
	private KafkaProducer kafkaProducer;

	@SneakyThrows
	@Test
	public void testKafka() {
		kafkaProducer.sendMessage("test", "你好");
		kafkaProducer.sendMessage("test", "在在");

		System.in.read();
	}

}

@Component
class KafkaProducer {

	private final KafkaTemplate kafkaTemplate;

	KafkaProducer(KafkaTemplate kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendMessage(String topic, String content) {
		kafkaTemplate.send(topic, content);
	}

}

@Component
class KafkaConsumer {

	/**
	 * 去test 主题读取消息，有就消费，没有就阻塞等待
	 */
	@KafkaListener(topics = {"test"})
	public void handleMessage(ConsumerRecord record) {
		System.out.println(record.value());
	}

}