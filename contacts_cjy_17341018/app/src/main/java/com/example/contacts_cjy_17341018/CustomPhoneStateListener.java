package com.example.contacts_cjy_17341018;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Calendar;


class CustomPhoneStateListener extends PhoneStateListener {
    // 重写电话状态改变时触发的方法
    private Context context;
    private int checked = 0;
    private int judge = 0;
    private int beginTime = 0;
    private int endTime = 0;
    private Uri callRecordUri = Uri.parse("content://com.example.providers.RecordDB/");
    private Uri contactUri = Uri.parse("content://com.example.providers.ContactDB/");
    private ContentResolver resolver;
    private ContentObserver observer;
    private static final String TAG = "MyPhoneCallListener";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 0) {
                addNewRecord();
            }
        }
    };

    /********************************
     * 返回电话状态
     * <p>
     * CALL_STATE_IDLE 无任何状态时
     * CALL_STATE_OFFHOOK 接起电话时
     * CALL_STATE_RINGING 电话进来时
     ********************************/
    CustomPhoneStateListener(Context context) {
        this.context = context;
        this.resolver = context.getContentResolver();
        this.observer = new Observer(context, handler);
        resolver.registerContentObserver(CallLog.Calls.CONTENT_URI, false, observer);
    }

    public void setChecked(int i) {
        checked = i;
    }
    public void setBeginTime(int i) {
        beginTime = i;
    }
    public void setEndTime(int i) {
        endTime = i;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:// 电话挂断
                if (judge != 0) {
                    judge = 0;
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK: //电话通话的状态
                judge = 1;
                break;
            case TelephonyManager.CALL_STATE_RINGING: //电话响铃的状态
                judge = 2;
                Calendar cal = Calendar.getInstance();
                int time = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
                Log.v("nowTime", time + "");
                if (checked == 0 || time < beginTime || time > endTime) {
                    break;
                }
                Cursor cursor = resolver.query(contactUri, new String[]{"number", "whitelist"}, "number = ?", new String[]{incomingNumber}, null);
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToNext();
                    int judge = cursor.getInt(cursor.getColumnIndex("whitelist"));
                    if (judge == 0) {
                        try {
                            // 延迟5秒后自动挂断电话
                            // 首先拿到TelephonyManager
                            TelephonyManager telMag = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            Class<TelephonyManager> c = TelephonyManager.class;

                            // 再去反射TelephonyManager里面的私有方法 getITelephony 得到 ITelephony对象
                            Method mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
                            //允许访问私有方法
                            mthEndCall.setAccessible(true);
                            Object obj = mthEndCall.invoke(telMag, (Object[]) null);

                            // 再通过ITelephony对象去反射里面的endCall方法，挂断电话
                            Method mt = obj.getClass().getMethod("endCall");
                            //允许访问私有方法
                            mt.setAccessible(true);
                            mt.invoke(obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        // 延迟5秒后自动挂断电话
                        // 首先拿到TelephonyManager
                        TelephonyManager telMag = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        Class<TelephonyManager> c = TelephonyManager.class;

                        // 再去反射TelephonyManager里面的私有方法 getITelephony 得到 ITelephony对象
                        Method mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
                        //允许访问私有方法
                        mthEndCall.setAccessible(true);
                        Object obj = mthEndCall.invoke(telMag, (Object[]) null);

                        // 再通过ITelephony对象去反射里面的endCall方法，挂断电话
                        Method mt = obj.getClass().getMethod("endCall");
                        //允许访问私有方法
                        mt.setAccessible(true);
                        mt.invoke(obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();
                break;
        }
        super.onCallStateChanged(state, incomingNumber);
    }
    private void addNewRecord() {
        getCallHistory record = new getCallHistory(context);
        ContentValues contentValues = new ContentValues();
        String number = record.getNumber();
        Cursor cursor = resolver.query(contactUri, new String[]{"number", "name", "attribution"}, "number = ?", new String[]{number}, null);
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToNext();
            contentValues.put("name", cursor.getString(cursor.getColumnIndex("name")));
            contentValues.put("attribution", cursor.getString(cursor.getColumnIndex("attribution")));
        } else {
            contentValues.put("name", number);
            contentValues.put("attribution", new QueryAttribution().getAttribution(number));
        }
        cursor.close();
        cursor = resolver.query(callRecordUri, new String[]{"id"}, null, null, "id desc");
        int index;
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            index = cursor.getInt(0);
        } else {
            index = 0;
        }
        cursor.close();
        contentValues.put("id", index + 1);
        contentValues.put("number", number);
        contentValues.put("status", record.getType());
        contentValues.put("calltime", record.getDate());
        contentValues.put("duration", record.getDuration());
        resolver.insert(callRecordUri, contentValues);
        Intent intent = new Intent();
        intent.setAction("update");
        context.sendBroadcast(intent);
    }
}
