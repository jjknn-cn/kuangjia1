package com.example.util;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JDBCTool {

    // ====================== 辅助方法：反射与命名转换 ======================
    /** 驼峰转下划线：entranceDate → entrance_date */
    private static String camelToUnderline(String camelStr) {
        StringBuilder sb = new StringBuilder();
        for (char c : camelStr.toCharArray()) {
            if (Character.isUpperCase(c)) sb.append('_').append(Character.toLowerCase(c));
            else sb.append(c);
        }
        return sb.toString();
    }

    /** 下划线转驼峰：entrance_date → entranceDate */
    private static String underlineToCamel(String underlineStr) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : underlineStr.toCharArray()) {
            if (c == '_') nextUpper = true;
            else {
                sb.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return sb.toString();
    }

    /** 获取表名：类名首字母小写 */
    private static String getTableName(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();
        return simpleName.substring(0,1).toLowerCase() + simpleName.substring(1);
    }

    /** 获取主键字段（默认id） */
    private static Field getPrimaryKey(Class<?> clazz) {
        try {
            return clazz.getDeclaredField("id");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("类" + clazz.getName() + "无主键id字段", e);
        }
    }

    /** 反射获取对象字段值 */
    private static Object getFieldValue(Object obj, Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("获取字段值失败", e);
        }
    }

    /** 反射设置对象字段值，含类型转换 */
    private static void setFieldValue(Object obj, Field field, Object value) {
        try {
            field.setAccessible(true);
            // TINYINT 转 Boolean
            if ((field.getType() == Boolean.class || field.getType() == boolean.class)
                    && value instanceof Number) {
                value = ((Number) value).intValue() == 1;
            }
            // java.sql.Date 转 LocalDate
            if (field.getType() == LocalDate.class && value instanceof Date) {
                value = ((Date) value).toLocalDate();
            }
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("设置字段值失败", e);
        }
    }

    // ====================== 核心方法 1：ResultSet 转对象列表 ======================
    public static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String fieldName = underlineToCamel(columnName);
                    Object value = rs.getObject(i);
                    try {
                        Field field = clazz.getDeclaredField(fieldName);
                        setFieldValue(obj, field, value);
                    } catch (NoSuchFieldException e) {
                        // 无对应字段则跳过
                    }
                }
                list.add(obj);
            }
        } catch (Exception e) {
            throw new RuntimeException("结果集转对象列表失败", e);
        }
        return list;
    }

    // ====================== 核心方法 2：新增（保存）数据 ======================
    public static <T> int save(T obj, Connection connection) {
        Class<?> clazz = obj.getClass();
        String tableName = getTableName(clazz);
        Field[] fields = clazz.getDeclaredFields();
        Field pk = getPrimaryKey(clazz);

        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        List<Object> params = new ArrayList<>();

        // 拼接列名与占位符，跳过自增主键
        for (Field field : fields) {
            if (field.getName().equals(pk.getName())) continue;
            String col = camelToUnderline(field.getName());
            Object val = getFieldValue(obj, field);

            if (columns.length() > 0) { columns.append(","); placeholders.append(","); }
            columns.append(col);
            placeholders.append("?");
            params.add(val);
        }

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i+1, params.get(i));
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("插入数据失败", e);
        }
    }

    // ====================== 核心方法 3：更新数据 ======================
    public static <T> int update(T obj, Connection connection) {
        Class<?> clazz = obj.getClass();
        String tableName = getTableName(clazz);
        Field[] fields = clazz.getDeclaredFields();
        Field pk = getPrimaryKey(clazz);
        Object pkValue = getFieldValue(obj, pk);

        StringBuilder setClause = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (Field field : fields) {
            if (field.getName().equals(pk.getName())) continue;
            String col = camelToUnderline(field.getName());
            Object val = getFieldValue(obj, field);

            if (setClause.length() > 0) setClause.append(",");
            setClause.append(col).append("=?");
            params.add(val);
        }
        params.add(pkValue); // 最后加主键条件

        String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i+1, params.get(i));
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新数据失败", e);
        }
    }

    // ====================== 核心方法 4：删除数据 ======================
    public static <T> int delete(T obj, Connection connection) {
        Class<?> clazz = obj.getClass();
        String tableName = getTableName(clazz);
        Field pk = getPrimaryKey(clazz);
        Object pkValue = getFieldValue(obj, pk);

        String sql = "DELETE FROM " + tableName + " WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, pkValue);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除数据失败", e);
        }
    }

    // ====================== 核心方法 5：根据ID查询单个对象 ======================
    public static <T> T getOneById(String id, Class<T> clazz, Connection connection) {
        String tableName = getTableName(clazz);
        String sql = "SELECT * FROM " + tableName + " WHERE id=?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, id);
            ResultSet rs = pstmt.executeQuery();
            List<T> list = resultSetToList(rs, clazz);
            return list.isEmpty() ? null : list.get(0);
        } catch (SQLException e) {
            throw new RuntimeException("根据ID查询失败", e);
        }
    }
}
