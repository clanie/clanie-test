/*
 * Copyright (C) 2026, Claus Nielsen, clausn999@gmail.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package dk.clanie.test.env;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit 5 extension that loads {@code .env.local} and injects its key-value
 * pairs as system properties before any tests in the annotated class run.
 *
 * <p>The extension searches the following locations in order and loads the
 * <em>first</em> file it finds:
 * <ol>
 *   <li>The project root (working directory — used on CI/Jenkins).</li>
 *   <li>One level above the project root (used for local development when
 *       secrets are kept in a parent workspace directory).</li>
 * </ol>
 *
 * <p>Only entries whose key is not already defined as a system property
 * <em>or</em> as a real environment variable are set, so CI-provided values
 * always take precedence.
 *
 * <p>Lines starting with {@code #} and blank lines are silently ignored.
 * Values may optionally be quoted with single or double quotes — the quotes
 * are stripped before the value is stored.
 *
 * <p>Usage:
 * <pre>{@code
 * @ExtendWith(EnvFileExtension.class)
 * class MyIntegrationTest {
 *     // System.getProperty("MY_KEY") now works
 * }
 * }</pre>
 */
public class EnvFileExtension implements BeforeAllCallback {

    private static final Logger log = LoggerFactory.getLogger(EnvFileExtension.class);

    private static final String ENV_FILE_NAME = ".env.local";


    @Override
    public void beforeAll(ExtensionContext context) {
        Path projectRoot = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        List<Path> candidates = List.of(
                projectRoot.resolve(ENV_FILE_NAME),
                projectRoot.getParent().resolve(ENV_FILE_NAME));

        for (Path candidate : candidates) {
            if (Files.isReadable(candidate)) {
                loadEnvFile(candidate);
                return;
            }
        }

        log.debug("No {} file found in {} or its parent directory — skipping.", ENV_FILE_NAME, projectRoot);
    }


    private void loadEnvFile(Path path) {
        log.info("Loading environment from {}", path);
        try {
            List<String> lines = Files.readAllLines(path);
            int loaded = 0;
            for (String line : lines) {
                String trimmed = line.strip();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).strip();
                String value = stripQuotes(trimmed.substring(eq + 1).strip());
                if (System.getenv(key) != null) {
                    log.debug("Skipping {} — already set as an environment variable.", key);
                    continue;
                }
                if (System.getProperty(key) != null) {
                    log.debug("Skipping {} — already set as a system property.", key);
                    continue;
                }
                System.setProperty(key, value);
                loaded++;
                log.debug("Set system property: {}", key);
            }
            log.info("Loaded {} entr{} from {}.", loaded, loaded == 1 ? "y" : "ies", path);
        } catch (IOException e) {
            log.warn("Failed to read {}: {}", path, e.getMessage());
        }
    }


    private String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

}
