package dev.lokspel.practice.util.minimessage;

import dev.lokspel.practice.util.minimessage.tag.HeadTagResolver;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

/** Central factory for the MiniMessage instances used across the plugin. */
public final class MiniMessageUtil {

  private MiniMessageUtil() {}

  /** Creates a MiniMessage instance with standard tags and custom player-head tags. */
  public static MiniMessage createMiniMessage() {
    return MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.defaults())
                .resolver(new HeadTagResolver())
                .build())
        .build();
  }
}
