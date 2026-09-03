package f1.corrida;

import f1.model.Carro;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Classificacao {

    private Classificacao() {}

    private record Resultado(Carro carro, double tempo) {}

    public static List<Carro> realizar(List<Carro> carros) {
        List<Resultado> resultados = new ArrayList<>();

        for (Carro carro : carros) {
            resultados.add(new Resultado(carro, melhorVolta(carro)));
        }

        resultados.sort(Comparator.comparingDouble(Resultado::tempo));

        List<Carro> grid = new ArrayList<>();
        System.out.println("\n========== CLASSIFICAÇÃO ==========");

        for (int i = 0; i < resultados.size(); i++) {
            Resultado resultado = resultados.get(i);
            grid.add(resultado.carro());

            System.out.printf(
                    "P%-2d %-20s %-13s  %.3fs%n",
                    i + 1,
                    resultado.carro().getPiloto().getNome(),
                    resultado.carro().getEquipe().getNome(),
                    resultado.tempo()
            );
        }

        return grid;
    }

    private static double melhorVolta(Carro carro) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double melhor = Double.MAX_VALUE;

        for (int tentativa = 1; tentativa <= 3; tentativa++) {
            double base = 91.5
                    + (100 - carro.getPiloto().getQualificacao()) * 0.060
                    + (100 - carro.getEquipe().getRitmo()) * 0.078;

            double volta = base + random.nextDouble(-0.38, 0.38);

            double chanceErro = 0.025
                    + Math.max(0, carro.getPiloto().getAgressividade() - 90) * 0.002;

            if (random.nextDouble() < chanceErro) {
                volta += random.nextDouble(0.8, 2.4);
            }

            if (random.nextDouble() < 0.012) {
                continue;
            }

            melhor = Math.min(melhor, volta);
        }

        if (melhor == Double.MAX_VALUE) {
            melhor = 99.0 + random.nextDouble(0.0, 2.0);
        }

        return melhor;
    }
}
