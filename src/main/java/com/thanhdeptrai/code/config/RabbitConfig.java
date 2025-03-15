package com.thanhdeptrai.code.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;


@Configuration
public class RabbitConfig {
    @Bean
    public Queue bookingQueue() {
        return new Queue("bookingQueue");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setIdClassMapping(Collections.singletonMap("bookingRequest", com.thanhdeptrai.code.dto.BookingRequest.class));
        typeMapper.setTrustedPackages("com.thanhdeptrai.code.dto"); // Allow package

        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
