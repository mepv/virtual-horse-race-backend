package com.example.horserace.model;

public class Horse {
    private final String name;
    private final int speed;
    private final int endurance;
    private int distanceCovered;

    public Horse(String name, int speed, int endurance) {
        this.name = name;
        this.speed = speed;
        this.endurance = endurance;
        this.distanceCovered = 0;
    }

    public String getName() {
        return name;
    }

    public int getSpeed() {
        return speed;
    }

    public int getEndurance() {
        return endurance;
    }

    public int getDistanceCovered() {
        return distanceCovered;
    }

    public void setDistanceCovered(int distanceCovered) {
        this.distanceCovered = distanceCovered;
    }
}
