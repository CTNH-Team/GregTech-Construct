package slimeknights.tconstruct.shared.block;

import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.world.block.DirtType;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class SlimeTypeTest extends BaseMcTest {
  @Test
  void getDirtTypeResolvesLazyDirtMapping() throws ReflectiveOperationException {
    resetDirtType(SlimeType.SKY);

    assertThat(SlimeType.SKY.getDirtType()).isEqualTo(DirtType.SKY);
  }

  private static void resetDirtType(SlimeType type) throws ReflectiveOperationException {
    Field field = SlimeType.class.getDeclaredField("dirtType");
    field.setAccessible(true);
    field.set(type, null);
  }
}
