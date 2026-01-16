package com.mumu.woodlin.system.util;

import com.mumu.woodlin.system.entity.SysPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限工具类
 *
 * @author mumu
 * @description 提供权限分类、过滤等工具方法
 * @since 2025-01-04
 */
public class PermissionUtil {

    /**
     * 权限类型：目录
     */
    public static final String TYPE_MENU = "M";

    /**
     * 权限类型：菜单
     */
    public static final String TYPE_COMPONENT = "C";

    /**
     * 权限类型：按钮
     */
    public static final String TYPE_BUTTON = "F";

    /**
     * 将权限列表按类型分类
     *
     * @param permissions 权限列表
     * @return 分类后的权限Map，key为权限类型，value为权限编码列表
     */
    public static Map<String, List<String>> categorizePermissions(List<SysPermission> permissions) {
        Map<String, List<String>> categorized = new HashMap<>();
        categorized.put(TYPE_MENU, new ArrayList<>());
        categorized.put(TYPE_COMPONENT, new ArrayList<>());
        categorized.put(TYPE_BUTTON, new ArrayList<>());

        if (permissions == null || permissions.isEmpty()) {
            return categorized;
        }

        for (SysPermission perm : permissions) {
            String type = perm.getPermissionType();
            String code = perm.getPermissionCode();

            if (code == null || code.isBlank()) {
                continue;
            }

            if (TYPE_BUTTON.equals(type)) {
                categorized.get(TYPE_BUTTON).add(code);
            } else if (TYPE_MENU.equals(type) || TYPE_COMPONENT.equals(type)) {
                categorized.get(TYPE_MENU).add(code);
                categorized.get(TYPE_COMPONENT).add(code);
            }
        }

        return categorized;
    }

    /**
     * 过滤菜单权限（包括目录和菜单，不包括按钮）
     *
     * @param permissions 权限列表
     * @return 菜单权限编码列表
     */
    public static List<String> filterMenuPermissions(List<SysPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }

        return permissions.stream()
            .filter(p -> TYPE_MENU.equals(p.getPermissionType())
                      || TYPE_COMPONENT.equals(p.getPermissionType()))
            .map(SysPermission::getPermissionCode)
            .filter(code -> code != null && !code.isBlank())
            .distinct()
            .toList();
    }

    /**
     * 过滤按钮权限
     *
     * @param permissions 权限列表
     * @return 按钮权限编码列表
     */
    public static List<String> filterButtonPermissions(List<SysPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }

        return permissions.stream()
            .filter(p -> TYPE_BUTTON.equals(p.getPermissionType()))
            .map(SysPermission::getPermissionCode)
            .filter(code -> code != null && !code.isBlank())
            .distinct()
            .toList();
    }

    /**
     * 获取所有权限编码（包括菜单和按钮）
     *
     * @param permissions 权限列表
     * @return 所有权限编码列表
     */
    public static List<String> getAllPermissionCodes(List<SysPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }

        return permissions.stream()
            .map(SysPermission::getPermissionCode)
            .filter(code -> code != null && !code.isBlank())
            .distinct()
            .toList();
    }

    /**
     * 判断是否为菜单权限
     *
     * @param permissionType 权限类型
     * @return 是否为菜单权限
     */
    public static boolean isMenuPermission(String permissionType) {
        return TYPE_MENU.equals(permissionType) || TYPE_COMPONENT.equals(permissionType);
    }

    /**
     * 判断是否为按钮权限
     *
     * @param permissionType 权限类型
     * @return 是否为按钮权限
     */
    public static boolean isButtonPermission(String permissionType) {
        return TYPE_BUTTON.equals(permissionType);
    }
}
