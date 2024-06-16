package me.kubbidev.laboratory.locale.translation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A message translator.
 *
 * <p>To see how to create a {@link Translator} with a {@link ResourceBundle}
 * see {@link TranslationRegistry#registerAll(Locale, ResourceBundle, boolean)}</p>
 *
 * <p>After creating a {@link Translator} you can add it to the {@link GlobalTranslator}
 * to enable automatic translations by the platforms.</p>
 *
 * @see TranslationRegistry
 */
public interface Translator {
    /**
     * Parses a {@link Locale} from a {@link String}.
     *
     * @param string the string
     * @return a locale
     */
    static @Nullable Locale parseLocale(@NotNull String string) {
        String[] segments = string.split("_", 3); // language_country_variant
        int length = segments.length;
        if (length == 1) {
            return Locale.of(string); // language
        } else if (length == 2) {
            return Locale.of(
                    segments[0],
                    segments[1]
            ); // language + country
        } else if (length == 3) {
            return Locale.of(
                    segments[0],
                    segments[1],
                    segments[2]
            ); // language + country + variant
        }
        return null;
    }

    /**
     * A key identifying this translation source.
     *
     * <p>Intended to be used for display to users.</p>
     *
     * @return an identifier for this translation source
     */
    @NotNull String name();

    /**
     * Gets a message format from a key and locale.
     *
     * @param locale a locale
     * @param key    a translation key
     * @return a message format or {@code null} to skip translation
     */
    @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale);
}