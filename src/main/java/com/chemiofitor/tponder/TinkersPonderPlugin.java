package com.chemiofitor.tponder;

import com.chemiofitor.tponder.index.AllPonderScenes;
import com.chemiofitor.tponder.index.AllPonderTags;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TinkersPonderPlugin implements PonderPlugin {
    @Override
    public @NotNull String getModId() {
        return TinkersPonder.MOD_ID;
    }

    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        AllPonderScenes.register(helper);
    }

    @Override
    public void registerTags(@NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
        AllPonderTags.register(helper);
        AllPonderTags.add(helper);
    }
}
