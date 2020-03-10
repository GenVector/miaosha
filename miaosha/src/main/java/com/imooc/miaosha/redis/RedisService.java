package com.imooc.miaosha.redis;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.*;

@Service
public class RedisService {

    private JedisPool jedisPool;

    @Autowired
    private  JedisPool autoJedisPool;

    public RedisService() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(10);
        poolConfig.setMaxWaitMillis(1000 * 100);
        poolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(poolConfig, "192.168.51.46", 6379, 100, "231514");
    }

    @Bean(name = "autoJedisPoolConfig")
    @ConfigurationProperties(prefix = "spring.redis.pool")
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
		//最大空闲数
		//config.setMaxIdle(10);
		//最小空闲数
		//config.setMinIdle(5);
		//最大链接数
		//config.setMaxTotal(20);
        System.out.println("默认值：" + config.getMaxIdle());
        System.out.println("默认值：" + config.getMinIdle());
        System.out.println("默认值：" + config.getMaxTotal());
        return config;
    }

    @Bean(name = "autoJedisConnectionFactory")
    @ConfigurationProperties(prefix = "spring.redis")
    public JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig config) {
        System.out.println("配置完毕：" + config.getMaxIdle());
        System.out.println("配置完毕：" + config.getMinIdle());
        System.out.println("配置完毕：" + config.getMaxTotal());

        JedisConnectionFactory factory = new JedisConnectionFactory();
        //关联链接池的配置对象
        factory.setPoolConfig(config);
        //配置链接Redis的信息
        //主机地址
		factory.setHostName("192.168.70.128");
		//端口
		factory.setPort(6379);
        return factory;
    }

    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        //关联
        template.setConnectionFactory(factory);

        //为key设置序列化器
        template.setKeySerializer(new StringRedisSerializer());
        //为value设置序列化器
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }

    //判断key是否存在
    public <T> boolean exists(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String tempkey = prefix.getPrefix();
            String realKey = tempkey + key;
            return jedis.exists(realKey);
            //连接池要释放掉
        } finally {
            returnToPool(jedis);
        }
    }

    //增加值（原子操作）：若key值不存在或者类型错误，把key赋值为0，再做加法；若key值正确，则返回value加1后的值
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            String tempkey = prefix.getPrefix();
            String realKey = tempkey + key;
            return jedis.incr(realKey);
            //连接池要释放掉
        } finally {
            returnToPool(jedis);
        }
    }

    //减少值（原子操作）：若key值不存在或者类型错误，把key赋值为0，再做减法；若key值正确，则返回value减1后的值
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            String tempkey = prefix.getPrefix();
            String realKey = tempkey + key;
            return jedis.decr(realKey);
            //连接池要释放掉
        } finally {
            returnToPool(jedis);
        }
    }

    //获取单个对象，转化为clazz类
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            String tempkey = prefix.getPrefix();
            String realKey = tempkey + key;
            jedis = jedisPool.getResource();
            String str = jedis.get(realKey);
            T t = stringToBean(str, clazz);
            return t;
            //连接池要释放掉
        } finally {
            returnToPool(jedis);
        }
    }

    //添加对象
    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        Jedis jedis = null;
        try {

            String tempkey = prefix.getPrefix();
            String realKey = tempkey + key;
            try {

                jedis = jedisPool.getResource();
            } catch (RuntimeException e) {
                if (jedis != null) {
                    jedisPool.returnBrokenResource(jedis);//获取连接失败时，应该返回给pool,否则每次发生异常将导致一个jedis对象没有被回收。
                }
            }
            String str = beanToString(value);
            if (str == null || str.length() <= 0) {
                return false;
            }
            int seconds = prefix.expireSeconds();
            if (seconds <= 0) {
                jedis.set(realKey, str);
            } else {
                //设置过期值
                jedis.setex(realKey, seconds, str);
            }
            return true;
            //连接池要释放掉
        } finally {
            returnToPool(jedis);
        }
    }

    //删除对象
    public <T> Boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();

            String tempkey = prefix.getPrefix();
            String realKey = tempkey + key;
            long res = jedis.del(realKey);
            return res > 0;
            //连接池要释放掉
        } finally {
            returnToPool(jedis);
        }
    }

    private <T> String beanToString(T value) {
        // TODO Auto-generated method stub
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return (String) value;
        } else if (clazz == Long.class || clazz == long.class) {
            return "" + value;
        } else {
            return JSON.toJSONString(value);
        }
    }

    private <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            //parseInt返回int基本数据类型
            //valueOf返回Integer对象
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(str);
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    private void returnToPool(Jedis jedis) {
        // TODO Auto-generated method stub
        if (jedis != null) {
            jedis.close();
        }
    }

    public boolean delete(KeyPrefix prefix) {
        if (prefix == null) {
            return false;
        }
        List<String> keys = scanKeys(prefix.getPrefix());
        if (keys == null || keys.size() <= 0) {
            return true;
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(keys.toArray(new String[0]));
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public List<String> scanKeys(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            List<String> keys = new ArrayList<String>();
            String cursor = "0";
            ScanParams sp = new ScanParams();
            sp.match("*" + key + "*");
            sp.count(100);
            do {
                ScanResult<String> ret = jedis.scan(cursor, sp);
                List<String> result = ret.getResult();
                if (result != null && result.size() > 0) {
                    keys.addAll(result);
                }
                //再处理cursor
                cursor = ret.getStringCursor();
            } while (!cursor.equals("0"));
            return keys;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
