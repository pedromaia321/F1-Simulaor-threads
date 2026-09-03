package f1.model;

public enum Pneu {
    SOFT(-0.65, 7.0, false),
    MEDIUM(0.00, 4.6, false),
    HARD(0.45, 3.1, false),
    INTERMEDIATE(1.40, 5.0, true),
    WET(2.40, 4.6, true);

    private final double deltaTempo;
    private final double desgasteBase;
    private final boolean chuva;

    Pneu(double deltaTempo, double desgasteBase, boolean chuva) {
        this.deltaTempo = deltaTempo;
        this.desgasteBase = desgasteBase;
        this.chuva = chuva;
    }

    public double getDeltaTempo() { return deltaTempo; }
    public double getDesgasteBase() { return desgasteBase; }
    public boolean isChuva() { return chuva; }
}
