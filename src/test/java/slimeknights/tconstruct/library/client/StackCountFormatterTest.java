package slimeknights.tconstruct.library.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StackCountFormatterTest {
  @Test
  void smallCountsKeepPlainFormat() {
    assertThat(StackCountFormatter.abbreviate(0)).isEqualTo("0");
    assertThat(StackCountFormatter.abbreviate(1)).isEqualTo("1");
    assertThat(StackCountFormatter.abbreviate(64)).isEqualTo("64");
    assertThat(StackCountFormatter.abbreviate(999)).isEqualTo("999");
  }

  @Test
  void thousandsUseOneDecimalPrecision() {
    assertThat(StackCountFormatter.abbreviate(1000)).isEqualTo("1.0k");
    assertThat(StackCountFormatter.abbreviate(1234)).isEqualTo("1.2k");
    assertThat(StackCountFormatter.abbreviate(9999)).isEqualTo("9.9k");
    assertThat(StackCountFormatter.abbreviate(10000)).isEqualTo("10.0k");
    assertThat(StackCountFormatter.abbreviate(12345)).isEqualTo("12.3k");
    assertThat(StackCountFormatter.abbreviate(99999)).isEqualTo("99.9k");
    assertThat(StackCountFormatter.abbreviate(100000)).isEqualTo("100k");
    assertThat(StackCountFormatter.abbreviate(999999)).isEqualTo("999k");
  }

  @Test
  void millionsUseOneDecimalPrecision() {
    assertThat(StackCountFormatter.abbreviate(1000000)).isEqualTo("1.0m");
    assertThat(StackCountFormatter.abbreviate(1234567)).isEqualTo("1.2m");
    assertThat(StackCountFormatter.abbreviate(10000000)).isEqualTo("10.0m");
    assertThat(StackCountFormatter.abbreviate(12345678)).isEqualTo("12.3m");
    assertThat(StackCountFormatter.abbreviate(100000000)).isEqualTo("100m");
    assertThat(StackCountFormatter.abbreviate(123456789)).isEqualTo("123m");
  }

  @Test
  void billionsUseOneDecimalPrecision() {
    assertThat(StackCountFormatter.abbreviate(1000000000)).isEqualTo("1.0b");
    assertThat(StackCountFormatter.abbreviate(1234567890)).isEqualTo("1.2b");
    assertThat(StackCountFormatter.abbreviate(Integer.MAX_VALUE)).isEqualTo("2.1b");
  }
}
