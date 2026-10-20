package dev.lokspel.practice.listener;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.format.ChatFormatUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Collection;
import java.util.Set;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerChatListener implements Listener {

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerChat(AsyncChatEvent e) {
    Player player = e.getPlayer();

    Profile profile = ProfileManager.getInstance().getProfile(player);
    Party party = PartyManager.getInstance().getParty(player);

    String rawMessage = PlainTextComponentSerializer.plainText().serialize(e.message());

    // Party chat
    String partyChatShortcut = PracticeConfig.settings().getChat().getPartyChat().getShortcut();

    boolean partyChatShortcutUsed =
        !partyChatShortcut.isEmpty() && rawMessage.startsWith(partyChatShortcut);

    if (PracticeConfig.settings().getChat().getPartyChat().isEnabled()
        && profile.isParty()
        && party != null
        && (partyChatShortcutUsed || profile.isPartyChat())) {

      String partyChatMessage =
          partyChatShortcutUsed ? rawMessage.substring(partyChatShortcut.length()) : rawMessage;

      if (party.canUsePartyChat(player)) {

        setViewers(e, party.getMembers());

        applyRenderer(
            e, Component.text(partyChatMessage), ChatFormatUtil.buildPartyChatMessage(player));

      } else {
        e.setCancelled(true);

        String disabled = PracticeConfig.language().getParty().getPartyChatDisabledByLeader();

        Bukkit.getScheduler()
            .runTask(AstralPractice.getInstance(), () -> Common.sendMmMessage(player, disabled));
      }

      return;
    }

    // Staff chat toggle
    if (profile.isStaffChat()) {

      applyStaffChat(e, player, rawMessage);

      return;
    }

    // Staff shortcut
    if (player.hasPermission("ap.staff")
        && PracticeConfig.settings().getChat().getStaffChat().isShortcut()
        && rawMessage.startsWith("#")) {

      applyStaffChat(e, player, rawMessage.substring(1));

      return;
    }

    // Server chat
    if (PracticeConfig.settings().getChat().isServerChatEnabled()) {

      applyMatchChatIsolation(e, player);

      applyRenderer(
          e, Component.text(rawMessage), ChatFormatUtil.buildServerChatMessage(profile, player));
    }
  }

  private void applyStaffChat(AsyncChatEvent e, Player player, String message) {

    setViewers(e, ChatFormatUtil.getStaffRecipients());

    applyRenderer(e, Component.text(message), ChatFormatUtil.buildStaffChatMessage(player));
  }

  private void applyRenderer(AsyncChatEvent e, Component message, String formatString) {
    e.renderer(
        (_, _, _, viewer) ->
            ChatFormatUtil.formatChatMessage(
                viewer instanceof CommandSender sender ? sender : null, formatString, message));
  }

  private void applyMatchChatIsolation(AsyncChatEvent e, Player sender) {

    if (!PracticeConfig.settings().getChat().isIsolateMatchChat()) {
      return;
    }

    MatchManager matchManager = MatchManager.getInstance();
    FfaManager ffaManager = FfaManager.getInstance();

    Set<Audience> viewers = e.viewers();

    viewers.add(Bukkit.getConsoleSender());

    Match match = matchManager.getLiveMatchByPlayer(sender);

    if (match != null) {
      viewers.clear();
      viewers.addAll(match.getPlayers());
      if (PracticeConfig.settings().getChat().isShowMatchChatToSpectators()) {
        viewers.addAll(match.getSpectators());
      }
      return;
    }

    Ffa ffa = ffaManager.getFfaByPlayer(sender);

    if (ffa != null) {
      viewers.clear();
      viewers.addAll(ffa.getPlayers().keySet());
      if (PracticeConfig.settings().getChat().isShowMatchChatToSpectators()) {
        viewers.addAll(ffa.getSpectators());
      }
      return;
    }

    viewers.removeIf(
        viewer ->
            viewer instanceof Player recipient
                && (matchManager.getLiveMatchByPlayer(recipient) != null
                    || ffaManager.getFfaByPlayer(recipient) != null));
  }

  private void setViewers(AsyncChatEvent e, Collection<? extends Audience> targets) {

    Set<Audience> viewers = e.viewers();

    viewers.clear();

    viewers.add(Bukkit.getConsoleSender());
    viewers.addAll(targets);
  }
}
