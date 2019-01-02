package com.ljheee.lock;


/**
 *
 */
public interface IDistributedLock {
    public boolean lock(String key) throws Exception;

    public void unLock(String key) throws Exception;
}
