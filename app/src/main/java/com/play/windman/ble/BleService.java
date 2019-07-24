package com.play.windman.ble;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * auth:大宛布衣听水凉
 * date:2019/7/24
 * des:
 */
public class BleService extends Service {
    private static final String TAG = "BleService";

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: vtbleservice");
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BleService getService() {
            return BleService.this;
        }
    }
}
