package com.crazyxacker.apps.tmu.helpers;

import java.util.Random;

public class Kaomoji {
    public enum KaomojiType { SADNESS }

    private static final String[] sadness = new String[] {
            "(-_-)",
            "(´-ω-`)",
            ".･ﾟﾟ･(／ω＼)･ﾟﾟ･.",
            "(μ_μ)",
            "(-ω-、)",
            "o(TヘTo)",
            "( ; ω ; )",
            "(｡╯︵╰｡)",
            "｡･ﾟﾟ*(>д<)*ﾟﾟ･｡",
            "(个_个)",
            "(╯︵╰,)",
            "｡･ﾟ(ﾟ><ﾟ)ﾟ･｡",
            "( ╥ω╥ )",
            "(╯_╰)",
            "(╥_╥)",
            ".｡･ﾟﾟ･(＞_＜)･ﾟﾟ･｡.",
            "(╥﹏╥)",
            "(つω`｡)",
            "(｡T ω T｡)",
            "･ﾟ･(｡>ω<｡)･ﾟ･",
            "(T_T)",
            "(>_<)",
            "(っ˘̩╭╮˘̩)っ",
            "｡ﾟ･ (>﹏<) ･ﾟ｡",
            "o(〒﹏〒)o",
            "(｡•́︿•̀｡)",
            "(ಥ﹏ಥ)"
    };

    public static String getRandomKaomoji(KaomojiType type) {
        switch (type) {
            case SADNESS:
                return sadness[new Random().nextInt(sadness.length)];
        }
        return "???";
    }
}
