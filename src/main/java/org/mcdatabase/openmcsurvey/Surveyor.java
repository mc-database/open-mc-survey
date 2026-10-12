package org.mcdatabase.openmcsurvey;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.DeviceInfo;
import com.mojang.renderpearl.api.device.GpuDevice;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.jspecify.annotations.Nullable;
import oshi.SystemInfo;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

public final class Surveyor {
    private static final long BYTES_PER_MB = 1024L * 1024L;

    private Surveyor() {}

    static Snapshot collect(boolean firstRequest) {
        FabricLoader loader = FabricLoader.getInstance();
        Minecraft client = Minecraft.getInstance();
        Options options = client.options;
        HardwareAbstractionLayer hal = new SystemInfo().getHardware();

        String mcVersion = SharedConstants.getCurrentVersion().name();
        String modVersion = modVersion(loader, OpenMCSurvey.MOD_ID);
        String environment = loader.getEnvironmentType().name().toLowerCase(Locale.ROOT);
        String loaderVersion = modVersion(loader, "fabricloader");
        String javaVersion = System.getProperty("java.version", String.valueOf(Runtime.version().feature()));
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        int cpuCores = Runtime.getRuntime().availableProcessors();
        String cpuModel = cpuModel(hal);
        Integer systemMemoryMB = systemMemoryMB(hal);
        String gpuModel = gpuModel(hal);
        Integer vramMB = vramMB(hal);
        String graphicsAPI = graphicsAPI();
        Integer memoryMB = (int) (Runtime.getRuntime().maxMemory() / BYTES_PER_MB);
        Integer renderDistance = options.renderDistance().get();
        Integer simulationDistance = options.simulationDistance().get();
        Boolean hasIris = loader.isModLoaded("iris");
        String resolutionBucket = resolutionBucket(client.getWindow());
        String locale = options.languageCode;
        Integer modCount = loader.getAllMods().size();

        return new Snapshot(
                Snapshot.SCHEMA,
                firstRequest,
                mcVersion,
                modVersion,
                environment,
                "fabric",
                loaderVersion,
                javaVersion,
                osName,
                osArch,
                cpuCores,
                cpuModel,
                gpuModel,
                memoryMB,
                systemMemoryMB,
                vramMB,
                graphicsAPI,
                renderDistance,
                simulationDistance,
                hasIris,
                resolutionBucket,
                locale,
                modCount);
    }

    private static @Nullable String modVersion(FabricLoader loader, String modId) {
        return loader.getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse(null);
    }

    private static @Nullable String stripToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.isEmpty() ? null : stripped;
    }

    private static @Nullable String cpuModel(HardwareAbstractionLayer hal) {
        if (hal.getProcessor() == null || hal.getProcessor().getProcessorIdentifier() == null) {
            return null;
        }
        return stripToNull(hal.getProcessor().getProcessorIdentifier().getName());
    }

    private static @Nullable Integer systemMemoryMB(HardwareAbstractionLayer hal) {
        long total = hal.getMemory().getTotal();
        return total <= 0 ? null : (int) (total / BYTES_PER_MB);
    }

    private static @Nullable String gpuModel(HardwareAbstractionLayer hal) {
        GpuDevice device = RenderSystem.tryGetDevice();
        if (device != null) {
            String name = stripToNull(device.getDeviceInfo().name());
            if (name != null) {
                return name;
            }
        }
        if (hal.getGraphicsCards() == null) {
            return null;
        }
        for (GraphicsCard card : hal.getGraphicsCards()) {
            if (card == null) {
                continue;
            }
            String name = stripToNull(card.getName());
            if (name != null && !name.equalsIgnoreCase("unknown")) {
                return name;
            }
        }
        return null;
    }

    private static @Nullable Integer vramMB(HardwareAbstractionLayer hal) {
        if (hal.getGraphicsCards() == null) {
            return null;
        }
        for (GraphicsCard card : hal.getGraphicsCards()) {
            if (card == null) {
                continue;
            }
            long vram = card.getVRam();
            if (vram > 0) {
                return (int) (vram / BYTES_PER_MB);
            }
        }
        return null;
    }

    private static @Nullable String graphicsAPI() {
        GpuDevice device = RenderSystem.tryGetDevice();
        if (device == null) {
            return null;
        }
        DeviceInfo info = device.getDeviceInfo();
        String backend = stripToNull(info.backendName());
        if (backend == null) {
            return null;
        }
        String driver = stripToNull(info.driverInfo());
        return driver == null ? backend : backend + " " + driver;
    }

    private static @Nullable String resolutionBucket(Window window) {
        if (window == null) {
            return null;
        }
        Monitor monitor = window.findBestMonitor();
        if (monitor == null) {
            return null;
        }
        return bucketHeight(monitor.currentMode().getHeight());
    }

    private static String bucketHeight(int height) {
        if (height <= 720) {
            return "720p-";
        }
        if (height <= 1080) {
            return "1080p-";
        }
        if (height <= 1440) {
            return "1440p-";
        }
        if (height <= 2160) {
            return "4k-";
        }
        return "+4k";
    }
}
