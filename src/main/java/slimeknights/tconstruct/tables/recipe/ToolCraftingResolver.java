package slimeknights.tconstruct.tables.recipe;

import com.gregtechceu.gtceu.api.item.CustomToolIngredientHelper;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.recipe.ingredient.ToolIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.mixin.gtceu.ToolIngredientAccess;
import slimeknights.tconstruct.tables.block.entity.inventory.CraftingContainerWrapper;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Resolves GT ToolIngredient recipes against the station's dedicated tool slots. */
public final class ToolCraftingResolver {
  private ToolCraftingResolver() {}
  private static final Map<net.minecraft.world.item.crafting.RecipeManager, List<CraftingRecipe>> TOOL_RECIPE_CACHE = new WeakHashMap<>();

  public record Match(CraftingRecipe recipe, int[] toolSlots, int[] toolCells, CraftingContainerWrapper inventory) {}

  /** Finds tool recipes matching the station grid without mutating the real grid. */
  public static List<Match> findMatches(CraftingStationBlockEntity station, Level level) {
    List<Match> matches = new ArrayList<>();
    List<CraftingRecipe> recipes;
    synchronized (TOOL_RECIPE_CACHE) {
      recipes = TOOL_RECIPE_CACHE.computeIfAbsent(level.getRecipeManager(), manager ->
        manager.getAllRecipesFor(RecipeType.CRAFTING).stream()
          .filter(recipe -> containsToolIngredient(recipe.getIngredients()))
          .toList());
    }
    for (CraftingRecipe recipe : recipes) {
      Match match;
      if (recipe instanceof ShapedRecipe shaped) {
        match = resolveShaped(station, level, recipe, shaped);
      } else if (recipe instanceof ShapelessRecipe) {
        match = resolveShapeless(station, level, recipe);
      } else {
        continue;
      }
      if (match != null) {
        matches.add(match);
      }
    }
    return matches;
  }

  @Nullable
  private static Match resolveShapeless(CraftingStationBlockEntity station, Level level, CraftingRecipe recipe) {
    List<ToolIngredient> tools = new ArrayList<>();
    List<Ingredient> normal = new ArrayList<>();
    for (Ingredient ingredient : recipe.getIngredients()) {
      if (ingredient instanceof ToolIngredient tool) tools.add(tool);
      else normal.add(ingredient);
    }
    int[] normalCells = assignNormalInputs(station, normal, 0, new int[9], new int[normal.size()]);
    if (normalCells == null) return null;
    boolean[] usedNormalCells = new boolean[9];
    for (int cell : normalCells) usedNormalCells[cell] = true;
    // A shapeless recipe must account for every non-empty grid cell. The previous
    // virtual container only copied assigned inputs, which accidentally hid unrelated
    // items from Recipe.matches() and then consumed them during takeResult().
    for (int cell = 0; cell < 9; cell++) {
      if (!station.getItem(cell).isEmpty() && !usedNormalCells[cell]) return null;
    }
    int[] toolSlots = assignTools(station, tools, 0, 0, new int[tools.size()]);
    if (toolSlots == null) return null;
    ItemStack[] virtual = new ItemStack[9];
    for (int cell = 0; cell < 9; cell++) virtual[cell] = station.getItem(cell).copy();
    List<Integer> toolCells = new ArrayList<>();
    for (int cell = 0; cell < 9 && toolCells.size() < tools.size(); cell++) {
      if (virtual[cell].isEmpty() && station.getItem(cell).isEmpty()) {
        toolCells.add(cell);
      }
    }
    if (toolCells.size() != tools.size()) return null;
    for (int i = 0; i < tools.size(); i++) virtual[toolCells.get(i)] = station.getToolStack(toolSlots[i]).copy();
    CraftingContainerWrapper wrapper = new CraftingContainerWrapper(new SimpleArrayContainer(virtual), 3, 3);
    return recipe.matches(wrapper, level)
      ? new Match(recipe, toolSlots, toolCells.stream().mapToInt(Integer::intValue).toArray(), wrapper) : null;
  }

  @Nullable
  private static int[] assignNormalInputs(CraftingStationBlockEntity station, List<Ingredient> requirements,
                                           int requirement, int[] usedCounts, int[] result) {
    if (requirement == requirements.size()) return result.clone();
    Ingredient ingredient = requirements.get(requirement);
    for (int cell = 0; cell < 9; cell++) {
      ItemStack stack = station.getItem(cell);
      if (usedCounts[cell] >= stack.getCount()) continue;
      if (!stack.isEmpty() && ingredient.test(stack.copy())) {
        result[requirement] = cell;
        usedCounts[cell]++;
        int[] assigned = assignNormalInputs(station, requirements, requirement + 1, usedCounts, result);
        usedCounts[cell]--;
        if (assigned != null) return assigned;
      }
    }
    return null;
  }

  public static boolean isStillValid(CraftingStationBlockEntity station, Match match) {
    if (station.getLevel() == null || !match.recipe().matches(match.inventory(), station.getLevel())) {
      return false;
    }
    for (int i = 0; i < match.toolSlots().length; i++) {
      ItemStack stack = station.getToolStack(match.toolSlots()[i]);
      if (stack.isEmpty() || ToolDamageUtil.isBroken(stack)) return false;
      Ingredient ingredient = match.recipe().getIngredients().stream()
        .filter(value -> value instanceof ToolIngredient).skip(i).findFirst().orElse(Ingredient.EMPTY);
      if (!ingredient.test(stack.copy())) return false;
    }
    return true;
  }

  private static boolean containsToolIngredient(List<Ingredient> ingredients) {
    return ingredients.stream().anyMatch(ingredient -> ingredient instanceof ToolIngredient);
  }

  @Nullable
  private static Match resolveShaped(CraftingStationBlockEntity station, Level level,
                                     CraftingRecipe recipe, ShapedRecipe shaped) {
    int width = shaped.getWidth();
    int height = shaped.getHeight();
    NonNullList<Ingredient> ingredients = recipe.getIngredients();
    for (boolean mirrored : new boolean[] {false, true}) {
      for (int offsetY = 0; offsetY <= 3 - height; offsetY++) {
        for (int offsetX = 0; offsetX <= 3 - width; offsetX++) {
          ItemStack[] virtual = new ItemStack[9];
          List<ToolIngredient> tools = new ArrayList<>();
          List<Integer> toolCells = new ArrayList<>();
          boolean valid = true;
          for (int y = 0; y < 3 && valid; y++) {
            for (int x = 0; x < 3; x++) {
              int index = x + y * 3;
              Ingredient ingredient = Ingredient.EMPTY;
              if (x >= offsetX && x < offsetX + width && y >= offsetY && y < offsetY + height) {
                int sourceX = mirrored ? width - 1 - (x - offsetX) : x - offsetX;
                ingredient = ingredients.get(sourceX + (y - offsetY) * width);
              }
              ItemStack input = station.getItem(index);
              if (ingredient instanceof ToolIngredient tool) {
                tools.add(tool);
                toolCells.add(index);
                virtual[index] = ItemStack.EMPTY;
              } else {
                if (!ingredient.test(input)) {
                  valid = false;
                  break;
                }
                virtual[index] = input.copy();
              }
            }
          }
          if (valid) {
            int[] assignment = assignTools(station, tools, 0, 0, new int[tools.size()]);
            if (assignment != null) {
              for (int i = 0; i < tools.size(); i++) {
                virtual[toolCells.get(i)] =
                  station.getToolStack(assignment[i]).copy();
              }
              Container backing = new SimpleArrayContainer(virtual);
              CraftingContainerWrapper wrapper = new CraftingContainerWrapper(backing, 3, 3);
              if (recipe.matches(wrapper, level)) {
                return new Match(recipe, assignment, toolCells.stream().mapToInt(Integer::intValue).toArray(), wrapper);
              }
            }
          }
        }
      }
    }
    return null;
  }

  @Nullable
  private static int[] assignTools(CraftingStationBlockEntity station, List<ToolIngredient> requirements,
                                   int requirement, int usedMask, int[] result) {
    if (requirement == requirements.size()) {
      return result.clone();
    }
    ToolIngredient ingredient = requirements.get(requirement);
    for (int slot = 0; slot < CraftingStationBlockEntity.TOOL_SLOT_COUNT; slot++) {
      if ((usedMask & (1 << slot)) != 0) continue;
      ItemStack stack = station.getToolStack(slot);
      if (stack.isEmpty() || ToolDamageUtil.isBroken(stack)) continue;
      if (ingredient.test(stack.copy())) {
        result[requirement] = slot;
        int[] assigned = assignTools(station, requirements, requirement + 1, usedMask | (1 << slot), result);
        if (assigned != null) return assigned;
      }
    }
    return null;
  }

  /** Applies one craft's tool damage after the normal grid inputs are consumed. */
  public static void damageTools(CraftingStationBlockEntity station, Match match,
                                 @Nullable LivingEntity user) {
    List<Ingredient> ingredients = match.recipe().getIngredients();
    int toolIndex = 0;
    for (Ingredient ingredient : ingredients) {
      if (ingredient instanceof ToolIngredient toolIngredient) {
        int slot = match.toolSlots()[toolIndex++];
        ItemStack stack = station.getToolStack(slot);
        if (stack.isEmpty()) continue;
        var type = ((ToolIngredientAccess) toolIngredient).tconstruct$getToolType();
        damageToolStack(station, slot, stack, type, user);
      }
    }
  }

  static void damageToolStack(CraftingStationBlockEntity station, int slot, ItemStack stack, GTToolType type,
                              @Nullable LivingEntity user) {
    ItemStack damaged = stack.copy();
    LivingEntity damageUser = user instanceof Player player && player.isCreative() ? null : user;
    if (!CustomToolIngredientHelper.damageTool(damaged, type, damageUser, 1)) {
      ToolHelper.damageItemWhenCrafting(damaged, damageUser);
    }
    if (damaged.getItem() instanceof IGTTool tool && user instanceof Player player) {
      tool.playCraftingSound(player, damaged);
    }
    station.setItem(CraftingStationBlockEntity.TOOL_SLOT_START + slot, damaged);
  }

  /** Minimal mutable container used only for a candidate recipe match. */
  private static final class SimpleArrayContainer implements Container {
    private final ItemStack[] stacks;
    private SimpleArrayContainer(ItemStack[] stacks) { this.stacks = stacks; }
    public int getContainerSize() { return stacks.length; }
    public boolean isEmpty() { for (ItemStack stack : stacks) if (!stack.isEmpty()) return false; return true; }
    public ItemStack getItem(int index) { return stacks[index]; }
    public ItemStack removeItem(int index, int count) { return ItemStack.EMPTY; }
    public ItemStack removeItemNoUpdate(int index) { return ItemStack.EMPTY; }
    public void setItem(int index, ItemStack stack) { stacks[index] = stack; }
    public void setChanged() {}
    public boolean stillValid(net.minecraft.world.entity.player.Player player) { return true; }
    public void clearContent() { java.util.Arrays.fill(stacks, ItemStack.EMPTY); }
  }
}
