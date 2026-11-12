package com.example.componentsvulns.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 给页面和服务读写笔记。
 * Provider 自己另有查询代码，用来把“字符串拼进 SQL”这件事单独摆出来。
 */
public final class NotesRepository {

    private static volatile NotesRepository instance;
    private final NotesDbHelper helper;

    private NotesRepository(Context context) {
        helper = NotesDbHelper.get(context);
    }

    public static NotesRepository get(Context context) {
        if (instance == null) {
            synchronized (NotesRepository.class) {
                if (instance == null) {
                    instance = new NotesRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /** 打开数据库。第一次打开时会建表并写入练习笔记。 */
    public void ensureCreated() {
        helper.getReadableDatabase();
    }

    public void reset() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            NotesDbHelper.seed(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteAll() {
        helper.getWritableDatabase().delete("notes", null, null);
    }

    public String dumpAll() {
        try (Cursor cursor = helper.getReadableDatabase().query(
                "notes", null, null, null, null, null, "_id ASC")) {
            return format(cursor);
        }
    }

    public String dumpPrivate() {
        try (Cursor cursor = helper.getReadableDatabase().query(
                "notes", null, "secret=?", new String[]{"1"}, null, null, "_id ASC")) {
            return format(cursor);
        }
    }

    /**
     * 把查询结果排成适合直接显示的文字。
     * secret 为 1 的行标成【私密】，为 0 的行标成【公开】。
     */
    public static String format(Cursor cursor) {
        if (cursor == null) {
            return "（没有返回数据）";
        }
        if (cursor.getCount() == 0) {
            return "（没有笔记。如果刚刚执行过清空，请回首页点“恢复演示数据”。）";
        }
        StringBuilder text = new StringBuilder();
        while (cursor.moveToNext()) {
            int secret = cursor.getInt(cursor.getColumnIndexOrThrow("secret"));
            text.append(secret == 1 ? "【私密】 " : "【公开】 ");
            text.append(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            text.append('\n');
            text.append(cursor.getString(cursor.getColumnIndexOrThrow("body")));
            text.append("\n\n");
        }
        return text.toString().trim();
    }
}
