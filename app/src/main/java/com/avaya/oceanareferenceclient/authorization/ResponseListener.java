package com.avaya.oceanareferenceclient.authorization;

public interface ResponseListener<T> {
    void done(T object);
}
