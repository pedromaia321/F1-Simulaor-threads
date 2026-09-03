package f1.model;

public class Piloto {
    private final String nome;
    private final int overall;
    private final int velocidade;
    private final int qualificacao;
    private final int ultrapassagem;
    private final int defesa;
    private final int chuva;
    private final int consistencia;
    private final int agressividade;
    private final int gerenciamentoPneus;
    private final double chanceErro;

    public Piloto(String nome, int overall, int velocidade, int qualificacao,
                  int ultrapassagem, int defesa, int chuva, int consistencia,
                  int agressividade, int gerenciamentoPneus, double chanceErro) {
        this.nome = nome;
        this.overall = overall;
        this.velocidade = velocidade;
        this.qualificacao = qualificacao;
        this.ultrapassagem = ultrapassagem;
        this.defesa = defesa;
        this.chuva = chuva;
        this.consistencia = consistencia;
        this.agressividade = agressividade;
        this.gerenciamentoPneus = gerenciamentoPneus;
        this.chanceErro = chanceErro;
    }

    public String getNome() { return nome; }
    public int getOverall() { return overall; }
    public int getVelocidade() { return velocidade; }
    public int getQualificacao() { return qualificacao; }
    public int getUltrapassagem() { return ultrapassagem; }
    public int getDefesa() { return defesa; }
    public int getChuva() { return chuva; }
    public int getConsistencia() { return consistencia; }
    public int getAgressividade() { return agressividade; }
    public int getGerenciamentoPneus() { return gerenciamentoPneus; }
    public double getChanceErro() { return chanceErro; }
}
