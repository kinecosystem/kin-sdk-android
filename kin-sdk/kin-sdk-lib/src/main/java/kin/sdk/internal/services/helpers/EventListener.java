package kin.sdk.internal.services.helpers;


public interface EventListener<T> {

    void onEvent(T data);
}
