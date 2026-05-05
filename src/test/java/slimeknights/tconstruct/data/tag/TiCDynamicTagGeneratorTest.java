package slimeknights.tconstruct.data.tag;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class TiCDynamicTagGeneratorTest extends BaseMcTest {
    @Test
    void registerSendsTwelveProviderFactories() {
        RecordingRunner runner = new RecordingRunner();

        TiCDynamicTagGenerator.register(runner);

        assertThat(runner.owner).isEqualTo("tconstruct-tags");
        assertThat(runner.providers).hasSize(12);
    }

    @Test
    void registerKeepsProviderOrder() {
        assertThat(TiCDynamicTagGenerator.createProviderEntries()).extracting(TiCDynamicTagGenerator.TagProviderEntry::name).containsExactly(
            "BlockTagProvider",
            "ItemTagProvider",
            "FluidTagProvider",
            "EntityTypeTagProvider",
            "BlockEntityTypeTagProvider",
            "BiomeTagProvider",
            "EnchantmentTagProvider",
            "MenuTypeTagProvider",
            "PotionTagProvider",
            "DamageTypeTagProvider",
            "MaterialTagProvider",
            "ModifierTagProvider"
        );
    }

    private static class RecordingRunner implements TiCDynamicTagGenerator.TagRunner {
        private String owner;
        private List<Function<PackOutput, ? extends DataProvider>> providers = List.of();

        @Override
        public void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers) {
            this.owner = owner;
            this.providers = providers;
        }
    }
}
