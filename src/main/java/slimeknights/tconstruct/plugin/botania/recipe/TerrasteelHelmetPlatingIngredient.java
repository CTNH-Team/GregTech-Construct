package slimeknights.tconstruct.plugin.botania.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.plugin.botania.modifier.AncientWillModifier;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialIds;
import slimeknights.tconstruct.tools.TinkerTools;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class TerrasteelHelmetPlatingIngredient extends AbstractIngredient {
    private static boolean registered = false;

    protected TerrasteelHelmetPlatingIngredient() {
        super(Stream.of(Value.INSTANCE));
    }

    public static synchronized void register() {
        if (!registered) {
            CraftingHelper.register(Serializer.ID, Serializer.INSTANCE);
            registered = true;
        }
    }

    public static TerrasteelHelmetPlatingIngredient of() {
        return new TerrasteelHelmetPlatingIngredient();
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(TinkerTags.Items.MODIFIABLE)
                && stack.getItem() == TinkerTools.plateArmor.get(ArmorItem.Type.HELMET).asItem()
                && AncientWillModifier.hasTerrasteelHelmetPlating(ToolStack.from(stack));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Serializer.ID.toString());
        return json;
    }

    private enum Value implements Ingredient.Value {
        INSTANCE;

        @Override
        public Collection<ItemStack> getItems() {
            IModifiable helmet = TinkerTools.plateArmor.get(ArmorItem.Type.HELMET);
            ItemStack stack = ToolBuildHandler.buildItemFromMaterials(
                    helmet,
                    MaterialNBT.EMPTY.replaceMaterial(0, MaterialVariant.of(BotaniaMaterialIds.terraSteel, "")));
            if (stack.isEmpty()) {
                stack = new ItemStack(Blocks.BARRIER).setHoverName(Component.literal("Empty Terrasteel Helmet Plating"));
            }
            return List.of(stack);
        }

        @Override
        public JsonObject serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("id", Serializer.ID.toString());
            return json;
        }
    }

    public enum Serializer implements IIngredientSerializer<TerrasteelHelmetPlatingIngredient> {
        INSTANCE;

        public static final ResourceLocation ID = TConstruct.getResource("botania_terrasteel_helmet_plating");

        @Override
        public TerrasteelHelmetPlatingIngredient parse(JsonObject json) {
            return TerrasteelHelmetPlatingIngredient.of();
        }

        @Override
        public TerrasteelHelmetPlatingIngredient parse(FriendlyByteBuf buffer) {
            return TerrasteelHelmetPlatingIngredient.of();
        }

        @Override
        public void write(FriendlyByteBuf buffer, TerrasteelHelmetPlatingIngredient ingredient) {}
    }
}
