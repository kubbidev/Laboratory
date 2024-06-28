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
 * An immutable and serializable chunk region object
 */
@Getter
public final class ChunkRegion implements GsonSerializable {

    public static ChunkRegion deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("min"));
        Preconditions.checkArgument(object.has("max"));

        ChunkPosition a = ChunkPosition.deserialize(object.get("min"));
        ChunkPosition b = ChunkPosition.deserialize(object.get("max"));

        return of(a, b);
    }

    public static ChunkRegion of(ChunkPosition a, ChunkPosition b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");

        if (!a.getWorld().equals(b.getWorld())) {
            throw new IllegalArgumentException("positions are in different worlds");
        }

        return new ChunkRegion(a, b);
    }

    private final ChunkPosition min;
    private final ChunkPosition max;

    private final int width;
    private final int depth;

    private ChunkRegion(ChunkPosition a, ChunkPosition b) {
        this.min = ChunkPosition.of(Math.min(a.getX(), b.getX()), Math.min(a.getZ(), b.getZ()), a.getWorld());
        this.max = ChunkPosition.of(Math.max(a.getX(), b.getX()), Math.max(a.getZ(), b.getZ()), a.getWorld());

        this.width = this.max.getX() - this.min.getX();
        this.depth = this.max.getZ() - this.min.getZ();
    }

    public boolean inRegion(ChunkPosition pos) {
        Objects.requireNonNull(pos, "pos");
        return pos.getWorld().equals(this.min.getWorld()) && inRegion(pos.getX(), pos.getZ());
    }

    public boolean inRegion(int x, int z) {
        return x >= this.min.getX() && x <= this.max.getX()
                && z >= this.min.getZ() && z <= this.max.getZ();
    }

    @Override
    public @NotNull JElement serialize() {
        return new JObject()
                .add("min", this.min.serialize())
                .add("max", this.max.serialize());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ChunkRegion other)) {
            return false;
        }
        return this.getMin().equals(other.getMin()) && this.getMax().equals(other.getMax());
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getMin().hashCode();
        result = result * PRIME + this.getMax().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ChunkRegion(min=" + this.getMin() + ", max=" + this.getMax() + ")";
    }
}