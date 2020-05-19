package com.nowcoder.community.controller;

import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.model.enums.Topic;
import com.nowcoder.community.model.event.Event;
import com.nowcoder.community.model.support.BaseResponse;
import com.nowcoder.community.utils.CodecUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 生成网站内的分享图，操作比较耗时，采用 kafka 异步实现
 * @author mafei007
 * @date 2020/5/19 15:46
 */

@Slf4j
@Controller
@Validated
public class ShareController {

	private final EventProducer eventProducer;

	@Value("${community.path.domain}")
	private String domain;

	@Value("${server.servlet.context-path}")
	private String contextPath;

	@Value("${wk.image.storage}")
	private String wkImageStorage;

	public ShareController(EventProducer eventProducer) {
		this.eventProducer = eventProducer;
	}

	@GetMapping("share")
	@ResponseBody
	public BaseResponse share(
			@NotBlank(message = "要分享的url不能为空") String htmlUrl) {

		String fileName = CodecUtils.generateUUID();

		// 异步生成长图
		Event event = Event.builder()
				.topic(Topic.Share)
				.build();
		event.setData("htmlUrl", htmlUrl)
				.setData("fileName", fileName)
				.setData("suffix", ".png");
		eventProducer.fireEvent(event);

		// 返回访问路径
		return BaseResponse.ok("success", domain + contextPath + "/share/image/" + fileName);
	}

	@GetMapping("share/image/{fileName}")
	public void getShareImage(@PathVariable @NotBlank(message = "url不能为空") String fileName,
							  HttpServletResponse response) {

		try {

			Path path = Paths.get(wkImageStorage, fileName + ".png");

			if (Files.exists(path)) {
				response.setContentType("image/png");
				ServletOutputStream outputStream = response.getOutputStream();

				Files.copy(path, outputStream);
			} else{
				response.setStatus(404);
			}
		} catch (IOException e) {
			log.error("读取分享图失败：" + e.getMessage(), e);
		}

	}

}
