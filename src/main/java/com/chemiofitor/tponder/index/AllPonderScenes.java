package com.chemiofitor.tponder.index;

import com.chemiofitor.tponder.scene.*;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

public class AllPonderScenes {
    public static void register(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        // Melter
        helper.forComponents(TinkerSmeltery.searedMelter.getId())
                .addStoryBoard("melter_building", MelterScene::building, AllPonderTags.SMELTERY)
                .addStoryBoard("melter_using", MelterScene::using, AllPonderTags.SMELTERY);

        // Alloyer
        helper.forComponents(TinkerSmeltery.scorchedAlloyer.getId())
                .addStoryBoard("alloyer_building", AlloyerScene::building, AllPonderTags.SMELTERY);

        // Heater
        helper.forComponents(TinkerSmeltery.searedHeater.getId())
                .addStoryBoard("heater_using", HeaterScene::using, AllPonderTags.SMELTERY);

        // Casting
        helper.forComponents(
                TinkerSmeltery.searedTable.getId(),
                TinkerSmeltery.searedBasin.getId(),
                TinkerSmeltery.scorchedTable.getId(),
                TinkerSmeltery.scorchedBasin.getId(),
                TinkerSmeltery.searedFaucet.getId(),
                TinkerSmeltery.scorchedFaucet.getId(),
                TinkerSmeltery.searedChannel.getId(),
                TinkerSmeltery.scorchedChannel.getId()
        ).addStoryBoard("casting", CastingScene::cast, AllPonderTags.SMELTERY);

        // Sand Casting
        helper.forComponents(
                TinkerSmeltery.blankSandCast.getId(),
                TinkerSmeltery.blankRedSandCast.getId()
        ).addStoryBoard("sand_casting", CastingScene::sand, AllPonderTags.SMELTERY);

        // Smeltery
        helper.forComponents(
                TinkerSmeltery.smelteryController.getId(),
                TinkerSmeltery.searedDrain.getId(),
                TinkerSmeltery.searedDuct.getId(),
                TinkerSmeltery.searedChute.getId()
        ).addStoryBoard("smeltery_building", SmelteryScene::building, AllPonderTags.SMELTERY)
                .addStoryBoard("smeltery_using", SmelteryScene::using, AllPonderTags.SMELTERY)
                .addStoryBoard("smeltery_mini", SmelteryScene::mini, AllPonderTags.SMELTERY);

        // Foundry
        helper.forComponents(
                TinkerSmeltery.foundryController.getId(),
                TinkerSmeltery.scorchedDrain.getId(),
                TinkerSmeltery.scorchedDuct.getId(),
                TinkerSmeltery.scorchedChute.getId()
        ).addStoryBoard("foundry_building", FoundryScene::building, AllPonderTags.SMELTERY);

        // Tanks
        helper.forComponents(
                TinkerSmeltery.searedCastingTank.getId(),
                TinkerSmeltery.scorchedProxyTank.getId()
        ).addStoryBoard("tank", TankScene::tank, AllPonderTags.SMELTERY);

        // Fluid Cannons
        helper.forComponents(
                TinkerSmeltery.searedFluidCannon.getId(),
                TinkerSmeltery.scorchedFluidCannon.getId()
        ).addStoryBoard("fluid_cannon", CannonScene::using, AllPonderTags.SMELTERY);
    }
}
