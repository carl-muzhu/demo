package org.muzhu.biz.disLock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;

/**
 * @auther muzhu
 * 2019/9/23.
 * 在同一个jvm进程中时，可以使用JUC提供的一些锁来解决多个线程竞争同一个共享资源时候的线程安全问题.
 * 但是当多个不同机器上的不同jvm进程共同竞争同一个共享资源时候，juc包的锁就无能无力了，这时候就需要分布式锁了。
 * 常见的有使用zk的最小版本，redis的set函数，数据库锁来实现，本节我们谈谈Redis单实例情况下使用set函数来实现分布式锁。
 */
public class RedisDistributeLock {
    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;

    private static void validParam(JedisPool jedisPool, String lockKey, String requestId, int expireTime) {
        if (null == jedisPool) {
            throw new IllegalArgumentException("jedisPool obj is null");
        }

        if (null == lockKey || "".equals(lockKey)) {
            throw new IllegalArgumentException("lock key  is blank");
        }

        if (null == requestId || "".equals(requestId)) {
            throw new IllegalArgumentException("requestId is blank");
        }

        if (expireTime < 0) {
            throw new IllegalArgumentException("expireTime is not allowed less zero");
        }
    }

    /**
     * @param jedisPool
     * @param lockKey
     * @param requestId
     * @param expireTime
     * @return
     */
    public static boolean tryLock(JedisPool jedisPool, String lockKey, String requestId, int expireTime) {

        validParam(jedisPool, lockKey, requestId, expireTime);

        Jedis jedis = null;
        try {

            jedis = jedisPool.getResource();
            SetParams setParams = SetParams.setParams();
            setParams.px(expireTime);
            setParams.nx();
            String result = jedis.set(lockKey, requestId, setParams);

            if (LOCK_SUCCESS.equals(result)) {
                return true;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }

        return false;
    }

    /**
     * @param jedisPool
     * @param lockKey
     * @param requestId
     * @param expireTime
     */
    public static void lock(JedisPool jedisPool, String lockKey, String requestId, int expireTime) {

        validParam(jedisPool, lockKey, requestId, expireTime);

        while (true) {
            if (tryLock(jedisPool, lockKey, requestId, expireTime)) {
                return;
            }
        }
    }

    /**
     * @param jedisPool
     * @param lockKey
     * @param requestId
     * @return
     */
    public static boolean unLock(JedisPool jedisPool, String lockKey, String requestId) {

        validParam(jedisPool, lockKey, requestId, 1);

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

        Jedis jedis = null;
        try {

            jedis = jedisPool.getResource();
            Object result = jedis.eval(script, Collections.singletonList(lockKey),
                    Collections.singletonList(requestId));

            if (RELEASE_SUCCESS.equals(result)) {
                return true;
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
        return false;

    }
}
