package dev.lokspel.practice.util.startup;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.util.Common;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class UpdateChecker {

  private UpdateChecker() {}

  private static final String RELEASE_URL =
      "https://api.github.com/repos/sylveya/AstralPractice/releases/latest";

  private static final String DOWNLOAD_URL = "https://github.com/sylveya/AstralPractice/releases";

  public static void checkAsync(AstralPractice plugin) {
    if (!PracticeConfig.settings().getUpdateChecker().isEnabled()) {
      return;
    }

    checkUpdates()
        .thenAccept(version -> report(plugin, version))
        .exceptionally(
            ignored -> {
              plugin.getLogger().warning("Update check failed");
              return null;
            });
  }

  public static CompletableFuture<String> checkUpdates() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return fetchLatestVersion();
          } catch (IOException e) {
            throw new CompletionException(e);
          }
        });
  }

  private static String fetchLatestVersion() throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(URI.create(RELEASE_URL).toURL().openStream()))) {

      JsonObject release = JsonParser.parseReader(reader).getAsJsonObject();

      return release.get("tag_name").getAsString().replaceFirst("^[vV]", "");
    }
  }

  private static void report(AstralPractice plugin, String version) {
    String current = plugin.getPluginMeta().getVersion().split("-")[0];

    if (version.equals(current)) {
      Common.sendConsoleMmMessage("<gray><green>You are using the latest version");
      return;
    }

    Common.sendConsoleMmMessage("<gray>[<yellow>A new version is available: <green>" + version);
    Common.sendConsoleMmMessage("<gray><yellow>Download: <aqua>" + DOWNLOAD_URL);
  }
}
