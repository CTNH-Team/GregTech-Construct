package slimeknights.tconstruct.tables.block.entity.table;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.ItemHandlerHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.shared.inventory.ConfigurableInvWrapperCapability;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.block.entity.inventory.CraftingContainerWrapper;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer.ILazyCrafter;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;
import slimeknights.tconstruct.tables.network.UpdateCraftingRecipePacket;
import slimeknights.tconstruct.tables.recipe.ToolCraftingResolver;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class CraftingStationBlockEntity extends RetexturedTableBlockEntity implements ILazyCrafter {
  public static final int CRAFTING_SLOT_COUNT = 9;
  public static final int TOOL_SLOT_COUNT = 9;
  public static final int TOOL_SLOT_START = CRAFTING_SLOT_COUNT;
  public static final Component UNCRAFTABLE = TConstruct.makeTranslation("gui", "crafting_station.uncraftable");
  private static final Component NAME = TConstruct.makeTranslation("gui", "crafting_station");

  /** Plugin hook selecting among conflicting crafting recipes, provided by an optional integration. */
  @Nullable
  public static ICraftingRecipeSelector recipeSelector;

  /** Hook for plugins resolving crafting recipe conflicts. */
  public interface ICraftingRecipeSelector {
    /**
     * Picks the recipe to use for the given crafting inventory from all matching recipes.
     * May return null when no recipe should be used.
     */
    @Nullable
    CraftingRecipe selectRecipe(CraftingStationBlockEntity tile, CraftingContainerWrapper inventory, List<CraftingRecipe> matches);
  }

  /** Last crafted crafting recipe */
  @Nullable
  private CraftingRecipe lastRecipe;
  /** Resolved virtual-input match when the selected recipe uses dedicated GT tool slots. */
  @Nullable
  private ToolCraftingResolver.Match lastToolMatch;
  /** Cached tool-recipe candidates; invalidated only when station inputs change. */
  @Nullable
  private List<ToolCraftingResolver.Match> cachedToolMatches;
  /** Result inventory, lazy loads results */
  @Getter
  private final LazyResultContainer craftingResult;
  /** Whether shift clicking the result moves it into the adjacent container first */
  private boolean shiftClickIntoStorage = true;
  /** Crafting inventory for the recipe calls */
  @Getter
  private final CraftingContainerWrapper craftingInventory;

  public CraftingStationBlockEntity(BlockPos pos, BlockState state) {
    super(TinkerTables.craftingStationTile.get(), pos, state, NAME, CRAFTING_SLOT_COUNT + TOOL_SLOT_COUNT);
    this.itemHandler = new ConfigurableInvWrapperCapability(this, false, false);
    this.itemHandlerCap = LazyOptional.of(() -> this.itemHandler);
    this.craftingInventory = new CraftingContainerWrapper(this, 3, 3, 0);
    this.craftingResult = new LazyResultContainer(this);
  }

  /** Returns a dedicated tool slot stack without exposing it through the crafting grid. */
  public ItemStack getToolStack(int index) {
    if (index < 0 || index >= TOOL_SLOT_COUNT) {
      return ItemStack.EMPTY;
    }
    return getItem(TOOL_SLOT_START + index);
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new CraftingStationContainerMenu(menuId, playerInventory, this);
  }

  @Override
  public AABB getRenderBoundingBox() {
    return new AABB(worldPosition, worldPosition.offset(1, 2, 1));
  }

  /* Crafting */

  @Override
  public ItemStack calcResult(@Nullable Player player) {
    if (this.level == null || isEmpty()) {
      return ItemStack.EMPTY;
    }
    // assume empty unless we learn otherwise
    ItemStack result = ItemStack.EMPTY;
    if (!this.level.isClientSide && this.level.getServer() != null) {
      RecipeManager manager = this.level.getServer().getRecipeManager();

      // first, try the cached recipe
      ForgeHooks.setCraftingPlayer(player);
      CraftingRecipe recipe = lastRecipe;
      // if it does not match, find a new recipe; plugins may resolve conflicts between the matches
      // note we intentionally have no player access during matches, that could lead to an unstable recipe
      if (recipe == null || (lastToolMatch == null && !recipe.matches(this.craftingInventory, this.level))
          || (lastToolMatch != null && !ToolCraftingResolver.isStillValid(this, lastToolMatch))) {
        List<CraftingRecipe> matches = manager.getRecipesFor(RecipeType.CRAFTING, this.craftingInventory, this.level);
        List<ToolCraftingResolver.Match> toolMatches = getToolMatches();
        toolMatches.forEach(match -> { if (!matches.contains(match.recipe())) matches.add(match.recipe()); });
        ICraftingRecipeSelector selector = recipeSelector;
        if (selector != null && !matches.isEmpty()) {
          recipe = selector.selectRecipe(this, this.craftingInventory, matches);
        } else {
          recipe = matches.isEmpty() ? null : matches.get(0);
        }
        CraftingRecipe selectedRecipe = recipe;
        lastToolMatch = selectedRecipe == null ? null : toolMatches.stream()
          .filter(match -> match.recipe().getId().equals(selectedRecipe.getId()))
          .findFirst().orElse(null);
      }

      // if we have a recipe, fetch its result
      if (recipe != null) {
        result = lastToolMatch == null ? recipe.assemble(this.craftingInventory, level.registryAccess())
          : recipe.assemble(lastToolMatch.inventory(), level.registryAccess());

        // sync if the recipe is different
        if (recipe != lastRecipe) {
          this.lastRecipe = recipe;
          this.syncToRelevantPlayers(this::syncRecipe);
        }
      }
      ForgeHooks.setCraftingPlayer(null);
    }
    else if (this.lastRecipe != null) {
      ToolCraftingResolver.Match toolMatch = lastToolMatch;
      if (toolMatch == null) {
        toolMatch = ToolCraftingResolver.findMatches(this, this.level).stream()
          .filter(match -> match.recipe().getId().equals(this.lastRecipe.getId())).findFirst().orElse(null);
      }
      if (toolMatch == null && !this.lastRecipe.matches(this.craftingInventory, this.level)) {
        return ItemStack.EMPTY;
      }
      ForgeHooks.setCraftingPlayer(player);
      result = toolMatch == null ? this.lastRecipe.assemble(this.craftingInventory, level.registryAccess())
        : this.lastRecipe.assemble(toolMatch.inventory(), level.registryAccess());
      this.lastToolMatch = toolMatch;
      ForgeHooks.setCraftingPlayer(null);
    }
    return result;
  }

  /**
   * Gets the player sensitive crafting result, also validating the player has access to this recipe
   * @param player  Player
   * @return  Player sensitive result
   */
  public ItemStack getResultForPlayer(Player player) {
    ForgeHooks.setCraftingPlayer(player);
    CraftingRecipe recipe = this.lastRecipe; // local variable just to prevent race conditions if the field changes, though that is unlikely

    // try matches again now that we have player access
    if (recipe == null || this.level == null) {
      ForgeHooks.setCraftingPlayer(null);
      return ItemStack.EMPTY;
    }
    if (lastToolMatch == null) {
      lastToolMatch = ToolCraftingResolver.findMatches(this, level).stream()
        .filter(match -> match.recipe().getId().equals(recipe.getId())).findFirst().orElse(null);
      if (lastToolMatch == null && !recipe.matches(craftingInventory, level)) {
        ForgeHooks.setCraftingPlayer(null);
        return ItemStack.EMPTY;
      }
    }

    // check if the player has access to the recipe, if not give up
    // Disabled because this is an absolute mess of logic, and the gain is rather small, treating this like a furnace instead
    // note the gamerule is client side only anyways, so you would have to sync it, such as in the container
    // if you want limited crafting, disable the crafting station, the design of the station is incompatible with the game rule and vanilla syncing
//    if (!recipe.isDynamic() && world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING)) {
//      // mojang, why can't PlayerEntity just have a RecipeBook getter, why must I go through the sided classes? grr
//      boolean locked;
//      if (!world.isRemote) {
//        locked = player instanceof ServerPlayerEntity && !((ServerPlayerEntity) player).getRecipeBook().isUnlocked(recipe);
//      } else {
//        locked = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> player instanceof ClientPlayerEntity && !((ClientPlayerEntity) player).getRecipeBook().isUnlocked(recipe));
//      }
//      // if the player cannot craft this, block crafting
//      if (locked) {
//        ForgeHooks.setCraftingPlayer(null);
//        return ItemStack.EMPTY;
//      }
//    }

    ItemStack result = lastToolMatch == null ? recipe.assemble(craftingInventory, level.registryAccess())
      : recipe.assemble(lastToolMatch.inventory(), level.registryAccess());
    ForgeHooks.setCraftingPlayer(null);
    return result;
  }

  /**
   * Removes the result from this inventory, updating inputs and triggering recipe hooks
   * @param player  Player taking result
   * @param result  Result removed
   * @param amount  Number of times crafted
   */
  public void takeResult(Player player, ItemStack result, int amount) {
    CraftingRecipe recipe = this.lastRecipe; // local variable just to prevent race conditions if the field changes, though that is unlikely
    if (recipe == null || this.level == null) {
      return;
    }
    ToolCraftingResolver.Match toolMatch = this.lastToolMatch;
    if (toolMatch == null && this.level != null) {
      // Re-resolve here because result clicks can arrive after a client recipe sync cleared the cached match.
      toolMatch = ToolCraftingResolver.findMatches(this, this.level).stream()
        .filter(match -> match.recipe().getId().equals(recipe.getId()))
        .findFirst()
        .orElse(null);
    }

    // fire crafting events
    if (!recipe.isSpecial()) {
      // unlock the recipe if it was not unlocked, so it shows in the recipe book
      player.awardRecipes(Collections.singleton(recipe));
    }
    result.onCraftedBy(this.level, player, amount);
    ForgeEventFactory.firePlayerCraftingEvent(player, result, this.craftingInventory);

    // update all slots in the inventory
    // remove remaining items
    // Dedicated GT tools live in TOOL_SLOT_START..; the virtual wrapper holds copies that share the same
    // CTPP SavedData record (UUID). Calling getRemainingItems on the wrapper would damage the shared
    // record a second time (tool 2 + wrapper 1 => 3). Compute remaining from the real 3x3 only; tool
    // durability is applied once in ToolCraftingResolver.damageTools.
    ForgeHooks.setCraftingPlayer(player);
    NonNullList<ItemStack> remaining = recipe.getRemainingItems(craftingInventory);
    ForgeHooks.setCraftingPlayer(null);
    for (int i = 0; i < remaining.size(); ++i) {
      ItemStack original = this.getItem(i);
      ItemStack newStack = remaining.get(i);

      // A virtual GT tool input has no corresponding real grid slot. Never materialize its
      // remaining stack into the 3x3 inventory; the dedicated tool slot is damaged below.
      if (toolMatch != null && contains(toolMatch.toolCells(), i)) {
        continue;
      }
      if (original.isEmpty() && !newStack.isEmpty()) {
        continue;
      }

      // if empty or size 1, set directly (decreases by 1)
      if (original.isEmpty() || original.getCount() == 1) {
        this.setItem(i, newStack);
      }
      else if (ItemStack.isSameItemSameTags(original, newStack)) {
        // if matching, merge (decreasing by 1
        newStack.grow(original.getCount() - 1);
        this.setItem(i, newStack);
      }
      else {
        // directly update the slot
        this.setItem(i, ItemHandlerHelper.copyStackWithSize(original, original.getCount() - 1));
        // otherwise, drop the item as the player
        if (!newStack.isEmpty() && !player.getInventory().add(newStack)) {
          player.drop(newStack, false);
        }
      }
    }
    if (toolMatch != null) {
      ToolCraftingResolver.damageTools(this, toolMatch, player);
    }
  }

  private static boolean contains(int[] values, int value) {
    for (int candidate : values) if (candidate == value) return true;
    return false;
  }

  /** Sends a message alerting the player this item is currently uncraftable, typically due to gamerules */
  public void notifyUncraftable(Player player) {
    // if empty, send a message so the player is more aware of why they cannot craft it, sent to chat as status bar is not visible
    // TODO: consider moving into the UI somewhere
    if (level != null && !level.isClientSide) {
      player.displayClientMessage(CraftingStationBlockEntity.UNCRAFTABLE, false);
    }
  }

  @Override
  public void onCraft(Player player, ItemStack result, int amount) {
    // update the inputs and trigger recipe hooks
    if (amount != 0 && !result.isEmpty()) {
      takeResult(player, result, amount);
    }
  }

  @Override
  public void setItem(int slot, ItemStack itemstack) {
    super.setItem(slot, itemstack);
    this.cachedToolMatches = null;
    this.lastToolMatch = null;
    // clear the crafting result when the matrix changes so we recalculate the result
    this.craftingResult.clearContent();
  }

  private List<ToolCraftingResolver.Match> getToolMatches() {
    if (this.cachedToolMatches == null && this.level != null) {
      this.cachedToolMatches = new ArrayList<>(ToolCraftingResolver.findMatches(this, this.level));
    }
    return this.cachedToolMatches == null ? Collections.emptyList() : this.cachedToolMatches;
  }

  /** Invalidates cached tool candidates after direct durability/NBT mutation. */
  public void invalidateToolMatches() {
    this.cachedToolMatches = null;
    this.lastToolMatch = null;
  }


  /* Syncing */

  /**
   * Sends the current recipe to the given player
   * @param player  Player to send an update to
   */
  public void syncRecipe(Player player) {
    // must have a last recipe and a server world
    if (this.lastRecipe != null && this.level != null && !this.level.isClientSide && player instanceof ServerPlayer) {
      TinkerNetwork.getInstance().sendTo(new UpdateCraftingRecipePacket(this.worldPosition, this.lastRecipe), (ServerPlayer) player);
    }
  }

  /**
   * Updates the recipe from the server
   * @param recipe  New recipe
   */
  public void updateRecipe(CraftingRecipe recipe) {
    this.lastRecipe = recipe;
    this.lastToolMatch = null;
    this.craftingResult.clearContent();
  }

  /** Whether shift clicking a crafted result prefers the adjacent container over the player inventory */
  public boolean isShiftClickIntoStorage() {
    return this.shiftClickIntoStorage;
  }

  /** Sets the shift click result target, marking the tile as changed */
  public void setShiftClickIntoStorage(boolean shiftClickIntoStorage) {
    if (this.shiftClickIntoStorage != shiftClickIntoStorage) {
      this.shiftClickIntoStorage = shiftClickIntoStorage;
      this.setChanged();
    }
  }

  @Override
  public void saveAdditional(CompoundTag tags) {
    super.saveAdditional(tags);
    tags.putBoolean("ShiftClickIntoStorage", this.shiftClickIntoStorage);
  }

  @Override
  public void load(CompoundTag tags) {
    super.load(tags);
    this.shiftClickIntoStorage = tags.getBoolean("ShiftClickIntoStorage");
  }
}
