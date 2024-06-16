package me.kubbidev.laboratory.locale.translation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * A registry of translations. Used to register localized strings for translation keys. The registry can be submitted
 * to the {@link GlobalTranslator} or can translate manually through {@link #translate(String, Locale)}.
 *
 * <p>The recommended way to register translations is through {@link #registerAll(Locale, ResourceBundle, boolean)}</p>
 */
public final class TranslationRegistry implements Translator {

    /**
     * A pattern which matches a single quote.
     */
    public static final Pattern SINGLE_QUOTE_PATTERN = Pattern.compile("'");

    /**
     * Creates a new standalone translation registry.
     *
     * @param name the registry id
     * @return a translation registry
     */
    public static @NotNull TranslationRegistry create(String name) {
        return new TranslationRegistry(Objects.requireNonNull(name, "name"));
    }

    private final String name;
    private final Map<String, Translation> translations = new ConcurrentHashMap<>();

    private Locale defaultLocale = Locale.US; // en_us

    TranslationRegistry(String name) {
        this.name = name;
    }

    /**
     * Registers a translation.
     *
     * <pre>
     *   TranslationRegistry registry;
     *   registry.register("example.hello", Locale.US, new MessageFormat("Hi, {0}. How are you?"));
     * </pre>
     *
     * @param key    a translation key
     * @param locale a locale
     * @param format a translation format
     * @throws IllegalArgumentException if the translation key is already exists
     */
    public void register(@NotNull String key, @NotNull Locale locale, @NotNull MessageFormat format) {
        this.translations.computeIfAbsent(key, Translation::new).register(locale, format);
    }

    /**
     * Registers a map of translations.
     *
     * <pre>
     *   TranslationRegistry registry;
     *   Map&#60;String, MessageFormat&#62; translations;
     *
     *   translations.put("example.greeting", new MessageFormat("Greetings {0}. Doing ok?));
     *   translations.put("example.goodbye", new MessageFormat("Goodbye {0}. Have a nice day!));
     *
     *   registry.registerAll(Locale.US, translations);
     * </pre>
     *
     * @param locale  a locale
     * @param formats a map of translation keys to formats
     * @throws IllegalArgumentException if a translation key is already exists
     * @see #register(String, Locale, MessageFormat)
     */
    public void registerAll(@NotNull Locale locale, @NotNull Map<String, MessageFormat> formats) {
        this.registerAll(locale, formats.keySet(), formats::get);
    }

    /**
     * Registers a resource bundle of translations.
     *
     * @param locale             a locale
     * @param path               a path to the resource bundle
     * @param escapeSingleQuotes whether to escape single quotes
     * @throws IllegalArgumentException if a translation key is already exists
     * @see #registerAll(Locale, ResourceBundle, boolean)
     */
    public void registerAll(@NotNull Locale locale, @NotNull Path path, boolean escapeSingleQuotes) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            this.registerAll(locale, new PropertyResourceBundle(reader), escapeSingleQuotes);
        } catch (final IOException e) {
            // ignored
        }
    }

    /**
     * Registers a resource bundle of translations.
     *
     * <p>It is highly recommended to create your bundle using {@link UTF8ResourceBundleControl} as your bundle control for UTF-8 support - for example:</p>
     *
     * <pre>
     *   ResourceBundle bundle = ResourceBundle.getBundle("my_bundle", Locale.GERMANY, UTF8ResourceBundleControl.get());
     *   registry.registerAll(Locale.GERMANY, bundle, false);
     * </pre>
     *
     * @param locale             a locale
     * @param bundle             a resource bundle
     * @param escapeSingleQuotes whether to escape single quotes
     * @throws IllegalArgumentException if a translation key is already exists
     * @see UTF8ResourceBundleControl
     */
    public void registerAll(@NotNull Locale locale, @NotNull ResourceBundle bundle, boolean escapeSingleQuotes) {
        this.registerAll(locale, bundle.keySet(), key -> {
            String format = bundle.getString(key);
            return new MessageFormat(
                    escapeSingleQuotes
                            ? SINGLE_QUOTE_PATTERN.matcher(format).replaceAll("''")
                            : format,
                    locale
            );
        });
    }

    /**
     * Registers a resource bundle of translations.
     *
     * @param locale   a locale
     * @param keys     the translation keys to register
     * @param function a function to transform a key into a message format
     * @throws IllegalArgumentException if a translation key is already exists
     */
    public void registerAll(@NotNull Locale locale, @NotNull Set<String> keys, Function<String, MessageFormat> function) {
        IllegalArgumentException firstError = null;
        int errorCount = 0;
        for (String key : keys) {
            try {
                this.register(key, locale, function.apply(key));
            } catch (IllegalArgumentException e) {
                if (firstError == null) {
                    firstError = e;
                }
                errorCount++;
            }
        }
        if (firstError != null) {
            if (errorCount == 1) {
                throw firstError;
            } else if (errorCount > 1) {
                throw new IllegalArgumentException(String.format("Invalid key (and %d more)", errorCount - 1), firstError);
            }
        }
    }

    /**
     * Unregisters a translation key.
     *
     * @param key a translation key
     */
    public void unregister(@NotNull String key) {
        this.translations.remove(key);
    }

    /**
     * Checks if any translations are explicitly registered for the specified key.
     *
     * @param key a translation key
     * @return whether the registry contains a value for the translation key
     */
    public boolean contains(@NotNull String key) {
        return this.translations.containsKey(key);
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
        return this.name;
    }

    /**
     * Gets a message format from a key and locale.
     *
     * <p>If a translation for {@code locale} is not found, we will then try {@code locale} without a country code, and then finally fallback to a default locale.</p>
     *
     * @param locale a locale
     * @param key    a translation key
     * @return a message format or {@code null} to skip translation
     */
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        Translation translation = this.translations.get(key);
        if (translation == null) return null;
        return translation.translate(locale);
    }

    /**
     * Sets the default locale used by this registry.
     *
     * @param defaultLocale the locale to use a default
     */
    public void defaultLocale(@NotNull Locale defaultLocale) {
        this.defaultLocale = Objects.requireNonNull(defaultLocale, "defaultLocale");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranslationRegistry)) return false;

        TranslationRegistry other = (TranslationRegistry) o;
        return this.name.equals(other.name)
                && this.translations.equals(other.translations)
                && this.defaultLocale.equals(other.defaultLocale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.translations, this.defaultLocale);
    }

    final class Translation {
        private final String key;
        private final Map<Locale, MessageFormat> formats;

        Translation(@NotNull String key) {
            this.key = Objects.requireNonNull(key, "translation key");
            this.formats = new ConcurrentHashMap<>();
        }

        void register(@NotNull Locale locale, @NotNull MessageFormat format) {
            if (this.formats.putIfAbsent(Objects.requireNonNull(locale, "locale"), Objects.requireNonNull(format, "message format")) != null) {
                throw new IllegalArgumentException(String.format("Translation already exists: %s for %s", this.key, locale));
            }
        }

        @Nullable
        MessageFormat translate(@NotNull Locale locale) {
            MessageFormat format = this.formats.get(Objects.requireNonNull(locale, "locale"));
            if (format == null) {
                format = this.formats.get(Locale.of(locale.getLanguage())); // try without country
                if (format == null) {
                    format = this.formats.get(TranslationRegistry.this.defaultLocale); // try local default locale
                    if (format == null) {
                        format = this.formats.get(Locale.getDefault()); // try global default locale
                    }
                }
            }
            return format;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;

            if (!(o instanceof Translation))
                return false;

            Translation other = (Translation) o;
            return this.key.equals(other.key) &&
                    this.formats.equals(other.formats);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.key, this.formats);
        }
    }
}