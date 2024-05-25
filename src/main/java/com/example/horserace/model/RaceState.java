package com.example.horserace.model;

import java.util.List;

public record RaceState(List<Horse> horses, List<String> finishOrder, int boosterPosition) {
}
