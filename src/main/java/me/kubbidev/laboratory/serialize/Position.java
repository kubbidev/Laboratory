package me.kubbidev.laboratory.serialize;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.kubbidev.laboratory.util.gson.GsonSerializable;
import me.kubbidev.laboratory.util.gson.builder.JObject;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An immutable and serializable location object
 */
@Getter
public final class Position implements GsonSerializable {

    public static Position deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("x"));
        Preconditions.checkArgument(object.has("y"));
        Preconditions.checkArgument(object.has("z"));
        Preconditions.checkArgument(object.has("world"));

        double x = object.get("x").getAsDouble();
        double y = object.get("y").getAsDouble();
        double z = object.get("z").getAsDouble();
        String world = object.get("world").getAsString();

        return of(x, y, z, world);
    }

    public static Position of(double x, double y, double z, String world) {
        Objects.requireNonNull(world, "world");
        return new Position(x, y, z, world);
    }

    private final double x;
    private final double y;
    private final double z;
    private final String world;

    private Position(double x, double y, double z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public BlockPosition floor() {
        return BlockPosition.of(bukkitFloor(this.x), bukkitFloor(this.y), bukkitFloor(this.z), this.world);
    }

    public Position add(double x, double y, double z) {
        return Position.of(this.x + x, this.y + y, this.z + z, this.world);
    }

    public Position subtract(double x, double y, double z) {
        return add(-x, -y, -z);
    }

    public double distanceSquared(Position pos) {
        double dx = this.x - pos.x;
        double dy = this.y - pos.y;
        double dz = this.z - pos.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public Region regionWith(Position other) {
        Objects.requireNonNull(other, "other");
        return Region.of(this, other);
    }

    public Point withDirection(Direction direction) {
        return Point.of(this, direction);
    }

    @Override
    public @NotNull JObject serialize() {
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
        if (!(o instanceof Position)) {
            return false;
        }
        Position other = (Position) o;
        return Double.compare(this.getX(), other.getX()) == 0 &&
                Double.compare(this.getY(), other.getY()) == 0 &&
                Double.compare(this.getZ(), other.getZ()) == 0 &&
                this.getWorld().equals(other.getWorld());
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;

        final long x = Double.doubleToLongBits(this.getX());
        final long y = Double.doubleToLongBits(this.getY());
        final long z = Double.doubleToLongBits(this.getZ());

        result = result * PRIME + Long.hashCode(x);
        result = result * PRIME + Long.hashCode(y);
        result = result * PRIME + Long.hashCode(z);
        result = result * PRIME + this.getWorld().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Position(x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ", world=" + this.getWorld() + ")";
    }

    private static int bukkitFloor(double num) {
        int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }
}