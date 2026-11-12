package com.example.componentsvulns.provider;

/**
 * 有漏洞的笔记查询把调用方传来的 selection 直接接在 WHERE 后面。
 * 这个类不依赖 Android，单元测试可以直接检查拼出来的 SQL。
 */
public final class SqlTexts {

    private SqlTexts() {
    }

    /**
     * selection 来自调用方，拼进 SQL 文本后由数据库解析。
     * secret=0 只选出公开行。secret=0 OR 1=1 中的 OR 1=1 使条件恒为真，私密行也会被选出。
     * SafeNotesProvider 把条件写成 secret=?，把 "0" 放进绑定参数，不使用这段 selection。
     */
    public static String vulnerableNotesQuery(String selection) {
        String sql = "SELECT _id, title, body, secret FROM notes";
        if (selection != null && !selection.trim().isEmpty()) {
            sql = sql + " WHERE " + selection;
        }
        return sql + " ORDER BY _id ASC";
    }

    /** selection 直接接在 DELETE 的 WHERE 后面。selection 为空时没有 WHERE，会删掉全表。 */
    public static String vulnerableNotesDelete(String selection) {
        String sql = "DELETE FROM notes";
        if (selection != null && !selection.trim().isEmpty()) {
            sql = sql + " WHERE " + selection;
        }
        return sql;
    }

    /** selection 直接接在 UPDATE 的 WHERE 后面。 */
    public static String vulnerableNotesUpdate(String selection) {
        String sql = "UPDATE notes SET body='已篡改'";
        if (selection != null && !selection.trim().isEmpty()) {
            sql = sql + " WHERE " + selection;
        }
        return sql;
    }
}
