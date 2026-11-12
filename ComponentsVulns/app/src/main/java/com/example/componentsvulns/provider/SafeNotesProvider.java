package com.example.componentsvulns.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.example.componentsvulns.App;
import com.example.componentsvulns.data.DemoFiles;
import com.example.componentsvulns.data.NotesDbHelper;
import com.example.componentsvulns.data.PathGuard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 【对照】清单里 exported="false"。其他应用不能打开它。
 * 同一个 App 内部仍然可以打开，所以实验页里的对照按钮会成功。
 * 查询使用固定的 secret=? 和参数数组，不把调用者的字符串拼进 SQL。
 * 读文件前用 PathGuard 确认路径仍在 demo 目录内。
 */
public class SafeNotesProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.componentsvulns.safenotes";
    public static final Uri NOTES_URI = Uri.parse("content://" + AUTHORITY + "/notes");

    public static Uri fileUri(String name) {
        return new Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .path("file")
                .appendQueryParameter("name", name)
                .build();
    }

    @Override
    public boolean onCreate() {
        DemoFiles.writeSamples(getContext());
        NotesDbHelper.get(getContext()).getReadableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = NotesDbHelper.get(getContext()).getReadableDatabase();
        Log.i(App.TAG, "安全 Provider 忽略调用者传入的 selection=" + selection);
        // ? 会被 selectionArgs 里的 "0" 替换。数据库把它当数据，不当成 SQL 语法。
        // 因此即使调用者传入 secret=0 OR 1=1，这里也不会采用那段文字。
        return db.query(
                "notes",
                new String[]{"_id", "title", "body", "secret"},
                "secret=?",
                new String[]{"0"},
                null,
                null,
                "_id ASC");
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        String name = uri.getQueryParameter("name");
        try {
            File target = PathGuard.requireInside(DemoFiles.demoDir(getContext()), name);
            Log.i(App.TAG, "安全 Provider 打开文件：" + target.getPath());
            return ParcelFileDescriptor.open(target, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (SecurityException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new FileNotFoundException(exception.getMessage());
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
