package org.mcdatabase.openmcsurvey;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.NonNull;

public record Snapshot(
        @SerializedName("schema") Integer schema,
        @SerializedName("first_request") @NonNull Boolean firstRequest,
        @SerializedName("mc_version") String mcVersion,
        @SerializedName("mod_version") String modVersion,
        @SerializedName("environment") String environment,
        @SerializedName("loader") String loader,
        @SerializedName("loader_version") String loaderVersion,
        @SerializedName("java_version") String javaVersion,
        @SerializedName("os_name") String osName,
        @SerializedName("os_arch") String osArch,
        @SerializedName("cpu_cores") Integer cpuCores,
        @SerializedName("cpu_model") String cpuModel,
        @SerializedName("gpu_model") String gpuModel,
        @SerializedName("memory_mb") Integer memoryMB,
        @SerializedName("system_memory_mb") Integer systemMemoryMB,
        @SerializedName("vram_mb") Integer vramMB,
        @SerializedName("graphics_api") String graphicsAPI,
        @SerializedName("render_distance") Integer renderDistance,
        @SerializedName("simulation_distance") Integer simulationDistance,
        @SerializedName("has_iris") Boolean hasIris,
        @SerializedName("resolution_bucket") String resolutionBucket,
        @SerializedName("locale") String locale,
        @SerializedName("mod_count") Integer modCount
) {
    public static final int SCHEMA = 1;

    private static final Gson GSON = new Gson();

    public Snapshot {
        if (schema == null || schema != SCHEMA) {
            throw new IllegalArgumentException("Unsupported snapshot schema: " + schema);
        }
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
