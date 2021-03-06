package com.example.contacts_cjy_17341018;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitmapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContactInfoFragment extends Fragment {
    private String name;
    private Uri contactUri = Uri.parse("content://com.example.providers.ContactDB/");
    private Uri callRecordUri = Uri.parse("content://com.example.providers.RecordDB/");
    private ContentResolver resolver;
    private ListView listView;
    private ArrayList<Map<String, Object>> lists;
    private SimpleAdapter adapter;
    private Toolbar toolbar;
    private AlertDialog alertDialog;

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle saved) {
        View rootView = inflater.inflate(R.layout.contact_info_fragment, viewGroup, false);
        resolver = getActivity().getContentResolver();
        setListView(rootView);
        setToolBar(rootView);
        return rootView;
    }

    private void setListView(View rootView) {
        listView = (ListView) rootView.findViewById(R.id.contact_info_listview);
        Cursor cursor = resolver.query(contactUri, new String[]{"number", "attribution"},
                "name = ?", new String[]{name}, null);
        lists = new ArrayList<>();
        while (cursor != null && cursor.moveToNext()) {
            Map<String, Object> map = new HashMap<>();
            map.put("number", cursor.getString(cursor.getColumnIndex("number")));
            map.put("attribution", cursor.getString(cursor.getColumnIndex("attribution")));
            lists.add(map);
        }
        adapter = new SimpleAdapter(getContext(), lists, R.layout.contact_info_list_item,
                new String[]{"number", "attribution"}, new int[]{R.id.number, R.id.attribution});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String phone_number = lists.get(i).get("number").toString();
                MakePhoneCall makePhoneCall = new MakePhoneCall(getContext(), resolver);
                makePhoneCall.makeCall(phone_number);
            }
        });
    }

    private void setToolBar(View rootView) {
        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.addView(LayoutInflater.from(getContext()).inflate(R.layout.contact_info_menu, null, false),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ImageButton edit = (ImageButton) rootView.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editContactInfo();
            }
        });
        ImageButton more = (ImageButton) rootView.findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.getMenuInflater().inflate(R.menu.contact_info_edit_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.share_contact:
                                shareContact();
                                break;
                            case R.id.add_white_list:
                                addWhiteList();
                                break;
                            case R.id.delete_record:
                                deleteRecord();
                                break;
                            case R.id.delete_contact:
                                deleteContact();
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    private void editContactInfo() {
        //编辑联系人信息
        Intent intent = new Intent(getContext(), EditContactInfoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        String birthday = "";
        Cursor cursor = resolver.query(contactUri, new String[]{"birthday"}, "name = ?", new String[]{name}, null);
        while (cursor != null && cursor.moveToNext()) {
            birthday = cursor.getString(cursor.getColumnIndex("birthday"));
            if (birthday != null && !birthday.isEmpty())
                break;
        }
        bundle.putString("birthday", birthday);
        bundle.putSerializable("numberList", lists);
        intent.putExtras(bundle);
        getContext().startActivity(intent);
    }

    private void addWhiteList() {
        //加入白名单
        ContentValues values = new ContentValues();
        values.put("whitelist", 1);
        resolver.update(contactUri, values, "name = ?", new String[]{name});
        Toast.makeText(getContext(), "已加入白名单", Toast.LENGTH_SHORT).show();
    }

    private void deleteRecord() {
        // 删除所有联系记录
        RelativeLayout form = new RelativeLayout(getContext());
        TextView textView = new TextView(getContext());
        textView.setText("是否删除所有联系记录？");
        textView.setPadding(0, 20, 0, 0);
        textView.setTextSize(18);
        form.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        form.addView(textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        alertDialog = builder.setIcon(R.drawable.ic_warning_green_24dp)
                .setTitle("警告")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        resolver.delete(callRecordUri, "name = ?", new String[]{name});
                    }
                }).create();
        alertDialog.show();
    }

    private void deleteContact() {
        //删除联系人
        RelativeLayout form = new RelativeLayout(getContext());
        TextView textView = new TextView(getContext());
        textView.setText("是否删除该联系人？");
        textView.setPadding(0, 20, 0, 0);
        textView.setTextSize(18);
        form.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        form.addView(textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        alertDialog = builder.setIcon(R.drawable.ic_warning_green_24dp)
                .setTitle("警告")
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        resolver.delete(contactUri, "name = ?", new String[]{name});
                        ContentValues values = new ContentValues();
                        values.put("name", "");
                        resolver.update(callRecordUri, values, "name = ?", new String[]{name});
                        getActivity().finish();
                    }
                }).create();
        alertDialog.show();
    }

    private void shareContact() {
        //分享联系人（二维码）
        Cursor cursor = resolver.query(contactUri, new String[]{"number", "name", "birthday", "attribution", "pinyin"},
                "name=?", new String[]{name}, null);
        String content = "";
        while (cursor.moveToNext()) {
            String nowname = cursor.getString(cursor.getColumnIndex("name"));
            String nowbirthday = cursor.getString(cursor.getColumnIndex("birthday"));
            String nownumber = cursor.getString(cursor.getColumnIndex("number"));
            String nowattribution = cursor.getString(cursor.getColumnIndex("attribution"));
            String newpinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
            content = content + nowname + "," + nownumber + "," + nowbirthday + "," + nowattribution + "," + newpinyin + '\n';
        }
        try {
            Bitmap bitmap = BitmapUtils.create2DCode(content);
            RelativeLayout form = new RelativeLayout(getContext());
            ImageView imageView = new ImageView(getContext());
            imageView.setImageBitmap(bitmap);
            form.setGravity(Gravity.CENTER);
            form.addView(imageView);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            AlertDialog alertDialog = builder.setTitle("分享联系人")
                    .setView(form)
                    .setPositiveButton("取消", null).create();
            alertDialog.show();
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
