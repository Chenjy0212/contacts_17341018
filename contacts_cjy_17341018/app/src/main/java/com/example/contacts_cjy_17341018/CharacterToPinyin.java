package com.example.contacts_cjy_17341018;

// 这个不支持maven下载，过程遇到很多bug，后来手动下载并导入就完事了
import com.github.promeg.pinyinhelper.Pinyin;

public class CharacterToPinyin {
    public static String toPinyin(String str) {
        return Pinyin.toPinyin(str, "");
    }

    public static boolean isChinese(String str) {
        for (int i = 0; i < str.length(); ++i)
            if (Pinyin.isChinese(str.charAt(i)))
                return true;
        return false;
    }
}
