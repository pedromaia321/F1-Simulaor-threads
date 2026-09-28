package f1.util;

public final class CoresConsole {

    public static final String RESET = "\u001B[0m";
    public static final String VERDE = "\u001B[32m";
    public static final String AMARELO = "\u001B[33m";
    public static final String ROXO = "\u001B[95m";
    public static final String CINZA = "\u001B[90m";

    private CoresConsole() {}

    public static String verde(String texto) {
        return VERDE + "🟢 " + texto + RESET;
    }

    public static String amarelo(String texto) {
        return AMARELO + "🟡 " + texto + RESET;
    }

    public static String roxo(String texto) {
        return ROXO + "🟣 " + texto + RESET;
    }

    public static String neutro(String texto) {
        return CINZA + "⚪ " + texto + RESET;
    }
}
