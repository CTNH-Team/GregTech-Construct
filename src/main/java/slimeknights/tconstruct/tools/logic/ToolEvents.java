package slimeknights.tconstruct.tools.logic;

import com.google.common.collect.Multiset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingVisibilityEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Vector3f;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.common.TinkerEffect;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.events.TinkerToolEvent.ToolHarvestEvent;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStats;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ProtectionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ShareDamageModifierHook.ShareDamageContext;
import slimeknights.tconstruct.library.modifiers.hook.mining.BreakSpeedContext;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.armor.MobDisguiseModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaDamageLimitModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaRecurrenceModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaArmorStatModule;
import slimeknights.tconstruct.library.modifiers.modules.technical.ArmorStatModule;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.PersistentDataCapability;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability;
import slimeknights.tconstruct.library.tools.capability.TinkerDataKeys;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.definition.module.mining.IsEffectiveToolHook;
import slimeknights.tconstruct.library.tools.helper.ArmorUtil;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.BlockSideHitListener;
import slimeknights.tconstruct.shared.TinkerAttributes;
import slimeknights.tconstruct.shared.TinkerEffects;
import slimeknights.tconstruct.shared.AchievementEvents;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.network.SyncProjectileModifiersPacket;
import slimeknights.tconstruct.tools.particle.ShareDamageParticleData;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import static net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb;
import static slimeknights.tconstruct.common.config.Config.COMMON;

/**
 * Event subscriber for tool events
 */
@EventBusSubscriber(modid = TConstruct.MOD_ID, bus = Bus.FORGE)
public class ToolEvents {
  private static final ThreadLocal<Boolean> SHARING_DAMAGE = ThreadLocal.withInitial(() -> false);
  private static final ThreadLocal<Deque<GuardianHurtContext>> GUARDIAN_HURT_CONTEXTS = ThreadLocal.withInitial(ArrayDeque::new);
  private static final Vector3f[] SHARE_DAMAGE_PARTICLE_COLORS = {
    new Vector3f(0.654f, 0.713f, 0.580f),
    new Vector3f(0.727f, 0.792f, 0.644f),
    new Vector3f(0.774f, 0.839f, 0.692f),
    new Vector3f(0.564f, 0.624f, 0.509f),
    new Vector3f(0.760f, 0.839f, 0.682f),
    new Vector3f(0.604f, 0.674f, 0.539f),
    new Vector3f(0.804f, 0.878f, 0.729f),
    new Vector3f(0.692f, 0.754f, 0.612f)
  };

  @SuppressWarnings("removal")
  @SubscribeEvent
  static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
    Player player = event.getEntity();

    // tool break speed hook
    ItemStack stack = player.getMainHandItem();
    if (stack.is(TinkerTags.Items.HARVEST)) {
      ToolStack tool = ToolStack.from(stack);
      if (!tool.isBroken()) {
        List<ModifierEntry> modifiers = tool.getModifierList();
        if (!modifiers.isEmpty()) {
          // build context
          BreakSpeedContext context = new BreakSpeedContext.Event(
            event,
            BlockSideHitListener.getSideHit(event.getEntity()),
            IsEffectiveToolHook.isEffective(tool, event.getState()),
            BreakSpeedContext.getMiningModifier(event.getEntity())
          );

          // run each modifier hook
          float speed = event.getNewSpeed();
          for (ModifierEntry entry : tool.getModifierList()) {
            speed = entry.getHook(ModifierHooks.BREAK_SPEED).modifyBreakSpeed(tool, entry, context, speed);
            // if any modifier cancels mining, stop right here
            if (speed < 0 || event.isCanceled()) {
              break;
            }
          }
          // update the speed
          event.setNewSpeed(speed);
        }
      }
    }

    // next, add in armor haste
    double armorMultiplier = player.getAttributeValue(TinkerAttributes.MINING_SPEED_MULTIPLIER.get()) + ArmorStatModule.getStat(player, TinkerDataKeys.MINING_SPEED);
    if (armorMultiplier >= 0) {
      event.setNewSpeed((float) (event.getNewSpeed() * armorMultiplier));
    }
  }

  @SubscribeEvent
  static void onHarvest(ToolHarvestEvent event) {
    // prevent processing if already processed
    if (event.getResult() != Result.DEFAULT) {
      return;
    }
    BlockState state = event.getState();
    Block block = state.getBlock();
    Level world = event.getWorld();
    BlockPos pos = event.getPos();

    // carve pumpkins
    if (block == Blocks.PUMPKIN) {
      Direction facing = event.getContext().getClickedFace();
      if (facing.getAxis() == Direction.Axis.Y) {
        facing = event.getContext().getHorizontalDirection().getOpposite();
      }
      // carve block
      world.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
      world.setBlock(pos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, facing), 11);
      // spawn seeds
      ItemEntity itemEntity = new ItemEntity(
        world,
        pos.getX() + 0.5D + facing.getStepX() * 0.65D,
        pos.getY() + 0.1D,
        pos.getZ() + 0.5D + facing.getStepZ() * 0.65D,
        new ItemStack(Items.PUMPKIN_SEEDS, 4));
      itemEntity.setDeltaMovement(
        0.05D * facing.getStepX() + world.random.nextDouble() * 0.02D,
        0.05D,
        0.05D * facing.getStepZ() + world.random.nextDouble() * 0.02D);
      world.addFreshEntity(itemEntity);
      event.setResult(Result.ALLOW);
    }

    // hives: get the honey
    if (block instanceof BeehiveBlock beehive) {
      int level = state.getValue(BeehiveBlock.HONEY_LEVEL);
      if (level >= 5) {
        // first, spawn the honey
        world.playSound(null, pos, SoundEvents.BEEHIVE_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);
        Block.popResource(world, pos, new ItemStack(Items.HONEYCOMB, 3));

        // if not smoking, make the bees angry
        if (!CampfireBlock.isSmokeyPos(world, pos)) {
          if (beehive.hiveContainsBees(world, pos)) {
            beehive.angerNearbyBees(world, pos);
          }
          beehive.releaseBeesAndResetHoneyLevel(world, state, pos, event.getPlayer(), BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
        } else {
          beehive.resetHoneyLevel(world, state, pos);
        }
        event.setResult(Result.ALLOW);
      } else {
        event.setResult(Result.DENY);
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  static void livingAttack(LivingAttackEvent event) {
    LivingEntity entity = event.getEntity();
    // client side always returns false, so this should be fine?
    if (entity.level().isClientSide() || entity.isDeadOrDying()) {
      return;
    }
    // I cannot think of a reason to run when invulnerable
    DamageSource source = event.getSource();
    if (entity.isInvulnerableTo(source)) {
      return;
    }

    // a lot of counterattack hooks want to detect direct attacks, so save time by calculating once
    boolean isDirectDamage = OnAttackedModifierHook.isDirectDamage(source);

    // determine if there is any modifiable armor, handles the target wearing modifiable armor
    EquipmentContext context = new EquipmentContext(entity);
    float amount = event.getAmount();
    if (context.hasModifiableArmor()) {
      // first we need to determine if any of the four slots want to cancel the event
      for (EquipmentSlot slotType : EquipmentSlot.values()) {
        if (ModifierUtil.validArmorSlot(entity, slotType)) {
          IToolStackView toolStack = context.getToolInSlot(slotType);
          if (toolStack != null && !toolStack.isBroken()) {
            for (ModifierEntry entry : toolStack.getModifierList()) {
              if (entry.getHook(ModifierHooks.DAMAGE_BLOCK).isDamageBlocked(toolStack, entry, context, slotType, source, amount)) {
                event.setCanceled(true);
                return;
              }
            }
          }
        }
      }

      // then we need to determine if any want to respond assuming its not canceled
      OnAttackedModifierHook.handleAttack(ModifierHooks.ON_ATTACKED, context, source, amount, isDirectDamage);
    }

    // next, consider the attacker is wearing modifiable armor
    Entity attacker = source.getEntity();
    if (attacker instanceof LivingEntity livingAttacker) {
      context = new EquipmentContext(livingAttacker);
      if (context.hasModifiableArmor()) {
        for (EquipmentSlot slotType : ModifiableArmorMaterial.ARMOR_SLOTS) {
          IToolStackView toolStack = context.getToolInSlot(slotType);
          if (toolStack != null && !toolStack.isBroken()) {
            for (ModifierEntry entry : toolStack.getModifierList()) {
              entry.getHook(ModifierHooks.DAMAGE_DEALT).onDamageDealt(toolStack, entry, context, slotType, entity, source, amount, isDirectDamage);
            }
          }
        }
      }
    }
  }

  /**
   * Determines how much to damage armor based on the given damage to the player
   * @param damage  Amount to damage the player
   * @return  Amount to damage the armor
   */
  private static int getArmorDamage(float damage) {
    damage /= 4;
    if (damage < 1) {
      return 1;
    }
    return (int)damage;
  }

  // low priority to minimize conflict as we apply reduction as if we are the final change to damage before vanilla
  @SuppressWarnings("removal")
  @SubscribeEvent(priority = EventPriority.LOW)
  static void livingHurt(LivingHurtEvent event) {
    LivingEntity entity = event.getEntity();
    GuardianHurtContext guardianHurt = currentGuardianHurtContext(entity);
    if (entity instanceof Player hurtPlayer) {
      Entity attacker = event.getSource().getEntity();
      if (attacker instanceof Player attackPlayer && attackPlayer != hurtPlayer && !hurtPlayer.isAlliedTo(attackPlayer)) {
        GuardingCache.recordHostility(hurtPlayer.getUUID(), attackPlayer.getUUID(), hurtPlayer.level().getGameTime());
      }
    }

    // determine if there is any modifiable armor, if not nothing to do
    DamageSource source = event.getSource();
    EquipmentContext context = new EquipmentContext(entity);
    int vanillaModifier = 0;
    float modifierValue = 0;
    float originalDamage = event.getAmount();
    boolean canProtect = DamageSourcePredicate.CAN_PROTECT.matches(source);

    Entity attacker = source.getEntity();
    if (attacker instanceof LivingEntity living) {
      // boost damage based on monster's melee weapon
      if (COMMON.allowMonsterMeleeModifiers.get() && source.is(TinkerTags.DamageTypes.MODIFIER_WHITELIST) && !living.getType().is(TinkerTags.EntityTypes.DAMAGE_MODIFIER_BLACKLIST)) {
        ItemStack weapon = living.getMainHandItem();
        if (!weapon.isEmpty() && weapon.is(TinkerTags.Items.MELEE_WEAPON)) {
          IToolStackView tool = ToolStack.from(weapon);
          // already know the player is null
          ToolAttackContext meleeContext = ToolAttackContext.attacker(living, null).target(entity).applyAttributes().build();
          float baseDamage = originalDamage;
          for (ModifierEntry entry : tool.getModifiers()) {
            originalDamage = entry.getHook(ModifierHooks.MONSTER_MELEE_DAMAGE).getMeleeDamage(tool, entry, meleeContext, baseDamage, originalDamage);
          }
        }
      }

      // run shulking global damage "boost", its a bit hardcoded Java wise to make it softcoded in JSON
      if (attacker.isCrouching()) {
        double crouchMultiplier = living.getAttributeValue(TinkerAttributes.CROUCH_DAMAGE_MULTIPLIER.get());
        crouchMultiplier += ArmorStatModule.getStat(attacker, TinkerDataKeys.CROUCH_DAMAGE);
        if (crouchMultiplier != 0) {
          originalDamage *= crouchMultiplier;
        }
      }
    }

    // conducting - boosts damage from fire
    if (source.is(TinkerTags.DamageTypes.FIRE_PROTECTION)) {
      int level = TinkerEffect.getLevel(entity, TinkerEffects.conductive);
      if (level > 0) {
        originalDamage *= Math.pow(2, level);
      }
    }
    // venom - boosts damage from magic
    if (source.is(TinkerTags.DamageTypes.MAGIC_PROTECTION)) {
      int level = TinkerEffect.getLevel(entity, TinkerEffects.venom);
      if (level > 0) {
        originalDamage *= Math.pow(2, level);
      }
    }
    
    // ensure any changes made so far apply, though we may change it again
    event.setAmount(originalDamage);

    float armor = 0, toughness = 0, armorStrength = 0, preReduction = 0, postReduction = 0, armorProtection = 0, armorAbsorptionCapModifier = 0;
    if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
      armor = entity.getArmorValue();
      toughness = (float)entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
      armorStrength = (float)entity.getAttributeValue(TinkerAttributes.ARMOR_STRENGTH.get());
      preReduction = (float)entity.getAttributeValue(TinkerAttributes.PRE_REDUCTION.get());
    }
    if (canProtect) {
      armorProtection = (float)entity.getAttributeValue(TinkerAttributes.ARMOR_PROTECTION.get());
    }
    ArmorDamageStats armorDamageStats = new ArmorDamageStats(armorStrength, preReduction, postReduction, armorProtection, originalDamage);
    ArmorDamageStats guardianCommonStats = null;

    // for our own armor, we have boosts from modifiers to consider
    if (context.hasModifiableArmor()) {
      // first, allow modifiers to change the damage being dealt and respond to it happening
      originalDamage = ModifyDamageModifierHook.modifyDamageTaken(ModifierHooks.MODIFY_HURT, context, source, originalDamage, OnAttackedModifierHook.isDirectDamage(source));
      event.setAmount(originalDamage);
      if (originalDamage <= 0) {
        event.setCanceled(true);
        return;
      }

      // remaining logic is reducing damage like vanilla protection
      // fetch vanilla enchant level, assuming its not bypassed in vanilla
      if (canProtect) {
        modifierValue = vanillaModifier = EnchantmentHelper.getDamageProtection(entity.getArmorSlots(), source);
      }

      if (guardianHurt != null) {
        for (EquipmentSlot slotType : EquipmentSlot.values()) {
          if (ModifierUtil.validArmorSlot(entity, slotType)) {
            IToolStackView tool = context.getToolInSlot(slotType);
            if (tool != null && !tool.isBroken()) {
              for (ModifierEntry entry : tool.getModifierList()) {
                modifierValue = entry.getHook(ModifierHooks.PROTECTION).getProtectionModifier(tool, entry, context, slotType, source, modifierValue);
              }
            }
          }
        }
        guardianCommonStats = collectGuardianCommonHooks(entity, context, source, originalDamage);
      } else {
        FormulaRecurrenceModule.beginDamageEvent(entity);
        try {
          // next, determine how much tinkers armor wants to change it
          // note that armor modifiers can choose to block "absolute damage" if they wish, currently just starving damage I think
          for (EquipmentSlot slotType : EquipmentSlot.values()) {
            if (ModifierUtil.validArmorSlot(entity, slotType)) {
              IToolStackView tool = context.getToolInSlot(slotType);
              if (tool != null && !tool.isBroken()) {
                for (ModifierEntry entry : tool.getModifierList()) {
                  modifierValue = entry.getHook(ModifierHooks.PROTECTION).getProtectionModifier(tool, entry, context, slotType, source, modifierValue);
                  entry.getHook(ModifierHooks.ARMOR_DAMAGE_STATS).addArmorDamageStats(tool, entry, context, slotType, source, armorDamageStats);
                }
              }
            }
          }
          for (EquipmentSlot slotType : EquipmentSlot.values()) {
            if (ModifierUtil.validArmorSlot(entity, slotType)) {
              IToolStackView tool = context.getToolInSlot(slotType);
              if (tool != null && !tool.isBroken()) {
                for (ModifierEntry entry : tool.getModifierList()) {
                  entry.getHook(ModifierHooks.DAMAGE_TO_PERSISTENT).onDamageToPersistent(tool, entry, context, slotType, source, armorDamageStats);
                }
              }
            }
          }
        } finally {
          FormulaRecurrenceModule.finishDamageEvent(armorDamageStats);
        }
      }

      // give slimes a 4x armor boost
      if (entity.getType().is(TinkerTags.EntityTypes.SMALL_ARMOR)) {
        modifierValue *= 4;
      }
    } else if (canProtect && entity.getType().is(TinkerTags.EntityTypes.SMALL_ARMOR)) {
      vanillaModifier = EnchantmentHelper.getDamageProtection(entity.getArmorSlots(), source);
      modifierValue = vanillaModifier * 4;
    }

    // if we changed anything, run our logic. Changing the cap has 2 problematic cases where same value will work:
    // * increased cap and vanilla is over the vanilla cap
    // * decreased cap and vanilla is now under the cap
    // that said, don't actually care about cap unless we have some protection, can use vanilla to simplify logic
    float cap = 20f;
    if (modifierValue > 0) {
      cap = (float) ProtectionModifierHook.getProtectionCap(entity, context.getTinkerData());
    }
    if (guardianHurt != null) {
      guardianHurt.applyToEvent(event, originalDamage, armor, toughness, vanillaModifier, modifierValue, cap, guardianCommonStats);
      return;
    }
    armorStrength = armorDamageStats.armorStrength();
    preReduction = armorDamageStats.preReduction();
    postReduction = armorDamageStats.postReduction();
    armorProtection = armorDamageStats.armorProtection();
    armorAbsorptionCapModifier = armorDamageStats.armorAbsorptionCap();
    boolean hasArmorPartStats = armorStrength > 0 || preReduction > 0 || postReduction > 0 || armorProtection > 0 || armorAbsorptionCapModifier != 0;
    float sharedRawDamage = 0;
    if (!SHARING_DAMAGE.get()) {
      sharedRawDamage = shareDamageWithNearbyGuardians(entity, source, originalDamage);
      originalDamage = Math.max(0, originalDamage - sharedRawDamage);
    }
    boolean handledArmorDamage = false;
    if (vanillaModifier != modifierValue || (cap > 20 && vanillaModifier > 20) || (cap < 20 && vanillaModifier > cap)
      || hasArmorPartStats || armorDamageStats.hasDamageLimit()) {

      // set the final dealt damage
      float finalDamage = armorDamageStats.hasDamageLimit()
        ? ArmorUtil.getDamageForEvent(originalDamage, armor, toughness, vanillaModifier, modifierValue, cap, armorStrength, preReduction, postReduction, armorProtection, armorAbsorptionCapModifier,
          damage -> armorDamageStats.applyDamageLimit(entity, (float)damage))
        : ArmorUtil.getDamageForEvent(originalDamage, armor, toughness, vanillaModifier, modifierValue, cap, armorStrength, preReduction, postReduction, armorProtection, armorAbsorptionCapModifier);
      handledArmorDamage = true;
      event.setAmount(finalDamage);
      if (originalDamage > 0 && finalDamage <= 0) {
        suppressHurtSound(entity);
      }

      // armor is damaged less as a result of our math, so damage the armor based on the difference if there is one
      if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
        int damageMissed = getArmorDamage(originalDamage) - getArmorDamage(finalDamage);
        damageArmorFromMissed(entity, source, context, damageMissed);
      }
    }
    if (!handledArmorDamage && context.hasModifiableArmor() && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
      float finalDamage = originalDamage;
      if (armorStrength > 0 || preReduction > 0 || postReduction > 0 || armorProtection > 0 || armorAbsorptionCapModifier != 0) {
        finalDamage = ArmorUtil.getDamageAfterArmorAbsorb(finalDamage, armor, toughness, armorStrength, preReduction, postReduction, armorProtection, Mth.clamp(0.8f + armorAbsorptionCapModifier, 0.2f, 0.95f));
      } else if (armor > 0) {
        finalDamage = getDamageAfterAbsorb(finalDamage, armor, toughness);
      }
      event.setAmount(finalDamage);
      if (originalDamage > 0 && finalDamage <= 0) {
        suppressHurtSound(entity);
      }

      int damageMissed = getArmorDamage(originalDamage) - getArmorDamage(finalDamage);
      damageArmorFromMissed(entity, source, context, damageMissed);
    }
  }

  private static void suppressHurtSound(LivingEntity entity) {
    if (entity instanceof Player) {
      HurtSoundHandler.mark(entity);
      if (!entity.level().isClientSide()) {
        TinkerNetwork.getInstance().sendToTrackingAndSelf(new slimeknights.tconstruct.common.network.SuppressHurtSoundPacket(entity.getId()), entity);
      }
    } else {
      entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), Sounds.FULLY_REDUCTED.getSound(), SoundSource.AMBIENT, 1.0F, 1.0F);
    }
  }

  /** Shared helper: damage all armor pieces for the missed amount, skipping tanned armor. */
  private static void damageArmorFromMissed(LivingEntity entity, DamageSource source, EquipmentContext context, int damageMissed) {
    if (damageMissed <= 0 || !(entity instanceof Player)) return;
    ToolDamageUtil.runWithDeferredArmorDamage(() -> {
      for (EquipmentSlot slotType : ModifiableArmorMaterial.ARMOR_SLOTS) {
        IToolStackView tool = context.getToolInSlot(slotType);
        if (tool != null && (!source.is(DamageTypeTags.IS_FIRE) || !tool.getItem().isFireResistant())) {
          if (tool.getModifierLevel(TinkerModifiers.tanned.getId()) == 0) {
            ToolDamageUtil.damageAnimated(tool, damageMissed, entity, slotType);
          }
        } else {
          ItemStack armorStack = entity.getItemBySlot(slotType);
          if (!armorStack.isEmpty() && (!source.is(DamageTypeTags.IS_FIRE) || !armorStack.getItem().isFireResistant()) && armorStack.getItem() instanceof ArmorItem) {
            armorStack.hurtAndBreak(damageMissed, entity, e -> e.broadcastBreakEvent(slotType));
          }
        }
      }
    });
  }

  private static float shareDamageWithNearbyGuardians(LivingEntity entity, DamageSource source, float rawDamage) {
    if (rawDamage <= 0 || entity.level().isClientSide || SHARING_DAMAGE.get()) {
      return 0;
    }

    boolean previous = SHARING_DAMAGE.get();
    SHARING_DAMAGE.set(true);
    try {
      for (Entity passenger : entity.getPassengers()) {
        if (passenger instanceof LivingEntity rider && rider.isAlive()) {
          ShareDamageResult riderResult = tryShareDamageWith(rider, entity, source, rawDamage);
          if (riderResult.claimed()) {
            return riderResult.sharedRawDamage();
          }
        }
      }

      if (entity instanceof net.minecraft.world.entity.TamableAnimal tamable && tamable.getOwner() instanceof Player owner && owner.isAlive()) {
        float ownerDistance = owner.distanceTo(entity);
        if (ownerDistance <= guardingScanRange(entity)) {
          ShareDamageResult ownerResult = tryShareDamageWith(owner, entity, source, rawDamage);
          if (ownerResult.claimed()) {
            return ownerResult.sharedRawDamage();
          }
        }
      }

      if (entity instanceof Player self) {
        record GuardianCandidate(Player player, double distance) {}
        List<GuardianCandidate> nearbyGuardians = new ArrayList<>();
        long gameTime = self.level().getGameTime();
        for (Player guardian : self.level().players()) {
          if (guardian == self || !guardian.isAlive()
            || (!self.isAlliedTo(guardian) && GuardingCache.isHostile(self.getUUID(), guardian.getUUID(), gameTime))) {
            continue;
          }
          double distance = guardian.distanceTo(self);
          if (distance <= guardingScanRange(self) && GuardingCache.hasAnyHook(guardian.getUUID())) {
            nearbyGuardians.add(new GuardianCandidate(guardian, distance));
          }
        }
        nearbyGuardians.sort(Comparator.comparingDouble(GuardianCandidate::distance));
        for (GuardianCandidate guardian : nearbyGuardians) {
          ShareDamageResult guardianResult = tryShareDamageWith(guardian.player(), entity, source, rawDamage);
          if (guardianResult.claimed()) {
            return guardianResult.sharedRawDamage();
          }
        }
      }
    } finally {
      PlayerPersistentDataCache.syncFromEntity(entity);
      SHARING_DAMAGE.set(previous);
    }
    return 0;
  }

  private static double guardingScanRange(LivingEntity protectedEntity) {
    return Config.guardingScanRange();
  }

  private static ShareDamageResult tryShareDamageWith(LivingEntity guardian, LivingEntity protectedEntity, DamageSource source, float rawDamage) {
    EquipmentContext guardianContext = new EquipmentContext(guardian);
    if (!guardianContext.hasModifiableArmor()) {
      return ShareDamageResult.NONE;
    }

    GuardingTransfer transfer = new GuardingTransfer();
    for (EquipmentSlot slotType : ModifiableArmorMaterial.ARMOR_SLOTS) {
      IToolStackView tool = guardianContext.getToolInSlot(slotType);
      if (tool == null || tool.isBroken()) {
        continue;
      }
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(ModifierHooks.SHARE_DAMAGE).collectShareDamage(tool, entry, guardian, slotType, protectedEntity, source, transfer);
      }
    }
    if (!transfer.claimed()) {
      return ShareDamageResult.NONE;
    }
    return transfer.apply(guardian, protectedEntity, source, rawDamage);
  }

  private static ArmorDamageStats collectGuardianCommonHooks(LivingEntity guardian, EquipmentContext context, DamageSource source, float originalDamage) {
    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0, 0, originalDamage);
    FormulaRecurrenceModule.beginDamageEvent(guardian);
    try {
      for (EquipmentSlot slotType : EquipmentSlot.values()) {
        if (!ModifierUtil.validArmorSlot(guardian, slotType)) {
          continue;
        }
        IToolStackView tool = context.getToolInSlot(slotType);
        if (tool == null || tool.isBroken()) {
          continue;
        }
        for (ModifierEntry entry : tool.getModifierList()) {
          entry.getHook(ModifierHooks.DAMAGE_TO_PERSISTENT).onDamageToPersistent(tool, entry, context, slotType, source, stats);
          collectGuardianDamageLimit(entry.getHook(ModifierHooks.ARMOR_DAMAGE_STATS), tool, entry, context, slotType, source, stats);
        }
      }
    } finally {
      FormulaRecurrenceModule.finishBypassDamageEvent(stats);
    }
    return stats;
  }

  private static void collectGuardianDamageLimit(ArmorDamageStatsModifierHook hook, IToolStackView tool, ModifierEntry entry,
                                                 EquipmentContext context, EquipmentSlot slotType, DamageSource source,
                                                 ArmorDamageStats stats) {
    if (hook instanceof FormulaDamageLimitModule module) {
      module.addArmorDamageStats(tool, entry, context, slotType, source, stats);
    } else if (hook instanceof ArmorDamageStatsModifierHook.AllMerger merger) {
      for (ArmorDamageStatsModifierHook module : merger.modules()) {
        collectGuardianDamageLimit(module, tool, entry, context, slotType, source, stats);
      }
    }
  }

  private record ShareDamageResult(boolean claimed, float sharedRawDamage) {
    private static final ShareDamageResult NONE = new ShareDamageResult(false, 0);
  }

  private static final class GuardingTransfer implements ShareDamageContext {
    private boolean claimed;
    private float remainingRatio = 1f;
    private float extraFactor = 1f;
    private float healthGround = Float.NEGATIVE_INFINITY;
    private float distanceFactor = 1f;
    private int particleColor = -1;

    @Override
    public void add(float shareRatio, float extraProtection, float healthGround, float distanceFactor) {
      add(shareRatio, extraProtection, healthGround, distanceFactor, -1);
    }

    @Override
    public void add(float shareRatio, float extraProtection, float healthGround, float distanceFactor, int color) {
      claimed = true;
      remainingRatio *= 1f - Mth.clamp(shareRatio, 0, 1);
      extraFactor *= 1f - Mth.clamp(extraProtection, 0, 1);
      this.healthGround = Math.max(this.healthGround, healthGround);
      this.distanceFactor = Math.min(this.distanceFactor, Mth.clamp(distanceFactor, 0, 1));
      if (color != -1) this.particleColor = color;
    }

    private boolean claimed() {
      return claimed;
    }

    private ShareDamageResult apply(LivingEntity guardian, LivingEntity protectedEntity, DamageSource source, float rawDamage) {
      float shareRatio = (1f - remainingRatio) * distanceFactor;
      if (shareRatio <= 0.01f) {
        return new ShareDamageResult(true, 0);
      }
      float requestedRawDamage = rawDamage * shareRatio;
      GuardianDamageProfile profile = GuardianDamageProfile.capture(guardian, source, rawDamage);
      float finalGuardianDamage = profile.apply(requestedRawDamage) * extraFactor;
      GuardianHurtContext hurtContext = new GuardianHurtContext(guardian, finalGuardianDamage, healthGround);
      if (finalGuardianDamage > 0) {
        hurtGuardian(guardian, source, hurtContext);
        applyShareKnockback(protectedEntity, guardian, hurtContext.appliedProfileFinalDamage(), distanceFactor);
      }
      float blockedRawDamage = hurtContext.blockedFinalDamage() > 0 && extraFactor > 0
        ? profile.revert(hurtContext.blockedFinalDamage() / extraFactor)
        : 0;
      float sharedRawDamage = Math.max(0, requestedRawDamage - Math.min(requestedRawDamage, blockedRawDamage));
      if (guardian instanceof ServerPlayer player) {
        if (rawDamage > 0 && sharedRawDamage / rawDamage >= 0.9f) {
          AchievementEvents.grantAdvancement(player, TConstruct.getResource("combat/shared_fate"));
        }
        if (hurtContext.blockedFinalDamage() > 0 && hurtContext.appliedFinalDamage() >= 10f) {
          AchievementEvents.grantAdvancement(player, TConstruct.getResource("combat/sacrifice"));
        }
      }
      if (sharedRawDamage > 0) {
        spawnShareDamageParticles(protectedEntity, guardian, particleColor);
      }
      return new ShareDamageResult(true, sharedRawDamage);
    }
  }

  private record GuardianDamageProfile(float preReduction, float strengthFactor, float armorFactor, float postReduction, float protectionFactor) {
    private static GuardianDamageProfile capture(LivingEntity guardian, DamageSource source, float rawDamage) {
      float armor = 0;
      float toughness = 0;
      float armorStrength = 0;
      if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
        armor = guardian.getArmorValue();
        toughness = (float)guardian.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        armorStrength = (float)guardian.getAttributeValue(TinkerAttributes.ARMOR_STRENGTH.get());
      }

      ArmorDamageStats stats = new ArmorDamageStats(
        armorStrength,
        source.is(TinkerTags.DamageTypes.BYPASSES_REDUCTION) ? 0 : (float)guardian.getAttributeValue(TinkerAttributes.PRE_REDUCTION.get()),
        0,
        source.is(TinkerTags.DamageTypes.BYPASSES_PROTECTION) ? 0 : (float)guardian.getAttributeValue(TinkerAttributes.ARMOR_PROTECTION.get()),
        rawDamage);
      collectFrozenGuardianAttributeStats(guardian, source, stats);
      armorStrength = source.is(DamageTypeTags.BYPASSES_ARMOR) ? 0 : stats.armorStrength();
      float preReduction = source.is(TinkerTags.DamageTypes.BYPASSES_REDUCTION) ? 0 : stats.preReduction();
      float postReduction = source.is(TinkerTags.DamageTypes.BYPASSES_BLOCKING) ? 0 : stats.postReduction();
      float armorProtection = source.is(TinkerTags.DamageTypes.BYPASSES_PROTECTION) ? 0 : stats.armorProtection();

      float toughnessFactor = Math.max(2.0f, 2.0f + toughness / 4.0f);
      float reducedDamage = Math.max(0, rawDamage - preReduction);
      float effectiveStrength = Math.max(0, armorStrength - reducedDamage / toughnessFactor);
      float strengthFactor = (float)Math.pow(0.8f, effectiveStrength / 4.0f);
      float strengthAbsorbedDamage = reducedDamage * strengthFactor;
      float effectiveArmor = Math.max((armor + toughness) / 5.0f, Math.min(armor, armor + effectiveStrength - strengthAbsorbedDamage / toughnessFactor));
      float armorFactor = Math.max(0.2f, 1.0f - effectiveArmor * 0.04f);
      return new GuardianDamageProfile(preReduction, strengthFactor, armorFactor, postReduction, 1.0f - Mth.clamp(armorProtection, 0, 0.8f));
    }

    private static void collectFrozenGuardianAttributeStats(LivingEntity guardian, DamageSource source, ArmorDamageStats stats) {
      EquipmentContext context = new EquipmentContext(guardian);
      if (!context.hasModifiableArmor()) {
        return;
      }
      for (EquipmentSlot slotType : ModifiableArmorMaterial.ARMOR_SLOTS) {
        IToolStackView tool = context.getToolInSlot(slotType);
        if (tool == null || tool.isBroken()) {
          continue;
        }
        for (ModifierEntry entry : tool.getModifierList()) {
          collectFrozenGuardianAttributeStats(entry.getHook(ModifierHooks.ARMOR_DAMAGE_STATS), tool, entry, context, slotType, source, stats);
        }
      }
    }

    private static void collectFrozenGuardianAttributeStats(ArmorDamageStatsModifierHook hook, IToolStackView tool, ModifierEntry entry,
                                                            EquipmentContext context, EquipmentSlot slotType, DamageSource source,
                                                            ArmorDamageStats stats) {
      if (hook instanceof FormulaArmorStatModule module) {
        if (module.stat() != ArmorDamageStatsModifierHook.ArmorDamageStat.ARMOR_ABSORPTION_CAP) {
          module.addArmorDamageStats(tool, entry, context, slotType, source, stats);
        }
      } else if (hook instanceof ArmorDamageStatsModifierHook.AllMerger merger) {
        for (ArmorDamageStatsModifierHook module : merger.modules()) {
          collectFrozenGuardianAttributeStats(module, tool, entry, context, slotType, source, stats);
        }
      }
    }

    private float apply(float rawDamage) {
      float damage = Math.max(0, rawDamage - preReduction);
      damage *= strengthFactor;
      damage *= armorFactor;
      damage = Math.max(0, damage - postReduction);
      return damage * protectionFactor;
    }

    private float revert(float finalDamage) {
      if (finalDamage <= 0) {
        return 0;
      }
      float damage = finalDamage / protectionFactor;
      damage += postReduction;
      damage /= armorFactor;
      damage /= strengthFactor;
      return damage + preReduction;
    }
  }

  private static void applyShareKnockback(LivingEntity protectedEntity, LivingEntity guardian, float damage, float distanceFactor) {
    double x = protectedEntity.getX() - guardian.getX();
    double z = protectedEntity.getZ() - guardian.getZ();
    double distance = Math.sqrt(x * x + z * z);
    if (distance > 0.01) {
      guardian.knockback(Math.min(damage * 0.05f, 0.5f) * distanceFactor, x / distance, z / distance);
    }
  }

  private static GuardianHurtContext currentGuardianHurtContext(LivingEntity entity) {
    Deque<GuardianHurtContext> contexts = GUARDIAN_HURT_CONTEXTS.get();
    if (contexts.isEmpty()) {
      return null;
    }
    GuardianHurtContext context = contexts.peek();
    return context.guardian() == entity ? context : null;
  }

  private static void hurtGuardian(LivingEntity guardian, DamageSource source, GuardianHurtContext context) {
    Deque<GuardianHurtContext> contexts = GUARDIAN_HURT_CONTEXTS.get();
    contexts.push(context);
    try {
      guardian.hurt(source, context.requestedFinalDamage());
    } finally {
      contexts.pop();
      if (contexts.isEmpty()) {
        GUARDIAN_HURT_CONTEXTS.remove();
      }
    }
  }

  private static final class GuardianHurtContext {
    private final LivingEntity guardian;
    private final float requestedFinalDamage;
    private final float healthGround;
    private float blockedFinalDamage;
    private float appliedFinalDamage;

    private GuardianHurtContext(LivingEntity guardian, float requestedFinalDamage, float healthGround) {
      this.guardian = guardian;
      this.requestedFinalDamage = requestedFinalDamage;
      this.healthGround = healthGround;
    }

    private LivingEntity guardian() {
      return guardian;
    }

    private float requestedFinalDamage() {
      return requestedFinalDamage;
    }

    private float appliedProfileFinalDamage() {
      return Math.max(0, requestedFinalDamage - blockedFinalDamage);
    }

    private float blockedFinalDamage() {
      return blockedFinalDamage;
    }

    private float appliedFinalDamage() {
      return appliedFinalDamage;
    }

    private void applyToEvent(LivingHurtEvent event, float damage, float armor, float toughness, int vanillaModifier, float modifierValue, float cap,
                              ArmorDamageStats commonStats) {
      if (commonStats != null && commonStats.hasDamageLimit()) {
        damage = commonStats.applyDamageLimit(guardian, damage);
      }
      float protectedDamage = ArmorUtil.getDamageAfterMagicAbsorb(damage, modifierValue, cap);
      float allowedDamage = Math.max(0, guardian.getHealth() - healthGround);
      float appliedDamage = Math.min(protectedDamage, allowedDamage);
      appliedFinalDamage = appliedDamage;
      float blockedDamage = protectedDamage - appliedDamage;
      if (blockedDamage > 0) {
        float protectionFactor = 1f - Mth.clamp(modifierValue, -20f, cap) / 25f;
        if (protectionFactor > 0) {
          blockedFinalDamage = blockedDamage / protectionFactor;
        }
      }
      if (vanillaModifier > 0) {
        appliedDamage = ArmorUtil.getDamageBeforeMagicAbsorb(appliedDamage, vanillaModifier);
      }
      if (!event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
        appliedDamage = ArmorUtil.getDamageBeforeArmorAbsorb(appliedDamage, armor, toughness);
      }
      event.setAmount(appliedDamage);
    }
  }

  private static void spawnShareDamageParticles(LivingEntity protectedEntity, LivingEntity guardian, int color) {
    if (!(protectedEntity.level() instanceof ServerLevel level)) {
      return;
    }
    // derive 8 color variants from the modifier color (TCAE style)
    Vector3f[] colors;
    if (color == -1) {
      colors = SHARE_DAMAGE_PARTICLE_COLORS;
    } else {
      int r = (color >> 16) & 0xFF;
      int g = (color >> 8) & 0xFF;
      int b = color & 0xFF;
      colors = new Vector3f[8];
      for (int i = 0; i < 8; i++) {
        float var = 0.85F + level.random.nextFloat() * 0.3F;
        colors[i] = new Vector3f(
          Math.min(1.0F, (r / 255F) * var),
          Math.min(1.0F, (g / 255F) * var),
          Math.min(1.0F, (b / 255F) * var)
        );
      }
    }
    Vec3 from = protectedEntity.position().add(0, protectedEntity.getBbHeight() / 2.0, 0);
    Vec3 to = guardian.position().add(0, guardian.getBbHeight() / 2.0, 0);
    Vec3 offset = to.subtract(from);
    double distance = offset.length();
    if (distance < 0.01 || distance > 32.0) {
      return;
    }

    Vec3 direction = offset.normalize();
    Vec3 random = new Vec3(level.random.nextGaussian(), level.random.nextGaussian(), level.random.nextGaussian());
    double dot = random.dot(direction);
    Vec3 bendDirection = random.subtract(direction.x * dot, direction.y * dot, direction.z * dot);
    if (bendDirection.lengthSqr() < 1.0E-4) {
      bendDirection = new Vec3(-direction.z, 0, direction.x);
    }
    bendDirection = bendDirection.normalize();

    int count = Math.max(4, (int)Math.ceil(distance) * 4);
    double height = Math.min(10.0, distance) * (0.1 + level.random.nextDouble() * 0.15);
    for (int i = 1; i <= count; i++) {
      double progress = (double)i / count;
      double bend = height * Math.sin(Math.PI * progress);
      Vec3 pos = from.add(direction.scale(distance * progress)).add(bendDirection.scale(bend));
      level.sendParticles(new ShareDamageParticleData(colors[level.random.nextInt(colors.length)], 0.5f), pos.x, pos.y, pos.z, 0, 0, 0, 0, 0);
    }

    for (int i = 0; i < 12; i++) {
      double x = to.x + (level.random.nextDouble() - 0.5) * 0.2;
      double y = to.y + (level.random.nextDouble() - 0.5) * 0.02;
      double z = to.z + (level.random.nextDouble() - 0.5) * 0.2;
      double xd = direction.x + level.random.nextDouble() - 0.5;
      double yd = 0.5 * (level.random.nextDouble() - 0.5);
      double zd = direction.z + level.random.nextDouble() - 0.5;
      level.sendParticles(new ShareDamageParticleData(colors[level.random.nextInt(colors.length)], 0.5f), x, y, z, 0, xd, yd, zd, 0.2);
    }
  }

  @SubscribeEvent
  static void livingDamage(LivingDamageEvent event) {
    LivingEntity entity = event.getEntity();
    DamageSource source = event.getSource();

    // give modifiers a chance to respond to damage happening
    float amount = event.getAmount();
    EquipmentContext context = new EquipmentContext(entity);
    if (context.hasModifiableArmor()) {
      amount = ModifyDamageModifierHook.modifyDamageTaken(ModifierHooks.MODIFY_DAMAGE, context, source, amount, OnAttackedModifierHook.isDirectDamage(source));
      event.setAmount(amount);
      if (amount <= 0) {
        event.setCanceled(true);
      }
    }

    // for remaining code, ensure amount is not more than they will take
    amount = Math.min(amount, entity.getHealth());

    // apply post hit modifier effects. Done regardless of damage dealt - don't care if absorption took it all
    if (Config.COMMON.allowMonsterMeleeModifiers.get() && source.is(TinkerTags.DamageTypes.MODIFIER_WHITELIST)) {
      Entity attacker = event.getSource().getEntity();
      if (attacker != null && !attacker.getType().is(TinkerTags.EntityTypes.DAMAGE_MODIFIER_BLACKLIST) && attacker instanceof LivingEntity living) {
        ItemStack weapon = living.getMainHandItem();
        if (!weapon.isEmpty() && weapon.is(TinkerTags.Items.MELEE_WEAPON)) {
          // already know we are not a player
          ToolAttackContext meleeContext = ToolAttackContext.attacker(living, null).target(event.getEntity()).applyAttributes().build();
          IToolStackView tool = ToolStack.from(weapon);
          for (ModifierEntry entry : tool.getModifiers()) {
            entry.getHook(ModifierHooks.MONSTER_MELEE_HIT).onMonsterMeleeHit(tool, entry, meleeContext, amount);
          }
        }
      }
    }

    // when damaging ender dragons, may drop scales - must be player caused explosion, end crystals and TNT are examples
    if (amount > 0 && Config.COMMON.dropDragonScales.get() && entity.getType() == EntityType.ENDER_DRAGON && event.getAmount() > 0
        && source.is(DamageTypeTags.IS_EXPLOSION) && source.getEntity() != null && source.getEntity().getType() == EntityType.PLAYER) {
      // drops 1 - 8 scales
      ModifierUtil.dropItem(entity, new ItemStack(TinkerModifiers.dragonScale, 1 + entity.level().random.nextInt(8)));
    }
  }

  /** Called the modifier hook when an entity's position changes */
  @SubscribeEvent
  static void livingWalk(LivingTickEvent event) {
    LivingEntity living = event.getEntity();
    // this event runs before vanilla updates prevBlockPos
    BlockPos pos = living.blockPosition();
    if (!living.isSpectator() && !living.level().isClientSide() && living.isAlive() && !Objects.equals(living.lastPos, pos)) {
      ItemStack boots = living.getItemBySlot(EquipmentSlot.FEET);
      if (!boots.isEmpty() && boots.is(TinkerTags.Items.BOOTS)) {
        ToolStack tool = ToolStack.from(boots);
        for (ModifierEntry entry : tool.getModifierList()) {
          entry.getHook(ModifierHooks.BOOT_WALK).onWalk(tool, entry, living, living.lastPos, pos);
        }
      }
    }
  }

  /** Handles visibility effects of mob disguise and projectile protection */
  @SubscribeEvent
  static void livingVisibility(LivingVisibilityEvent event) {
    // always nonnull in vanilla, not sure when it would be nullable but I dont see a need for either modifier
    Entity lookingEntity = event.getLookingEntity();
    if (lookingEntity == null) {
      return;
    }
    LivingEntity living = event.getEntity();
    TinkerDataCapability.Holder data = TinkerDataCapability.getData(living);
    if (data != null) {
      // mob disguise
      Multiset<EntityType<?>> disguises = data.get(MobDisguiseModule.DISGUISES);
      if (disguises != null) {
        int count = disguises.count(lookingEntity.getType());
        if (count > 0) {
          // halves the range per level
          event.modifyVisibility(1 / Math.pow(2, count));
        }
      }
    }
  }

  /** Syncs arrow modifier list to the client */
  @SubscribeEvent
  static void projectileSync(PlayerEvent.StartTracking event) {
    Entity entity = event.getTarget();
    if (entity instanceof Projectile) {
      TinkerNetwork.getInstance().sendTo(new SyncProjectileModifiersPacket(entity), event.getEntity());
    }
  }

  /** Implements projectile hit hook */
  @SuppressWarnings("removal")  // can't update without losing Neo compat
  @SubscribeEvent
  static void projectileHit(ProjectileImpactEvent event) {
    Projectile projectile = event.getProjectile();
    ModifierNBT modifiers = EntityModifierCapability.getOrEmpty(projectile);
    if (!modifiers.isEmpty()) {
      ModDataNBT nbt = PersistentDataCapability.getOrWarn(projectile);
      HitResult hit = event.getRayTraceResult();
      HitResult.Type type = hit.getType();
      // extract a firing entity as that is a common need
      LivingEntity attacker = projectile.getOwner() instanceof LivingEntity l ? l : null;
      ModuleHook<ProjectileHitModifierHook> hook = projectile.level().isClientSide ? ModifierHooks.PROJECTILE_HIT_CLIENT : ModifierHooks.PROJECTILE_HIT;
      switch(type) {
        case ENTITY -> {
          EntityHitResult entityHit = (EntityHitResult)hit;
          // cancel all effects on endermen unless we have enderference, endermen like to teleport away
          // yes, hardcoded to enderference, if you need your own enderference for whatever reason, talk to us
          Entity entity = entityHit.getEntity();
          if (entity.getType() != EntityType.ENDERMAN || modifiers.getLevel(TinkerModifiers.enderference.getId()) > 0) {
            // extract a living target as that is the most common need
            LivingEntity target = ToolAttackUtil.getLivingEntity(entity);

            // if its a piercing arrow, skip modifier effects when at the piercing limit, arrow is going to skip the hit
            boolean canBlock = true;
            if (projectile instanceof AbstractArrow arrow) {
              int pierce = arrow.getPierceLevel();
              if (pierce > 0) {
                if (arrow.piercingIgnoreEntityIds != null && arrow.piercingIgnoreEntityIds.size() >= pierce + 1) {
                  return;
                }
                canBlock = false;
              }
            }

            // ensure we are not blocking, that means projectile shouldn't hit
            boolean notBlocked = true;
            if (canBlock && target != null && target.isBlocking()) {
              Vec3 direction = projectile.position().vectorTo(target.position()).normalize();
              direction = new Vec3(direction.x, 0.0D, direction.z);
              if (direction.dot(target.getViewVector(1.0F)) < 0.0D) {
                notBlocked = false;
              }
            }
            for (ModifierEntry entry : modifiers.getModifiers()) {
              if (entry.getHook(hook).onProjectileHitEntity(modifiers, nbt, entry, projectile, entityHit, attacker, target, notBlocked)) {
                // on forge, this means the cancelled entity won't be hit again if its a piercing arrow
                // on neo, they will get processed again next frame. Is this something we need to work around?
                event.setCanceled(true);
                break;
              }
            }
          }
        }
        case BLOCK -> {
          BlockHitResult blockHit = (BlockHitResult)hit;
          for (ModifierEntry entry : modifiers.getModifiers()) {
            if (entry.getHook(hook).onProjectileHitsBlock(modifiers, nbt, entry, projectile, blockHit, attacker)) {
              event.setCanceled(true);
              break;
            }
          }
        }
      }
    }
  }

}
