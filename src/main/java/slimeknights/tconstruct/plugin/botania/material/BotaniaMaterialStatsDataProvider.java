package slimeknights.tconstruct.plugin.botania.material;

import net.minecraft.data.PackOutput;

import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialStatsDataProvider;
import slimeknights.tconstruct.tools.stats.ArmorPartMaterialStats;
import slimeknights.tconstruct.tools.stats.GripMaterialStats;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.tools.stats.LimbMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

import static net.minecraft.world.item.Tiers.DIAMOND;
import static net.minecraft.world.item.Tiers.IRON;

/**
 * Botania-owned material stats.
 */
public class BotaniaMaterialStatsDataProvider extends AbstractMaterialStatsDataProvider {

    public BotaniaMaterialStatsDataProvider(PackOutput packOutput) {
        this(packOutput, new BotaniaMaterialDataProvider(packOutput));
    }

    public BotaniaMaterialStatsDataProvider(PackOutput packOutput, AbstractMaterialDataProvider materials) {
        super(packOutput, materials);
    }

    @Override
    public String getName() {
        return "Tinkers' Construct Botania Material Stats";
    }

    @Override
    protected void addMaterialStats() {
        addMaterialStats(BotaniaMaterialIds.manaSteel,
                new HeadMaterialStats(400, 6.5f, IRON, 2.5f),
                HandleMaterialStats.multipliers().durability(1.15f).miningSpeed(1.05f).attackSpeed(1.05f)
                        .attackDamage(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(BotaniaMaterialIds.manaSteel,
                new LimbMaterialStats(350, 0.15f, 0.1f, 0.1f),
                new GripMaterialStats(0.15f, 0.1f, 2.5f));
        addArmorShieldStats(BotaniaMaterialIds.manaSteel,
                PlatingMaterialStats.builder().durabilityFactor(20).armor(3, 5, 6, 3).toughness(2)
                        .knockbackResistance(0.05f),
                ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f));

        addMaterialStats(BotaniaMaterialIds.terraSteel,
                new HeadMaterialStats(750, 7.5f, DIAMOND, 3.5f),
                HandleMaterialStats.multipliers().durability(1.35f).miningSpeed(1.2f).attackSpeed(1.2f)
                        .attackDamage(1.3f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(BotaniaMaterialIds.terraSteel,
                new LimbMaterialStats(750, 0.2f, 0.3f, 0.3f),
                new GripMaterialStats(0.35f, 0.3f, 3.5f));
        addArmorShieldStats(BotaniaMaterialIds.terraSteel,
                PlatingMaterialStats.builder().durabilityFactor(50).armor(3, 6, 8, 4).toughness(3)
                        .knockbackResistance(0.15f),
                ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f));
    }
}
