package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.LoginRequiredInterceptor;
import com.nowcoder.community.controller.interceptor.LoginTicketInterceptor;
import com.nowcoder.community.controller.interceptor.MessageCountInterceptor;
import com.nowcoder.community.factory.StringToEnumConverterFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author mafei007
 * @date 2020/4/3 23:09
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	private final LoginTicketInterceptor loginTicketInterceptor;
	// private final LoginRequiredInterceptor loginRequiredInterceptor;
	private final MessageCountInterceptor messageCountInterceptor;

	public WebMvcConfig(LoginTicketInterceptor loginTicketInterceptor/*, LoginRequiredInterceptor loginRequiredInterceptor*/, MessageCountInterceptor messageCountInterceptor) {
		this.loginTicketInterceptor = loginTicketInterceptor;
		// this.loginRequiredInterceptor = loginRequiredInterceptor;
		this.messageCountInterceptor = messageCountInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {

		registry.addInterceptor(loginTicketInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg");

		// registry.addInterceptor(loginRequiredInterceptor)
		// 		.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg");

		registry.addInterceptor(messageCountInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.jpeg");

	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverterFactory(new StringToEnumConverterFactory());
	}


	/**
	 * 新增枚举转换器
	 *
	 * @param converters
	 */
/*
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.serializationInclusion(JsonInclude.Include.NON_NULL);
		ObjectMapper objectMapper = builder.build();

		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addSerializer(ValueEnum.class, new JacksonEnumComponent.ValueEnumJsonSerializer());

		objectMapper.registerModule(simpleModule);
		objectMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
		converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
	}
*/


}
