package me.kubbidev.laboratory.locale;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.kubbidev.laboratory.Main;
import me.kubbidev.laboratory.locale.translation.GlobalTranslator;
import me.kubbidev.laboratory.locale.translation.TranslationRegistry;
import me.kubbidev.laboratory.locale.translation.Translator;
import me.kubbidev.laboratory.locale.translation.UTF8ResourceBundleControl;
import me.kubbidev.laboratory.util.MoreFiles;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class TranslationManager {
    /**
     * The default locale used by Laboratory messages
     */
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    @Getter
    private final Path translationsDirectory;
    private final Set<Locale> installed = ConcurrentHashMap.newKeySet();

    private TranslationRegistry registry;

    public TranslationManager() {
        this.translationsDirectory = Main.getApplicationFolder().resolve("translations");
        try {
            MoreFiles.createDirectoriesIfNotExists(this.translationsDirectory);
        } catch (IOException e) {
            // ignore
        }
    }

    public Set<Locale> getInstalledLocales() {
        return Collections.unmodifiableSet(this.installed);
    }

    public void reload() {
        // remove any previous registry
        if (this.registry != null) {
            GlobalTranslator.translator().removeSource(this.registry);
            this.installed.clear();
        }

        // create a translation registry
        this.registry = TranslationRegistry.create("laboratory");
        this.registry.defaultLocale(DEFAULT_LOCALE);

        // load custom translations first, then the base (built-in) translations after.
        loadFromFileSystem(this.translationsDirectory);
        loadFromResourceBundle();

        // register it to the global source, so our translations can be picked up by laboratory-platform
        GlobalTranslator.translator().addSource(this.registry);
    }

    /**
     * Loads the base (English) translations from the jar file.
     */
    private void loadFromResourceBundle() {
        ResourceBundle bundle = ResourceBundle.getBundle("laboratory", DEFAULT_LOCALE, UTF8ResourceBundleControl.get());
        try {
            this.registry.registerAll(DEFAULT_LOCALE, bundle, false);
        } catch (IllegalArgumentException e) {
            log.warn("Error loading default locale file", e);
        }
    }

    public static boolean isTranslationFile(Path path) {
        return path.getFileName().toString().endsWith(".properties");
    }

    /**
     * Loads custom translations (in any language) from the plugin configuration folder.
     */
    public void loadFromFileSystem(Path directory) {
        List<Path> translationFiles;
        try (Stream<Path> stream = Files.list(directory)) {
            translationFiles = stream.filter(TranslationManager::isTranslationFile).collect(Collectors.toList());
        } catch (IOException e) {
            translationFiles = Collections.emptyList();
        }

        if (translationFiles.isEmpty()) {
            return;
        }

        Map<Locale, ResourceBundle> loaded = new HashMap<>();
        for (Path translationFile : translationFiles) {
            try {
                Map.Entry<Locale, ResourceBundle> result = loadTranslationFile(translationFile);
                loaded.put(result.getKey(), result.getValue());
            } catch (Exception e) {
                log.warn("Error loading locale file: {}", translationFile.getFileName(), e);
            }
        }

        // try registering the locale without a country code - if we don't already have a registration for that
        loaded.forEach((locale, bundle) -> {
            Locale localeWithoutCountry = Locale.of(locale.getLanguage());
            if (!locale.equals(localeWithoutCountry) && !localeWithoutCountry.equals(DEFAULT_LOCALE) && this.installed.add(localeWithoutCountry)) {
                try {
                    this.registry.registerAll(localeWithoutCountry, bundle, false);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        });
    }

    private Map.Entry<Locale, ResourceBundle> loadTranslationFile(Path translationFile) throws IOException {
        String fileName = translationFile.getFileName().toString();
        String localeString = fileName.substring(0, fileName.length() - ".properties".length());
        Locale locale = parseLocale(localeString);

        if (locale == null) {
            throw new IllegalStateException("Unknown locale '" + localeString + "' - unable to register.");
        }

        PropertyResourceBundle bundle;
        try (BufferedReader reader = Files.newBufferedReader(translationFile, StandardCharsets.UTF_8)) {
            bundle = new PropertyResourceBundle(reader);
        }

        this.registry.registerAll(locale, bundle, false);
        this.installed.add(locale);
        return Maps.immutableEntry(locale, bundle);
    }

    public static String render(String key, Object... args) {
        return render(key, null, args);
    }

    public static String render(String key, @Nullable Locale locale, Object... args) {
        if (locale == null) {
            locale = Locale.getDefault();
            if (locale == null) {
                locale = DEFAULT_LOCALE;
            }
        }
        MessageFormat message = GlobalTranslator.translator().translate(key, locale);
        if (message == null)
            return key;

        return message.format(args);
    }

    @Nullable
    public static Locale parseLocale(@Nullable String locale) {
        return locale == null ? null : Translator.parseLocale(locale);
    }
}
