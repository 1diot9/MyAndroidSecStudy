package com.example.componentsvulns.data;

import java.io.File;
import java.io.IOException;

/**
 * 【对照】读取文件前，先把路径变成绝对路径，再确认它仍在允许的目录里面。
 * 有漏洞的 Provider 故意不用这个检查。见 VulnerableNotesProvider.openFile。
 */
public final class PathGuard {

    private PathGuard() {
    }

    /**
     * @param root         允许访问的目录，本练习里是 files/demo
     * @param relativeName 调用者传来的文件名，可能含有 ../ 或绝对路径
     * @return 确认仍在 root 之内的文件
     */
    public static File requireInside(File root, String relativeName) throws IOException {
        if (relativeName == null || relativeName.trim().isEmpty()) {
            relativeName = "readme.txt";
        }
        File canonicalRoot = root.getCanonicalFile();
        // getCanonicalFile 会把 ../ 和多余的分隔符解析掉，得到最终路径。
        File target = new File(canonicalRoot, relativeName).getCanonicalFile();
        String prefix = canonicalRoot.getPath() + File.separator;
        if (!target.getPath().startsWith(prefix)) {
            throw new SecurityException(
                    "拒绝访问允许目录以外的文件。\n请求的名字：" + relativeName + "\n实际路径：" + target.getPath());
        }
        return target;
    }
}
