package com.chemiofitor.tponder;

import com.chemiofitor.tponder.scene.PonderConstants;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TinkersPonder.MOD_ID)
public class TinkersPonder {
    public static final String MOD_ID = "tponder";
    public static final String NAME = "Tinkers' Ponder";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public TinkersPonder() {
        final FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        final IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::client);
        modEventBus.addListener(this::gatherData);

        // PonderIndex 的静态初始化会加载 ClientLevel，在专用服务器上触及它会被
        // RuntimeDistCleaner 拒绝并导致 mod 构造失败。Ponder 场景本身只用于客户端，
        // 所以和 Create 一样把注册限制在 CLIENT 端。
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> PonderIndex.addPlugin(new TinkersPonderPlugin()));

        LOGGER.info("Tinker's Ponder has loaded!");
    }

    public void client(FMLClientSetupEvent event) {
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        gen.addProvider(true, new LanguageProvider(gen.getPackOutput(), TinkersPonder.MOD_ID, "en_us") {
            @Override
            protected void addTranslations() {
                PonderIndex.getLangAccess().provideLang(TinkersPonder.MOD_ID, this::add);
            }
        });
    }

    public static void init5x5(SceneBuilder builder, SceneBuildingUtil util) {
        builder.configureBasePlate(0, 0, 5);
        builder.scaleSceneView(PonderConstants.SCALE_5x5);
        builder.world().showSection(util.select().layer(0), Direction.UP);
    }

    public static void init7x7(SceneBuilder builder, SceneBuildingUtil util) {
        builder.configureBasePlate(0, 0, 7);
        builder.scaleSceneView(PonderConstants.SCALE_7x7);
        builder.world().showSection(util.select().layer(0), Direction.UP);
    }

    public static void init9x9(SceneBuilder builder, SceneBuildingUtil util) {
        builder.configureBasePlate(0, 0, 9);
        builder.scaleSceneView(PonderConstants.SCALE_9x9);
        builder.world().showSection(util.select().layer(0), Direction.UP);
    }

    public static void rotateAround(SceneBuilder builder, int duration, int angle) {
        int steps = 360 / angle;
        int stepDuration = duration / steps;

        for (int i = 0; i < steps; i++) {
            rotate(builder, stepDuration, angle);
        }
    }

    public static void rotate(SceneBuilder builder, int time, int angle) {
        builder.rotateCameraY(angle);
        builder.idle(time);
    }
}
