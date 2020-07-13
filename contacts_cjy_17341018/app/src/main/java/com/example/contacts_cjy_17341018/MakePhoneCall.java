package com.example.contacts_cjy_17341018;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class MakePhoneCall {
    private Uri callRecordUri = Uri.parse("content://com.example.providers.RecordDB/");
    private Uri contactUri = Uri.parse("content://com.example.providers.ContactDB/");
    private ContentResolver resolver;
    private Context context;

    public MakePhoneCall(Context context, ContentResolver resolver) {
        this.context = context;
        this.resolver = resolver;
    }

    public void makeCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "没有权限，请给予权限！", Toast.LENGTH_LONG).show();
        }
    }
}
