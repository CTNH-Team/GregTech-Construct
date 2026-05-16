package slimeknights.tconstruct.tables.block.entity.table;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.SoundUtils;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.client.model.ModelProperties;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.shared.inventory.ConfigurableInvWrapperCapability;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.apotheosis.ApotheosisSocketMode;
import slimeknights.tconstruct.tables.block.TinkerStationBlock;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer.ILazyCrafter;
import slimeknights.tconstruct.tables.block.entity.inventory.TinkerStationContainerWrapper;
import slimeknights.tconstruct.tables.menu.TinkerStationContainerMenu;
import slimeknights.tconstruct.tables.network.UpdateTinkerStationRecipePacket;

import javax.annotation.Nullable;
import java.util.Objects;

import static slimeknights.tconstruct.library.tools.part.IMaterialItem.MATERIAL_TAG;

public class TinkerStationBlockEntity extends RetexturedTableBlockEntity implements ILazyCrafter {
  private static final String TAG_GEM_MODE = "gem_mode";
  private static final String TAG_CACHED_ORDINARY_INPUTS = "cached_ordinary_inputs";
  /** Slot index of the tool slot */
  public static final int TINKER_SLOT = 0;
  /** Slot index of the first input slot */
  public static final int INPUT_SLOT = 1;
  /** Name of the TE */
  private static final Component NAME = TConstruct.makeTranslation("gui", "tinker_station");

  /** Last crafted crafting recipe */
  @Nullable @Getter
  private ITinkerStationRecipe lastRecipe;
  /** Result inventory, lazy loads results */
  @Getter
  private final LazyResultContainer craftingResult;
  /** Crafting inventory for the recipe calls */
  private final TinkerStationContainerWrapper inventoryWrapper;

  /** Current result, may be modified again later */
  @Nullable
  private LazyToolStack result = null;
  /** Error from the last recipe */
  @Nullable
  @Getter
  private Component currentError = null;
  /** Current text in the text field */
  @Getter
  private String itemName = "";
  /** Shared gem mode state for all viewers of this anvil */
  @Getter
  private boolean gemMode = false;
  /** Cached copies of the five ordinary input slots while gem mode is active */
  @Getter
  private final NonNullList<ItemStack> cachedOrdinaryInputs = NonNullList.withSize(5, ItemStack.EMPTY);

  /** Material variant texture, alterantive to {@link #getTexture()} in the model. */
  @Getter
  private MaterialVariantId material = IMaterial.UNKNOWN_ID;

  public TinkerStationBlockEntity(BlockPos pos, BlockState state) {
    // if the block is the right type, use it for slot count
    this(pos, state, (state.getBlock() instanceof TinkerStationBlock station) ? station.getSlotCount() : 6);
  }

  public TinkerStationBlockEntity(BlockPos pos, BlockState state, int slots) {
    super(TinkerTables.tinkerStationTile.get(), pos, state, NAME, slots);
    this.itemHandler = new ConfigurableInvWrapperCapability(this, false, false);
    this.itemHandlerCap = LazyOptional.of(() -> this.itemHandler);
    this.inventoryWrapper = new TinkerStationContainerWrapper(this);
    this.craftingResult = new LazyResultContainer(this);
  }

  @Override
  public Component getDefaultName() {
    if (this.level == null) {
      return super.getDefaultName();
    }
    return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
  }

  /**
   * Gets the number of item input slots, ignoring the tool
   * @return  Input count
   */
  public int getInputCount() {
    return getContainerSize() - 1;
  }

  /** Gets the tool contained in this block entity */
  public LazyToolStack getTool() {
    return inventoryWrapper.getTool();
  }

  /** Gets the recipe result */
  @Nullable
  public LazyToolStack getResult() {
    // ensure the result has been resolved else we may be returning null when we shouldn't
    // if we return null that means there is no result, not its not calculated.
    craftingResult.getResult();
    return result;
  }

  /** @deprecated use {@link #getResult()} */
  @SuppressWarnings("unused")
  @Deprecated(forRemoval = true)
  @Nullable
  public LazyToolStack getResult(@Nullable Player player) {
    return getResult();
  }

  @Override
  public void resize(int size) {
    super.resize(size);
    inventoryWrapper.resize();
  }

  /** Moves the ordinary inputs into a shared cache and clears the live input slots. */
  public void enterGemMode() {
    if (this.gemMode) {
      return;
    }
    this.gemMode = true;
    for (int i = 0; i < Math.min(5, this.getInputCount()); i++) {
      ItemStack existing = this.getItem(INPUT_SLOT + i);
      this.cachedOrdinaryInputs.set(i, existing.copy());
      if (!existing.isEmpty()) {
        super.setItem(INPUT_SLOT + i, ItemStack.EMPTY);
        this.inventoryWrapper.refreshInput(INPUT_SLOT + i);
      }
    }
    this.craftingResult.clearContent();
    setChanged();
  }

  /** Restores cached ordinary inputs back into the live input slots and exits gem mode. */
  public void exitGemMode() {
    if (!this.gemMode) {
      return;
    }
    this.gemMode = false;
    for (int i = 0; i < Math.min(5, this.getInputCount()); i++) {
      super.setItem(INPUT_SLOT + i, this.cachedOrdinaryInputs.get(i));
      this.cachedOrdinaryInputs.set(i, ItemStack.EMPTY);
      this.inventoryWrapper.refreshInput(INPUT_SLOT + i);
    }
    this.craftingResult.clearContent();
    setChanged();
  }

  /** Sends a screen refresh to all viewers currently sharing this station. */
  public void syncGemModeViewers() {
    syncScreenToRelevantPlayers();
  }

  /** Immediately inserts a carried gem into the selected visible socket and returns the carried remainder. */
  public ItemStack insertGem(int visibleSocketIndex, ItemStack carriedGem) {
    if (!this.gemMode || carriedGem.isEmpty()) {
      return carriedGem;
    }

    ItemStack tool = this.getItem(TINKER_SLOT);
    var sockets = ApotheosisSocketMode.getVisibleSockets(tool);
    if (tool.isEmpty() || visibleSocketIndex < 0 || visibleSocketIndex >= sockets.size()) {
      return carriedGem;
    }

    ApotheosisBridge.SocketGem socket = sockets.get(visibleSocketIndex);
    ItemStack singleGem = ItemHandlerHelper.copyStackWithSize(carriedGem, 1);
    if (!ApotheosisBridge.sockets().canInsertGem(tool, socket.rawSocketIndex(), singleGem)) {
      return carriedGem;
    }

    ItemStack updatedTool = ApotheosisSocketMode.insertGem(tool, visibleSocketIndex, singleGem);
    if (updatedTool.isEmpty()) {
      return carriedGem;
    }

    this.setItem(TINKER_SLOT, updatedTool);
    this.itemName = "";
    return carriedGem.getCount() <= 1 ? ItemStack.EMPTY : ItemHandlerHelper.copyStackWithSize(carriedGem, carriedGem.getCount() - 1);
  }

  /** Immediately removes a gem from the selected visible socket and returns it to the caller. */
  public ItemStack removeGem(int visibleSocketIndex) {
    if (!this.gemMode) {
      return ItemStack.EMPTY;
    }

    ItemStack tool = this.getItem(TINKER_SLOT);
    var sockets = ApotheosisSocketMode.getVisibleSockets(tool);
    if (tool.isEmpty() || visibleSocketIndex < 0 || visibleSocketIndex >= sockets.size()) {
      return ItemStack.EMPTY;
    }

    ApotheosisBridge.SocketGem socket = sockets.get(visibleSocketIndex);
    if (!socket.isFilled()) {
      return ItemStack.EMPTY;
    }

    ItemStack removedGem = ApotheosisBridge.sockets().copyGem(tool, socket.rawSocketIndex());
    ItemStack updatedTool = ApotheosisSocketMode.removeGem(tool, visibleSocketIndex);
    if (removedGem.isEmpty() || updatedTool.isEmpty()) {
      return ItemStack.EMPTY;
    }

    this.setItem(TINKER_SLOT, updatedTool);
    this.itemName = "";
    return removedGem;
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new TinkerStationContainerMenu(menuId, playerInventory, this);
  }

  /* Crafting */

  @Override
  public ItemStack calcResult(@Nullable Player player) {
    if (this.level == null) {
      return ItemStack.EMPTY;
    }

    // assume empty unless we learn otherwise
    result = null;
    this.currentError = null;

    if (this.gemMode) {
      ItemStack tool = this.getItem(TINKER_SLOT).copy();
      if (!tool.isEmpty()) {
        result = LazyToolStack.from(tool);
      }
      return tool;
    }

    if (!this.level.isClientSide && this.level.getServer() != null) {
      RecipeManager manager = this.level.getServer().getRecipeManager();

      // first, try the cached recipe
      ITinkerStationRecipe recipe = lastRecipe;
      // if it does not match, find a new recipe
      if (recipe == null || !recipe.matches(this.inventoryWrapper, this.level)) {
        recipe = manager.getRecipeFor(TinkerRecipeTypes.TINKER_STATION.get(), this.inventoryWrapper, this.level).orElse(null);
      }

      // if we have a recipe, fetch its result
      boolean needsSync = true;
      if (recipe != null) {
        // sync if the recipe is different
        if (lastRecipe != recipe) {
          this.lastRecipe = recipe;
          this.syncToRelevantPlayers(this::syncRecipe);
          needsSync = false;
        }

        // try for UI errors
        RecipeResult<LazyToolStack> validatedResult = recipe.getValidatedResult(this.inventoryWrapper, level.registryAccess());
        if (validatedResult.isSuccess()) {
          result = validatedResult.getResult();
        } else if (validatedResult.hasError()) {
          this.currentError = validatedResult.getMessage();
        }
      }
      // recipe will sync screen, so only need to call it when not syncing the recipe
      if (needsSync) {
        syncScreenToRelevantPlayers();
      }
    }
    // client side only needs to update result, server syncs message elsewhere
    else if (this.lastRecipe != null && this.lastRecipe.matches(this.inventoryWrapper, level)) {
      RecipeResult<LazyToolStack> validatedResult = this.lastRecipe.getValidatedResult(this.inventoryWrapper, level.registryAccess());
      if (validatedResult.isSuccess()) {
        result = validatedResult.getResult();
      } else if (validatedResult.hasError()) {
        this.currentError = validatedResult.getMessage();
      }
    }

    if (result != null) {
      // set name if we have one
      if (!itemName.isEmpty()) {
        TooltipUtil.setDisplayName(result.getStack(), itemName);
      }

      return result.getStack();
    } else {
      return ItemStack.EMPTY;
    }
  }

  @Override
  public void onCraft(Player player, ItemStack resultItem, int amount) {
    if (this.gemMode) {
      return;
    }

    // the recipe should match if we got this far, but being null is a problem
    LazyToolStack result = this.result;  // result is going to get cleared as we update things
    if (amount == 0 || this.level == null || this.lastRecipe == null || result == null) {
      return;
    }

    // fire crafting events
    resultItem.onCraftedBy(this.level, player, amount);
    ForgeEventFactory.firePlayerCraftingEvent(player, resultItem, this.inventoryWrapper);
    this.playCraftSound(player);

    // fetch this before updating inputs so they can do input sensitive shrinking
    ItemStack tinkerable = this.getItem(TINKER_SLOT);
    int shrinkToolSlot = tinkerable.isEmpty() ? 0 : lastRecipe.shrinkToolSlotBy(result, inventoryWrapper);

    // run the recipe, will shrink inputs
    // run both sides for the sake of shift clicking
    this.inventoryWrapper.setPlayer(player);
    this.lastRecipe.updateInputs(result, inventoryWrapper, !level.isClientSide);
    this.inventoryWrapper.setPlayer(null);

    // remove the center slot item, just clear it entirely (if you want shrinking you should use the outer slots or ask nicely for a shrink amount hook)
    if (shrinkToolSlot > 0) {
      if (tinkerable.getCount() <= shrinkToolSlot) {
        this.setItem(TINKER_SLOT, ItemStack.EMPTY);
      } else {
        this.setItem(TINKER_SLOT, ItemHandlerHelper.copyStackWithSize(tinkerable, tinkerable.getCount() - shrinkToolSlot));
      }
    }
    this.itemName = "";
  }

  @Override
  public void setItem(int slot, ItemStack itemstack) {
    super.setItem(slot, itemstack);
    // clear the crafting result when the matrix changes so we recalculate the result
    this.craftingResult.clearContent();
    this.inventoryWrapper.refreshInput(slot);
  }
  
  @Override
  protected void playCraftSound(Player player) {
    if (isSoundReady(player)) {
      if (this.getInputCount() > 4) {
        SoundUtils.playSoundForAll(player, SoundEvents.ANVIL_USE, 0.4f, 0.9f + 0.1f * player.getRandom().nextFloat());
      } else {
        SoundUtils.playSoundForAll(player, Sounds.SAW.getSound(), 0.8f, 0.8f + 0.4f * player.getRandom().nextFloat());
      }
    }
  }


  /* Item name */

  /** Sets the name of the item */
  public void setItemName(String name) {
    this.itemName = name;
    ItemStack result = craftingResult.getResult();
    if (!result.isEmpty()) {
      // if blank, set name to original name
      if (StringUtils.isBlank(name)) {
        // if the input was named, instead of clearing restore the old name
        ItemStack input = getItem(TINKER_SLOT);
        if (!input.isEmpty()) {
          name = TooltipUtil.getDisplayName(input);
        } else {
          // empty string will clear the stack tag
          name = "";
        }
      }
      TooltipUtil.setDisplayName(result, name);
    }
  }


  /* Syncing */

  /**
   * Sends the current recipe to the given player
   * @param player  Player to send an update to
   */
  public void syncRecipe(Player player) {
    // must have a last recipe and a server level
    if (this.lastRecipe != null && this.level != null && !this.level.isClientSide && player instanceof ServerPlayer server) {
      TinkerNetwork.getInstance().sendTo(new UpdateTinkerStationRecipePacket(this.worldPosition, this.lastRecipe), server);
    }
  }

  /**
   * Updates the recipe from the server
   * @param recipe  New recipe
   */
  public void updateRecipe(ITinkerStationRecipe recipe) {
    this.lastRecipe = recipe;
    this.craftingResult.clearContent();
  }


  /* Texture */

  @Override
  public ModelData getModelData() {
    // include material and texture, practically only one of the two should do anything
    return RetexturedHelper.getModelDataBuilder(texture).with(ModelProperties.MATERIAL, material).build();
  }

  @Override
  public void updateTexture(String name) {
    // reset material
    if (!name.isEmpty()) {
      this.material = IMaterial.UNKNOWN_ID;
    }
    super.updateTexture(name);
  }

  /** Called to update the material on the block. */
  public void setMaterial(MaterialVariantId material) {
    MaterialVariantId oldMaterial = this.material;
    // TODO: resolve redirects?
    this.material = material;
    // reset other texture
    this.texture = Blocks.AIR;
    if (!oldMaterial.equals(material)) {
      setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }

  @Override
  public void saveSynced(CompoundTag tags) {
    super.saveSynced(tags);
    if (material != IMaterial.UNKNOWN_ID) {
      tags.putString(MATERIAL_TAG, material.toString());
    }
    tags.putBoolean(TAG_GEM_MODE, this.gemMode);
    ListTag cachedInputs = new ListTag();
    for (ItemStack stack : this.cachedOrdinaryInputs) {
      CompoundTag itemTag = new CompoundTag();
      stack.save(itemTag);
      cachedInputs.add(itemTag);
    }
    tags.put(TAG_CACHED_ORDINARY_INPUTS, cachedInputs);
  }

  @Override
  public void load(CompoundTag tags) {
    super.load(tags);
    if (tags.contains(MATERIAL_TAG, Tag.TAG_STRING)) {
      material = Objects.requireNonNullElse(MaterialVariantId.tryParse(tags.getString(MATERIAL_TAG)), IMaterial.UNKNOWN_ID);
      RetexturedHelper.onTextureUpdated(this);
    }
    this.gemMode = tags.getBoolean(TAG_GEM_MODE);
    for (int i = 0; i < this.cachedOrdinaryInputs.size(); i++) {
      this.cachedOrdinaryInputs.set(i, ItemStack.EMPTY);
    }
    if (tags.contains(TAG_CACHED_ORDINARY_INPUTS, Tag.TAG_LIST)) {
      ListTag cachedInputs = tags.getList(TAG_CACHED_ORDINARY_INPUTS, Tag.TAG_COMPOUND);
      for (int i = 0; i < Math.min(this.cachedOrdinaryInputs.size(), cachedInputs.size()); i++) {
        this.cachedOrdinaryInputs.set(i, ItemStack.of(cachedInputs.getCompound(i)));
      }
    }
  }
}
