package com.cloudguard.api;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("============================================================");
        System.out.println(" 🚀 Iniciando CloudGuard Risk-API (Demostración Académica) ");
        System.out.println("============================================================\n");

        RiskEngine engine = new RiskEngine();
        AuditQueue auditQueue = new AuditQueue();

        // Damos medio segundo para que el worker de BD se inicie limpiamente
        Thread.sleep(500);

        // ==========================================
        // CASO 1: Transacción Normal
        // ==========================================
        System.out.println("\n--- PRUEBA 1: Transacción Normal ---");
        procesarTransaccion(new Transaction("USER_001", 150.00, "Quito, EC"), engine, auditQueue, "Operación estándar");

        // Esperamos a que la BD termine de procesar la cola para no mezclar los textos
        Thread.sleep(200);

        // ==========================================
        // CASO 2: Monto Elevado
        // ==========================================
        System.out.println("\n--- PRUEBA 2: Monto Elevado ---");
        procesarTransaccion(new Transaction("USER_002", 2500.00, "Guayaquil, EC"), engine, auditQueue, "Monto inusual detectado");
        Thread.sleep(200);

        // ==========================================
        // CASO 3: Ataque de Velocidad (Queue / FIFO)
        // ==========================================
        System.out.println("\n--- PRUEBA 3: Ataque de Velocidad (Demostración de Fila FIFO) ---");
        System.out.println("Condición de Riesgo: 3 o más transacciones en la cola en menos de 1 minuto.\n");

        procesarTransaccion(new Transaction("USER_HACKER", 10.00, "Lima, PE"), engine, auditQueue, "Transacción 1 (Tamaño de cola: 1)");
        Thread.sleep(100); // Simulamos el paso de milisegundos en la vida real

        procesarTransaccion(new Transaction("USER_HACKER", 15.00, "Lima, PE"), engine, auditQueue, "Transacción 2 (Tamaño de cola: 2)");
        Thread.sleep(100);

        System.out.println("\n   --> [Aviso] La siguiente transacción llena la cola y dispara la regla:");
        procesarTransaccion(new Transaction("USER_HACKER", 5.00, "Lima, PE"), engine, auditQueue, "Transacción 3 (Tamaño de cola: 3) -> ¡ALERTA!");
        Thread.sleep(100);

        procesarTransaccion(new Transaction("USER_HACKER", 12.00, "Lima, PE"), engine, auditQueue, "Transacción 4 (Tamaño de cola: 4) -> Sigue bloqueado");

        // Pausa final para que la cola de BD termine de vaciarse
        Thread.sleep(1000);
        System.out.println("\n============================================================");
        System.out.println(" ✅ Pruebas finalizadas. Estructuras de datos operando OK. ");
        System.out.println("============================================================");
    }

    // Método auxiliar para mantener el código principal limpio
    private static void procesarTransaccion(Transaction tx, RiskEngine engine, AuditQueue queue, String contexto) {
        System.out.println("📥 [API] Evaluando tx: " + tx.getUserId() + " | Monto: $" + tx.getAmount() + " | Contexto: " + contexto);

        RiskEngine.EvaluationResult resultado = engine.evaluarTransaccion(tx);

        System.out.println("   🚨 -> Score: " + resultado.getScore() + " | Flags: " + resultado.getFlags());

        queue.registrarTransaccion(tx);
    }
}