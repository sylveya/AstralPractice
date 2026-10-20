package dev.lokspel.practice.util.minimessage.tag;

import dev.lokspel.practice.util.minimessage.MiniMessageTags;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** MiniMessage {@link TagResolver} providing the custom player-head tags. */
public final class HeadTagResolver implements TagResolver {

  @Override
  public @Nullable Tag resolve(
      @NonNull String name, @NonNull ArgumentQueue args, @NonNull Context ctx) {
    if (MiniMessageTags.TEXTURE_TAGS.contains(name)) {
      if (!args.hasNext()) {
        throw ctx.newException("Missing texture argument for head_texture tag", args);
      }

      String texture = args.pop().value();

      return createHead(texture, parseHat(args));
    }

    if (!MiniMessageTags.HEAD_TAG.equals(name) || !args.hasNext()) {
      return null;
    }

    Tag.Argument argument = args.peek();

    if (argument == null || !HeadTexture.isTexture(argument.value())) {
      return null;
    }

    args.pop();

    return createHead(argument.value(), parseHat(args));
  }

  @Override
  public boolean has(@NonNull String name) {
    return MiniMessageTags.TEXTURE_TAGS.contains(name) || MiniMessageTags.HEAD_TAG.equals(name);
  }

  private static Tag createHead(String texture, boolean hat) {
    String parsedTexture = HeadTexture.parse(texture);

    PlayerHeadObjectContents contents =
        ObjectContents.playerHead()
            .id(UUID.nameUUIDFromBytes(parsedTexture.getBytes(StandardCharsets.UTF_8)))
            .profileProperty(PlayerHeadObjectContents.property("textures", parsedTexture))
            .hat(hat)
            .build();

    return Tag.selfClosingInserting(Component.object(contents));
  }

  private static boolean parseHat(ArgumentQueue args) {
    if (!args.hasNext()) {
      return true;
    }

    Tag.Argument argument = args.pop();
    String value = argument.value();

    return !argument.isFalse() && !"false".equalsIgnoreCase(value);
  }
}
