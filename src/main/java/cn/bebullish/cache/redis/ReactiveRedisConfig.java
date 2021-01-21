package cn.bebullish.cache.redis;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.core.ReactiveZSetOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

import io.lettuce.core.RedisURI;

/**
 * Reactive redis config
 *
 * @author Marlon
 * @since 1.0.0
 */
@Configuration
@ComponentScan
public class ReactiveRedisConfig {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";

    private final ObjectMapper redisObjectMapper;
    private final RedisProperties redisProperties;

    public ReactiveRedisConfig(@Qualifier(value = "redisObjectMapper") ObjectMapper redisObjectMapper,
                               RedisProperties redisProperties) {
        this.redisObjectMapper = redisObjectMapper;
        this.redisProperties = redisProperties;
    }

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        RedisURI redisURI = RedisURI.create(redisProperties.getUri());
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisURI.getHost());
        configuration.setPort(redisURI.getPort());
        configuration.setDatabase(redisURI.getDatabase());
        Optional.ofNullable(redisURI.getPassword()).ifPresent(configuration::setPassword);
        redisProperties.printLog(configuration);
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(redisProperties.getTimeout())
                .poolConfig(localPoolConfig())
                .build();
        return new LettuceConnectionFactory(configuration, clientConfig);
    }

    @SuppressWarnings("rawtypes")
    private GenericObjectPoolConfig localPoolConfig() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(redisProperties.getMaxActive());
        config.setMaxIdle(redisProperties.getMaxIdle());
        config.setMinIdle(redisProperties.getMinIdle());
        config.setMaxWaitMillis(redisProperties.getMaxWait().toMillis());
        return config;
    }

    private RedisSerializationContext<String, Object> redisSerializationContext() {
        RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder = RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(redisObjectMapper);
        return builder.key(new StringRedisSerializer())
                .hashKey(new StringRedisSerializer())
                .value(jackson2JsonRedisSerializer)
                .hashValue(jackson2JsonRedisSerializer)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        // 配置日期转换
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // Date
        javaTimeModule.addSerializer(Date.class, new DateSerializer(false, new SimpleDateFormat(DATE_TIME_PATTERN)));
        javaTimeModule.addDeserializer(Date.class, new DateDeserializers.DateDeserializer(new DateDeserializers.DateDeserializer(), new SimpleDateFormat(DATE_TIME_PATTERN), DATE_TIME_PATTERN));
        // LocalDateTime
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        // LocalDate
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        // LocalTime
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

    @Bean
    ReactiveRedisOperations<String, Object> reactiveRedisOperations(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, redisSerializationContext());
    }

    @Bean
    ReactiveValueOperations<String, Object> reactiveValueOperations(ReactiveRedisOperations<String, Object> reactiveRedisOperations) {
        return reactiveRedisOperations.opsForValue();
    }

    @Bean
    ReactiveListOperations<String, Object> reactiveListOperations(ReactiveRedisOperations<String, Object> reactiveRedisOperations) {
        return reactiveRedisOperations.opsForList();
    }

    @Bean
    ReactiveHashOperations<String, String, Object> reactiveHashOperations(ReactiveRedisOperations<String, Object> reactiveRedisOperations) {
        return reactiveRedisOperations.opsForHash();
    }

    @Bean
    ReactiveSetOperations<String, Object> reactiveSetOperations(ReactiveRedisOperations<String, Object> reactiveRedisOperations) {
        return reactiveRedisOperations.opsForSet();
    }

    @Bean
    ReactiveZSetOperations<String, Object> reactiveZSetOperations(ReactiveRedisOperations<String, Object> reactiveRedisOperations) {
        return reactiveRedisOperations.opsForZSet();
    }

}
