package com.aimango.robot.server.pojo;

import com.aimango.robot.server.core.annotation.validate.NotEmpty;
import com.aimango.robot.server.core.annotation.validate.Validate;

@Validate
public class Pojo {
    @NotEmpty
    private String name;
    @NotEmpty
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
