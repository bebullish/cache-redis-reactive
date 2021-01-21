package cn.bebullish.cache.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

import javax.validation.constraints.NotEmpty;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * See {@link ReactiveRedisConfig}
 *
 * @author Marlon
 * @since 1.0.0
 */
@Data
@Slf4j
@Validated
@Configuration
@ConfigurationProperties(value = "redis")
public class RedisProperties {

    /**
     * <h3>redis 连接地址</h3>
     *
     * <b>Redis Standalone</b> <blockquote> <i>redis</i><b>{@code ://}</b>[[<i>username</i>{@code :}]<i>password@</i>]<i>host</i>
     * [<b>{@code :} </b> <i>port</i>][<b>{@code /}</b><i>database</i>][<b>{@code ?}</b>
     * [<i>timeout=timeout</i>[<i>d|h|m|s|ms|us|ns</i>]] [ <i>&amp;database=database</i>] [<i>&amp;clientName=clientName</i>]]
     * </blockquote>
     */
    @NotEmpty
    private String uri;
    /**
     * 打印 redis 配置日志
     */
    private Boolean printLog = true;
    /**
     * redis 连接最大同时活跃数
     */
    private Integer maxActive = 8;
    /**
     * redis 连接最大空闲数
     */
    private Integer maxIdle = 8;
    /**
     * redis 最长等待时长
     */
    private Duration maxWait = Duration.ofMillis(-1);
    /**
     * redis 连接最小空闲数
     */
    private Integer minIdle = 0;
    /**
     * redis 命令超时时间
     */
    private Duration timeout = Duration.ofSeconds(60);

    public void printLog(RedisStandaloneConfiguration configuration) {
        if (printLog) {
            log.info("[redis] host={}, port={}, database={}, password={}",
                    configuration.getHostName(),
                    configuration.getPort(),
                    configuration.getDatabase(),
                    configuration.getPassword().isPresent());
            log.info("[redis] maxActive={}, maxIdle={}, minIdle={}, maxWait={}ms, timeout={}ms",
                    maxActive,
                    maxIdle,
                    minIdle,
                    maxWait.toMillis(),
                    timeout.toMillis());
        }
    }

}
