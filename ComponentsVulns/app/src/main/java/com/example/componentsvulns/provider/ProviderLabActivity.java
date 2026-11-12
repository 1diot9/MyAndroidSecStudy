package com.example.componentsvulns.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.NotesRepository;
import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivityProviderLabBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Provider 实验页。按钮都通过 ContentResolver 访问，这和其他应用来访问用的是同一套接口。
 */
public class ProviderLabActivity extends AppCompatActivity {

    private ActivityProviderLabBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProviderLabBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("数据 Provider");

        binding.basicInsert.setOnClickListener(v -> {
            ContentValues values = new ContentValues();
            values.put("title", "练习条目");
            Uri inserted = getContentResolver().insert(DemoNotesProvider.URI, values);
            showScratch("insert 返回：\n" + inserted);
        });
        binding.basicQuery.setOnClickListener(v -> showScratch("query " + DemoNotesProvider.URI));
        binding.basicUpdate.setOnClickListener(v -> {
            ContentValues values = new ContentValues();
            values.put("title", "已修改");
            int rows = getContentResolver().update(DemoNotesProvider.URI, values, null, null);
            showScratch("update 返回：" + rows + "\n写入使用 ContentValues，没有把字符串拼进 SQL。");
        });
        binding.basicDelete.setOnClickListener(v -> {
            int rows = getContentResolver().delete(DemoNotesProvider.URI, null, null);
            showScratch("delete 返回：" + rows);
        });

        binding.queryAll.setOnClickListener(v -> showQuery(
                binding.leakResult,
                "不传条件。导出的 Provider 会把整张表交出去，私密行也在里面。",
                VulnerableNotesProvider.NOTES_URI,
                null));
        binding.queryPublic.setOnClickListener(v -> showQuery(
                binding.sqlResult,
                "传入 secret=0。这一次看起来只剩公开笔记，因为这段文字刚好是开发者期望的条件。",
                VulnerableNotesProvider.NOTES_URI,
                "secret=0"));
        binding.queryBypass.setOnClickListener(v -> showQuery(
                binding.sqlResult,
                "传入 secret=0 OR 1=1。OR 1=1 使整句条件恒成立，私密笔记又出现了。",
                VulnerableNotesProvider.NOTES_URI,
                "secret=0 OR 1=1"));
        binding.updateInject.setOnClickListener(v -> showMutation(
                binding.sqlResult,
                false,
                VulnerableNotesProvider.NOTES_URI,
                "secret=1"));
        binding.deleteInject.setOnClickListener(v -> showMutation(
                binding.sqlResult,
                true,
                VulnerableNotesProvider.NOTES_URI,
                "secret=0 OR 1=1"));
        binding.querySafe.setOnClickListener(v -> showQuery(
                binding.safeResult,
                "同样传入 secret=0 OR 1=1，但交给未导出、并且使用参数绑定的 Provider。",
                SafeNotesProvider.NOTES_URI,
                "secret=0 OR 1=1"));
        binding.deleteSafe.setOnClickListener(v -> showMutation(
                binding.safeResult,
                true,
                SafeNotesProvider.NOTES_URI,
                "secret=0 OR 1=1"));
        binding.readPublicFile.setOnClickListener(v -> showFile(
                binding.fileResult,
                "读取 demo/readme.txt。这是文件接口本来打算提供的文件。",
                VulnerableNotesProvider.fileUri("readme.txt")));
        binding.readPrivateFile.setOnClickListener(v -> showFile(
                binding.fileResult,
                "文件名参数是 ../private/pin.txt。有漏洞的实现没有检查最终路径。",
                VulnerableNotesProvider.fileUri("../private/pin.txt")));
        binding.readSafeFile.setOnClickListener(v -> showFile(
                binding.safeResult,
                "同一个文件名交给安全 Provider。路径检查应该拒绝它。",
                SafeNotesProvider.fileUri("../private/pin.txt")));
    }

    private void showScratch(String title) {
        String body;
        try (Cursor cursor = getContentResolver().query(DemoNotesProvider.URI, null, null, null, null)) {
            body = formatScratch(cursor);
        } catch (RuntimeException exception) {
            body = exception.getClass().getSimpleName() + "：\n" + exception.getMessage();
        }
        ResultEcho.show(binding.basicResult, title + "\n\n" + body);
    }

    private static String formatScratch(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            return "（scratch 表是空的）";
        }
        StringBuilder text = new StringBuilder();
        while (cursor.moveToNext()) {
            text.append(cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
            text.append("  ");
            text.append(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            text.append('\n');
        }
        return text.toString().trim();
    }

    private void showQuery(TextView target, String title, Uri uri, String selection) {
        String sqlNote;
        if (VulnerableNotesProvider.AUTHORITY.equals(uri.getAuthority())) {
            sqlNote = "实际拼出来的 SQL：\n" + SqlTexts.vulnerableNotesQuery(selection);
        } else {
            sqlNote = "安全 Provider 不使用传入的条件。\n"
                    + "它固定执行 WHERE secret=? ，参数是字符串 0。\n"
                    + "所以私密笔记不会因为 OR 1=1 出现。";
        }
        String body;
        try (Cursor cursor = getContentResolver().query(uri, null, selection, null, null)) {
            body = NotesRepository.format(cursor);
        } catch (RuntimeException exception) {
            body = exception.getClass().getSimpleName() + "：\n" + exception.getMessage();
        }
        ResultEcho.show(target, title + "\n\n" + sqlNote + "\n\n返回：\n" + body);
    }

    private void showMutation(TextView target, boolean delete, Uri uri, String selection) {
        String sql = delete
                ? SqlTexts.vulnerableNotesDelete(selection)
                : SqlTexts.vulnerableNotesUpdate(selection);
        String sqlNote = VulnerableNotesProvider.AUTHORITY.equals(uri.getAuthority())
                ? "实际执行的 SQL：\n" + sql
                : "安全 Provider 不执行调用方传来的 selection。";
        String body;
        try {
            int rows = delete
                    ? getContentResolver().delete(uri, selection, null)
                    : getContentResolver().update(uri, new ContentValues(), selection, null);
            body = (delete ? "delete" : "update") + " 返回：" + rows;
        } catch (RuntimeException exception) {
            body = exception.getClass().getSimpleName() + "：\n" + exception.getMessage();
        }
        ResultEcho.show(target, sqlNote + "\n\n" + body + "\n\n当前笔记：\n" + NotesRepository.get(this).dumpAll());
    }

    private void showFile(TextView target, String title, Uri uri) {
        String body;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            body = input == null ? "（没有打开文件）" : readUtf8(input);
        } catch (Exception exception) {
            body = exception.getClass().getSimpleName() + "：\n" + exception.getMessage();
        }
        ResultEcho.show(target, title + "\n\n地址：\n" + uri + "\n\n内容：\n" + body);
    }

    private static String readUtf8(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = input.read(buffer)) >= 0) {
            output.write(buffer, 0, count);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }
}
