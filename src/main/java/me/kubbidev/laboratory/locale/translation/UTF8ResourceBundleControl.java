package me.kubbidev.laboratory.locale.translation;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * A {@link ResourceBundle.Control} that enforces UTF-8 string encoding.
 *
 * <p>See <a href="https://stackoverflow.com/a/4660195">stackoverflow.com/a/4660195</a> for more details.</p>
 */
public final class UTF8ResourceBundleControl extends ResourceBundle.Control {
    private static final UTF8ResourceBundleControl INSTANCE = new UTF8ResourceBundleControl();

    /**
     * Gets the shared instance.
     *
     * @return a resource bundle control
     */
    public static ResourceBundle.@NotNull Control get() {
        return INSTANCE;
    }

    @Override
    public ResourceBundle newBundle(String baseName,
                                    Locale locale,
                                    String format,
                                    ClassLoader loader, boolean reload)

            throws IllegalAccessException, InstantiationException, IOException {
        if (format.equals("java.properties")) {

            String bundle = this.toBundleName(baseName, locale);
            String resource = this.toResourceName(bundle, "properties");

            InputStream is = null;
            if (reload) {
                URL url = loader.getResource(resource);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        is = connection.getInputStream();
                    }
                }
            } else {
                is = loader.getResourceAsStream(resource);
            }

            if (is != null) {
                try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    return new PropertyResourceBundle(isr);
                }
            } else {
                return null;
            }
        } else {
            return super.newBundle(baseName, locale, format, loader, reload);
        }
    }
}