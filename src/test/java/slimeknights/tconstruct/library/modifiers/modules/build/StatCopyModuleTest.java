package slimeknights.tconstruct.library.modifiers.modules.build;

import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStatId;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatCopyModuleTest extends BaseMcTest {
  private static final FloatToolStat SOURCE = new FloatToolStat(new ToolStatId("test", "stat_copy_source"), 0, 0, 0, 10_000);
  private static final FloatToolStat TARGET = new FloatToolStat(new ToolStatId("test", "stat_copy_target"), 0, 0, 0, 10_000);
  private static final ModifierEntry MODIFIER = new ModifierEntry(new ModifierId("test", "stat_copy"), 2);

  @Test
  void levelingCopyUsesSourceToTargetAndNormalizesSourceMultiplier() {
    IToolContext context = mock(IToolContext.class);
    when(context.getItem()).thenReturn(Items.AIR);
    ModifierStatsBuilder stats = ModifierStatsBuilder.builder();
    SOURCE.add(stats, 100);
    SOURCE.multiplyAll(stats, 0.5f);

    StatCopyModule module = StatCopyModule.builder(TARGET, SOURCE).eachLevel(0.1f);
    module.addToolStats(context, MODIFIER, stats);

    assertThat(module.getPriority()).isEqualTo(50);
    assertThat(stats.getStat(TARGET)).isEqualTo(20f);
  }

  @Test
  void staticCopyRetainsDynamicPackBehavior() {
    StatCopyModule module = StatCopyModule.copy(SOURCE, TARGET, 0.15f);

    assertThat(module.levelingMultiplier()).isNull();
    assertThat(module.normalizeSourceMultiplier()).isFalse();
    assertThat(module.getPriority()).isNull();
  }
}
