package com.atguigu.gulimall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 配置文件中的东西没有用上
 *
 * 1、原来和配置文件绑定的配置类是这样子的
 *      @ConfigurationProperties(prefix = "spring.cache")
 *      public class CacheProperties
 *
 * 2、要让他生效
 *      @EnableConfigurationProperties(CacheProperties.class)
 *
 * @return
 */
@EnableConfigurationProperties(CacheProperties.class)   // 这样就能拿到CacheProperties
@EnableCaching
@Configuration
public class MyCacheConfig {

// 将cacheProperties放到方法参数位置，就能自动从容器中获取到
//    @Autowired
//    CacheProperties cacheProperties;

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        // 先拿到默认配置对象
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        // key序列化， 使用默认的String序列化器就行，因为redis的key都是字符串
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        // value序列化，使用泛型Jackson2Json序列化器，因为是Object
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        //将配置文件（application.properties）中的所有配置都让它生效，比如设定的ttl
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        if(redisProperties.getTimeToLive() != null){
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if(redisProperties.getKeyPrefix() != null){
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        if(!redisProperties.isCacheNullValues()){
            config = config.disableCachingNullValues();
        }
        if(!redisProperties.isUseKeyPrefix()){
            config = config.disableKeyPrefix();
        }
        return config;
    }


}
