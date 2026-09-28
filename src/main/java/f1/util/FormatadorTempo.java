package f1.util;

import java.util.Locale;

public final class FormatadorTempo {

    private FormatadorTempo() {}

    public static String formatar(double segundos) {
        if (Double.isNaN(segundos) || Double.isInfinite(segundos)) {
            return "--:--.---";
        }

        int minutos = (int) (segundos / 60.0);
        double resto = segundos - (minutos * 60.0);
        return String.format(Locale.US, "%d:%06.3f", minutos, resto);
    }

    public static String formatarIntervalo(double segundos) {
        return "+" + formatar(Math.max(0.0, segundos));
    }
}
