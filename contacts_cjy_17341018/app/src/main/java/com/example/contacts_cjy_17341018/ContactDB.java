package com.example.contacts_cjy_17341018;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ContactDB extends ContentProvider {
    private ContactDBHandler contactDBHandler;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        contactDBHandler = new ContactDBHandler(getContext(), "contacts", null, 1);
        db = contactDBHandler.getWritableDatabase();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        db.insert("contacts", null, contentValues);
        return uri;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereargs) {
        int ret = db.delete("contacts", where, whereargs);
        return ret;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] whereargs) {
        int ret = db.update("contacts", contentValues, where, whereargs);
        return ret;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String where, String[] whereargs, String sortorder) {
        return db.query("contacts", projection, where, whereargs, null, null, sortorder);
    }
}
