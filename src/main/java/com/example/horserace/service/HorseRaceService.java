package com.example.horserace.service;

import com.example.horserace.model.Horse;
import com.example.horserace.model.RaceState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class HorseRaceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HorseRaceService.class);
    private static final List<String> HORSE_NAMES = List.of("Pegaso", "Cosmo", "Xylon", "Rayo Veloz", "Calisto", "Bastiaan");
    private static final int RACE_DISTANCE = 1000;

    private final List<Horse> horses = new ArrayList<>();
    private final List<Horse> finishOrder = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger finishedHorses = new AtomicInteger(0);
    private final Semaphore semaphore = new Semaphore(1);
    private int boosterPosition = -1;
    private ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final Random random = new Random();

    public void initRace(int numHorses) {
        horses.clear();
        finishOrder.clear();
        finishedHorses.set(0);

        for (int i = 0; i < numHorses; i++) {
            String name = "Caballo-" + (i + 1) + "-" + HORSE_NAMES.get(random.nextInt(6));
            int speed = random.nextInt(3) + 1;
            int endurance = random.nextInt(3) + 1;
            horses.add(new Horse(name, speed, endurance));
        }

        LOGGER.info("Caballos participantes:");
        for (Horse horse : horses) {
            LOGGER.info("{} - Velocidad: {}, Resistencia: {}", horse.getName(), horse.getSpeed(), horse.getEndurance());
        }

        if (numHorses > 10) {
            LOGGER.info("Justificacion: El numero de caballos es mayor a 10, usamos virtual threads para mayor escalabilidad con muchos caballos.");
            executorService = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
        } else {
            executorService = Executors.newFixedThreadPool(numHorses);
        }

        CompletableFuture.runAsync(this::startRace);
    }

    private void startRace() {
        Thread boosterThread = booster();
        boosterThread.start();

        for (Horse horse : horses) {
            executorService.submit(() -> {
                try {
                    raceTrack(horse);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (finishedHorses.get() >= 3) {
                scheduledExecutorService.shutdown();
                LOGGER.info("Carrera terminada. Orden de llegada:");
                for (Horse horse : finishOrder) {
                    LOGGER.info("{} - Distancia final: {} metros.", horse.getName(), horse.getDistanceCovered());
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void raceTrack(Horse horse) throws InterruptedException {
        while (horse.getDistanceCovered() < RACE_DISTANCE && finishedHorses.get() < 3) {
            int advance = horse.getSpeed() * (random.nextInt(10) + 1);
            horse.setDistanceCovered(horse.getDistanceCovered() + advance);
            LOGGER.info("{} avanza a {} metros.", horse.getName(), horse.getDistanceCovered());

            if (horse.getDistanceCovered() >= boosterPosition &&
                    horse.getDistanceCovered() <= boosterPosition + 50) {
                semaphore.acquire();
                LOGGER.info("{} entra en el area potenciadora.", horse.getName());
                Thread.sleep(7000);
                horse.setDistanceCovered(horse.getDistanceCovered() + 100);
                LOGGER.info("{} avanza a {} metros por potenciador.", horse.getName(), horse.getDistanceCovered());
                semaphore.release();
            }

            if (horse.getDistanceCovered() >= RACE_DISTANCE) {
                synchronized (finishOrder) {
                    if (finishedHorses.get() < 3) {
                        finishOrder.add(horse);
                        finishedHorses.incrementAndGet();
                    }
                }
                LOGGER.info("********** {} ha terminado la carrera! **********", horse.getName());
                break;
            }

            int waitTime = random.nextInt(5) + 1 - horse.getEndurance();
            if (waitTime > 0) {
                Thread.sleep(waitTime * 1000L);
            }
        }
    }

    private Thread booster() {
        Runnable boosterRunnable = () -> {
            try {
                while (finishedHorses.get() < 3) {
                    semaphore.acquire();
                    boosterPosition = random.nextInt(RACE_DISTANCE - 50);
                    semaphore.release();
                    Thread.sleep(15000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread boosterThread = new Thread(boosterRunnable);
        boosterThread.setDaemon(true);
        return boosterThread;
    }

    public RaceState getRaceState() {
        return new RaceState(horses,
                finishOrder
                        .stream()
                        .map(Horse::getName)
                        .toList(),
                boosterPosition);
    }

    public List<Horse> getHorses() {
        return horses;
    }
}
