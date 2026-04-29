package com.cloudguard.api;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AuditQueue {

    // ESTRUCTURA DE DATOS: Una cola segura para subprocesos (Thread-safe FIFO)
    private final BlockingQueue<Transaction> queue = new LinkedBlockingQueue<>();

    public AuditQueue() {
        // Al instanciar esta clase, iniciamos el hilo consumidor (Worker)
        iniciarWorkerDeBaseDeDatos();
    }

    // Método que usará el motor para "encolar" transacciones (Productor)
    public void registrarTransaccion(Transaction tx) {
        queue.offer(tx); // Agrega a la cola sin bloquear el hilo principal
    }

    // Subproceso que corre en segundo plano (Consumidor)
    private void iniciarWorkerDeBaseDeDatos() {
        Thread workerThread = new Thread(() -> {
            System.out.println("✅ [SISTEMA] Worker de Base de Datos iniciado y esperando transacciones...");
            try {
                while (true) {
                    // .take() extrae el elemento más antiguo (FIFO).
                    // Si la cola está vacía, el hilo se pausa automáticamente ahorrando CPU.
                    Transaction tx = queue.take();
                    guardarEnPostgreSQL(tx);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("⚠️ [SISTEMA] El worker de BD fue interrumpido.");
            }
        });

        // Lo marcamos como Daemon para que no impida que el programa se cierre
        workerThread.setDaemon(true);
        workerThread.start();
    }

    // Simulación de la inserción en BD (Lo conectaremos a PostgreSQL más adelante)
    private void guardarEnPostgreSQL(Transaction tx) {
        try {
            // Simulamos la latencia de la red hacia la base de datos (50ms)
            Thread.sleep(50);
            System.out.println("💾 [BD] Transacción de " + tx.getUserId() +
                    " por $" + tx.getAmount() + " guardada en PostgreSQL.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}