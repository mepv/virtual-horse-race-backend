package com.example.horserace.controller;

import com.example.horserace.model.Horse;
import com.example.horserace.model.RaceState;
import com.example.horserace.service.HorseRaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/race")
public class HorseRaceController {

    private final HorseRaceService horseService;

    public HorseRaceController(HorseRaceService horseService) {
        this.horseService = horseService;
    }

    @PostMapping("/start")
    public void startRace(@RequestParam int numHorses) {
        horseService.initRace(numHorses);
    }

    @GetMapping("/state")
    public RaceState getRaceState() {
        return horseService.getRaceState();
    }

    @GetMapping("/horses")
    public List<Horse> getHorses() {
        return horseService.getHorses();
    }
}
