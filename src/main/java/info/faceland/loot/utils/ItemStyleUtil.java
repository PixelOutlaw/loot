package info.faceland.loot.utils;

import com.google.common.collect.Lists;
import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.WordUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.items.SocketGem;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;

public class ItemStyleUtil {

  public static final List<String> ENCHANT_CHARS = Lists.reverse(
      List.of("俼", "俪", "俫", "俬", "俭", "修", "俯", "俱", "俲", "俳"));
  public static final List<String> CORRUPT_STAT_FX = List.of("懜" + "懝" + "懞" + "懟");

  public static final String EMPTY_GEM = "並";
  public static final String LOCKED_GEM = "丧";
  public static final String GEM_T1 = "丨";
  public static final String GEM_T2 = "丩";
  public static final String GEM_T3 = "个";
  public static final String GEM_T4 = "丫";
  public static final String GEM_T3_2 = "丬";
  public static final String GEM_T4_2 = "中";

  public static final String UNTRADEABLE_NORMAL =
      GUI.spacesOf(12) + FaceColor.NO_SHADOW + "俎" + GUI.spacesOf(-123 + -13);
  public static final String UNTRADEABLE_SMALL =
      GUI.spacesOf(5) + FaceColor.NO_SHADOW + "俎" + GUI.spacesOf(-123 + -6);

  public static final String CORRUPTION = "俾";

  public static final String HALFWAY = GUI.spacesOf(74);
  public static final String HALFWAY_SMALL = GUI.spacesOf(67);

  public static final String BUFFER_1_GEM = HALFWAY + GUI.spacesOf(-5);
  public static final String BUFFER_2_GEM = HALFWAY + GUI.spacesOf(-25);
  public static final String BUFFER_3_GEM = HALFWAY + GUI.spacesOf(-34);
  public static final String BUFFER_4_GEM = HALFWAY + GUI.spacesOf(-48);

  public static final String SPACING_2_GEMS = GUI.spacesOf(27);
  public static final String SPACING_3_GEMS = GUI.spacesOf(18);
  public static final String SPACING_4_GEMS = GUI.spacesOf(17);

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

  public static String centerText(String s, boolean large) {
    return large ? centerLargeText(s) : centerSmallText(s);
  }

  public static String centerLargeText(String s) {
    String stripped = ChatColor.stripColor(s);
    return HALFWAY + GUI.spacesOf(-countNormalTextSize(stripped) / 2) + s + "\uF80B";
  }

  public static String centerSmallText(String s) {
    String stripped = ChatColor.stripColor(s);
    return HALFWAY_SMALL + GUI.spacesOf(-countNormalTextSize(stripped) / 2) + s + "\uF80B";
  }

  public static String generateGemString(int slots, int extendSlots, List<SocketGem> sockets) {
    return switch (slots) {
      case 1 -> BUFFER_1_GEM + socketSlotStyle(sockets, 0, extendSlots > 0);
      case 2 -> BUFFER_2_GEM + socketSlotStyle(sockets, 0, extendSlots > 1) +
          SPACING_2_GEMS + socketSlotStyle(sockets, 1, extendSlots > 0);
      case 3 -> BUFFER_3_GEM + socketSlotStyle(sockets, 0, extendSlots > 2) +
          SPACING_3_GEMS + socketSlotStyle(sockets, 1, extendSlots > 1) +
          SPACING_3_GEMS + socketSlotStyle(sockets, 2, extendSlots > 0);
      default -> BUFFER_4_GEM + socketSlotStyle(sockets, 0, extendSlots > 3) +
          SPACING_4_GEMS + socketSlotStyle(sockets, 1, extendSlots > 2) +
          SPACING_4_GEMS + socketSlotStyle(sockets, 2, extendSlots > 1) +
          SPACING_4_GEMS + socketSlotStyle(sockets, 3, extendSlots > 0);
    };
  }

  public static String getCorruptAddon() {
    return FaceColor.TRUE_WHITE + " 應";
  }

  public static String socketSlotStyle(List<SocketGem> sockets, int index, boolean extend) {
    if (extend) {
      return LOCKED_GEM;
    }
    if (index > sockets.size() - 1) {
      return EMPTY_GEM;
    }
    SocketGem gem = sockets.get(index);
    return switch (gem.getCustomModelData()) {
      case 2000 -> GEM_T1;
      case 2001 -> GEM_T2;
      case 2002 -> GEM_T3;
      case 2003 -> GEM_T4;
      case 2004 -> GEM_T3_2;
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

  public static String doNameThing(String s) {
    return s.toLowerCase()
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
    size -= StringUtils.countMatches(s, ' ') * 3; // space is 4 chars, base is 6, so sub 3
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

  public static int countNormalTextSize(String s) {
    int size = s.length() * 6;
    size -= StringUtils.countMatches(s, '.') * 4;
    size -= StringUtils.countMatches(s, ',') * 4;
    size -= StringUtils.countMatches(s, '1');
    size -= StringUtils.countMatches(s, '!') * 4;
    size -= StringUtils.countMatches(s, ':') * 4;
    size -= StringUtils.countMatches(s, 'i') * 4;
    size -= StringUtils.countMatches(s, 'I') * 2;
    size -= StringUtils.countMatches(s, 'l') * 3;
    size -= StringUtils.countMatches(s, '\'') * 3;
    size -= StringUtils.countMatches(s, 't') * 2;
    size -= StringUtils.countMatches(s, 'f');
    size -= StringUtils.countMatches(s, '[') * 2;
    size -= StringUtils.countMatches(s, ']') * 2;
    size -= StringUtils.countMatches(s, '(') * 2;
    size -= StringUtils.countMatches(s, ')') * 2;
    size -= StringUtils.countMatches(s, '/') * 2;
    size -= StringUtils.countMatches(s, ' ') * 2;
    size += StringUtils.countMatches(s, '丐') * 29;
    size += StringUtils.countMatches(s, '丑') * 41;
    size += StringUtils.countMatches(s, '丒') * 39;
    size += StringUtils.countMatches(s, '专') * 53;
    size += StringUtils.countMatches(s, '且') * 65;
    size += StringUtils.countMatches(s, '丕') * 21;
    size += StringUtils.countMatches(s, '世') * 41;
    size += StringUtils.countMatches(s, '丗') * 15;
    size += StringUtils.countMatches(s, '丘') * 34;
    size += StringUtils.countMatches(s, '丙') * 32;
    size += StringUtils.countMatches(s, '业') * 39;
    size += StringUtils.countMatches(s, '丛') * 40;
    size += StringUtils.countMatches(s, '东') * 34;
    size += StringUtils.countMatches(s, '丝') * 33; //todo: air jump
    return size;
  }
}
