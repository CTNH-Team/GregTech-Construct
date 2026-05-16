package slimeknights.tconstruct.tables.client.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.library.tools.item.ITinkerStationDisplay;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ToolTableScreenTest extends BaseMcTest {
  @AfterEach
  void resetBridge() {
    ApotheosisBridge.resetSocketHooks();
  }

  @Test
  void appendsSocketBonusesToExistingTooltipLines() {
    List<Component> tooltip = new ArrayList<>(List.of(Component.literal("Base line")));

    ApotheosisBridge.installSocketHooks(new FakeSocketHooks(List.of(
      Component.literal("+10% speed"),
      Component.literal("+2 luck")
    )));

    ToolTableScreen.appendApotheosisSocketBonuses(new ItemStack(Items.DIAMOND_PICKAXE), tooltip);

    assertThat(tooltip).extracting(Component::getString)
                       .containsExactly("Base line", "", "stat.tconstruct.socket_bonus", "+10% speed", "+2 luck");
  }

  @Test
  void appendsSocketCountBelowModifierSlots() {
    List<Component> tooltip = new ArrayList<>(List.of(
      Component.literal("Upgrades: 2"),
      Component.literal("Abilities: 1")
    ));

    ApotheosisBridge.installSocketHooks(FakeSocketHooks.withSockets(
      List.of(),
      List.of(
        new ApotheosisBridge.SocketGem(0, new ItemStack(Items.DIAMOND))
      ),
      3
    ));

    ToolTableScreen.appendApotheosisSocketCount(new ItemStack(Items.DIAMOND_PICKAXE), tooltip);

    assertThat(tooltip).extracting(Component::getString)
                       .containsExactly("Upgrades: 2", "Abilities: 1", "stat.tconstruct.socket_count1/3");
  }

  @Test
  void socketAttributeBonusesReplaceDisplayedPrimaryStats() {
    ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
    IToolStackView tool = Mockito.mock(IToolStackView.class);
    ITinkerStationDisplay display = new TestDisplay(Items.DIAMOND_PICKAXE);
    float attackDamage = 5;
    float attackSpeed = 3;
    float miningSpeedValue = 4;
    StatsNBT stats = StatsNBT.builder()
                             .set(ToolStats.ATTACK_DAMAGE, attackDamage)
                             .set(ToolStats.ATTACK_SPEED, attackSpeed)
                             .set(ToolStats.MINING_SPEED, miningSpeedValue)
                             .build();
    Mockito.when(tool.getStats()).thenReturn(stats);
    List<Component> tooltip = new ArrayList<>(List.of(
      ToolStats.ATTACK_DAMAGE.formatValue(attackDamage),
      ToolStats.ATTACK_SPEED.formatValue(attackSpeed)
    ));

    ApotheosisBridge.installSocketHooks(FakeSocketHooks.withAttributes(
      List.of(),
      List.of(
        new AttributeEntry(Attributes.ATTACK_DAMAGE, new AttributeModifier("test_ad", 2, AttributeModifier.Operation.ADDITION)),
        new AttributeEntry(Attributes.ATTACK_SPEED, new AttributeModifier("test_as", 0.5, AttributeModifier.Operation.MULTIPLY_BASE))
      )
    ));

    ToolTableScreen.applyApotheosisSocketStatOverrides(stack, tool, display, null, tooltip);

    assertThat(tooltip).extracting(Component::getString)
                       .containsExactly(
                         ToolStats.ATTACK_DAMAGE.formatValue(attackDamage + 2).getString(),
                         ToolStats.ATTACK_SPEED.formatValue(attackSpeed + 2).getString()
                       );
  }

  private record AttributeEntry(Attribute attribute, AttributeModifier modifier) {}

  private record TestDisplay(Item item) implements ITinkerStationDisplay {
    @Override
    public Item asItem() {
      return item;
    }
  }

  private static class FakeSocketHooks implements ApotheosisBridge.SocketHooks {
    private final List<Component> tooltip;
    private final List<AttributeEntry> attributes;
    private final List<ApotheosisBridge.SocketGem> gems;
    private final int socketCount;

    private FakeSocketHooks(List<Component> tooltip) {
      this(tooltip, List.of(), List.of(), 0);
    }

    private static FakeSocketHooks withAttributes(List<Component> tooltip, List<AttributeEntry> attributes) {
      return new FakeSocketHooks(tooltip, attributes, List.of(), 0);
    }

    private static FakeSocketHooks withSockets(List<Component> tooltip, List<ApotheosisBridge.SocketGem> gems, int socketCount) {
      return new FakeSocketHooks(tooltip, List.of(), gems, socketCount);
    }

    private FakeSocketHooks(List<Component> tooltip, List<AttributeEntry> attributes, List<ApotheosisBridge.SocketGem> gems, int socketCount) {
      this.tooltip = tooltip;
      this.attributes = attributes;
      this.gems = gems;
      this.socketCount = socketCount;
    }

    @Override
    public boolean hasSocketedGems(ItemStack stack) {
      return !tooltip.isEmpty() || !attributes.isEmpty();
    }

    @Override
    public int getSocketCount(ItemStack stack) {
      return socketCount;
    }

    @Override
    public List<ItemStack> getSocketedGems(ItemStack stack) {
      return gems.stream().filter(ApotheosisBridge.SocketGem::isFilled).map(ApotheosisBridge.SocketGem::gem).toList();
    }

    @Override
    public List<ApotheosisBridge.SocketGem> getSocketedGemData(ItemStack stack) {
      return gems;
    }

    @Override
    public void appendTooltip(ItemStack stack, java.util.function.Consumer<Component> consumer) {
      tooltip.forEach(consumer);
    }

    @Override
    public void addAttributeModifiers(ItemStack stack, EquipmentSlot slot, java.util.function.BiConsumer<Attribute,AttributeModifier> consumer) {
      attributes.forEach(entry -> consumer.accept(entry.attribute(), entry.modifier()));
    }

    @Override
    public ItemStack removeGem(ItemStack stack, int socketIndex) {
      return ItemStack.EMPTY;
    }

    @Override
    public boolean canInsertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
      return false;
    }

    @Override
    public ItemStack insertGem(ItemStack tool, int rawSocketIndex, ItemStack gem) {
      return ItemStack.EMPTY;
    }

    @Override
    public ItemStack copyGem(ItemStack stack, int socketIndex) {
      return ItemStack.EMPTY;
    }
  }
}
