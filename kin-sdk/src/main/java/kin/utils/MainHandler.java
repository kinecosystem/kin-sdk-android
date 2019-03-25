package kin.utils;

public interface MainHandler
{
    void post(Runnable runnable);

    void removeCallbacksAndMessages(Object o);
}
