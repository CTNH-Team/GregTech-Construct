package slimeknights.tconstruct.tools.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3f;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.Locale;

public class ShareDamageParticleData implements ParticleOptions {
  public static final Codec<ShareDamageParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    Codec.FLOAT.fieldOf("r").forGetter(data -> data.color.x()),
    Codec.FLOAT.fieldOf("g").forGetter(data -> data.color.y()),
    Codec.FLOAT.fieldOf("b").forGetter(data -> data.color.z()),
    Codec.FLOAT.fieldOf("scale").forGetter(data -> data.scale)
  ).apply(instance, (r, g, b, scale) -> new ShareDamageParticleData(new Vector3f(r, g, b), scale)));

  private static final Deserializer<ShareDamageParticleData> DESERIALIZER = new Deserializer<>() {
    @Override
    public ShareDamageParticleData fromCommand(ParticleType<ShareDamageParticleData> type, StringReader reader) throws CommandSyntaxException {
      reader.expect(' ');
      float r = reader.readFloat();
      reader.expect(' ');
      float g = reader.readFloat();
      reader.expect(' ');
      float b = reader.readFloat();
      reader.expect(' ');
      float scale = reader.readFloat();
      return new ShareDamageParticleData(new Vector3f(r, g, b), scale);
    }

    @Override
    public ShareDamageParticleData fromNetwork(ParticleType<ShareDamageParticleData> type, FriendlyByteBuf buffer) {
      return new ShareDamageParticleData(new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()), buffer.readFloat());
    }
  };

  private final Vector3f color;
  private final float scale;

  public ShareDamageParticleData(Vector3f color, float scale) {
    this.color = color;
    this.scale = scale;
  }

  public Vector3f getColor() {
    return color;
  }

  public float getScale() {
    return scale;
  }

  @Override
  public ParticleType<?> getType() {
    return TinkerTools.shareDamageParticle.get();
  }

  @Override
  public void writeToNetwork(FriendlyByteBuf buffer) {
    buffer.writeFloat(color.x());
    buffer.writeFloat(color.y());
    buffer.writeFloat(color.z());
    buffer.writeFloat(scale);
  }

  @Override
  public String writeToString() {
    return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(getType()), color.x(), color.y(), color.z(), scale);
  }

  public static class Type extends ParticleType<ShareDamageParticleData> {
    public Type() {
      super(false, DESERIALIZER);
    }

    @Override
    public Codec<ShareDamageParticleData> codec() {
      return CODEC;
    }
  }
}
