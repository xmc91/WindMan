package com.play.windman.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ViewUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.play.windman.R;
import com.play.windman.UI.Activitys.GaoDeMapActivity;
import com.play.windman.utils.SPUtils;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import rx.functions.Action1;

public class BleActivity extends AppCompatActivity  {
    private static final String TAG = "BleActivity";

    private BluetoothAdapter mBluetoothAdapter;

    private String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    private String sid = "71623259-7848-43d1-b8c6-4928f6bf7867";
    private String cid_write = "71623259-7848-43d1-b8c6-4928f6bf7967";
    private String cid_notify = "71623259-7848-43d1-b8c6-4928f6bf7a67";

    private BluetoothGatt mGatt;
    private BluetoothManager bleManger;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(listenBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        RxPermissions.getInstance(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            Log.d(TAG, "call: 已同意权限");
                        } else {
                            Log.d(TAG, "call: 未同意权限");
                        }
                    }
                });
        registerBoradcastReceiver();
        bleManger = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bleManger.getAdapter();
        findViewById(R.id.bt_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
        findViewById(R.id.bt_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
        });
    }


    public static void actionStart(Context context, String data) {
        Intent intent = new Intent(context, BleActivity.class);
        intent.putExtra("data", data);
        context.startActivity(intent);
    }

    private void send() {
        try {
            if (mGatt != null) {
                byte head = (byte) 0x88;
                byte conten = 0x01;
                byte end = (byte) (head | conten);
                byte[] value = new byte[]{head, conten, end};
                Log.i(TAG, "send: " + bytesToHexString2(value));
                BluetoothGattCharacteristic mChar = mGatt.getService(UUID.fromString(sid))
                        .getCharacteristic(UUID.fromString(cid_write));
                mChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                mChar.setValue(value);
                mGatt.writeCharacteristic(mChar);
            } else {
                Log.e(TAG, "send: gatt is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "send: " + e.getMessage());
        }
    }

    /**
     * 扫描回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String name = device.getName();
            Log.d(TAG, "onLeScan: scaing ," + name);
            if (!TextUtils.isEmpty(name) && name.equals("VElecCar")) {
                Log.d(TAG, "onLeScan: 已发现VElecCar");
                device.connectGatt(BleActivity.this, false, gattCallback);
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    };
    /**
     * 数据回调
     */
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String address = gatt.getDevice().getAddress();
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                }
                Log.i(TAG, "gattCallback，连接上设备:" + address);
                boolean discover = gatt.discoverServices();
                Log.i(TAG, "gattCallback 发起发现服务:" + discover);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "gattCallback 断开:" + address + ", status = " + status);
//                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "发现服务 address = " + gatt.getDevice().getAddress());
                // 设置notify
                mGatt = gatt;
                String addr = gatt.getDevice().getAddress();
                SPUtils.setParam(BleActivity.this, "addr", addr);
                boolean isNotify = gatt.setCharacteristicNotification(
                        gatt.getService(UUID.fromString(sid))
                                .getCharacteristic(UUID.fromString(cid_notify)),
                        true);

                BluetoothGattDescriptor descriptor = gatt.getService(UUID.fromString(sid))
                        .getCharacteristic(UUID.fromString(cid_notify)).getDescriptor(UUID
                                .fromString(CLIENT_CHARACTERISTIC_CONFIG));
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    );
                    boolean notify = gatt.writeDescriptor(descriptor);
                    if (notify) {
                        Log.d(TAG, "notify 设置成功");
                    }
                }
            } else {
                Log.w(TAG, "onServicesDisc overed received: " + status);
                gatt.disconnect();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "写入回调：" + bytesToHexString2(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "Dev--->Phone：" + bytesToHexString2(characteristic.getValue()));
        }
    };

    private void registerBoradcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(listenBLE, filter);
    }

    BroadcastReceiver listenBLE = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            Log.d(TAG, "STATE_OFF 手机蓝牙关闭");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d(TAG, "STATE_TURNING_OFF 手机蓝牙正在关闭");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG, "STATE_ON 手机蓝牙开启");
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d(TAG, "STATE_TURNING_ON 手机蓝牙正在开启");
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    final String addr = (String) SPUtils.getParam(BleActivity.this, "addr", "");
                    Log.d(TAG, "onReceive: 已连接,sp 缓存地址：" + addr);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(addr);
                    if (device == null) {
                        Log.d(TAG, "onReceive: device is null");
                    } else {
                        device.connectGatt(BleActivity.this, false, new BluetoothGattCallback() {
                            @Override
                            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                                super.onConnectionStateChange(gatt, status, newState);
                                if (newState == BluetoothProfile.STATE_CONNECTED) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                                    }
                                    boolean discover = gatt.discoverServices();
                                    if (discover) {
                                        mGatt = gatt;
                                        send();
                                        Log.d(TAG, "connectGatt，，mGatt: " + mGatt);
                                    }
                                }
                            }
                        });
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Log.d(TAG, "onReceive: 已断开");
                    break;
            }
        }
    };

    private BluetoothDevice getConn() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {//得到连接状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);
            if (state == BluetoothAdapter.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothAdapter.STATE_CONNECTED");
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                Log.i(TAG, "devices:" + devices.size());

                for (BluetoothDevice device : devices) {
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if (isConnected) {
                        String n = device.getAddress();
                        Log.i("BLUETOOTH", "connected:" + device.getName());
                        String addr = (String) SPUtils.getParam(BleActivity.this, "addr", "");
                        if (!TextUtils.isEmpty(n) && n.equals(addr)) {
                            return device;
                        }
                    }
                }
            } else {
                Log.e(TAG, "getConn: 没有已连接的设备");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String bytesToHexString2(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv).append("-");
        }
        return stringBuilder.toString();
    }
}
