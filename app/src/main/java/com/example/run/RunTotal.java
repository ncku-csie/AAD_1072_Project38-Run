package com.example.run;

public class RunTotal {
    int totaltime;
    float totaldistance;
    int totalcount;
    float totalspeed;

    public RunTotal() {}

    public RunTotal(int time, float distance, int count, float speed) {
        this.totaltime = time;
        this.totaldistance = distance;
        this.totalcount = count;
        this.totalspeed = speed;
    }

    public int getTotaltime() {
        return totaltime;
    }

    public void setTotaltime(int totaltime) {
        this.totaltime = totaltime;
    }

    public float getTotaldistance() {
        return totaldistance;
    }

    public void setTotaldistance(float totaldistance) {
        this.totaldistance = totaldistance;
    }

    public int getTotalcount() {
        return totalcount;
    }

    public void setTotalcount(int totalcount) {
        this.totalcount = totalcount;
    }

    public float getTotalspeed() {
        return totalspeed;
    }

    public void setTotalspeed(float speed) {
        this.totalspeed = speed;
    }
}
