package f1.pitstop;

import f1.model.Carro;
import f1.model.Penalidade;
import f1.model.Pneu;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public final class PitStop {

    private PitStop() {}

    public static double realizar(Carro carro, Pneu novoPneu) {
        ReentrantLock lock = carro.getEquipe().getBoxLock();
        boolean conseguiuDireto = lock.tryLock();
        double espera = 0.0;

        if (!conseguiuDireto) {
            System.out.println("⏳ DOUBLE STACK: " + carro.getPiloto().getNome()
                    + " aguarda o box da " + carro.getEquipe().getNome() + ".");
            espera = ThreadLocalRandom.current().nextDouble(2.0, 5.5);
            lock.lock();
        }

        try {
            System.out.println("🔧 " + carro.getPiloto().getNome()
                    + " entrou nos boxes para " + novoPneu + ".");

            double tempoParado = ThreadLocalRandom.current().nextDouble(2.0, 3.2);

            double chanceProblema = 0.015
                    + (100 - carro.getEquipe().getPitStop()) * 0.0022;

            if (ThreadLocalRandom.current().nextDouble() < chanceProblema) {
                double atraso = ThreadLocalRandom.current().nextDouble(2.5, 8.0);
                tempoParado += atraso;
                System.out.printf("⚠️ Pit stop lento de %s! +%.1fs%n",
                        carro.getPiloto().getNome(), atraso);
            }

            if (ThreadLocalRandom.current().nextDouble() < 0.012) {
                carro.receberPenalidade(Penalidade.CINCO_SEGUNDOS, "unsafe release");
            }

            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(15, 45));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            carro.trocarPneu(novoPneu);
            carro.incrementarPitStops();

            double perdaTotal = 18.0 + tempoParado + espera;
            System.out.printf("Pit stop de %s: %.2fs de perda total%n",
                    carro.getPiloto().getNome(), perdaTotal);

            return perdaTotal;
        } finally {
            lock.unlock();
        }
    }
}
