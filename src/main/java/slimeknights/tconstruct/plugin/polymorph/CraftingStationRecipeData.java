package slimeknights.tconstruct.plugin.polymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.illusivesoulworks.polymorph.api.common.base.IPolymorphPacketDistributor;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.common.impl.RecipePair;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Polymorph recipe data for the crafting station, attached to the station's block entity as a
 * capability. Keeps the list of matching recipes in sync with listeners and remembers the
 * player's selection, which {@link PolymorphRecipeSelector} feeds back into the station.
 */
public class CraftingStationRecipeData implements IBlockEntityRecipeData {
  private static final String TAG_SELECTED_RECIPE = "selected_recipe";

  private final CraftingStationBlockEntity tile;
  private final Set<ServerPlayer> listeners = ConcurrentHashMap.newKeySet();
  private SortedSet<IRecipePair> recipesList = new TreeSet<>();
  /** Input items from the previous tick, used to detect changes like Polymorph's built-in recipe data. */
  private NonNullList<Item> lastInput = NonNullList.create();
  @Nullable
  private ResourceLocation selectedRecipe;
  private boolean failing;

  public CraftingStationRecipeData(CraftingStationBlockEntity tile) {
    this.tile = tile;
  }

  /** Refreshes the candidate list from the current matches and pushes it to the listeners. */
  public void updateCandidates(List<CraftingRecipe> matches) {
    SortedSet<IRecipePair> pairs = new TreeSet<>();
    for (CraftingRecipe recipe : matches) {
      ItemStack output = recipe.getResultItem(tile.getLevel().registryAccess());
      pairs.add(new RecipePair(recipe.getId(), output));
    }
    this.recipesList = pairs;
    this.sendRecipesListToListeners(false);
  }

  @Override
  public <T extends Recipe<C>, C extends Container> Optional<T> getRecipe(RecipeType<T> type, C container, Level level, List<T> recipes) {
    if (recipes.isEmpty()) {
      return Optional.empty();
    }
    // the player's selection wins, otherwise default to the first match
    ResourceLocation selected = this.selectedRecipe;
    if (selected != null) {
      for (T recipe : recipes) {
        if (recipe.getId().equals(selected)) {
          return Optional.of(recipe);
        }
      }
    }
    return Optional.of(recipes.get(0));
  }

  @Override
  public void selectRecipe(Recipe<?> recipe) {
    this.selectedRecipe = recipe.getId();
    // update the station's cached recipe so the result recalculates and syncs to the client
    this.tile.updateRecipe((CraftingRecipe) recipe);
    this.tile.setChanged();
    // the client result display calculates from its own cached recipe, sync it to every listener
    for (ServerPlayer player : this.listeners) {
      this.tile.syncRecipe(player);
    }
    this.sendRecipesListToListeners(true);
  }

  @Override
  public Optional<? extends Recipe<?>> getSelectedRecipe() {
    ResourceLocation selected = this.selectedRecipe;
    if (selected == null || tile.getLevel() == null) {
      return Optional.empty();
    }
    return tile.getLevel().getRecipeManager().byKey(selected);
  }

  @Override
  public void setSelectedRecipe(Recipe<?> recipe) {
    this.selectedRecipe = recipe.getId();
  }

  @Override
  public SortedSet<IRecipePair> getRecipesList() {
    return this.recipesList;
  }

  @Override
  public void setRecipesList(SortedSet<IRecipePair> recipes) {
    this.recipesList = recipes;
  }

  @Override
  public boolean isEmpty(Container container) {
    // always claim non-empty so Polymorph registers listeners even when the grid is empty;
    // recipes can still appear once the player places items
    return false;
  }

  @Override
  public Set<ServerPlayer> getListeners() {
    return this.listeners;
  }

  @Override
  public void sendRecipesListToListeners(boolean force) {
    if (this.listeners.isEmpty()) {
      return;
    }
    IPolymorphPacketDistributor distributor = PolymorphApi.common().getPacketDistributor();
    for (ServerPlayer player : this.listeners) {
      distributor.sendRecipesListS2C(player, this.recipesList, this.selectedRecipe);
    }
  }

  @Override
  public Pair<SortedSet<IRecipePair>, ResourceLocation> getPacketData() {
    return Pair.of(this.recipesList, this.selectedRecipe);
  }

  @Override
  public CraftingStationBlockEntity getOwner() {
    return this.tile;
  }

  @Override
  public boolean isFailing() {
    return this.failing;
  }

  @Override
  public void setFailing(boolean failing) {
    this.failing = failing;
  }

  @Override
  public CompoundTag writeNBT() {
    CompoundTag tag = new CompoundTag();
    if (this.selectedRecipe != null) {
      tag.putString(TAG_SELECTED_RECIPE, this.selectedRecipe.toString());
    }
    return tag;
  }

  @Override
  public void readNBT(CompoundTag tag) {
    this.selectedRecipe = tag.contains(TAG_SELECTED_RECIPE) ? ResourceLocation.tryParse(tag.getString(TAG_SELECTED_RECIPE)) : null;
  }

  @Override
  public void tick() {
    Level level = tile.getLevel();
    if (level == null || level.isClientSide || this.listeners.isEmpty()) {
      return;
    }
    // detect input changes and refresh the candidates, like Polymorph's built-in recipe data;
    // widgets rebuilt after a resize also recover their list from the periodic push
    boolean changed = detectInputChange();
    if (changed) {
      this.recipesList = computePairs();
    }
    this.sendRecipesListToListeners(false);
  }

  /** Detects whether any of the station's input slots changed since the last tick. */
  private boolean detectInputChange() {
    boolean changed = false;
    int size = tile.getContainerSize();
    this.lastInput = validateList(this.lastInput, size);
    for (int i = 0; i < size; i++) {
      ItemStack stack = tile.getItem(i);
      Item item = stack.isEmpty() ? Items.AIR : stack.getItem();
      if (this.lastInput.get(i) != item) {
        changed = true;
      }
      this.lastInput.set(i, item);
    }
    return changed;
  }

  /** Recomputes the candidate list from all currently matching recipes. */
  private SortedSet<IRecipePair> computePairs() {
    SortedSet<IRecipePair> pairs = new TreeSet<>();
    Level level = tile.getLevel();
    if (level != null) {
      for (CraftingRecipe recipe : level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, tile.getCraftingInventory(), level)) {
        pairs.add(new RecipePair(recipe.getId(), recipe.getResultItem(level.registryAccess())));
      }
    }
    return pairs;
  }

  private static NonNullList<Item> validateList(NonNullList<Item> list, int size) {
    if (list.size() == size) {
      return list;
    }
    NonNullList<Item> resized = NonNullList.withSize(size, Items.AIR);
    for (int i = 0; i < Math.min(resized.size(), list.size()); i++) {
      resized.set(i, list.get(i));
    }
    return resized;
  }

  @Override
  public void addListener(ServerPlayer player) {
    this.listeners.add(player);
  }

  @Override
  public void removeListener(ServerPlayer player) {
    this.listeners.remove(player);
  }
}
