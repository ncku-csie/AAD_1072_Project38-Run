package com.example.run;

public class LoginTime {
    int loginDate;
    int loginMonth;
    int loginYear;

    public LoginTime(){}

    public LoginTime(int date, int month, int year) {
        this.loginDate = date;
        this.loginMonth = month;
        this.loginYear = year;
    }

    public int getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(int loginDate) { this.loginDate = loginDate; }

    public int getLoginMonth() {
        return loginMonth;
    }

    public void setLoginMonth(int loginMonth) {
        this.loginMonth = loginMonth;
    }

    public int getLoginYear() {
        return loginYear;
    }

    public void setLoginYear(int loginYear) {
        this.loginYear = loginYear;
    }
}