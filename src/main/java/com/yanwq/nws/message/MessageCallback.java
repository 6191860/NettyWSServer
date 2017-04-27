package com.yanwq.nws.message;

/**
 * Created by dodoca_android on 2017/4/27.
 */
public interface MessageCallback {

    void onSuccess(String message);

    void onFailure(String message, FailureType failureType);

    public enum FailureType {
        UNKNOWN_CHANNEL, UNKNOWN_USER
    }
}
