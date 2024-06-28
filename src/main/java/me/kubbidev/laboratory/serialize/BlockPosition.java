package me.kubbidev.laboratory.serialize;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.kubbidev.laboratory.util.gson.GsonSerializable;
import me.kubbidev.laboratory.util.gson.builder.JElement;
import me.kubbidev.laboratory.util.gson.builder.JObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An immutable and serializable block location object
 */
@Getter
public final class BlockPosition implements GsonSerializable {

    public static BlockPosition deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("x"));
        Preconditions.checkArgument(object.has("y"));
        Preconditions.checkArgument(object.has("z"));
        Preconditions.checkArgument(object.has("world"));

        int x = object.get("x").getAsInt();
        int y = object.get("y").getAsInt();
        int z = object.get("z").getAsInt();
        String world = object.get("world").getAsString();

        return of(x, y, z, world);
    }

    public static BlockPosition of(int x, int y, int z, String world) {
        Objects.requireNonNull(world, "world");
        return new BlockPosition(x, y, z, world);
    }

    private final int x;
    private final int y;
    private final int z;
    private final String world;

    private BlockPosition(int x, int y, int z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public Position toPosition() {
        return Position.of(this.x, this.y, this.z, this.world);
    }

    public Position toPositionCenter() {
        return Position.of(this.x + 0.5d, this.y + 0.5d, this.z + 0.5d, this.world);
    }

    public ChunkPosition toChunk() {
        return ChunkPosition.of(this.x >> 4, this.z >> 4, this.world);
    }

    public boolean contains(Position position) {
        return equals(position.floor());
    }

    public BlockPosition add(int x, int y, int z) {
        return BlockPosition.of(this.x + x, this.y + y, this.z + z, this.world);
    }

    public BlockPosition subtract(int x, int y, int z) {
        return add(-x, -y, -z);
    }

    public BlockRegion regionWith(BlockPosition other) {
        Objects.requireNonNull(other, "other");
        return BlockRegion.of(this, other);
    }

    @Override
    public @NotNull JElement serialize() {
        return new JObject()
                .add("x", this.x)
                .add("y", this.y)
                .add("z", this.z)
                .add("world", this.world);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BlockPosition other)) {
            return false;
        }
        return this.getX() == other.getX()
                && this.getY() == other.getY()
                && this.getZ() == other.getZ()
                && this.getWorld().equals(other.getWorld());
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getX();
        result = result * PRIME + this.getY();
        result = result * PRIME + this.getZ();
        result = result * PRIME + this.getWorld().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BlockPosition(x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ", world=" + this.getWorld() + ")";
    }
}
