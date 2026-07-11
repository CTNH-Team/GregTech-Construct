package slimeknights.tconstruct.library.modifiers.modules.carriage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public enum CarriageLoadable implements Loadable<Carriage> {
    INSTANCE;

    private static final Map<String, Loadable<? extends Carriage>> REGISTRY = new HashMap<>();

    public static void register(String type, Loadable<? extends Carriage> loader) {
        REGISTRY.put(type, loader);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Carriage convert(@Nonnull JsonElement json, @Nonnull String key, @Nonnull TypedMap context) {
        JsonObject obj = GsonHelper.convertToJsonObject(json, key);
        String type = GsonHelper.getAsString(obj, "type", null);
        if (type == null) return Carriage.EMPTY;
        var loader = (Loadable<Carriage>) REGISTRY.get(type);
        if (loader == null) return Carriage.EMPTY;
        return loader.convert(json, key, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonElement serialize(@Nonnull Carriage value) {
        var loader = (Loadable<Carriage>) REGISTRY.get(value.type());
        if (loader == null) return null;
        JsonObject obj = loader.serialize(value).getAsJsonObject();
        obj.addProperty("type", value.type());
        return obj;
    }

    @Override
    public Carriage decode(@Nonnull FriendlyByteBuf buffer, @Nonnull TypedMap context) {
        throw new UnsupportedOperationException("Carriage network decode not supported");
    }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull Carriage value) {
        throw new UnsupportedOperationException("Carriage network encode not supported");
    }
}
