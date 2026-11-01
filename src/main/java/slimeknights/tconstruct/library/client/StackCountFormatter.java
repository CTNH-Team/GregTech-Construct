package slimeknights.tconstruct.library.client;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * 将超出槽位显示范围的堆叠数量缩写为 k/m/b 格式：
 * 1000 起按千位缩写，保留一位小数 (1234 → "1.2k", 12345 → "12.3k", 123456 → "123k",
 * 1234567 → "1.2m", 1234567890 → "1.2b")；小于 1000 原样显示。
 * 按住 Shift 时由 {@code GuiGraphicsMixin} 显示未缩写的原始数量。
 */
public final class StackCountFormatter {
  private static final String[] SUFFIXES = {"k", "m", "b"};
  private static final DecimalFormatSymbols ROOT_SYMBOLS = new DecimalFormatSymbols(Locale.ROOT);
  private static final DecimalFormat ONE_DIGIT_PRECISION = new DecimalFormat("##.0", ROOT_SYMBOLS);

  static {
    ONE_DIGIT_PRECISION.setRoundingMode(RoundingMode.DOWN);
  }

  private StackCountFormatter() {}

  /** 缩写堆叠数量；截断而非四舍五入，保证显示数量不超过实际数量。 */
  public static String abbreviate(int count) {
    if (count < 1000) {
      return String.valueOf(count);
    }
    int exponent = 0;
    double value = count;
    while (value >= 1000.0 && exponent < SUFFIXES.length) {
      value /= 1000.0;
      exponent++;
    }
    if (value >= 100.0) {
      return (int)value + SUFFIXES[exponent - 1];
    }
    return ONE_DIGIT_PRECISION.format(value) + SUFFIXES[exponent - 1];
  }
}
