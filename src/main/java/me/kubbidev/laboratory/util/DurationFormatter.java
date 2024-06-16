package me.kubbidev.laboratory.util;

import me.kubbidev.laboratory.locale.TranslationManager;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class DurationFormatter {
    public static final DurationFormatter LONG = new DurationFormatter(false);
    public static final DurationFormatter CONCISE = new DurationFormatter(true);
    public static final DurationFormatter CONCISE_LOW_ACCURACY = new DurationFormatter(true, 3);

    private static final ChronoUnit[] UNITS = new ChronoUnit[]{
            ChronoUnit.YEARS,
            ChronoUnit.MONTHS,
            ChronoUnit.WEEKS,
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS
    };

    private final boolean concise;
    private final int accuracy;

    public DurationFormatter(boolean concise) {
        this(concise, Integer.MAX_VALUE);
    }

    public DurationFormatter(boolean concise, int accuracy) {
        this.concise = concise;
        this.accuracy = accuracy;
    }

    /**
     * Formats {@code duration} as a string.
     *
     * @param duration the duration.
     * @return the formatted string.
     */
    public String format(Duration duration) {
        return format(duration, null);
    }

    /**
     * Formats {@code duration} as a string.
     *
     * @param duration the duration.
     * @param locale the locale to format.
     * @return the formatted string.
     */
    public String format(Duration duration, @Nullable Locale locale) {
        long seconds = duration.getSeconds();
        StringBuilder builder = new StringBuilder();
        int outputSize = 0;

        for (ChronoUnit unit : UNITS) {
            long n = seconds / unit.getDuration().getSeconds();
            if (n > 0) {
                seconds -= unit.getDuration().getSeconds() * n;
                if (outputSize != 0) {
                    builder.append(' ');
                }
                builder.append(formatPart(n, unit, locale));
                outputSize++;
            }
            if (seconds <= 0 || outputSize >= this.accuracy) {
                break;
            }
        }

        if (outputSize == 0) {
            return formatPart(0, ChronoUnit.SECONDS, locale);
        }
        return builder.toString();
    }

    // Translation keys are in the format:
    //   laboratory.duration.unit.years.plural={0} years
    //   laboratory.duration.unit.years.singular={0} year
    //   laboratory.duration.unit.years.short={0}y
    // ... and so on

    private String formatPart(long amount, ChronoUnit unit, @Nullable Locale locale) {
        String format = this.concise ? "short" : amount == 1 ? "singular" : "plural";
        String translationKey = "laboratory.duration.unit." + unit.name().toLowerCase(Locale.ROOT) + "." + format;
        return TranslationManager.render(translationKey, locale, amount);
    }
}