package f1.corrida;

import f1.model.Carro;
import f1.util.FormatadorTempo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Corrida {

    private final List<Carro> grid;
    private final ControleCorrida controle;

    public Corrida(List<Carro> grid, ControleCorrida controle) {
        this.grid = new ArrayList<>(grid);
        this.controle = controle;
    }

    public void iniciar() {
        controle.definirGrid(grid);
        controle.prepararSincronizacao();

        List<Thread> threads = new ArrayList<>();

        for (Carro carro : grid) {
            Thread thread = new Thread(carro, carro.getPiloto().getNome());
            threads.add(thread);
        }

        System.out.println("\n==================================");
        System.out.println("🟢 LARGADA DA CORRIDA!");
        System.out.println("==================================");

        threads.forEach(Thread::start);

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("A thread principal foi interrompida.", e);
            }
        }

        mostrarResultadoFinal();
    }

    private void mostrarResultadoFinal() {
        System.out.println("\n🏁 ========== RESULTADO FINAL ==========");

        List<Carro> finalistas = controle.getCarros().stream()
                .filter(c -> !c.isAbandonou())
                .sorted(Comparator.comparingDouble(Carro::getTempoTotal))
                .toList();

        if (finalistas.isEmpty()) {
            System.out.println("Nenhum piloto recebeu a bandeirada.");
        } else {
            double lider = finalistas.get(0).getTempoTotal();

            for (int i = 0; i < finalistas.size(); i++) {
                Carro carro = finalistas.get(i);
                String tempo = i == 0
                        ? FormatadorTempo.formatar(carro.getTempoTotal())
                        : FormatadorTempo.formatarIntervalo(carro.getTempoTotal() - lider);

                System.out.printf(
                        "P%-2d %-20s %-13s %-12s | pits: %d | melhor volta: %s%n",
                        i + 1,
                        carro.getPiloto().getNome(),
                        carro.getEquipe().getNome(),
                        tempo,
                        carro.getPitStops(),
                        FormatadorTempo.formatar(carro.getMelhorVoltaPessoal())
                );
            }
        }

        if (!controle.getPilotoMelhorVolta().isBlank()) {
            System.out.printf("%n🟣 MELHOR VOLTA DA CORRIDA: %s — %s%n",
                    controle.getPilotoMelhorVolta(),
                    FormatadorTempo.formatar(controle.getMelhorVoltaCorrida()));
        }

        List<Carro> dnfs = controle.getCarros().stream()
                .filter(Carro::isAbandonou)
                .sorted(Comparator.comparingInt(Carro::getVolta).reversed())
                .toList();

        if (!dnfs.isEmpty()) {
            System.out.println("\nDNFs:");
            for (Carro carro : dnfs) {
                System.out.printf("- %-20s volta %-2d — %s | pits: %d%n",
                        carro.getPiloto().getNome(),
                        carro.getVolta(),
                        carro.getMotivoAbandono(),
                        carro.getPitStops());
            }
        }

        System.out.println("\n✅ Simulação encerrada sem threads presas.");
    }

}
