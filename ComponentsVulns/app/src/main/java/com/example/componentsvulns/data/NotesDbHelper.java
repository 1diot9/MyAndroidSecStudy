package com.example.componentsvulns.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 练习用的笔记表。secret=0 表示公开笔记，secret=1 表示私密笔记。
 * 内容全是假数据。
 */
public final class NotesDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "notes.db";
    private static final int DB_VERSION = 1;
    private static NotesDbHelper instance;

    private NotesDbHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static synchronized NotesDbHelper get(Context context) {
        if (instance == null) {
            instance = new NotesDbHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "title TEXT,"
                + "body TEXT,"
                + "secret INTEGER)");
        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
    }

    /** 清空后重新放入四条固定的练习笔记。首页的“恢复演示数据”会走到这里。 */
    static void seed(SQLiteDatabase db) {
        db.delete("notes", null, null);
        insert(db, "购物清单", "买牛奶、面包。这条是公开笔记。", 0);
        insert(db, "会议提醒", "周五下午讨论课堂作业。这条也是公开笔记。", 0);
        insert(db, "工资条", "演示数据：月薪 8000。假数据，用来代表不该被别人看到的内容。", 1);
        insert(db, "银行卡备注", "演示卡号 6222-0000-0000-0000。假数据。", 1);
    }

    private static void insert(SQLiteDatabase db, String title, String body, int secret) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("body", body);
        values.put("secret", secret);
        db.insert("notes", null, values);
    }
}
