package com.example.test;

import com.example.entity.College;
import com.example.entity.Student;
import com.example.util.DBUtil;
import com.example.util.JDBCTool;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public class DateTest {
    public static void main(String[] args) {
        Connection conn = DBUtil.getConnection();

        System.out.println("========== 学院表增删改查测试 ==========");
        // 1. 新增学院
        College c1 = new College("计算机学院", "CS001");
        College c2 = new College("电子工程学院", "EE002");
        System.out.println("新增学院影响行数：" + (JDBCTool.save(c1, conn) + JDBCTool.save(c2, conn)));

        // 2. ID查询
        College college = JDBCTool.getOneById("1", College.class, conn);
        System.out.println("查询id=1：" + college);

        // 3. 更新学院
        college.setName("计算机科学与技术学院");
        System.out.println("更新影响行数：" + JDBCTool.update(college, conn));
        System.out.println("更新后：" + JDBCTool.getOneById("1", College.class, conn));

        System.out.println("\n========== 学生表增删改查测试 ==========");
        // 1. 新增学生
        Student s1 = new Student("张三", "软件工程", 20, LocalDate.of(2021,9,1), false, new BigDecimal("5000.00"));
        Student s2 = new Student("李四", "计算机科学与技术", 21, LocalDate.of(2020,9,1), true, new BigDecimal("4800.00"));
        System.out.println("新增学生影响行数：" + (JDBCTool.save(s1, conn) + JDBCTool.save(s2, conn)));

        // 2. ID查询
        Student student = JDBCTool.getOneById("1", Student.class, conn);
        System.out.println("查询id=1：" + student);

        // 3. 更新学生
        student.setAge(22);
        student.setTuition(new BigDecimal("5200.00"));
        System.out.println("更新影响行数：" + JDBCTool.update(student, conn));
        System.out.println("更新后：" + JDBCTool.getOneById("1", Student.class, conn));

        // 4. 全量查询（演示resultSetToList）
        try {
            java.sql.ResultSet rs = conn.prepareStatement("SELECT * FROM student").executeQuery();
            List<Student> students = JDBCTool.resultSetToList(rs, Student.class);
            System.out.println("\n所有学生列表：");
            students.forEach(System.out::println);
        } catch (Exception e) { e.printStackTrace(); }

        // 5. 删除数据
        Student delStu = new Student(); delStu.setId(2);
        College delCol = new College(); delCol.setId(2);
        System.out.println("\n删除学生id=2：" + JDBCTool.delete(delStu, conn) + " 行");
        System.out.println("删除学院id=2：" + JDBCTool.delete(delCol, conn) + " 行");

        try { conn.close(); } catch (Exception e) { e.printStackTrace(); }
        System.out.println("\n========== 全部测试完成 ==========");
    }
}
