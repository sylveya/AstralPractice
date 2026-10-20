package dev.lokspel.practice.util;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class MiniMessageTagResolver {

  private static final String TEXTURE_URL = "https://textures.minecraft.net/texture/";

  private static final Set<String> TEXTURE_TAGS = Set.of("head_texture", "headtexture");

  private static final Pattern TEXTURE_HASH = Pattern.compile("[0-9a-fA-F]{64}");

  private static final Pattern UUID_PATTERN =
      Pattern.compile(
          "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-" + "[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-" + "[0-9a-fA-F]{12}");

  private static final Base64.Decoder DECODER = Base64.getDecoder();
  private static final Base64.Encoder ENCODER = Base64.getEncoder();

  private MiniMessageTagResolver() {}

  /** Creates a MiniMessage instance with standard tags and custom player-head tags. */
  public static MiniMessage createMiniMessage() {
    return MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.defaults())
                .resolver(createResolver())
                .build())
        .build();
  }

  /** Creates a resolver for custom player-head tags. */
  private static TagResolver createResolver() {
    return new HeadTagResolver();
  }

  private static Tag createHead(String texture, boolean hat) {
    String parsedTexture = parseTexture(texture);

    PlayerHeadObjectContents contents =
        ObjectContents.playerHead()
            .id(UUID.nameUUIDFromBytes(parsedTexture.getBytes(StandardCharsets.UTF_8)))
            .profileProperty(PlayerHeadObjectContents.property("textures", parsedTexture))
            .hat(hat)
            .build();

    return Tag.selfClosingInserting(Component.object(contents));
  }

  private static String parseTexture(String texture) {
    texture = texture.trim();

    if (texture.length() >= 2 && texture.startsWith("\"") && texture.endsWith("\"")) {
      texture = texture.substring(1, texture.length() - 1);
    }

    if (isHttpsUrl(texture)) {
      return encodeTexture(texture);
    }

    if (TEXTURE_HASH.matcher(texture).matches()) {
      return encodeTexture(TEXTURE_URL + texture);
    }

    return texture;
  }

  private static String encodeTexture(String url) {
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

  private static boolean isTexture(String texture) {
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

  private static boolean parseHat(ArgumentQueue args) {
    if (!args.hasNext()) {
      return true;
    }

    Tag.Argument argument = args.pop();
    String value = argument.value();

    return !argument.isFalse() && !"false".equalsIgnoreCase(value);
  }

  private static final class HeadTagResolver implements TagResolver {

    @Override
    public @Nullable Tag resolve(
        @NonNull String name, @NonNull ArgumentQueue args, @NonNull Context ctx) {
      if (TEXTURE_TAGS.contains(name)) {
        if (!args.hasNext()) {
          throw ctx.newException("Missing texture argument for head_texture tag", args);
        }

        String texture = args.pop().value();

        return createHead(texture, parseHat(args));
      }

      if (!"head".equals(name) || !args.hasNext()) {
        return null;
      }

      Tag.Argument argument = args.peek();

      if (argument == null || !isTexture(argument.value())) {
        return null;
      }

      args.pop();

      return createHead(argument.value(), parseHat(args));
    }

    @Override
    public boolean has(@NonNull String name) {
      return TEXTURE_TAGS.contains(name) || "head".equals(name);
    }
  }
}
