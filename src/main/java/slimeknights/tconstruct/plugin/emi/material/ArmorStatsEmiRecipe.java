package slimeknights.tconstruct.plugin.emi.material;

import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ArmorStatsEmiRecipe extends MaterialStatsEmiRecipe {
  public static final TagKey<Item> PART_TAG = TinkerTags.Items.ARMOR;
  private static final int LINE_HEIGHT = 11;
  private static final int STATS_BOTTOM_PADDING = 12;

  public ArmorStatsEmiRecipe(EMIConstants.TConstructEmiCategory category, IMaterial material,
                             List<MaterialStatsId> statIds, RecipeManager recipeManager) {
    super(category, material, statIds, PART_TAG, recipeManager);
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addInputWidgets(widgets);
    int header = addHeader(widgets);
    int traitsBottom = addArmorTraits(widgets, header);
    int statsTop = Math.max(header, traitsBottom) + 24;
    int linesPerPage = visibleLineCount(getDisplayHeight(), statsTop);
    List<List<ArmorLine>> pages = createPages(linesPerPage);
    PageManager manager = new PageManager(pages.size());

    addPageControls(widgets, manager, statsTop - 18);
    widgets.addDrawable(0, statsTop, getDisplayWidth(), getDisplayHeight() - statsTop,
        (graphics, x, y, delta) -> {
          List<ArmorLine> page = pages.get(manager.page());
          for (int index = 0; index < page.size(); index++) {
            ArmorLine line = page.get(index);
            graphics.drawString(Minecraft.getInstance().font, line.text(), 4, index * LINE_HEIGHT,
                line.header() ? getMaterialColor() : 0xFFFFFF, line.header());
          }
        });
    for (int index = 0; index < linesPerPage; index++) {
      int lineIndex = index;
      widgets.addTooltip((mouseX, mouseY) -> {
        List<ArmorLine> page = pages.get(manager.page());
        if (lineIndex >= page.size()) {
          return List.of();
        }
        return EmiRenderHelper.tooltip(page.get(lineIndex).tooltip());
      }, 4, statsTop + index * LINE_HEIGHT, getDisplayWidth() - 8, LINE_HEIGHT);
    }
  }

  private int addArmorTraits(WidgetHolder widgets, int line) {
    Set<ModifierId> seen = new HashSet<>();
    int y = line - 2;
    for (IMaterialStats stats : getPresentStats()) {
      for (ModifierEntry trait : getTraits(stats.getIdentifier())) {
        if (!seen.add(trait.getId())) {
          continue;
        }
        Component name = trait.getDisplayName();
        int width = Minecraft.getInstance().font.width(name);
        int x = getDisplayWidth() - width - 4;
        widgets.addText(name, x, y, getMaterialColor(), true);
        widgets.addTooltip(EmiRenderHelper.tooltip(trait.getModifier().getDescriptionList()), x, y, width, 10);
        y += 10;
      }
    }
    return Math.max(line, y);
  }

  private void addPageControls(WidgetHolder widgets, PageManager manager, int y) {
    if (manager.pageCount() <= 1) {
      return;
    }
    widgets.addButton(2, y, 12, 12, 0, 0, () -> true, (mouseX, mouseY, button) -> manager.scroll(-1));
    widgets.addButton(getDisplayWidth() - 14, y, 12, 12, 12, 0, () -> true,
        (mouseX, mouseY, button) -> manager.scroll(1));
    widgets.addDrawable(0, y, getDisplayWidth(), 16, (graphics, x, yOffset, delta) -> {
      Component page = TConstruct.makeTranslation("emi", "armor.page", manager.page() + 1, manager.pageCount());
      int pageWidth = Minecraft.getInstance().font.width(page);
      graphics.drawString(Minecraft.getInstance().font, page, (getDisplayWidth() - pageWidth) / 2, 1, 0xFFFFFF, false);

      int barLeft = 18;
      int barWidth = getDisplayWidth() - 36;
      graphics.fill(barLeft, 12, barLeft + barWidth, 14, 0x55555555);
      int thumbWidth = Math.max(barWidth / manager.pageCount(), 1);
      int thumbLeft = barLeft + barWidth * manager.page() / manager.pageCount();
      if (manager.page() == manager.pageCount() - 1) {
        thumbLeft = barLeft + barWidth - thumbWidth;
      }
      graphics.fill(thumbLeft, 12, thumbLeft + thumbWidth, 14, 0xFFFFFFFF);
    });
  }

  private List<List<ArmorLine>> createPages(int linesPerPage) {
    Map<String, List<ArmorLine>> groups = new LinkedHashMap<>();
    for (IMaterialStats stats : getPresentStats()) {
      ArmorLine line = new ArmorLine(compactSummary(stats), fullTooltip(stats), false);
      groups.computeIfAbsent(MaterialStatsEmiConstants.armorGroupKey(stats.getIdentifier()),
          ignored -> new ArrayList<>()).add(line);
    }

    List<List<ArmorLine>> pages = new ArrayList<>();
    for (Map.Entry<String, List<ArmorLine>> group : groups.entrySet()) {
      addGroupPages(pages, groupTitle(group.getKey()), group.getValue(), linesPerPage);
    }
    return List.copyOf(pages);
  }

  private static Component groupTitle(String group) {
    return TConstruct.makeTranslation("emi", "armor.group." + group)
        .withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE);
  }

  private static void addGroupPages(List<List<ArmorLine>> pages, Component title, List<ArmorLine> lines, int linesPerPage) {
    if (lines.isEmpty()) {
      return;
    }
    int pageSize = Math.max(1, linesPerPage - 1);
    int pageCount = pageCount(lines.size(), pageSize);
    for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
      int start = pageIndex * pageSize;
      List<ArmorLine> page = new ArrayList<>();
      page.add(new ArmorLine(title, List.of(), true));
      page.addAll(lines.subList(start, Math.min(start + pageSize, lines.size())));
      pages.add(List.copyOf(page));
    }
  }

  static int pageCount(int lineCount, int linesPerPage) {
    if (lineCount <= 0 || linesPerPage <= 0) {
      return 0;
    }
    return (lineCount - 1) / linesPerPage + 1;
  }

  static int visibleLineCount(int displayHeight, int statsTop) {
    return Math.max(2, (displayHeight - statsTop - STATS_BOTTOM_PADDING) / LINE_HEIGHT);
  }

  static Component compactSummary(IMaterialStats stats) {
    List<Component> info = stats.getLocalizedInfo();
    Component summary = info.isEmpty() ? Component.empty() : info.get(0);
    if (info.size() > 1) {
      summary = summary.copy().append(Component.literal(" ")).append(info.get(1));
    }
    return stats.getLocalizedName().plainCopy()
        .withStyle(ChatFormatting.UNDERLINE)
        .append(Component.literal(": "))
        .append(summary);
  }

  private static List<Component> fullTooltip(IMaterialStats stats) {
    List<Component> tooltip = new ArrayList<>();
    tooltip.add(stats.getLocalizedName().copy().withStyle(ChatFormatting.UNDERLINE));
    tooltip.addAll(stats.getLocalizedInfo());
    return tooltip;
  }

  private record ArmorLine(Component text, List<Component> tooltip, boolean header) {}

  static final class PageManager {
    private final int pageCount;
    private int page;

    PageManager(int pageCount) {
      this.pageCount = pageCount;
    }

    void scroll(int delta) {
      if (pageCount > 0) {
        page = Math.floorMod(page + delta, pageCount);
      }
    }

    int page() {
      return page;
    }

    int pageCount() {
      return pageCount;
    }
  }
}
