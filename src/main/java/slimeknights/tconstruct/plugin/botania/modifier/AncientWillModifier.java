package slimeknights.tconstruct.plugin.botania.modifier;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialIds;
import vazkii.botania.api.item.AncientWillContainer.AncientWillType;
import vazkii.botania.common.BotaniaDamageTypes;
import vazkii.botania.common.item.BotaniaItems;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public class AncientWillModifier extends NoLevelsModifier {
    private static final Map<Player, CriticalTarget> CRITICAL_TARGETS = new WeakHashMap<>();
    private static final Set<Player> ARMOR_PIERCING_REENTRY = Collections.newSetFromMap(new WeakHashMap<>());

    private final Will will;

    public AncientWillModifier(Will will) {
        this.will = will;
    }

    public Will will() {
        return will;
    }

    public static void onCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();
        Event.Result result = event.getResult();
        if (player.level().isClientSide || result == Event.Result.DENY || result == Event.Result.DEFAULT && !event.isVanillaCritical()
                || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        EnumSet<Will> wills = getActiveWills(player);
        if (wills.isEmpty()) {
            return;
        }
        if (wills.contains(Will.DHAROK)) {
            event.setDamageModifier(event.getDamageModifier() * getDharokCritDamageMult(player));
        }
        CRITICAL_TARGETS.put(player, new CriticalTarget(target, player.level().getGameTime()));
        if (player instanceof AncientWillAccess access) {
            access.tconstruct$setAncientWillCritTarget(target);
        }
    }

    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || ARMOR_PIERCING_REENTRY.contains(player)) {
            return;
        }
        LivingEntity target = event.getEntity();
        CriticalTarget criticalTarget = CRITICAL_TARGETS.get(player);
        if (criticalTarget == null) {
            return;
        }
        if (!criticalTarget.matches(player, target)) {
            CRITICAL_TARGETS.remove(player);
            return;
        }
        CRITICAL_TARGETS.remove(player);
        EnumSet<Will> wills = getActiveWills(player);
        if (wills.isEmpty()) {
            return;
        }
        applyEffects(wills, event.getAmount(), player, target);
        if (wills.contains(Will.VERAC)) {
            event.setCanceled(true);
            ARMOR_PIERCING_REENTRY.add(player);
            try {
                target.hurt(armorPiercingSource(player), event.getAmount());
            } finally {
                ARMOR_PIERCING_REENTRY.remove(player);
            }
        }
    }

    public static DamageSource onCriticalAttack(DamageSource source, float amount, Player player, LivingEntity target) {
        CriticalTarget criticalTarget = CRITICAL_TARGETS.get(player);
        if (criticalTarget == null) {
            return source;
        }
        if (!criticalTarget.matches(player, target)) {
            CRITICAL_TARGETS.remove(player);
            return source;
        }
        CRITICAL_TARGETS.remove(player);
        EnumSet<Will> wills = getActiveWills(player);
        if (wills.isEmpty()) {
            return source;
        }
        applyEffects(wills, amount, player, target);
        return wills.contains(Will.VERAC) ? armorPiercingSource(player) : source;
    }

    public static boolean hasTerrasteelHelmetPlating(IToolContext tool) {
        return BotaniaModifierIds.PLATE_HELMET.equals(tool.getDefinition().getId()) && tool.getMaterial(0).matchesVariant(BotaniaMaterialIds.terraSteel);
    }

    public static float getDharokCritDamageMult(float health, float maxHealth) {
        if (maxHealth <= 0) {
            return 1F;
        }
        return 1F + (1F - health / maxHealth) * 0.5F;
    }

    static void applyEffects(EnumSet<Will> wills, float amount, Player player, LivingEntity target) {
        if (wills.contains(Will.AHRIM)) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20, 1));
        }
        if (wills.contains(Will.GUTHAN)) {
            player.heal(amount * 0.25F);
        }
        if (wills.contains(Will.TORAG)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        }
        if (wills.contains(Will.KARIL)) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
        }
    }

    private static float getDharokCritDamageMult(Player player) {
        return getDharokCritDamageMult(player.getHealth(), player.getMaxHealth());
    }

    private static EnumSet<Will> getActiveWills(Player player) {
        IToolStackView tool = getTerrasteelHelmet(player);
        EnumSet<Will> wills = EnumSet.noneOf(Will.class);
        if (tool == null) {
            return wills;
        }
        for (Will will : Will.values()) {
            if (tool.getModifierLevel(will.modifierId()) > 0) {
                wills.add(will);
            }
        }
        return wills;
    }

    private static IToolStackView getTerrasteelHelmet(Player player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (stack.isEmpty() || !stack.is(TinkerTags.Items.MODIFIABLE)) {
            return null;
        }
        ToolStack tool = ToolStack.from(stack);
        return !tool.isBroken() && hasTerrasteelHelmetPlating(tool) ? tool : null;
    }

    private static DamageSource armorPiercingSource(Player player) {
        return BotaniaDamageTypes.Sources.playerAttackArmorPiercing(player.level().registryAccess(), player);
    }

    private record CriticalTarget(LivingEntity target, long gameTime) {
        boolean matches(Player player, LivingEntity match) {
            return target == match && player.level().getGameTime() <= gameTime + 1;
        }
    }

    public enum Will {
        AHRIM(AncientWillType.AHRIM, BotaniaModifierIds.ancientWillAhrim, () -> BotaniaItems.ancientWillAhrim),
        DHAROK(AncientWillType.DHAROK, BotaniaModifierIds.ancientWillDharok, () -> BotaniaItems.ancientWillDharok),
        GUTHAN(AncientWillType.GUTHAN, BotaniaModifierIds.ancientWillGuthan, () -> BotaniaItems.ancientWillGuthan),
        TORAG(AncientWillType.TORAG, BotaniaModifierIds.ancientWillTorag, () -> BotaniaItems.ancientWillTorag),
        VERAC(AncientWillType.VERAC, BotaniaModifierIds.ancientWillVerac, () -> BotaniaItems.ancientWillVerac),
        KARIL(AncientWillType.KARIL, BotaniaModifierIds.ancientWillKaril, () -> BotaniaItems.ancientWillKaril);

        private final AncientWillType type;
        private final ModifierId modifierId;
        private final Supplier<Item> item;

        Will(AncientWillType type, ModifierId modifierId, Supplier<Item> item) {
            this.type = type;
            this.modifierId = modifierId;
            this.item = item;
        }

        public AncientWillType type() {
            return type;
        }

        public ModifierId modifierId() {
            return modifierId;
        }

        public Item item() {
            return item.get();
        }

        public String key() {
            return type.name().toLowerCase(Locale.ROOT);
        }
    }
}
