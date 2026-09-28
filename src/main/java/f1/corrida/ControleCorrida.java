package f1.corrida;

import f1.eventos.Clima;
import f1.eventos.ModoCorrida;
import f1.model.Carro;
import f1.util.CoresConsole;
import f1.util.FormatadorTempo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;

public class ControleCorrida {

    private final int totalVoltas;
    private final long pausaEntreVoltasMs;
    private final List<Carro> carros = new ArrayList<>();
    private final Map<Carro, Integer> posicoesAnteriores = new HashMap<>();

    private volatile Clima clima = Clima.SECO;
    private volatile ModoCorrida modoCorrida = ModoCorrida.NORMAL;
    private volatile int modoAteVolta = 0;
    private volatile String motivoModo = "";

    private Phaser phaser;
    private volatile int voltaConcluida;
    private volatile double melhorVoltaCorrida = Double.MAX_VALUE;
    private volatile String pilotoMelhorVolta = "";

    public ControleCorrida(int totalVoltas) {
        this(totalVoltas, 0);
    }

    public ControleCorrida(int totalVoltas, long pausaEntreVoltasMs) {
        if (totalVoltas <= 0) {
            throw new IllegalArgumentException("O número de voltas deve ser maior que zero.");
        }
        if (pausaEntreVoltasMs < 0) {
            throw new IllegalArgumentException("A pausa entre voltas não pode ser negativa.");
        }
        this.totalVoltas = totalVoltas;
        this.pausaEntreVoltasMs = pausaEntreVoltasMs;
    }

    public int getTotalVoltas() {
        return totalVoltas;
    }

    public synchronized void adicionarCarro(Carro carro) {
        if (phaser != null) {
            throw new IllegalStateException("Não é possível adicionar carros depois do início da sincronização.");
        }
        carros.add(carro);
    }

    public synchronized void definirGrid(List<Carro> grid) {
        posicoesAnteriores.clear();

        for (int i = 0; i < grid.size(); i++) {
            Carro carro = grid.get(i);
            int posicao = i + 1;
            carro.setPosicaoLargada(posicao);
            carro.adicionarTempoInicial(i * 0.18);
            posicoesAnteriores.put(carro, posicao);
        }
    }

    public synchronized void prepararSincronizacao() {
        if (carros.isEmpty()) {
            throw new IllegalStateException("Não existem carros cadastrados.");
        }

        if (phaser != null) {
            throw new IllegalStateException("A sincronização já foi preparada.");
        }

        phaser = new Phaser(carros.size()) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                int volta = phase + 1;
                processarFimDaVolta(volta);
                return volta >= totalVoltas || registeredParties == 0;
            }
        };
    }

    public int aguardarFimDaVolta() {
        Phaser atual = phaser;
        if (atual == null) {
            throw new IllegalStateException("Sincronização não preparada.");
        }
        return atual.arriveAndAwaitAdvance();
    }

    public void sairDaSincronizacao() {
        Phaser atual = phaser;
        if (atual != null && !atual.isTerminated()) {
            atual.arriveAndDeregister();
        }
    }

    public boolean isSincronizacaoTerminada() {
        return phaser == null || phaser.isTerminated();
    }

    private synchronized void processarFimDaVolta(int volta) {
        voltaConcluida = volta;

        if (modoCorrida == ModoCorrida.SAFETY_CAR) {
            comprimirPelotao(0.85);
        } else if (modoCorrida == ModoCorrida.BANDEIRA_VERMELHA) {
            comprimirPelotao(0.45);
        }

        atualizarMelhorVolta(volta);
        mostrarClassificacaoParcial(volta);

        // Como este método é executado no avanço do Phaser, todas as Threads dos carros
        // permanecem sincronizadas enquanto a tela fica parada. Isso deixa cada volta
        // visível por tempo suficiente para a apresentação, sem alterar os tempos simulados.
        pausarParaApresentacao(pausaEntreVoltasMs);

        if (modoCorrida == ModoCorrida.BANDEIRA_VERMELHA) {
            System.out.println("\n🟥 CORRIDA INTERROMPIDA. Aguardando liberação da pista...");
            pausarParaApresentacao(Math.max(1800, pausaEntreVoltasMs));
            System.out.println("🟢 REINÍCIO AUTORIZADO.\n");
            modoCorrida = ModoCorrida.NORMAL;
            modoAteVolta = 0;
            motivoModo = "";
        } else if (modoCorrida != ModoCorrida.NORMAL && volta >= modoAteVolta) {
            System.out.println("🟩 PISTA LIBERADA — fim de " + nomeModo(modoCorrida) + ".\n");
            modoCorrida = ModoCorrida.NORMAL;
            modoAteVolta = 0;
            motivoModo = "";
        }

        atualizarClima();
    }

    private void pausarParaApresentacao(long milissegundos) {
        if (milissegundos <= 0) {
            return;
        }

        try {
            Thread.sleep(milissegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void mostrarClassificacaoParcial(int volta) {
        List<Carro> ativos = carros.stream()
                .filter(c -> !c.isAbandonou())
                .sorted(Comparator.comparingDouble(Carro::getTempoTotal))
                .toList();

        System.out.printf("%n========== VOLTA %d/%d ==========%n", volta, totalVoltas);

        if (ativos.isEmpty()) {
            System.out.println("Nenhum carro permanece na corrida.");
            return;
        }

        double tempoLider = ativos.get(0).getTempoTotal();

        for (int i = 0; i < ativos.size(); i++) {
            Carro carro = ativos.get(i);
            int atual = i + 1;
            int anterior = posicoesAnteriores.getOrDefault(carro, carro.getPosicaoLargada());
            String movimento = atual < anterior ? "▲" : atual > anterior ? "▼" : " ";
            String intervalo = i == 0
                    ? "LÍDER"
                    : FormatadorTempo.formatarIntervalo(carro.getTempoTotal() - tempoLider);

            String voltaColorida = formatarVoltaColorida(carro);
            System.out.printf(
                    "P%-2d %s %-19s %-12s %-10s | TEMPO DA VOLTA %-24s | %s | pits:%d%n",
                    atual,
                    movimento,
                    carro.getPiloto().getNome(),
                    carro.getEquipe().getNome(),
                    intervalo,
                    voltaColorida,
                    carro.getPneu(),
                    carro.getPitStops()
            );
        }

        for (int i = 0; i < ativos.size(); i++) {
            posicoesAnteriores.put(ativos.get(i), i + 1);
        }

        long abandonos = carros.stream().filter(Carro::isAbandonou).count();
        System.out.printf("Clima: %s | Controle: %s | DNFs: %d | Melhor volta: %s — %s%n",
                clima, modoCorrida, abandonos,
                pilotoMelhorVolta.isBlank() ? "--" : pilotoMelhorVolta,
                melhorVoltaCorrida == Double.MAX_VALUE ? "--:--.---" : FormatadorTempo.formatar(melhorVoltaCorrida));
    }

    private void atualizarMelhorVolta(int volta) {
        for (Carro carro : carros) {
            if (carro.getVolta() != volta || Double.isNaN(carro.getTempoUltimaVolta())) {
                continue;
            }

            if (carro.getTempoUltimaVolta() < melhorVoltaCorrida) {
                melhorVoltaCorrida = carro.getTempoUltimaVolta();
                pilotoMelhorVolta = carro.getPiloto().getNome();
            }
        }
    }

    private String formatarVoltaColorida(Carro carro) {
        double atual = carro.getTempoUltimaVolta();
        double anterior = carro.getTempoVoltaAnterior();
        String texto = FormatadorTempo.formatar(atual);

        if (Math.abs(atual - melhorVoltaCorrida) < 0.0005) {
            return CoresConsole.roxo(texto);
        }
        if (Double.isNaN(anterior)) {
            return CoresConsole.neutro(texto);
        }
        return atual < anterior ? CoresConsole.verde(texto) : CoresConsole.amarelo(texto);
    }

    private void comprimirPelotao(double intervaloPorCarro) {
        List<Carro> ativos = carros.stream()
                .filter(c -> !c.isAbandonou())
                .sorted(Comparator.comparingDouble(Carro::getTempoTotal))
                .toList();

        if (ativos.size() < 2) {
            return;
        }

        double lider = ativos.get(0).getTempoTotal();

        for (int i = 1; i < ativos.size(); i++) {
            Carro carro = ativos.get(i);
            double maximo = lider + (i * intervaloPorCarro);
            if (carro.getTempoTotal() > maximo) {
                carro.setTempoTotal(maximo);
            }
        }
    }

    private void atualizarClima() {
        double sorte = ThreadLocalRandom.current().nextDouble();

        switch (clima) {
            case SECO -> {
                if (sorte < 0.025) {
                    clima = Clima.NUBLADO;
                    System.out.println("☁️ O céu ficou nublado.");
                }
            }
            case NUBLADO -> {
                if (sorte < 0.045) {
                    clima = Clima.CHUVA_LEVE;
                    System.out.println("🌦️ Começou uma chuva leve.");
                } else if (sorte > 0.93) {
                    clima = Clima.SECO;
                    System.out.println("☀️ O tempo abriu novamente.");
                }
            }
            case CHUVA_LEVE -> {
                if (sorte < 0.055) {
                    clima = Clima.CHUVA_FORTE;
                    System.out.println("🌧️ A chuva ficou forte!");
                } else if (sorte > 0.92) {
                    clima = Clima.NUBLADO;
                    System.out.println("🌤️ A chuva diminuiu.");
                }
            }
            case CHUVA_FORTE -> {
                if (sorte > 0.90) {
                    clima = Clima.CHUVA_LEVE;
                    System.out.println("🌦️ A chuva começou a diminuir.");
                }
            }
        }
    }

    public synchronized void ativarBandeiraAmarela(String motivo, int volta) {
        ativarModo(ModoCorrida.BANDEIRA_AMARELA, motivo, volta + 1);
    }

    public synchronized void ativarVSC(String motivo, int volta) {
        int duracao = ThreadLocalRandom.current().nextInt(1, 3);
        ativarModo(ModoCorrida.VSC, motivo, volta + duracao);
    }

    public synchronized void ativarSafetyCar(String motivo, int volta) {
        int duracao = ThreadLocalRandom.current().nextInt(2, 5);
        ativarModo(ModoCorrida.SAFETY_CAR, motivo, volta + duracao);
    }

    public synchronized void ativarBandeiraVermelha(String motivo, int volta) {
        ativarModo(ModoCorrida.BANDEIRA_VERMELHA, motivo, volta);
    }

    private void ativarModo(ModoCorrida novoModo, String motivo, int ateVolta) {
        if (prioridade(novoModo) < prioridade(modoCorrida)) {
            return;
        }

        boolean mudou = modoCorrida != novoModo;
        modoCorrida = novoModo;
        modoAteVolta = Math.max(modoAteVolta, ateVolta);
        motivoModo = motivo;

        if (mudou || novoModo == ModoCorrida.BANDEIRA_VERMELHA) {
            System.out.printf("%n%s %s — %s%n",
                    iconeModo(novoModo), nomeModo(novoModo), motivo);
        }
    }

    private int prioridade(ModoCorrida modo) {
        return switch (modo) {
            case NORMAL -> 0;
            case BANDEIRA_AMARELA -> 1;
            case VSC -> 2;
            case SAFETY_CAR -> 3;
            case BANDEIRA_VERMELHA -> 4;
        };
    }

    private String iconeModo(ModoCorrida modo) {
        return switch (modo) {
            case NORMAL -> "🟢";
            case BANDEIRA_AMARELA -> "🟨";
            case VSC -> "🚗";
            case SAFETY_CAR -> "🚨";
            case BANDEIRA_VERMELHA -> "🟥";
        };
    }

    private String nomeModo(ModoCorrida modo) {
        return switch (modo) {
            case NORMAL -> "PISTA NORMAL";
            case BANDEIRA_AMARELA -> "BANDEIRA AMARELA";
            case VSC -> "VIRTUAL SAFETY CAR";
            case SAFETY_CAR -> "SAFETY CAR";
            case BANDEIRA_VERMELHA -> "BANDEIRA VERMELHA";
        };
    }

    public Clima getClima() { return clima; }
    public ModoCorrida getModoCorrida() { return modoCorrida; }
    public int getVoltaConcluida() { return voltaConcluida; }
    public String getMotivoModo() { return motivoModo; }
    public List<Carro> getCarros() { return List.copyOf(carros); }
    public double getMelhorVoltaCorrida() { return melhorVoltaCorrida; }
    public String getPilotoMelhorVolta() { return pilotoMelhorVolta; }
}

