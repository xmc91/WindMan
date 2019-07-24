package com.play.windman;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationMonitor extends NotificationListenerService {
        @Override  
        public void onNotificationPosted(StatusBarNotification sbn) {
              //有新通知添加的时候调用  
              Log.e("onNotificationPosted","posted");
        }  

        @Override  
        public void onNotificationRemoved(StatusBarNotification sbn) {
              //通知被移除的时候调用  
              Log.e("onNotificationRemoved","posted");     
        }  
}