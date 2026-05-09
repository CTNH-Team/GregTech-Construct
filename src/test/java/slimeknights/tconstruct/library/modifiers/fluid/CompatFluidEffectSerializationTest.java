package slimeknights.tconstruct.library.modifiers.fluid;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.library.modifiers.fluid.general.CompatSetBlockFluidEffect;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class CompatFluidEffectSerializationTest extends BaseMcTest {
  private static final ResourceLocation IE_CONCRETE_SPRAYED = ResourceLocation.tryBuild("immersiveengineering", "concrete_sprayed");
  private static final ResourceLocation IE_CONCRETE_FEET = ResourceLocation.tryBuild("immersiveengineering", "concrete_feet");

  @Test
  void setBlockByIdSerializesRawBlockId() {
    CompatSetBlockFluidEffect effect = new CompatSetBlockFluidEffect(IE_CONCRETE_SPRAYED);

    JsonObject json = CompatSetBlockFluidEffect.LOADER.serialize(effect).getAsJsonObject();

    assertThat(json.get("block").getAsString()).isEqualTo("immersiveengineering:concrete_sprayed");
  }

  @Test
  void fluidMobEffectByIdSerializesRawEffectId() {
    CompatFluidMobEffect effect = new CompatFluidMobEffect(IE_CONCRETE_FEET, 40, 2);

    JsonObject json = CompatFluidMobEffect.LOADABLE.serialize(effect).getAsJsonObject();

    assertThat(json.get("effect").getAsString()).isEqualTo("immersiveengineering:concrete_feet");
    assertThat(json.get("time").getAsInt()).isEqualTo(40);
    assertThat(json.get("level").getAsInt()).isEqualTo(2);
  }
}
