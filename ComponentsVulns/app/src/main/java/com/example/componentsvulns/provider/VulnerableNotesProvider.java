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

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 导出的 ContentProvider，且没有 readPermission。任意应用可以调用 query 和 openFile。
 * query 把 selection 拼进 SQL，再交给 rawQuery，绑定参数数组为 null。
 * openFile 把查询参数 name 直接传给 new File(demoDir, name)，不会先解析并限制最终路径。
 * 对照实现是 SafeNotesProvider。
 */
public class VulnerableNotesProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.componentsvulns.vulnnotes";
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
        // Provider 可能比 Application.onCreate 更早执行，这里也准备一次练习文件。
        DemoFiles.writeSamples(getContext());
        NotesDbHelper.get(getContext()).getReadableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!"notes".equals(firstSegment(uri))) {
            throw new IllegalArgumentException("这个地址不提供查询：" + uri);
        }
        SQLiteDatabase db = NotesDbHelper.get(getContext()).getReadableDatabase();
        String sql = SqlTexts.vulnerableNotesQuery(selection);
        Log.i(App.TAG, "有漏洞的 Provider 执行：" + sql);
        // 参数数组被丢掉了。调用者的文字进入 SQL 解析器，? 占位不会发生。
        return db.rawQuery(sql, null);
    }

    /**
     * name 直接作为 demo 目录下的相对路径。
     * "../private/pin.txt" 解析后是 files/private/pin.txt，不在 files/demo 里。
     */
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"file".equals(firstSegment(uri))) {
            throw new FileNotFoundException("这个地址不提供文件：" + uri);
        }
        String name = uri.getQueryParameter("name");
        if (name == null || name.isEmpty()) {
            name = "readme.txt";
        }
        File target = new File(DemoFiles.demoDir(getContext()), name);
        Log.i(App.TAG, "有漏洞的 Provider 打开文件：" + target.getPath());
        return ParcelFileDescriptor.open(target, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // 本课只演示读取。真实项目里如果 insert/update/delete 也这样对外开着，别人还能改数据。
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (!"notes".equals(firstSegment(uri))) {
            return 0;
        }
        SQLiteDatabase db = NotesDbHelper.get(getContext()).getWritableDatabase();
        String sql = SqlTexts.vulnerableNotesDelete(selection);
        Log.i(App.TAG, "有漏洞的 Provider 执行：" + sql);
        db.execSQL(sql);
        return 1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (!"notes".equals(firstSegment(uri))) {
            return 0;
        }
        SQLiteDatabase db = NotesDbHelper.get(getContext()).getWritableDatabase();
        String sql = SqlTexts.vulnerableNotesUpdate(selection);
        Log.i(App.TAG, "有漏洞的 Provider 执行：" + sql);
        db.execSQL(sql);
        return 1;
    }

    private static String firstSegment(Uri uri) {
        if (uri.getPathSegments().isEmpty()) {
            return "";
        }
        return uri.getPathSegments().get(0);
    }
}
