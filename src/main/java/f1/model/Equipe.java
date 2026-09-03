package f1.model;

import java.util.concurrent.locks.ReentrantLock;

public class Equipe {
    private final String nome;
    private final int ritmo;
    private final int confiabilidade;
    private final int estrategia;
    private final int pitStop;
    private final ReentrantLock boxLock = new ReentrantLock(true);

    public Equipe(String nome, int ritmo, int confiabilidade, int estrategia, int pitStop) {
        this.nome = nome;
        this.ritmo = ritmo;
        this.confiabilidade = confiabilidade;
        this.estrategia = estrategia;
        this.pitStop = pitStop;
    }

    public String getNome() { return nome; }
    public int getRitmo() { return ritmo; }
    public int getConfiabilidade() { return confiabilidade; }
    public int getEstrategia() { return estrategia; }
    public int getPitStop() { return pitStop; }
    public ReentrantLock getBoxLock() { return boxLock; }
}
