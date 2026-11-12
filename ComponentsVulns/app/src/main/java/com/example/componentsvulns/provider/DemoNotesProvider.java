package com.example.componentsvulns.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.componentsvulns.data.NotesDbHelper;

/**
 * 基本用法：不导出的 Provider。增删改查都使用 SQLite 的参数接口，不拼接调用方字符串。
 * 数据在 scratch 表，和笔记表 notes 分开。
 */
public class DemoNotesProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.componentsvulns.demonotes";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/scratch");

    @Override
    public boolean onCreate() {
        ensureTable();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = writable();
        return db.query("scratch", new String[]{"_id", "title"}, null, null, null, null, "_id ASC");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = writable().insert("scratch", null, values);
        return ContentUris.withAppendedId(URI, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return writable().delete("scratch", null, null);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return writable().update("scratch", values, null, null);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    private SQLiteDatabase writable() {
        SQLiteDatabase db = NotesDbHelper.get(getContext()).getWritableDatabase();
        ensureTable(db);
        return db;
    }

    private void ensureTable() {
        ensureTable(NotesDbHelper.get(getContext()).getWritableDatabase());
    }

    private static void ensureTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS scratch (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT)");
    }
}
