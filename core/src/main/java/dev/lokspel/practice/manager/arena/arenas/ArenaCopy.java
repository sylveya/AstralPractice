package dev.lokspel.practice.manager.arena.arenas;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.arena.ArenaLayoutData;
import dev.lokspel.practice.manager.arena.arenas.interfaces.NormalArena;
import dev.lokspel.practice.util.Common;
import java.util.Map;
import lombok.Getter;

@Getter
public class ArenaCopy extends NormalArena {

  protected Arena mainArena;
  private final int number;

  public ArenaCopy(String name, Arena originalArena) {
    super(name);
    this.mainArena = originalArena;
    this.number = getCopyNumber(name);
  }

  public void delete() {
    AstralPractice.getArenaCopyUtilListener().deleteArena(mainArena.getDisplayName(), cuboid);
    Map<String, ArenaLayoutData> copies = mainArena.getArenaFile().getData().getCopies();
    if (copies != null) {
      copies.remove(name);
      if (copies.isEmpty()) {
        mainArena.getArenaFile().getData().setCopies(null);
      }
    }
  }

  @Override
  public String getDisplayName() {
    return mainArena.getDisplayName();
  }

  @Override
  public boolean isLocatorBar() {
    return mainArena.isLocatorBar();
  }

  private static int getCopyNumber(final String name) {
    int number;
    try {
      int lastUnderscoreIndex = name.lastIndexOf("_");
      if (lastUnderscoreIndex != -1 && lastUnderscoreIndex < name.length() - 1) {
        String numberPart = name.substring(lastUnderscoreIndex + 1);
        number = Integer.parseInt(numberPart);
        return number;
      } else {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      Common.sendConsoleMmMessage("<red>Invalid copy name: " + name);
    }
    return -1;
  }
}
