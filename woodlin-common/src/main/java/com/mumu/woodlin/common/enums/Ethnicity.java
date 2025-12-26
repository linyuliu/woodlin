package com.mumu.woodlin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 民族枚举（GB/T 3304-1991 中国各民族名称的罗马字母拼写法和代码）
 *
 * @author mumu
 * @description 中华人民共和国56个民族标准代码
 *              代码规则：01-56按国务院公布顺序编码
 * @since 2025-12-26
 */
@Getter
@AllArgsConstructor
public enum Ethnicity implements DictEnum {

    HAN(1, "汉族", "Han"),
    MONGOL(2, "蒙古族", "Mongol"),
    HUI(3, "回族", "Hui"),
    TIBETAN(4, "藏族", "Tibetan"),
    UYGHUR(5, "维吾尔族", "Uyghur"),
    MIAO(6, "苗族", "Miao"),
    YI(7, "彝族", "Yi"),
    ZHUANG(8, "壮族", "Zhuang"),
    BOUYEI(9, "布依族", "Bouyei"),
    KOREAN(10, "朝鲜族", "Korean"),
    MANCHU(11, "满族", "Manchu"),
    DONG(12, "侗族", "Dong"),
    YAO(13, "瑶族", "Yao"),
    BAI(14, "白族", "Bai"),
    TUJIA(15, "土家族", "Tujia"),
    HANI(16, "哈尼族", "Hani"),
    KAZAK(17, "哈萨克族", "Kazak"),
    DAI(18, "傣族", "Dai"),
    LI(19, "黎族", "Li"),
    LISU(20, "傈僳族", "Lisu"),
    VA(21, "佤族", "Va"),
    SHE(22, "畲族", "She"),
    GAOSHAN(23, "高山族", "Gaoshan"),
    LAHU(24, "拉祜族", "Lahu"),
    SUI(25, "水族", "Sui"),
    DONGXIANG(26, "东乡族", "Dongxiang"),
    NAXI(27, "纳西族", "Naxi"),
    JINGPO(28, "景颇族", "Jingpo"),
    KIRGIZ(29, "柯尔克孜族", "Kirgiz"),
    TU(30, "土族", "Tu"),
    DAUR(31, "达斡尔族", "Daur"),
    MULAM(32, "仫佬族", "Mulam"),
    QIANG(33, "羌族", "Qiang"),
    BLANG(34, "布朗族", "Blang"),
    SALAR(35, "撒拉族", "Salar"),
    MAONAN(36, "毛南族", "Maonan"),
    GELAO(37, "仡佬族", "Gelao"),
    XIBE(38, "锡伯族", "Xibe"),
    ACHANG(39, "阿昌族", "Achang"),
    PUMI(40, "普米族", "Pumi"),
    TAJIK(41, "塔吉克族", "Tajik"),
    NU(42, "怒族", "Nu"),
    UZBEK(43, "乌孜别克族", "Uzbek"),
    RUSSIAN(44, "俄罗斯族", "Russian"),
    EWENKI(45, "鄂温克族", "Ewenki"),
    DEANG(46, "德昂族", "Deang"),
    BONAN(47, "保安族", "Bonan"),
    YUGUR(48, "裕固族", "Yugur"),
    JING(49, "京族", "Jing"),
    TATAR(50, "塔塔尔族", "Tatar"),
    DERUNG(51, "独龙族", "Derung"),
    OROQEN(52, "鄂伦春族", "Oroqen"),
    HEZHEN(53, "赫哲族", "Hezhen"),
    MONBA(54, "门巴族", "Monba"),
    LHOBA(55, "珞巴族", "Lhoba"),
    JINO(56, "基诺族", "Jino");

    /**
     * 民族代码（GB/T 3304标准）
     */
    private final Integer code;

    /**
     * 民族名称（中文）
     */
    private final String name;

    /**
     * 民族名称（拼音/英文）
     */
    private final String nameEn;

    @Override
    public Object getValue() {
        return code;
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public String getDesc() {
        return name + " / " + nameEn;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 民族代码
     * @return 民族枚举
     */
    public static Ethnicity fromCode(Integer code) {
        if (code == null) {
            return HAN;
        }
        for (Ethnicity ethnicity : values()) {
            if (ethnicity.getCode().equals(code)) {
                return ethnicity;
            }
        }
        return HAN;
    }

    /**
     * 根据名称获取枚举
     *
     * @param name 民族名称
     * @return 民族枚举
     */
    public static Ethnicity fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return HAN;
        }
        for (Ethnicity ethnicity : values()) {
            if (ethnicity.getName().equals(name) || ethnicity.getNameEn().equalsIgnoreCase(name)) {
                return ethnicity;
            }
        }
        return HAN;
    }
}
