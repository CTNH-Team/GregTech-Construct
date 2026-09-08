package slimeknights.tconstruct.common.data.tags;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.TConstruct;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.tags.DamageTypeTags.*;
import static net.minecraft.world.damagesource.DamageTypes.*;
import static slimeknights.tconstruct.common.TinkerDamageTypes.*;
import static slimeknights.tconstruct.common.TinkerDamageTypes.EXPLOSION;
import static slimeknights.tconstruct.common.TinkerTags.DamageTypes.*;

@SuppressWarnings("removal")
public class DamageTypeTagProvider extends DamageTypeTagsProvider {
    public DamageTypeTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookup, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, lookup, TConstruct.MOD_ID, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(Provider pProvider) {
        tag(IS_FIRE).add(SMELTERY_HEAT).add(FLUID_FIRE.values());
        tag(IS_EXPLOSION).add(SELF_DESTRUCT).add(EXPLOSION.values()).add(MOB_EXPLOSION.values());
        tag(IS_FREEZING).add(FLUID_COLD.values());
        tag(WITCH_RESISTANT_TO).add(SMELTERY_MAGIC).add(FLUID_MAGIC.values());
        tag(BYPASSES_ARMOR).add(PIERCING, SELF_DESTRUCT, BLEEDING, ENTANGLED, SPINY).add(WATER.values()).add(FLUID_SPIKE.values());
        tag(BYPASSES_ENCHANTMENTS).add(BLEEDING);
        tag(BYPASSES_EFFECTS).add(ENTANGLED, SPINY);
        tag(AVOIDS_GUARDIAN_THORNS).add(BLEEDING, SHOCK);
        // whole reason these are a pair is so we can tag one as projectile
        tag(IS_PROJECTILE).add(THROWN_TOOL, FISHING_HOOK, FLUID_IMPACT.ranged(), FLUID_FIRE.ranged(), FLUID_COLD.ranged(), FLUID_MAGIC.ranged(), WATER.ranged(), FLUID_SPIKE.ranged(), EXPLOSION.ranged(), MOB_EXPLOSION.ranged());

        // modifiers
        tag(MODIFIER_WHITELIST).add(MOB_ATTACK, MOB_ATTACK_NO_AGGRO);
        // loot modifiers come from the held tool, so limit them to melee damage the tool is responsible for
        // projectiles are not needed here, they use the modifiers stored on the projectile instead
        tag(LOOT_MODIFIER_WHITELIST).addTag(IS_MELEE).add(PIERCING, FLUID_FIRE.melee(), FLUID_COLD.melee(), FLUID_MAGIC.melee(), WATER.melee(), EXPLOSION.melee(), MOB_EXPLOSION.melee());
        // damage caused by a melee attack, shared by the melee protection modifier and the loot modifier whitelist
        tag(IS_MELEE).add(PLAYER_ATTACK, MOB_ATTACK, MOB_ATTACK_NO_AGGRO, STING, FLUID_IMPACT.melee(), FLUID_SPIKE.melee());

        // protection modifier tags
        // cramming is not an attack, so it gets protection without making the held tool responsible for the kill
        tag(MELEE_PROTECTION).addTag(IS_MELEE).add(CRAMMING);
        tag(PROJECTILE_PROTECTION).addTag(IS_PROJECTILE).add(FALLING_ANVIL, FALLING_BLOCK, FALLING_STALACTITE);
        tag(FIRE_PROTECTION).addTags(IS_FIRE, IS_LIGHTNING).add(SHOCK);
        tag(BLAST_PROTECTION).addTag(IS_EXPLOSION);
        tag(MAGIC_PROTECTION).addTag(WITCH_RESISTANT_TO).add(WITHER, WITHER_SKULL, DRAGON_BREATH);
        tag(PHYSICS).addTags(MELEE_PROTECTION, PROJECTILE_PROTECTION, BLAST_PROTECTION);
        tag(INSULATION).addTag(IS_LIGHTNING);
        tag(FALL_PROTECTION).addTag(IS_FALL).add(FLY_INTO_WALL);
        tag(BYPASSES_REDUCTION).addTags(BYPASSES_EFFECTS, BYPASSES_INVULNERABILITY, BYPASSES_ENCHANTMENTS,
                BYPASSES_ARMOR, MAGIC_PROTECTION, FIRE_PROTECTION, FALL_PROTECTION);
        tag(BYPASSES_BLOCKING).addTags(BYPASSES_EFFECTS, BYPASSES_INVULNERABILITY, BYPASSES_ENCHANTMENTS);
        tag(BYPASSES_PROTECTION).addTags(BYPASSES_EFFECTS, BYPASSES_INVULNERABILITY, BYPASSES_ENCHANTMENTS);

        // TF support
        String tf = "twilightforest";
        addOptional(MODIFIER_WHITELIST, tf, "axing", "slam", "ant");
        // all of these are attacks made by a TF mob, and mobs can use looting, so they belong on the shared melee tag
        addOptional(IS_MELEE, tf, "ghast_tear", "hydra_bite", "squish", "axing", "slam", "yeeted", "ant", "clamped", "spiked");
        addOptional(MAGIC_PROTECTION, tf, "haunt", "ominous_fire", "twilight_scepter");
        addOptional(PROJECTILE_PROTECTION, tf, "falling_ice");
        // anything "magic" is good against lich shields, so tag our magic fluids
        tag(TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.tryBuild(tf, "breaks_lich_shields"))).add(FLUID_MAGIC.values());
    }

    /** Adds the given IDs from the given domain to the tag as optional entries. */
    private void addOptional(TagKey<DamageType> tag, String domain, String... names) {
        TagAppender<DamageType> appender = tag(tag);
        for (String name : names) {
            appender.addOptional(ResourceLocation.tryBuild(domain, name));
        }
    }
}
