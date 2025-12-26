package com.mumu.woodlin.common.service;

import com.mumu.woodlin.common.entity.AdministrativeDivision;
import com.mumu.woodlin.common.enums.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 系统字典服务
 *
 * @author mumu
 * @description 提供统一的字典数据访问，所有字典遵循国家标准
 *              - 性别：GB/T 2261.1-2003
 *              - 民族：GB/T 3304-1991
 *              - 婚姻状况：GB/T 2261.2-2003
 *              - 政治面貌：GB/T 4762-1984
 *              - 学历：GB/T 4658-2006
 *              - 行政区划：GB/T 2260
 * @since 2025-12-26
 */
@Slf4j
@Service
public class SystemDictionaryService {

    /**
     * 获取性别字典
     *
     * @return 性别字典列表
     */
    @Cacheable(value = "dict:gender", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getGenderDict() {
        return enumToDict(Gender.values());
    }

    /**
     * 获取民族字典
     *
     * @return 民族字典列表
     */
    @Cacheable(value = "dict:ethnicity", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getEthnicityDict() {
        return enumToDict(Ethnicity.values());
    }

    /**
     * 获取学历字典
     *
     * @return 学历字典列表
     */
    @Cacheable(value = "dict:education", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getEducationLevelDict() {
        return enumToDict(EducationLevel.values());
    }

    /**
     * 获取婚姻状况字典
     *
     * @return 婚姻状况字典列表
     */
    @Cacheable(value = "dict:marital", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getMaritalStatusDict() {
        return enumToDict(MaritalStatus.values());
    }

    /**
     * 获取政治面貌字典
     *
     * @return 政治面貌字典列表
     */
    @Cacheable(value = "dict:political", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getPoliticalStatusDict() {
        return enumToDict(PoliticalStatus.values());
    }

    /**
     * 获取证件类型字典
     *
     * @return 证件类型字典列表
     */
    @Cacheable(value = "dict:idtype", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getIdTypeDict() {
        return Stream.of(IdType.values())
                .map(idType -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", idType.getValue());
                    map.put("label", idType.getLabel());
                    map.put("desc", idType.getDesc());
                    map.put("pattern", idType.getPattern());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取省级行政区划
     *
     * @return 省级列表
     */
    @Cacheable(value = "dict:province", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getProvinces() {
        // 这里返回省级行政区划
        // 实际数据应该从数据库或配置文件加载
        return getProvincesData();
    }

    /**
     * 获取市级行政区划
     *
     * @param provinceCode 省级代码
     * @return 市级列表
     */
    @Cacheable(value = "dict:city", key = "#provinceCode", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getCitiesByProvince(String provinceCode) {
        // 根据省级代码获取市级行政区划
        return getCitiesData(provinceCode);
    }

    /**
     * 获取区县级行政区划
     *
     * @param cityCode 市级代码
     * @return 区县列表
     */
    @Cacheable(value = "dict:district", key = "#cityCode", unless = "#result == null || #result.isEmpty()")
    public List<Map<String, Object>> getDistrictsByCity(String cityCode) {
        // 根据市级代码获取区县级行政区划
        return getDistrictsData(cityCode);
    }

    /**
     * 获取所有字典数据（用于前端一次性加载）
     *
     * @return 所有字典数据
     */
    @Cacheable(value = "dict:all", unless = "#result == null || #result.isEmpty()")
    public Map<String, Object> getAllDictionaries() {
        Map<String, Object> result = new HashMap<>();
        result.put("gender", getGenderDict());
        result.put("ethnicity", getEthnicityDict());
        result.put("education", getEducationLevelDict());
        result.put("marital", getMaritalStatusDict());
        result.put("political", getPoliticalStatusDict());
        result.put("idType", getIdTypeDict());
        result.put("provinces", getProvinces());
        return result;
    }

    /**
     * 将枚举转换为字典格式
     */
    private List<Map<String, Object>> enumToDict(DictEnum[] enums) {
        return Stream.of(enums)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("value", e.getValue());
                    map.put("label", e.getLabel());
                    map.put("desc", e.getDesc());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取省级数据（实际应从数据库加载）
     */
    private List<Map<String, Object>> getProvincesData() {
        List<Map<String, Object>> provinces = new ArrayList<>();
        
        // 省级行政区划数据（34个省级行政区，按照民政部最新标准）
        String[][] provinceData = {
            {"110000", "北京市", "Beijing", "BJ"},
            {"120000", "天津市", "Tianjin", "TJ"},
            {"130000", "河北省", "Hebei", "HE"},
            {"140000", "山西省", "Shanxi", "SX"},
            {"150000", "内蒙古自治区", "Inner Mongolia", "NM"},
            {"210000", "辽宁省", "Liaoning", "LN"},
            {"220000", "吉林省", "Jilin", "JL"},
            {"230000", "黑龙江省", "Heilongjiang", "HL"},
            {"310000", "上海市", "Shanghai", "SH"},
            {"320000", "江苏省", "Jiangsu", "JS"},
            {"330000", "浙江省", "Zhejiang", "ZJ"},
            {"340000", "安徽省", "Anhui", "AH"},
            {"350000", "福建省", "Fujian", "FJ"},
            {"360000", "江西省", "Jiangxi", "JX"},
            {"370000", "山东省", "Shandong", "SD"},
            {"410000", "河南省", "Henan", "HA"},
            {"420000", "湖北省", "Hubei", "HB"},
            {"430000", "湖南省", "Hunan", "HN"},
            {"440000", "广东省", "Guangdong", "GD"},
            {"450000", "广西壮族自治区", "Guangxi", "GX"},
            {"460000", "海南省", "Hainan", "HI"},
            {"500000", "重庆市", "Chongqing", "CQ"},
            {"510000", "四川省", "Sichuan", "SC"},
            {"520000", "贵州省", "Guizhou", "GZ"},
            {"530000", "云南省", "Yunnan", "YN"},
            {"540000", "西藏自治区", "Tibet", "XZ"},
            {"610000", "陕西省", "Shaanxi", "SN"},
            {"620000", "甘肃省", "Gansu", "GS"},
            {"630000", "青海省", "Qinghai", "QH"},
            {"640000", "宁夏回族自治区", "Ningxia", "NX"},
            {"650000", "新疆维吾尔自治区", "Xinjiang", "XJ"},
            {"710000", "台湾省", "Taiwan", "TW"},
            {"810000", "香港特别行政区", "Hong Kong", "HK"},
            {"820000", "澳门特别行政区", "Macao", "MO"}
        };
        
        for (String[] data : provinceData) {
            Map<String, Object> map = new HashMap<>();
            map.put("code", data[0]);
            map.put("name", data[1]);
            map.put("pinyin", data[2]);
            map.put("shortName", data[3]);
            map.put("level", 1);
            provinces.add(map);
        }
        
        return provinces;
    }

    /**
     * 获取市级数据（实际应从数据库加载，这里仅作示例）
     */
    private List<Map<String, Object>> getCitiesData(String provinceCode) {
        // 实际应该从数据库加载
        // 这里返回空列表，实际使用时需要导入完整的行政区划数据
        log.warn("市级数据需要从数据库加载，provinceCode={}", provinceCode);
        return new ArrayList<>();
    }

    /**
     * 获取区县数据（实际应从数据库加载，这里仅作示例）
     */
    private List<Map<String, Object>> getDistrictsData(String cityCode) {
        // 实际应该从数据库加载
        // 这里返回空列表，实际使用时需要导入完整的行政区划数据
        log.warn("区县数据需要从数据库加载，cityCode={}", cityCode);
        return new ArrayList<>();
    }
}
