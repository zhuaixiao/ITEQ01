package com.example.iteq;

import cn.bmob.v3.BmobUser;

public class UserBean extends BmobUser {
    //员工姓名
    private String personName;
    //员工工号
    private String personId;
    //员工部门
    private String personDepartment;
    //员工职务
    private String personPost;
    //员工入职日期
    private String personDate;

    public UserBean(String personName, String personId) {
        this.personName = personName;
        this.personId = personId;
    }

    public UserBean(String personName, String personId, String personDepartment, String personPost, String personDate) {
        this.personName = personName;
        this.personId = personId;
        this.personDepartment = personDepartment;
        this.personPost = personPost;
        this.personDate = personDate;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonDepartment() {
        return personDepartment;
    }

    public void setPersonDepartment(String personDepartment) {
        this.personDepartment = personDepartment;
    }

    public String getPersonPost() {
        return personPost;
    }

    public void setPersonPost(String personPost) {
        this.personPost = personPost;
    }

    public String getPersonDate() {
        return personDate;
    }

    public void setPersonDate(String personDate) {
        this.personDate = personDate;
    }
}
