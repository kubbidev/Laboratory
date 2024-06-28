package me.kubbidev.laboratory.serialize;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.kubbidev.laboratory.cache.Lazy;
import me.kubbidev.laboratory.util.gson.GsonSerializable;
import me.kubbidev.laboratory.util.gson.builder.JElement;
import me.kubbidev.laboratory.util.gson.builder.JObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An immutable and serializable chuck location object
 */
public final class ChunkPosition implements GsonSerializable {

    public static ChunkPosition deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("x"));
        Preconditions.checkArgument(object.has("z"));
        Preconditions.checkArgument(object.has("world"));

        int x = object.get("x").getAsInt();
        int z = object.get("z").getAsInt();
        String world = object.get("world").getAsString();

        return of(x, z, world);
    }

    public static ChunkPosition of(int x, int z, String world) {
        Objects.requireNonNull(world, "world");
        return new ChunkPosition(x, z, world);
    }

    public static ChunkPosition of(long encodedLong, String world) {
        Objects.requireNonNull(world, "world");
        return of((int) encodedLong, (int) (encodedLong >> 32), world);
    }

    @Getter
    private final int x;

    @Getter
    private final int z;

    @Getter
    private final String world;

    private final Lazy<Collection<BlockPosition>> blocks = Lazy.suppliedBy(() -> {
        List<BlockPosition> blocks = new ArrayList<>(16 * 16 * 256);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    blocks.add(getBlock(x, y, z));
                }
            }
        }
        return Collections.unmodifiableList(blocks);
    });

    private ChunkPosition(int x, int z, String world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    public boolean contains(BlockPosition block) {
        return equals(block.toChunk());
    }

    public boolean contains(Position position) {
        return equals(position.floor().toChunk());
    }

    public BlockPosition getBlock(int x, int y, int z) {
        return BlockPosition.of((this.x << 4) | (x & 0xF), y, (this.z << 4) | (z & 0xF), this.world);
    }

    public Collection<BlockPosition> blocks() {
        return this.blocks.get();
    }

    public ChunkPosition add(int x, int z) {
        return ChunkPosition.of(this.x + x, this.z + z, this.world);
    }

    public ChunkPosition subtract(int x, int z) {
        return add(-x, -z);
    }

    public long asEncodedLong() {
        return (long) this.x & 0xffffffffL | ((long) this.z & 0xffffffffL) << 32;
    }

    @Override
    public @NotNull JElement serialize() {
        return new JObject()
                .add("x", this.x)
                .add("z", this.z)
                .add("world", this.world);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ChunkPosition other)) {
            return false;
        }
        return this.getX() == other.getX() && this.getZ() == other.getZ() && this.getWorld().equals(other.getWorld());
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getX();
        result = result * PRIME + this.getZ();
        result = result * PRIME + this.getWorld().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ChunkPosition(x=" + this.getX() + ", z=" + this.getZ() + ", world=" + this.getWorld() + ")";
    }
}