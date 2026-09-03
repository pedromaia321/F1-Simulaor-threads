package f1.model;

import f1.corrida.ControleCorrida;
import f1.eventos.Clima;
import f1.eventos.GerenciadorEventos;
import f1.eventos.ModoCorrida;
import f1.pitstop.PitStop;

import java.util.concurrent.ThreadLocalRandom;

public class Carro implements Runnable {

    private final Piloto piloto;
    private final Equipe equipe;
    private final ControleCorrida controle;

    private volatile Pneu pneu = Pneu.MEDIUM;
    private volatile Penalidade penalidadeServico = Penalidade.NENHUMA;

    private volatile int volta;
    private double tempoTotal;
    private volatile double desgastePneu;
    private volatile boolean abandonou;
    private volatile String motivoAbandono = "";
    private volatile boolean pitObrigatorio;
    private volatile int avisosLimitePista;
    private volatile int pitStops;
    private volatile int posicaoLargada;

    public Carro(Piloto piloto, Equipe equipe, ControleCorrida controle) {
        this.piloto = piloto;
        this.equipe = equipe;
        this.controle = controle;
    }

    @Override
    public void run() {
        boolean registradoNoPhaser = true;

        try {
            while (volta < controle.getTotalVoltas() && !abandonou) {
                volta++;

                cumprirPenalidadeDePassagem();
                executarVolta();

                if (!abandonou) {
                    GerenciadorEventos.verificarEvento(this);
                }

                if (!abandonou) {
                    verificarPitStop();
                }

                if (abandonou) {
                    controle.sairDaSincronizacao();
                    registradoNoPhaser = false;
                    break;
                }

                if (volta == controle.getTotalVoltas()) {
                    aplicarPenalidadeNaoCumpridaNaChegada();
                }

                int fase = controle.aguardarFimDaVolta();
                if (fase < 0) {
                    break;
                }
            }
        } catch (RuntimeException e) {
            abandonar("erro inesperado: " + e.getClass().getSimpleName());

            if (registradoNoPhaser && !controle.isSincronizacaoTerminada()) {
                controle.sairDaSincronizacao();
                registradoNoPhaser = false;
            }

            System.err.println("Erro na thread de " + piloto.getNome() + ": " + e.getMessage());
        } finally {
            if (registradoNoPhaser && abandonou && !controle.isSincronizacaoTerminada()) {
                controle.sairDaSincronizacao();
            }
        }
    }

    private void executarVolta() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double variacao = random.nextDouble(-0.38, 0.38)
                * (1.0 + (100 - piloto.getConsistencia()) / 45.0);

        double tempoVolta = 89.0
                + (100 - piloto.getVelocidade()) * 0.055
                + (100 - equipe.getRitmo()) * 0.075
                + (100 - piloto.getConsistencia()) * 0.012
                + pneu.getDeltaTempo()
                + variacao;

        if (desgastePneu > 55) {
            tempoVolta += (desgastePneu - 55) * 0.025;
        }

        Clima clima = controle.getClima();

        if (clima == Clima.CHUVA_LEVE) {
            if (pneu == Pneu.INTERMEDIATE) {
                tempoVolta -= 0.55;
            } else if (pneu == Pneu.WET) {
                tempoVolta += 1.5;
            } else {
                tempoVolta += 7.5 - (piloto.getChuva() - 80) * 0.07;
            }
        } else if (clima == Clima.CHUVA_FORTE) {
            if (pneu == Pneu.WET) {
                tempoVolta -= 0.45;
            } else if (pneu == Pneu.INTERMEDIATE) {
                tempoVolta += 4.5;
            } else {
                tempoVolta += 16.0 - (piloto.getChuva() - 80) * 0.09;
            }
        } else {
            if (pneu == Pneu.INTERMEDIATE) {
                tempoVolta += 2.8;
            } else if (pneu == Pneu.WET) {
                tempoVolta += 4.8;
            }
        }

        ModoCorrida modo = controle.getModoCorrida();
        switch (modo) {
            case BANDEIRA_AMARELA -> tempoVolta += 2.0;
            case VSC -> tempoVolta += 12.0;
            case SAFETY_CAR -> tempoVolta += 28.0;
            case BANDEIRA_VERMELHA, NORMAL -> { }
        }

        if (modo == ModoCorrida.NORMAL && random.nextDouble() < 0.13) {
            double disputa = (piloto.getUltrapassagem() - 85) * 0.015
                    + random.nextDouble(-0.18, 0.28);
            tempoVolta -= disputa;
        }

        adicionarTempo(tempoVolta);

        double fatorGerenciamento = 1.12 - piloto.getGerenciamentoPneus() / 180.0;
        desgastePneu += pneu.getDesgasteBase() * Math.max(0.48, fatorGerenciamento);

        if (clima == Clima.CHUVA_FORTE && !pneu.isChuva()) {
            desgastePneu += 1.0;
        }

        try {
            Thread.sleep(random.nextInt(6, 20));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            abandonar("thread interrompida");
        }
    }

    private void verificarPitStop() {
        Pneu ideal = escolherPneuIdeal();
        boolean pneuErrado = ideal != pneu
                && (ideal.isChuva() != pneu.isChuva()
                || controle.getClima() == Clima.CHUVA_FORTE);

        double limiteDesgaste = switch (pneu) {
            case SOFT -> 62.0;
            case MEDIUM -> 72.0;
            case HARD -> 78.0;
            case INTERMEDIATE, WET -> 70.0;
        };

        boolean fimProximo = controle.getTotalVoltas() - volta <= 3;

        if (pitObrigatorio || pneuErrado || (!fimProximo && desgastePneu >= limiteDesgaste)) {
            Pneu novoPneu = ideal;
            if (!pneuErrado && !pitObrigatorio && !ideal.isChuva()) {
                novoPneu = escolherCompostoSeco();
            }

            double perda = PitStop.realizar(this, novoPneu);
            adicionarTempo(perda);
            desgastePneu = 0;
            pitObrigatorio = false;
        }
    }

    private Pneu escolherPneuIdeal() {
        return switch (controle.getClima()) {
            case CHUVA_FORTE -> Pneu.WET;
            case CHUVA_LEVE -> Pneu.INTERMEDIATE;
            case SECO, NUBLADO -> escolherCompostoSeco();
        };
    }

    private Pneu escolherCompostoSeco() {
        int restantes = controle.getTotalVoltas() - volta;

        if (restantes <= 15) {
            return Pneu.SOFT;
        }
        if (restantes <= 30) {
            return Pneu.MEDIUM;
        }
        return Pneu.HARD;
    }

    private void cumprirPenalidadeDePassagem() {
        if (penalidadeServico == Penalidade.DRIVE_THROUGH) {
            double perda = ThreadLocalRandom.current().nextDouble(20.0, 25.0);
            adicionarTempo(perda);
            System.out.printf("🚨 %s cumpriu DRIVE-THROUGH (+%.1fs).%n",
                    piloto.getNome(), perda);
            penalidadeServico = Penalidade.NENHUMA;
        } else if (penalidadeServico == Penalidade.STOP_AND_GO) {
            double perda = ThreadLocalRandom.current().nextDouble(30.0, 36.0);
            adicionarTempo(perda);
            System.out.printf("🚨 %s cumpriu STOP-AND-GO (+%.1fs).%n",
                    piloto.getNome(), perda);
            penalidadeServico = Penalidade.NENHUMA;
        }
    }

    private void aplicarPenalidadeNaoCumpridaNaChegada() {
        if (penalidadeServico == Penalidade.DRIVE_THROUGH) {
            adicionarTempo(25.0);
            System.out.println("⚖️ " + piloto.getNome()
                    + " não pôde cumprir o drive-through e recebeu +25s.");
        } else if (penalidadeServico == Penalidade.STOP_AND_GO) {
            adicionarTempo(35.0);
            System.out.println("⚖️ " + piloto.getNome()
                    + " não pôde cumprir o stop-and-go e recebeu +35s.");
        }
        penalidadeServico = Penalidade.NENHUMA;
    }

    public synchronized void receberPenalidade(Penalidade penalidade, String motivo) {
        switch (penalidade) {
            case CINCO_SEGUNDOS -> {
                tempoTotal += 5.0;
                System.out.println("⚖️ " + piloto.getNome()
                        + " recebeu 5 segundos — " + motivo + ".");
            }
            case DEZ_SEGUNDOS -> {
                tempoTotal += 10.0;
                System.out.println("⚖️ " + piloto.getNome()
                        + " recebeu 10 segundos — " + motivo + ".");
            }
            case DRIVE_THROUGH -> {
                penalidadeServico = Penalidade.DRIVE_THROUGH;
                System.out.println("🚨 " + piloto.getNome()
                        + " recebeu DRIVE-THROUGH — " + motivo + ".");
            }
            case STOP_AND_GO -> {
                penalidadeServico = Penalidade.STOP_AND_GO;
                System.out.println("🚨 " + piloto.getNome()
                        + " recebeu STOP-AND-GO — " + motivo + ".");
            }
            case NENHUMA -> { }
        }
    }

    public synchronized int registrarLimiteDePista() {
        avisosLimitePista++;
        return avisosLimitePista;
    }

    public synchronized void abandonar(String motivo) {
        if (!abandonou) {
            abandonou = true;
            motivoAbandono = motivo;
            System.out.println("❌ DNF: " + piloto.getNome() + " — " + motivo + ".");
        }
    }

    public synchronized void adicionarTempo(double segundos) { tempoTotal += segundos; }
    public synchronized void adicionarTempoInicial(double segundos) { tempoTotal += segundos; }
    public synchronized double getTempoTotal() { return tempoTotal; }
    public synchronized void setTempoTotal(double tempoTotal) { this.tempoTotal = tempoTotal; }

    public void marcarPitObrigatorio() { pitObrigatorio = true; }
    public void trocarPneu(Pneu pneu) { this.pneu = pneu; }
    public void setDesgastePneu(double desgastePneu) { this.desgastePneu = desgastePneu; }
    public void incrementarPitStops() { pitStops++; }

    public Piloto getPiloto() { return piloto; }
    public Equipe getEquipe() { return equipe; }
    public ControleCorrida getControle() { return controle; }
    public Pneu getPneu() { return pneu; }
    public int getVolta() { return volta; }
    public double getDesgastePneu() { return desgastePneu; }
    public boolean isAbandonou() { return abandonou; }
    public String getMotivoAbandono() { return motivoAbandono; }
    public int getPitStops() { return pitStops; }
    public int getPosicaoLargada() { return posicaoLargada; }
    public void setPosicaoLargada(int posicaoLargada) { this.posicaoLargada = posicaoLargada; }
}
