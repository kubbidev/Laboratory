package me.kubbidev.laboratory.locale.translation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A global source of translations.
 *
 * <p>To add your translations to this source, use {@link GlobalTranslator#addSource(Translator)}
 * with a {@link TranslationRegistry} or your own implementation of a {@link Translator}.</p>
 */
public final class GlobalTranslator implements Translator {

    private static final String NAME = "global";
    private static final GlobalTranslator INSTANCE = new GlobalTranslator();

    /**
     * Gets the global translation source.
     *
     * @return the source
     */
    public static @NotNull GlobalTranslator translator() {
        return GlobalTranslator.INSTANCE;
    }

    private final Set<Translator> sources = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private GlobalTranslator() {
    }

    /**
     * A key identifying this translation source.
     *
     * <p>Intended to be used for display to users.</p>
     *
     * @return an identifier for this translation source
     */
    @Override
    public @NotNull String name() {
        return NAME;
    }

    /**
     * Gets the sources.
     *
     * @return the sources
     */
    public @NotNull Iterable<? extends Translator> sources() {
        return Collections.unmodifiableSet(this.sources);
    }

    /**
     * Adds a translation source.
     *
     * <p>Duplicate sources will be ignored.</p>
     *
     * @param source the source
     * @return {@code true} if registered, {@code false} otherwise
     * @throws IllegalArgumentException if source is {@link GlobalTranslator}
     */
    public boolean addSource(@NotNull Translator source) {
        Objects.requireNonNull(source, "source");
        if (source == this) throw new IllegalArgumentException("GlobalTranslationSource");
        return this.sources.add(source);
    }

    /**
     * Removes a translation source.
     *
     * @param source the source to unregister
     * @return {@code true} if unregistered, {@code false} otherwise
     */
    public boolean removeSource(@NotNull Translator source) {
        Objects.requireNonNull(source, "source");
        return this.sources.remove(source);
    }

    /**
     * Gets a message format from a key and locale.
     *
     * @param locale a locale
     * @param key    a translation key
     * @return a message format or {@code null} to skip translation
     */
    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(locale, "locale");

        for (Translator source : this.sources) {
            MessageFormat translation = source.translate(key, locale);
            if (translation != null) return translation;
        }
        return null;
    }
}