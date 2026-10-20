package dev.lokspel.practice.util.minimessage.tag;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Resolves, validates and encodes Minecraft player-head texture values used by MiniMessage tags.
 */
public final class HeadTexture {

  private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/";

  private static final Pattern TEXTURE_HASH = Pattern.compile("[0-9a-fA-F]{64}");

  private static final Pattern UUID_PATTERN =
      Pattern.compile(
          "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-" + "[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-" + "[0-9a-fA-F]{12}");

  private static final Base64.Decoder DECODER = Base64.getDecoder();
  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  private HeadTexture() {}

  /**
   * Normalizes a raw texture argument into the value accepted by {@code
   * PlayerHeadObjectContents.property}. URLs and raw hashes are encoded into the base64 JSON
   * payload, already encoded values are passed through unchanged.
   */
  public static String parse(String texture) {
    texture = texture.trim();

    if (texture.length() >= 2 && texture.startsWith("\"") && texture.endsWith("\"")) {
      texture = texture.substring(1, texture.length() - 1);
    }

    if (isHttpsUrl(texture)) {
      return encode(texture);
    }

    if (TEXTURE_HASH.matcher(texture).matches()) {
      return encode(TEXTURE_URL + texture);
    }

    return texture;
  }

  /** Returns whether the value already resolves to a valid skin texture. */
  public static boolean isTexture(String texture) {
    if (isHttpsUrl(texture) || TEXTURE_HASH.matcher(texture).matches()) {
      return true;
    }

    if (UUID_PATTERN.matcher(texture).matches() || texture.contains(":")) {
      return false;
    }

    try {
      byte[] decoded = DECODER.decode(texture);

      if (decoded.length == 0) {
        return false;
      }

      String decodedTexture = new String(decoded, StandardCharsets.UTF_8);

      return decodedTexture.contains("\"textures\"")
          && decodedTexture.contains("\"SKIN\"")
          && decodedTexture.contains("\"url\"");
    } catch (IllegalArgumentException ignored) {
      return false;
    }
  }

  private static String encode(String url) {
    String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";

    return ENCODER.encodeToString(json.getBytes(StandardCharsets.UTF_8));
  }

  private static boolean isHttpsUrl(String value) {
    try {
      URI uri = URI.create(value);

      return "https".equalsIgnoreCase(uri.getScheme())
          && uri.getHost() != null
          && !uri.getHost().isBlank()
          && uri.getUserInfo() == null;
    } catch (IllegalArgumentException ignored) {
      return false;
    }
  }
}
