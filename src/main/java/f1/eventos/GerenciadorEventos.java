package f1.eventos;

import f1.model.Carro;
import f1.model.Penalidade;

import java.util.concurrent.ThreadLocalRandom;

public final class GerenciadorEventos {

    private GerenciadorEventos() {}

    public static void verificarEvento(Carro carro) {
        if (carro.isAbandonou()) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();

        verificarFalhaMecanica(carro, random);
        if (carro.isAbandonou()) {
            return;
        }

        verificarErroPiloto(carro, random);
        if (carro.isAbandonou()) {
            return;
        }

        verificarLimitesDePista(carro, random);
    }

    private static void verificarFalhaMecanica(Carro carro, ThreadLocalRandom random) {
        int perdaConfiabilidade = 100 - carro.getEquipe().getConfiabilidade();
        double chanceFalha = Math.max(0.00015, perdaConfiabilidade * 0.00006);

        if (random.nextDouble() >= chanceFalha) {
            return;
        }

        double tipo = random.nextDouble();

        if (tipo < 0.52) {
            carro.abandonar("falha de motor");
            System.out.println("🔥 Motor de " + carro.getPiloto().getNome() + " falhou.");

            if (random.nextDouble() < 0.30) {
                carro.getControle().ativarSafetyCar(
                        "carro parado em posição perigosa", carro.getVolta());
            } else {
                carro.getControle().ativarVSC(
                        "remoção do carro parado", carro.getVolta());
            }
        } else if (tipo < 0.76) {
            carro.adicionarTempo(random.nextDouble(4.0, 10.0));
            System.out.println("⚙️ " + carro.getPiloto().getNome()
                    + " teve problema no câmbio.");
        } else {
            carro.adicionarTempo(random.nextDouble(3.0, 8.0));
            System.out.println("🔋 " + carro.getPiloto().getNome()
                    + " sofreu perda de potência elétrica.");
        }
    }

    private static void verificarErroPiloto(Carro carro, ThreadLocalRandom random) {
        double agressividade = 1.0 + Math.max(0, carro.getPiloto().getAgressividade() - 85) * 0.018;
        double desgaste = 1.0 + Math.max(0, carro.getDesgastePneu() - 50) * 0.012;
        double clima = switch (carro.getControle().getClima()) {
            case SECO -> 1.0;
            case NUBLADO -> 1.03;
            case CHUVA_LEVE -> 1.45;
            case CHUVA_FORTE -> 2.10;
        };

        double chance = carro.getPiloto().getChanceErro()
                * agressividade
                * desgaste
                * clima;

        if (random.nextDouble() >= chance) {
            return;
        }

        double tipo = random.nextDouble();

        if (tipo < 0.34) {
            double perda = random.nextDouble(1.0, 4.0);
            carro.adicionarTempo(perda);
            System.out.printf("⚠️ %s cometeu um erro e perdeu %.1fs.%n",
                    carro.getPiloto().getNome(), perda);

        } else if (tipo < 0.58) {
            double perda = random.nextDouble(5.0, 14.0);
            carro.adicionarTempo(perda);
            System.out.printf("🌀 %s RODOU! Perdeu %.1fs.%n",
                    carro.getPiloto().getNome(), perda);

            if (random.nextDouble() < 0.45) {
                carro.getControle().ativarBandeiraAmarela(
                        "carro rodado de " + carro.getPiloto().getNome(),
                        carro.getVolta());
            }

        } else if (tipo < 0.72) {
            double perda = random.nextDouble(8.0, 18.0);
            carro.adicionarTempo(perda);
            carro.setDesgastePneu(100);
            carro.marcarPitObrigatorio();
            System.out.printf("🛞 %s sofreu um furo e perdeu %.1fs.%n",
                    carro.getPiloto().getNome(), perda);

        } else if (tipo < 0.82) {
            double perda = random.nextDouble(4.0, 9.0);
            carro.adicionarTempo(perda);
            carro.marcarPitObrigatorio();
            System.out.printf("🪽 %s danificou a asa dianteira (+%.1fs).%n",
                    carro.getPiloto().getNome(), perda);

        } else if (tipo < 0.93) {
            acidente(carro, random);

        } else {
            aplicarPenalidadeAleatoria(carro, random);
        }
    }

    private static void acidente(Carro carro, ThreadLocalRandom random) {
        carro.abandonar("acidente");

        double gravidade = random.nextDouble();

        if (gravidade < 0.12) {
            carro.getControle().ativarBandeiraVermelha(
                    "acidente forte de " + carro.getPiloto().getNome(),
                    carro.getVolta());
        } else if (gravidade < 0.72) {
            carro.getControle().ativarSafetyCar(
                    "acidente de " + carro.getPiloto().getNome(),
                    carro.getVolta());
        } else {
            carro.getControle().ativarVSC(
                    "remoção do carro de " + carro.getPiloto().getNome(),
                    carro.getVolta());
        }
    }

    private static void verificarLimitesDePista(Carro carro, ThreadLocalRandom random) {
        if (carro.getControle().getModoCorrida() != ModoCorrida.NORMAL) {
            return;
        }

        double chance = 0.0012
                + Math.max(0, carro.getPiloto().getAgressividade() - 88) * 0.00014;

        if (random.nextDouble() < chance) {
            int avisos = carro.registrarLimiteDePista();
            System.out.println("⚠️ " + carro.getPiloto().getNome()
                    + " excedeu os limites de pista. Aviso " + avisos + "/3.");

            if (avisos >= 3) {
                carro.receberPenalidade(Penalidade.CINCO_SEGUNDOS,
                        "reincidência em limites de pista");
            }
        }
    }

    private static void aplicarPenalidadeAleatoria(Carro carro, ThreadLocalRandom random) {
        double r = random.nextDouble();

        if (r < 0.45) {
            carro.receberPenalidade(Penalidade.CINCO_SEGUNDOS,
                    "incidente de corrida");
        } else if (r < 0.75) {
            carro.receberPenalidade(Penalidade.DEZ_SEGUNDOS,
                    "causar colisão");
        } else if (r < 0.94) {
            carro.receberPenalidade(Penalidade.DRIVE_THROUGH,
                    "infração grave");
        } else {
            carro.receberPenalidade(Penalidade.STOP_AND_GO,
                    "infração muito grave");
        }
    }
}
