package slimeknights.tconstruct.tools.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import org.joml.Vector3f;
import slimeknights.tconstruct.tools.particle.ShareDamageParticleData;

public class ShareDamageParticle extends TextureSheetParticle {
  private final SpriteSet sprites;

  protected ShareDamageParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, ShareDamageParticleData data, SpriteSet sprites) {
    super(level, x, y, z);
    this.xd = xd;
    this.yd = yd;
    this.zd = zd;
    this.sprites = sprites;
    Vector3f color = data.getColor();
    this.rCol = color.x();
    this.gCol = color.y();
    this.bCol = color.z();
    this.alpha = 1.0F;
    this.gravity = 0.0F;
    this.lifetime = 30;
    this.quadSize = data.getScale() * 0.5F;
    this.setSpriteFromAge(sprites);
  }

  @Override
  public void move(double x, double y, double z) {
    this.setBoundingBox(this.getBoundingBox().move(x, y, z));
    this.setLocationFromBoundingbox();
  }

  @Override
  public void tick() {
    super.tick();
    this.setSpriteFromAge(this.sprites);
    this.alpha = 1.0F - (float)this.age / this.lifetime;
  }

  @Override
  public ParticleRenderType getRenderType() {
    return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
  }

  public static class Provider implements ParticleProvider<ShareDamageParticleData> {
    private final SpriteSet sprites;

    public Provider(SpriteSet sprites) {
      this.sprites = sprites;
    }

    @Override
    public Particle createParticle(ShareDamageParticleData data, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
      return new ShareDamageParticle(level, x, y, z, xd, yd, zd, data, sprites);
    }
  }
}
