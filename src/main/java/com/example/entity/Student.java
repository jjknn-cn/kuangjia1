package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Student {
    private Integer id;
    private String name;
    private String major;
    private Integer age;
    private LocalDate entranceDate; // 入学时间
    private Boolean graduated;      // 是否毕业
    private BigDecimal tuition;     // 学费

    public Student() {}
    public Student(String name, String major, Integer age, LocalDate entranceDate, Boolean graduated, BigDecimal tuition) {
        this.name = name;
        this.major = major;
        this.age = age;
        this.entranceDate = entranceDate;
        this.graduated = graduated;
        this.tuition = tuition;
    }

    // Getter/Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public LocalDate getEntranceDate() { return entranceDate; }
    public void setEntranceDate(LocalDate entranceDate) { this.entranceDate = entranceDate; }
    public Boolean getGraduated() { return graduated; }
    public void setGraduated(Boolean graduated) { this.graduated = graduated; }
    public BigDecimal getTuition() { return tuition; }
    public void setTuition(BigDecimal tuition) { this.tuition = tuition; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', major='" + major +
                "', age=" + age + ", entranceDate=" + entranceDate +
                ", graduated=" + graduated + ", tuition=" + tuition + "}";
    }
}
