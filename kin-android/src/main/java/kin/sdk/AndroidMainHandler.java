package kin.sdk;

import android.os.Handler;
import android.os.Looper;

import kin.utils.MainHandler;

public class AndroidMainHandler implements MainHandler
{
    private Handler _handler = new Handler(Looper.getMainLooper());

    @Override
    public void post(Runnable runnable)
    {
        _handler.post(runnable);
    }

    @Override
    public void removeCallbacksAndMessages(Object o)
    {
        _handler.removeCallbacksAndMessages(o);
    }
}
