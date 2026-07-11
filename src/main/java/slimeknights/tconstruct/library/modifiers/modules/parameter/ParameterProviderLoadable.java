package slimeknights.tconstruct.library.modifiers.modules.parameter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public enum ParameterProviderLoadable implements Loadable<ParameterProvider> {
    INSTANCE;

    private static final Map<String, Loadable<? extends ParameterProvider>> REGISTRY = new HashMap<>();

    public static void register(String type, Loadable<? extends ParameterProvider> loader) {
        REGISTRY.put(type, loader);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParameterProvider convert(@Nonnull JsonElement json, @Nonnull String key, @Nonnull TypedMap context) {
        JsonObject obj = GsonHelper.convertToJsonObject(json, key);
        String type = GsonHelper.getAsString(obj, "type", null);
        if (type == null) return ParameterProvider.EMPTY;
        var loader = (Loadable<ParameterProvider>) REGISTRY.get(type);
        if (loader == null) return ParameterProvider.EMPTY;
        return loader.convert(json, key, context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonElement serialize(@Nonnull ParameterProvider provider) {
        var loader = (Loadable<ParameterProvider>) REGISTRY.get(provider.type());
        if (loader == null) return null;
        JsonObject obj = loader.serialize(provider).getAsJsonObject();
        obj.addProperty("type", provider.type());
        return obj;
    }

    @Override
    public ParameterProvider decode(@Nonnull FriendlyByteBuf buffer, @Nonnull TypedMap context) {
        throw new UnsupportedOperationException("ParameterProvider network decode not supported");
    }

    @Override
    public void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull ParameterProvider value) {
        throw new UnsupportedOperationException("ParameterProvider network encode not supported");
    }
}
