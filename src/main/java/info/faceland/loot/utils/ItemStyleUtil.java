package info.faceland.loot.utils;

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.TRUE_WHITE;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.color;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.fakeNoShadow;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.noShadow;

import com.google.common.collect.Lists;
import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.items.SocketGem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ItemStyleUtil {

  public static final List<String> ENCHANT_CHARS = Lists.reverse(
      List.of("俼", "俪", "俫", "俬", "俭", "修", "俯", "俱", "俲", "俳"));
  public static final List<String> CORRUPT_STAT_FX = List.of("懜" + "懝" + "懞" + "懟");

  public static final TextComponent EMPTY_GEM = noShadow(Component.text("並"));
  public static final TextComponent LOCKED_GEM = noShadow(Component.text("丧"));
  public static final TextComponent GEM_T1 = noShadow(Component.text("丨"));
  public static final TextComponent GEM_T2 = noShadow(Component.text("丩"));
  public static final TextComponent GEM_T3 = noShadow(Component.text("个"));
  public static final TextComponent GEM_T4 = noShadow(Component.text("丫"));
  public static final TextComponent GEM_T3_2 = noShadow(Component.text("丬"));
  public static final TextComponent GEM_T4_2 = noShadow(Component.text("中"));

  public static final TextComponent UNTRADEABLE_NORMAL = noShadow(color(Component.text(
      GUI.spacesOf(12) + "俎" + GUI.spacesOf(-123 + -13)), TRUE_WHITE));
  public static final TextComponent UNTRADEABLE_SMALL = noShadow(color(Component.text(
      GUI.spacesOf(5) + "俎" + GUI.spacesOf(-123 + -6)), TRUE_WHITE));

  public static final String CORRUPTION = "俾";

  public static final TextComponent HALFWAY =  Component.text(GUI.spacesOf(74));
  public static final TextComponent HALFWAY_SMALL =  Component.text(GUI.spacesOf(67));
  public static final TextComponent JUMP_BACK = Component.text("\uF80B");

  public static final TextComponent BUFFER_1_GEM = HALFWAY.append(Component.text(GUI.spacesOf(-5)));
  public static final TextComponent BUFFER_2_GEM = HALFWAY.append(Component.text(GUI.spacesOf(-25)));
  public static final TextComponent BUFFER_3_GEM = HALFWAY.append(Component.text(GUI.spacesOf(-34)));
  public static final TextComponent BUFFER_4_GEM = HALFWAY.append(Component.text(GUI.spacesOf(-48)));

  public static final TextComponent SPACING_2_GEMS = Component.text(GUI.spacesOf(27));
  public static final TextComponent SPACING_3_GEMS = Component.text(GUI.spacesOf(18));
  public static final TextComponent SPACING_4_GEMS = Component.text(GUI.spacesOf(17));

  public static List<String> upgradeNumbers = List.of(
      "丰",
      "丱",
      "串",
      "丳",
      "临",
      "丵",
      "丶",
      "丷",
      "丸",
      "\uF822丹",
      "\uF822为",
      "\uF822主",
      "\uF822丼",
      "\uF822丽",
      "\uF822举"
  );

  private static final Map<Character, Float> NORMAL_CHAR_WIDTHS = buildCharWidths();

  /*
  public static List<String> breakText(String s, boolean large) {
    int maxLineSize = large ? 138 : 112;
    String[] strs = s.split(" ");
    List<String> lore = new ArrayList<>();
    List<String> line = new ArrayList<>();
    int spaceWidth = 3;
    int lineCount = 0;
    for (String word : strs) {
      String stripped = ChatColor.stripColor(word);
      int wordSize = countNormalTextSize(stripped);
      if (lineCount + wordSize + (line.size() * spaceWidth) > maxLineSize) {
        lore.add(StringUtils.join(line, " "));
        line.clear();
        line.add(word);
        lineCount = wordSize;
      } else {
        line.add(word);
        lineCount += wordSize;
      }
    }
    if (!line.isEmpty()) {
      lore.add(StringUtils.join(line, " "));
    }
    return lore;
  }
  */

  public static TextComponent centerText(TextComponent s, boolean large) {
    return centerText(s, large, true);
  }

  public static TextComponent centerText(TextComponent s, boolean large, boolean jumpBack) {
    return large ? centerLargeText(s, jumpBack) : centerSmallText(s, jumpBack);
  }

  public static TextComponent centerLargeText(TextComponent s) {
    return centerLargeText(s, true);
  }

  public static TextComponent centerSmallText(TextComponent s)  {
    return centerSmallText(s, true);
  }

  public static TextComponent centerLargeText(TextComponent s, boolean jumpBack) {
    var txt = HALFWAY.append(Component.text(GUI.spacesOf(-countNormalTextSize(s) / 2)).append(s));
    return jumpBack ? txt.append(JUMP_BACK) : txt;
  }

  public static TextComponent centerSmallText(TextComponent s, boolean jumpBack)  {
    var txt = HALFWAY_SMALL.append(Component.text(GUI.spacesOf(-countNormalTextSize(s) / 2))).append(s);
    return jumpBack ? txt.append(JUMP_BACK) : txt;
  }

  public static TextComponent generateGemString(int slots, int extendSlots, List<SocketGem> sockets) {
    return switch (slots) {
      case 1 -> BUFFER_1_GEM
          .append(socketSlotStyle(sockets, 0, extendSlots > 0).append(JUMP_BACK));
      case 2 -> BUFFER_2_GEM
          .append(socketSlotStyle(sockets, 0, extendSlots > 1).append(SPACING_2_GEMS)
          .append(socketSlotStyle(sockets, 1, extendSlots > 0).append(JUMP_BACK)));
      case 3 -> BUFFER_3_GEM
          .append(socketSlotStyle(sockets, 0, extendSlots > 2).append(SPACING_3_GEMS)
          .append(socketSlotStyle(sockets, 1, extendSlots > 1).append(SPACING_3_GEMS)
          .append(socketSlotStyle(sockets, 2, extendSlots > 0)))).append(JUMP_BACK);
      default -> BUFFER_4_GEM
          .append(socketSlotStyle(sockets, 0, extendSlots > 3)).append(SPACING_4_GEMS)
          .append(socketSlotStyle(sockets, 1, extendSlots > 2)).append(SPACING_4_GEMS)
          .append(socketSlotStyle(sockets, 2, extendSlots > 1)).append(SPACING_4_GEMS)
          .append(socketSlotStyle(sockets, 3, extendSlots > 0)).append(JUMP_BACK);
    };
  }

  public static TextComponent getCorruptAddon() {
    return Component.text(" 應").color(NamedTextColor.WHITE);
  }

  public static TextComponent socketSlotStyle(List<SocketGem> sockets, int index, boolean extend) {
    if (extend) {
      return LOCKED_GEM;
    }
    if (index > sockets.size() - 1) {
      return EMPTY_GEM;
    }
    SocketGem gem = sockets.get(index);
    if (gem == null) {
      return GEM_T1;
    }
    return switch (gem.getRarity()) {
      case "common" -> GEM_T1;
      case "uncommon" -> GEM_T2;
      case "rare" -> GEM_T3;
      case "epic" -> GEM_T4;
      case "unique" -> GEM_T3_2;
      default -> GEM_T4_2;
    };
  }

  public static String doPrefix(String s) {
    return s.toLowerCase()
        .replace("a", "倀\uF802")
        .replace("b", "倁\uF802")
        .replace("c", "倂\uF802")
        .replace("d", "倃\uF802")
        .replace("e", "倄\uF802")
        .replace("f", "倅\uF802")
        .replace("g", "倆\uF802")
        .replace("h", "倇\uF802")
        .replace("i", "倈\uF802")
        .replace("j", "倉\uF802")
        .replace("k", "倊\uF802")
        .replace("l", "個\uF802")
        .replace("m", "倌\uF802")
        .replace("n", "倍\uF802")
        .replace("o", "倎\uF802")
        .replace("p", "倏\uF802")
        .replace("q", "倖\uF802")
        .replace("r", "倗\uF802")
        .replace("s", "倘\uF802")
        .replace("t", "候\uF802")
        .replace("u", "倚\uF802")
        .replace("v", "倛\uF802")
        .replace("w", "倜\uF802")
        .replace("x", "倝\uF802")
        .replace("y", "倞\uF802")
        .replace("z", "借\uF802")
        .replace(" ", "俿\uF802");
  }

  public static String doSuffix(String s) {
    return s.toLowerCase()
        .replace("a", "乀\uF802")
        .replace("b", "乁\uF802")
        .replace("c", "乂\uF802")
        .replace("d", "乃\uF802")
        .replace("e", "乄\uF802")
        .replace("f", "久\uF802")
        .replace("g", "乆\uF802")
        .replace("h", "乇\uF802")
        .replace("i", "么\uF802")
        .replace("j", "义\uF802")
        .replace("k", "乊\uF802")
        .replace("l", "之\uF802")
        .replace("m", "乌\uF802")
        .replace("n", "乍\uF802")
        .replace("o", "乎\uF802")
        .replace("p", "乏\uF802")
        .replace("q", "乐\uF802")
        .replace("r", "乑\uF802")
        .replace("s", "乒\uF802")
        .replace("t", "乓\uF802")
        .replace("u", "乔\uF802")
        .replace("v", "乕\uF802")
        .replace("w", "乖\uF802")
        .replace("x", "乗\uF802")
        .replace("y", "乘\uF802")
        .replace("z", "乙\uF802")
        .replace(" ", "乚\uF802");
  }

  public static TextComponent nameLoreLine(String name, boolean edited) {
    return HALFWAY.append(Component.text(GUI.spacesOf(-countTitleSize(name) / 2))
        .append(fakeNoShadow(Component.text(doNameThing(name, edited) + "\uF80C")).color(NamedTextColor.WHITE)));
  }

  public static String doNameThing(String s, boolean edited) {
    String str = s.toLowerCase()
        .replace("a", "俐")
        .replace("b", "俑")
        .replace("c", "俒")
        .replace("d", "俓")
        .replace("e", "俔")
        .replace("f", "俕")
        .replace("g", "俖")
        .replace("h", "俗")
        .replace("i", "俘")
        .replace("j", "俙")
        .replace("k", "俚")
        .replace("l", "俛")
        .replace("m", "俜")
        .replace("n", "保")
        .replace("o", "俞")
        .replace("p", "俟")
        .replace("q", "俠")
        .replace("r", "信")
        .replace("s", "俢")
        .replace("t", "俣")
        .replace("u", "俤")
        .replace("v", "俥")
        .replace("w", "俦")
        .replace("x", "俧")
        .replace("y", "俨")
        .replace("z", "俩")
        .replace("'", "俏");
    if (edited) {
      str += "俻";
    }
    return str;
  }

  public static TextComponent smallNameLoreLine(String name) {
    return HALFWAY_SMALL.append(Component.text(GUI.spacesOf(-countTitleSize(name) / 2))
        .append(fakeNoShadow(Component.text(doSmallNameThing(name) + "\uF80C").color(NamedTextColor.WHITE))));
  }

  public static String doSmallNameThing(String s) {
    return s.toLowerCase()
        .replace("a", "加")
        .replace("b", "务")
        .replace("c", "劢")
        .replace("d", "劣")
        .replace("e", "劤")
        .replace("f", "劥")
        .replace("g", "劦")
        .replace("h", "劧")
        .replace("i", "动")
        .replace("j", "助")
        .replace("k", "努")
        .replace("l", "劫")
        .replace("m", "劬")
        .replace("n", "劭")
        .replace("o", "劮")
        .replace("p", "劯")
        .replace("q", "劰")
        .replace("r", "励")
        .replace("s", "劲")
        .replace("t", "劳")
        .replace("u", "労")
        .replace("v", "劵")
        .replace("w", "劶")
        .replace("x", "劷")
        .replace("y", "劸")
        .replace("z", "効")
        .replace("'", "婱");
  }

  public static int countSubtitleSize(String s) {
    s = s.toLowerCase();
    int size = s.length() * 6;
    size -= StringUtils.countMatches(s, ' ') * 2;
    size -= StringUtils.countMatches(s, 'z');
    size -= StringUtils.countMatches(s, 'x');
    size -= StringUtils.countMatches(s, 'k');
    size -= StringUtils.countMatches(s, 'l');
    size -= StringUtils.countMatches(s, 'h');
    size -= StringUtils.countMatches(s, 'i') * 2;
    size -= StringUtils.countMatches(s, 'e');
    size -= StringUtils.countMatches(s, 'f');
    return size;
  }

  public static int countTitleSize(String s) {
    s = s.toLowerCase();
    int size = s.length() * 7;
    size += StringUtils.countMatches(s, 'm'); // space is 7 chars, base is 6, so sub 3
    size += StringUtils.countMatches(s, 'x');
    size += StringUtils.countMatches(s, 'w');
    size -= StringUtils.countMatches(s, 'l');
    size -= StringUtils.countMatches(s, "'") * 4;
    size -= StringUtils.countMatches(s, ' ') * 3;
    return size;
  }

  public static int countNormalTextSize(TextComponent s) {
    float size = 0;
    var mainContent = s.content();
    for (var letter : mainContent.toCharArray()) {
      size += NORMAL_CHAR_WIDTHS.getOrDefault(letter, 6f);
    }
    for (var component : s.children()) {
      if (component instanceof TextComponent) {
        var str = ((TextComponent) component).content();
        for (var letter : str.toCharArray()) {
          size += NORMAL_CHAR_WIDTHS.getOrDefault(letter, 6f);
        }
      }
    }
    return (int) size;
  }

  private static Map<Character, Float> buildCharWidths() {
    Map<Character, Float> chars = new HashMap<>();
    chars.put('.', 2f);
    chars.put('*', 5f);
    chars.put(',', 2f);
    chars.put('1', 5f);
    chars.put('!', 2f);
    chars.put(':', 2f);
    chars.put('i', 2f);
    chars.put('I', 4f);
    chars.put('l', 3f);
    chars.put('\'', 3f);
    chars.put('t', 4f);
    chars.put('f', 5f);
    chars.put('[', 4f);
    chars.put(']', 4f);
    chars.put('(', 4f);
    chars.put(')', 4f);
    chars.put(' ', 4f);
    chars.put('丐', 36f);
    chars.put('丑', 48f);
    chars.put('丒', 46f);
    chars.put('专', 58f);
    chars.put('且', 70f);
    chars.put('丕', 26f);
    chars.put('世', 48f);
    chars.put('丗', 22f);
    chars.put('丘', 42f);
    chars.put('丙', 40f);
    chars.put('业', 46f);
    chars.put('丛', 48f);
    chars.put('东', 42f);
    chars.put('丝', 40f); // air
    chars.put('九', 40f); // rage
    chars.put('僶', 40f); // unknown
    return chars;
  }
}
