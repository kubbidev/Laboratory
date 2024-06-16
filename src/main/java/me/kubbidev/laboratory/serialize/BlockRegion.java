package me.kubbidev.laboratory.serialize;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.kubbidev.laboratory.util.gson.GsonSerializable;
import me.kubbidev.laboratory.util.gson.builder.JElement;
import me.kubbidev.laboratory.util.gson.builder.JObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An immutable and serializable block region object
 */
@Getter
public final class BlockRegion implements GsonSerializable {
    public static BlockRegion deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("min"));
        Preconditions.checkArgument(object.has("max"));

        BlockPosition a = BlockPosition.deserialize(object.get("min"));
        BlockPosition b = BlockPosition.deserialize(object.get("max"));

        return of(a, b);
    }

    public static BlockRegion of(BlockPosition a, BlockPosition b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");

        if (!a.getWorld().equals(b.getWorld())) {
            throw new IllegalArgumentException("positions are in different worlds");
        }

        return new BlockRegion(a, b);
    }

    private final BlockPosition min;
    private final BlockPosition max;

    private final int width;
    private final int height;
    private final int depth;

    private BlockRegion(BlockPosition a, BlockPosition b) {
        this.min = BlockPosition.of(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()), a.getWorld());
        this.max = BlockPosition.of(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()), a.getWorld());

        this.width = this.max.getX() - this.min.getX();
        this.height = this.max.getY() - this.min.getY();
        this.depth = this.max.getZ() - this.min.getZ();
    }

    public boolean inRegion(BlockPosition pos) {
        Objects.requireNonNull(pos, "pos");
        return pos.getWorld().equals(this.min.getWorld()) && inRegion(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean inRegion(int x, int y, int z) {
        return x >= this.min.getX() && x <= this.max.getX()
                && y >= this.min.getY() && y <= this.max.getY()
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
        if (!(o instanceof BlockRegion)) {
            return false;
        }
        BlockRegion other = (BlockRegion) o;
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
        return "BlockRegion(min=" + this.getMin() + ", max=" + this.getMax() + ")";
    }
}
