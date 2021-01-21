package cn.bebullish.cache.redis;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

@SpringBootApplication
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class RedisTest {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(RedisTest.class, args);
    }

    @Resource
    private ReactiveValueOperations<String, Object> reactiveValueOperations;
    @Resource
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    @Resource
    private ObjectMapper redisObjectMapper;

    @Test
    public void mainTest() {
        testExpire();
    }

    private void testExpire() {
        reactiveValueOperations.set("key", "2")
                .flatMap(it -> reactiveRedisTemplate.getExpire("key"))
                .filter(it -> it.getSeconds() == 0)
                .flatMap(it -> reactiveRedisTemplate.delete("key"))
                .filter(it -> it > 0)
                .switchIfEmpty(Mono.error(new RuntimeException("报错了"))).subscribe();
    }

}
