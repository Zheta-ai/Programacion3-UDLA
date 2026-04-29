package com.cloudguard.api;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RiskEngine {

    // =========================================================
    // ESTRUCTURA DE DATOS: Fila (Queue) para Velocity Check
    // Mapeamos cada UserID a una Cola (LinkedList) de fechas.
    // =========================================================
    private final Map<String, Queue<LocalDateTime>> userTransactionHistory = new ConcurrentHashMap<>();

    public EvaluationResult evaluarTransaccion(Transaction tx) {
        int score = 0;
        List<String> flags = new ArrayList<>();

        // =========================================================
        // REGLA 1: Monto Elevado
        // =========================================================
        if (tx.getAmount() > 1000.00) {
            score += 50;
            flags.add("high_amount_detected");
        }

        // =========================================================
        // REGLA 2: Velocidad (Aplicando Conceptos de Filas - FIFO)
        // =========================================================
        LocalDateTime now = LocalDateTime.now();

        // Obtenemos la fila de este usuario, si no existe, la creamos
        Queue<LocalDateTime> historyQueue = userTransactionHistory.computeIfAbsent(
                tx.getUserId(), k -> new LinkedList<>()
        );

        // Ingresa la transacción actual a la fila
        historyQueue.add(now);

        // APLICACIÓN FIFO: Retiramos los elementos más antiguos (First-Out)
        // que sobrepasen el límite de 1 minuto
        while (!historyQueue.isEmpty() && ChronoUnit.MINUTES.between(historyQueue.peek(), now) >= 1) {
            historyQueue.poll(); // Extrae y elimina el dato más viejo
        }

        // Si después de limpiar las viejas, aún hay 3 o más en la fila, hay riesgo
        if (historyQueue.size() >= 3) {
            score += 40;
            flags.add("high_velocity_detected");
        }

        // =========================================================
        // Lógica final de evaluación
        // =========================================================
        // Limitar score a 100
        score = Math.min(score, 100);

        // Formatear flags
        String resumenFlags = flags.isEmpty() ? "ok" : String.join(" | ", flags);

        return new EvaluationResult(score, resumenFlags);
    }

    // Clase interna para retornar múltiples valores (Score y Flags)
    public static class EvaluationResult {
        private final int score;
        private final String flags;

        public EvaluationResult(int score, String flags) {
            this.score = score;
            this.flags = flags;
        }

        public int getScore() { return score; }
        public String getFlags() { return flags; }
    }
}