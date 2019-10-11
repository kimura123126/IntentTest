package com.example.intenttest2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button searchBn = findViewById(R.id.search);
        Button addBn = findViewById(R.id.add);
        searchBn.setOnClickListener(view ->
        //请求读取联系人信息的权限
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},0x123 ));
        addBn.setOnClickListener(view ->
        requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS},0x456 ));
    }

    @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
          @NonNull int[] grantResults) {
        if(grantResults[0] == 0) {
            if(requestCode == 0x123) {
                //封装系统联系人的信息，指定联系人的电话号码等详情
                List<String> names = new ArrayList<>();
//每个人的个人信息为detail 包括电话 和邮箱
                List<List<String>> details = new ArrayList<>();
                //查询联系人数据
                Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                        null, null, null, null);


                while (cursor.moveToNext()) {

                    //获取联系人ID
                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    //获取联系人名字
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    names.add(name);
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
                                    + contactId, null, null);

                    List<String> detail = new ArrayList<>();
                    //遍历查询结果获取该联系人的多个号码
                    while (phones.moveToNext()) {
                        //获取查询结果中电话号码列中的数据
                        String phoneNumber = phones.getString(phones.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        detail.add("电话号码：" + phoneNumber);
                    }
                    phones.close();

                    Cursor emails = getContentResolver().
                            query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + "="
                                            + contactId, null, null);

                    //获取该联系人的多个邮箱地址
                    while (emails.moveToNext()) {
                        //获取查询结果中邮箱地址列中的数据
                        String emailAddress = emails.getString(
                                emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        detail.add("邮箱地址：" + emailAddress);
                    }
                    emails.close();
                    details.add(detail);

                }
                cursor.close();

                View resultDialog = getLayoutInflater().inflate(R.layout.result, null);
                //看清楚类
               // ExpandableListActivity list = resultDialog.findViewById(R.id.list);
                ExpandableListView list = resultDialog.findViewById(R.id.list);
                ExpandableListAdapter adapter = new BaseExpandableListAdapter() {
                    //所有联系人数量  父表的每一行姓名
                    @Override
                    public int getGroupCount() {
                        return names.size();
                    }
                   //每个子表包含的数量  比如该联系人多个电话或邮箱 ，或都有
                    @Override
                    public int getChildrenCount(int groupPosition) {
                        return details.get(groupPosition).size();
                    }

                    //获取指定组位置处的数据  比如得到该处联系人名字
                    @Override
                    public Object getGroup(int groupPosition) {
                        return names.get(groupPosition);
                    }

                    //获取指定组位置，指定子列表项处的子列表数据
                    //比如在子列表出点击了邮箱 或电话 获得邮箱或电话数据
                    @Override
                    public Object getChild(int groupPosition, int childPosition) {
                        return details.get(groupPosition).get(childPosition);
                    }

                    @Override
                    public long getGroupId(int gruopPosition) {
                        return gruopPosition;
                    }

                    @Override
                    public long getChildId(int gruopPosition, int childPosition) {
                        return childPosition;
                    }

                    @Override
                    public boolean hasStableIds() {
                        return true;
                    }

                    //该方法决定每个组的外观
                    @Override
                    public View getGroupView(int groupPosition, boolean isExpanded,
                                             View convertView, ViewGroup parent) {
                        TextView textView;
                        if (convertView == null) {
                            textView = createTextView();
                        } else {
                            textView = (TextView) convertView;
                        }
                        textView.setTextSize(18f);
                        textView.setPadding(90, 10, 0, 10);
                        textView.setText(getGroup(groupPosition).toString());
                        return textView;
                    }

                    //该方法决定每个  子选项  的外观
                    @Override
                    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                             View convertView, ViewGroup parent) {
                        TextView textView;
                        if (convertView == null) {
                            textView = createTextView();
                        } else {
                            textView = (TextView) convertView;
                        }

                        textView.setText(getChild(groupPosition, childPosition).toString());
                        return textView;

                    }

                    @Override
                    public boolean isChildSelectable(int groupPosition, int childPosition) {
                        return true;
                    }

                    private TextView createTextView() {
                        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        TextView textView = new TextView(MainActivity.this);
                        textView.setLayoutParams(lp);
                        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                        textView.setPadding(40, 5, 0, 5);
                        textView.setTextSize(15f);
                        return textView;
                    }
                };

                list.setAdapter(adapter);
                new AlertDialog.Builder(MainActivity.this)
                        .setView(resultDialog)
                        .setPositiveButton("确定", null)
                        .show();
            }
            if(requestCode == 0x456){
                // 获取程序界面中的三个文本框的内容
                String name = ((EditText)findViewById(R.id.name)).getText().toString().trim();
                String phone = ((EditText)findViewById(R.id.phone)).getText().toString().trim();
                String email = ((EditText)findViewById(R.id.email)).getText().toString().trim();

                if (name.equals(""))
                {
                    return;
                }

                // 创建一个空的ContentValues
                ContentValues values = new ContentValues();
                // 向RawContacts.CONTENT_URI执行一个空值插入，获取新添加联系人的URI
                // 目的是获取系统返回的rawContactId
                Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
                long rawContactId = ContentUris.parseId(rawContactUri);
                values.clear();
                values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId);
                // 设置内容类型
                values.put(ContactsContract.RawContacts.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);



                // 设置联系人名字
                values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
                // 向联系人URI添加联系人名字
                getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
                values.clear();
                values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.RawContacts.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);



                // 设置联系人的电话号码
                values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
                // 设置电话类型
                values.put(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                // 向联系人电话号码URI添加电话号码
                getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
                values.clear();
                values.put(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, rawContactId);
                values.put(ContactsContract.RawContacts.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);



                // 设置联系人的E-mail地址
                values.put(ContactsContract.CommonDataKinds.Email.DATA, email);
                // 设置该电子邮件的类型
                values.put(ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.TYPE_WORK);
                // 向联系人E-mail URI添加E-mail数据
                getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);


                Toast.makeText(MainActivity.this, "联系人数据添加成功", Toast.LENGTH_SHORT).show();


            }
        }else{
            Toast.makeText(this, R.string.permission_tip, Toast.LENGTH_SHORT)
                    .show();
        }

    }

}
