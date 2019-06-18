package com.example.run;

public class RunToday {
    int time;
    float distance;
    int count;
    float speed;

    public RunToday() {}

    public RunToday(int Time, float Distance, int Count, float Speed) {
        this.time = Time;
        this.distance = Distance;
        this.count = Count;
        this.speed = Speed*1000;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}