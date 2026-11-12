package com.example.componentsvulns.imposter;

import android.app.Activity;
import android.os.Bundle;

/** 清单里的 taskAffinity 是 com.example.componentsvulns。 */
public class HijackActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hijack);
        findViewById(R.id.move_back).setOnClickListener(v -> moveTaskToBack(true));
    }
}
