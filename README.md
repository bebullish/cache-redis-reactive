# cache-redis-reactive

## 仓库说明

自身打为 Jar 包为 SpringWebFlux 应用提供 reactive-redis 默认配置

## 如何引用

### 添加依赖

```groovy
repositories {
    maven {
        url 'https://marlon-maven.pkg.coding.net/repository/artifact/public/'
    }
}

dependencies {
    implementation 'cn.bebullish:cache-redis-reactive:1.0.0'
}
```

### 使配置生效

```java
@SpringBootApplication
@EnableReactiveRedis
```

### 自定义 value 序列化

```

@Configuration
public class JacksonObjectMapperConfiguration {

    @Bean
    ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

}


```

### 变量配置

[RedisProperties](https://github.com/bebullish/cache-redis-reactive/blob/master/src/main/java/cn/bebullish/cache/redis/RedisProperties.java)