package com.bobsystem.exercise.commons;

import com.yy.yycloud.module.commons.ABaseModel;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Redis 的读写时机：
 *   add  加入 Redis；但如果实体有部分字段是数据库层面生成的，并且他们是关键字段，不应该加入 Redis
 *   update 不加入 Redis，因为编辑时的数据部分时候不全面；而应该从 Redis 删除
 *   delete 从 Redis 删除
 *
 * 三类业务场景：
 *   1. 频繁获取单个对象场景
 *   2. 频繁获取全部列表的场景，应把列表缓存起来
 *   3. 频繁获取对象中部分数据的场景
 */
public class RedisKit {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisKit.class);
    private static final int DB_INDEX = 0;

    //region REDIS_KEY
    //endregion

    //region 初始化 Redis 池
    private static final JedisPool JEDIS_POOL;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        int maxTotal = SystemParameter.redisMaxTotal;
        int maxIdle = SystemParameter.redisMaxIdle;
        int minIdle = SystemParameter.redisMinIdle;
        if (SystemParameter.devMode) {
            maxTotal = 200;
            maxIdle = 50;
            minIdle = 10;
        }
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        JEDIS_POOL = new JedisPool(
            config,
            SystemParameter.redisHost,
            SystemParameter.redisPort,
            Protocol.DEFAULT_TIMEOUT,
            SystemParameter.redisPassword);
    }
    //endregion

    //region 获取 Jedis 客户端。记得用完关闭客户端 Jedis.close();
    public static Jedis getJedis() {
        Jedis client = JEDIS_POOL.getResource();
        client.select(DB_INDEX);
        return client;
    }

    public static void destroy() {
        JEDIS_POOL.destroy();
        LOGGER.info("手动关闭 Redis 连接池");
    }
    //endregion

    //region 字符串的操作
    //region public static <T> T get()
    @SuppressWarnings("unchecked")
    public static <T> T get(byte[] key) {
        try (Jedis jedis = RedisKit.getJedis()) {
            if (key != null) {
                byte[] bytes = jedis.get(key);
                if (bytes != null) {
                    return (T)Serializer.unserizlize(bytes);
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public static <T> T get(String key) {
        LOGGER.debug("get(key) key=" + key);
        try {
            return get(key.getBytes("utf-8"));
        }
        catch (UnsupportedEncodingException ex) {
            LOGGER.error("不支持 utf-8 还怎么玩", ex);
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }
    //endregion

    //region public static String set(String key, ABaseModel model)
    public static String set(String key, Object obj) {
        return set(key, obj, 0);
    }

    public static String set(byte[] key, Object obj) {
        return set(key, obj, 0);
    }

    // 设置有效期的方式是 expireAt
    public static String set(String key, Object obj, long milliseconds) {
        milliseconds -= System.currentTimeMillis();
        milliseconds /= 1000;
        return set(key, obj, (int)milliseconds);
    }

    // 设置有效期的方式是 expire
    public static String set(String key, Object obj, int seconds) {
        String result = "";
        if (obj == null) return result;
        try {
            LOGGER.debug("set(key, obj, seconds) key=" + key);
            return set(key.getBytes("utf-8"), obj, seconds);
        }
        catch (UnsupportedEncodingException ex) {
            LOGGER.error("不支持 utf-8 还怎么玩", ex);
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return result;
    }

    // 设置有效期的方式是 expire
    public static String set(byte[] key, Object obj, int seconds) {
        String result = "";
        if (obj == null) return result;
        try (Jedis jedis = RedisKit.getJedis()) {
            byte[] bytes = Serializer.serialize(obj);
            if (bytes != null) {
                result = jedis.set(key, bytes);
                if (seconds != 0) {
                    jedis.expire(key, seconds);
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return result;
    }
    //endregion

    public static Long del(String key) {
        try (Jedis jedis = RedisKit.getJedis()) {
            LOGGER.debug("del(key) key=" + key);
            return jedis.del(key);
        }
    }
    //endregion

    //region Hash 的操作
    //region public static Long hset()
    public static Long hset(String key, ABaseModel model) {
        Long result = -1L;
        if (model == null) return result;
        try (Jedis jedis = RedisKit.getJedis()) {
            String identifier = model.identifier();
            byte[] obj = model.serialize();
            if (obj != null) {
                result = jedis.hset(key.getBytes("utf-8"), identifier.getBytes("utf-8"), obj);
            }
            LOGGER.debug(String.format("hset(key, model) key=%s, field=%s", key, identifier));
        }
        catch (UnsupportedEncodingException ex) {
            LOGGER.error("不支持 utf-8 还怎么玩", ex);
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return result;
    }

    //public static Long hset(String key, List<? extends ABaseModel> list) {
    //    Long result = -1L;
    //    if (list == null || list.size() == 0) return result;
    //    Jedis jedis = RedisKit.getJedis();
    //    for (ABaseModel model : list) {
    //        if (model == null) continue;
    //        try {
    //            String identifier = model.identifier();
    //            byte[] obj = model.serialize();
    //            if (obj != null) {
    //                result = jedis.hset(key.getBytes("utf-8"), identifier.getBytes("utf-8"), obj);
    //            }
    //            LOGGER.debug(String.format("hset(key, list) key=%s, field=%s", key, identifier));
    //        }
    //        catch (UnsupportedEncodingException ex) {
    //            LOGGER.error("不支持 utf-8 还怎么玩", ex);
    //            break;
    //        }
    //        catch (Exception ex) {
    //            LOGGER.error(ex.getMessage(), ex);
    //            //continue;
    //        }
    //    }
    //    jedis.close();
    //    return result;
    //}

    public static Long hset(String key, String identifier, Object obj) {
        Long result = -1L;
        if (StringUtils.isBlank(identifier) || obj == null) return result;
        try (Jedis jedis = RedisKit.getJedis()) {
            byte[] bytes = Serializer.serialize(obj);
            if (bytes != null) {
                result = jedis.hset(key.getBytes("utf-8"), identifier.getBytes("utf-8"), bytes);
            }
            LOGGER.debug("hset(key, identifier, model) key={}, field={}",
                key, identifier);
        }
        catch (UnsupportedEncodingException ex) {
            LOGGER.error("不支持 utf-8 还怎么玩", ex);
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return result;
    }
    //endregion

    //region public static <T extends ABaseModel> T hget()
    @SuppressWarnings("unchecked")
    public static <T> T hget(String key, String field) {
        if (StringUtils.isBlank(field)) return null;
        try (Jedis jedis = RedisKit.getJedis()) {
            LOGGER.debug("hget(key, field) key={}, field={}", key, field);
            byte[] bytes = jedis.hget(key.getBytes("utf-8"), field.getBytes("utf-8"));
            if (bytes != null) {
                return (T)Serializer.unserizlize(bytes);
            }
        }
        catch (UnsupportedEncodingException ex) {
            LOGGER.error("不支持 utf-8 还怎么玩", ex);
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends ABaseModel> T hget(String key, ABaseModel model) {
        if (model == null) return null;
        try (Jedis jedis = RedisKit.getJedis()) {
            String identifier = model.identifier();
            LOGGER.debug("hget(key, model) key={}, field={}", key, identifier);
            byte[] bytes = jedis.hget(key.getBytes(Protocol.CHARSET),
                identifier.getBytes(Protocol.CHARSET));
            if (bytes != null) {
                return (T)Serializer.unserizlize(bytes);
            }
        }
        catch (UnsupportedEncodingException ex) {
            LOGGER.error("不支持 utf-8 还怎么玩", ex);
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }
    //endregion

    //region public static Long hdel()
    public static Long hdel(String key, String identifier) {
        if (StringUtils.isBlank(identifier)) return 0L;
        try (Jedis jedis = RedisKit.getJedis()) {
            LOGGER.debug("hdel(key, identifier) key={}, field={}", key, identifier);
            return jedis.hdel(key, identifier);
        }
    }

    public static Long hdel(String key, List<String> identifiers) {
        if (identifiers == null || identifiers.size() == 0) return -1L;
        try (Jedis jedis = RedisKit.getJedis()) {
            LOGGER.debug("hdel(String key, List<String>) key={}, field={}",
                key, identifiers);
            return jedis.hdel(key, identifiers.toArray(new String[] {}));
        }
    }

    public static Long hdelex(String key, List<? extends ABaseModel> list) {
        if (list == null || list.size() == 0) return -1L;
        List<String> fields = new LinkedList<>();
        for (ABaseModel model : list) {
            fields.add(model.identifier());
        }
        return hdel(key, fields);
    }

    public static Long hdel(String key, ABaseModel model) {
        if (model == null) return 0L;
        try (Jedis jedis = RedisKit.getJedis()) {
            String identifier = model.identifier();
            LOGGER.debug("hdel(key, model) key={}, field={}", key, identifier);
            return jedis.hdel(key, identifier);
        }
    }
    //endregion
    //endregion

    //region Key 操作
    public static Set<String> keys(String pattern) {
        if (StringUtils.isBlank(pattern)) return null;
        try (Jedis jedis = RedisKit.getJedis()) {
            LOGGER.debug("keys(pattern) pattern=" + pattern);
            return jedis.keys(pattern);
        }
    }

    public static int count(String pattern) {
        Set<String> keys = keys(pattern);
        if (keys == null) return 0;
        return keys.size();
    }
    //endregion

    //region constructor
    private RedisKit() { }
    //endregion
}
