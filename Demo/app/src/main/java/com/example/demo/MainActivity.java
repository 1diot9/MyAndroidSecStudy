package com.example.demo;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.demo.databinding.ActivityMainBinding;

/**
 * Button 和 TextView 的常见用法。
 * 布局在 res/layout/activity_main.xml，文案在 res/values/strings.xml。
 */
public class MainActivity extends AppCompatActivity {

    // 加载 cpp 里的 native 库。这个示例只用它返回一句话，演示 setText 可以接任意字符串。
    static {
        System.loadLibrary("demo");
    }

    // ViewBinding 根据布局 id 生成字段，例如 android:id="@+id/statusText" -> binding.statusText。
    private ActivityMainBinding binding;
    private int count;
    // 样式文字当前是否显示，用来配合 setVisibility 做显示/隐藏。
    private boolean detailVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // inflate 把 XML 变成真正的 View 树，getRoot() 是这棵树的根节点。
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // setContentView(R.layout.activity_main);
        setContentView(binding.getRoot());

        // setText 有两种常用重载：
        // 1. setText(R.string.xxx) 直接传资源 id
        // 2. setText(String) 传已经拼好的字符串
        // getString(id, args) 会把 strings.xml 里的 %1$s 换成 stringFromJNI() 的返回值。
        binding.statusText.setText(getString(R.string.status_ready, stringFromJNI()));
        applyStyledText();
        applyClickableText();

        // 点击后同时改文字、颜色和字号。setTextSize(float) 的单位是 sp。
        binding.changeTextButton.setOnClickListener(v -> {
            binding.statusText.setText(R.string.status_clicked);
            // 颜色优先从 colors.xml 取，避免在代码里写死色值。
            binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.teal_700));
            binding.statusText.setTextSize(18);
        });

        // 每次点击让数字加 1，再写回 TextView。界面上的数字以 TextView 里的文字为准。
        binding.countButton.setOnClickListener(v -> {
            count++;
            // %1$d 对应这里的 int。数字必须先变成字符串，TextView 不能直接显示 int 资源以外的数字。
            binding.counterText.setText(getString(R.string.counter_value, count));
        });

        // VISIBLE 显示并占位置；GONE 隐藏并且不占位置；INVISIBLE 隐藏但仍占位置。
        binding.toggleButton.setOnClickListener(v -> {
            detailVisible = !detailVisible;
            binding.styledText.setVisibility(detailVisible ? View.VISIBLE : View.GONE);
            // 按钮文字跟着当前状态变，用户能看出下一次点击会做什么。
            binding.toggleButton.setText(detailVisible
                    ? R.string.btn_toggle_hide
                    : R.string.btn_toggle_show);
        });

        // setEnabled(false) 会让按钮变灰，并且不再响应点击和长按。
        binding.disableButton.setOnClickListener(v -> {
            boolean enabled = !binding.longClickButton.isEnabled();
            binding.longClickButton.setEnabled(enabled);
            binding.disableButton.setText(enabled ? R.string.btn_disable : R.string.btn_enable);
        });

        // 短按：手指按下后很快松开。Toast 是短暂提示，不占用页面布局。
        binding.longClickButton.setOnClickListener(v ->
                Toast.makeText(this, R.string.toast_click, Toast.LENGTH_SHORT).show());

        // 长按：按住约 500 毫秒。返回 true 表示事件已经处理，系统不再继续往下分发。
        binding.longClickButton.setOnLongClickListener(v -> {
            binding.statusText.setText(R.string.status_long_click);
            // append 在现有文字后面追加，不会清掉 setText 刚写进去的前半句。
            binding.statusText.append(String.valueOf(count));
            Toast.makeText(this, R.string.toast_long_click, Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    /**
     * 同一段文字里，只给其中几个字加粗或变色。
     * SpannableString 是带标记的字符串，setSpan 指定从哪个下标到哪个下标生效。
     * 下标规则和 String.substring 一样：包含 start，不包含 end。
     */
    private void applyStyledText() {
        String raw = getString(R.string.styled_sample);
        SpannableString span = new SpannableString(raw);

        int boldStart = raw.indexOf("加粗");
        if (boldStart >= 0) {
            span.setSpan(new StyleSpan(Typeface.BOLD), boldStart, boldStart + 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        int colorStart = raw.indexOf("变色");
        if (colorStart >= 0) {
            span.setSpan(new ForegroundColorSpan(Color.parseColor("#E91E63")),
                    colorStart, colorStart + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.styledText.setText(span);
    }

    /**
     * 让文字中的一小段可以点击。
     * ClickableSpan 负责点击回调，UnderlineSpan 负责画出下划线。
     * 只 setSpan 还不够，必须再 setMovementMethod，TextView 才会把触摸事件交给这些 Span。
     */
    private void applyClickableText() {
        String raw = getString(R.string.link_sample);
        String target = "点这里";
        SpannableString span = new SpannableString(raw);
        int start = raw.indexOf(target);
        if (start >= 0) {
            int end = start + target.length();
            span.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    // 匿名内部类里的 this 是 ClickableSpan，Activity 要用 MainActivity.this。
                    Toast.makeText(MainActivity.this, R.string.toast_span_click, Toast.LENGTH_SHORT).show();
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.linkText.setText(span);
        binding.linkText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /** 由 cpp/native-lib.cpp 实现，返回一句本地代码生成的字符串。 */
    public native String stringFromJNI();
}
