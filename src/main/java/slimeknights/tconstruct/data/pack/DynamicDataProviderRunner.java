package slimeknights.tconstruct.data.pack;

import lombok.extern.log4j.Log4j2;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.List;
import java.util.function.Function;

@Log4j2
public class DynamicDataProviderRunner {
    private DynamicDataProviderRunner() {}

    @SafeVarargs
    public static void run(String owner, Function<PackOutput, ? extends DataProvider>... providers) {
        run(owner, List.of(providers));
    }

    public static void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers) {
        DynamicServerDataRunner.run(
            log,
            owner,
            "dynamic datagen provider",
            "dynamic data resources",
            providers,
            TiCDynamicDataPack::addData
        );
    }
}
