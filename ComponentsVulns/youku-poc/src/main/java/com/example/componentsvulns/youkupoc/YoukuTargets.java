package com.example.componentsvulns.youkupoc;

import android.content.ComponentName;

final class YoukuTargets {

    static final String PACKAGE = "com.youku.phone";
    static final ComponentName START_ACTIVITY_SERVICE = new ComponentName(
            PACKAGE, "com.youku.service.push.StartActivityService");
    static final ComponentName UPDATE_ACTIVITY = new ComponentName(
            PACKAGE, "com.youku.ui.activity.UpdateActivity");

    static final String EXTRA_PUSH_MSG = "PushMsg";
    static final String KEY_URL = "updateurl";
    static final String KEY_NEW_VERSION = "updateversion";
    static final String KEY_CONTENT = "updatecontent";
    static final String KEY_UPDATE_TYPE = "updateType";
    static final int UPDATE_TYPE_PUSH = 2;

    private YoukuTargets() {
    }
}
