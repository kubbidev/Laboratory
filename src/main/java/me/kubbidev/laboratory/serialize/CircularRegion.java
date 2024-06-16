package me.kubbidev.laboratory.serialize;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.kubbidev.laboratory.util.FastMath;
import me.kubbidev.laboratory.util.gson.GsonSerializable;
import me.kubbidev.laboratory.util.gson.builder.JElement;
import me.kubbidev.laboratory.util.gson.builder.JObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
public final class CircularRegion implements GsonSerializable {

    public static CircularRegion deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("center"));
        Preconditions.checkArgument(object.has("radius"));

        Position center = Position.deserialize(object.get("center"));
        double radius = object.get("radius").getAsDouble();

        return of(center, radius);
    }

    public static CircularRegion of(Position center, double radius) {
        Objects.requireNonNull(center, "center");
        if (radius <= 0) {
            throw new IllegalArgumentException("radius cannot be negative");
        }
        return new CircularRegion(center, radius);
    }

    private final Position center;

    private final double radius;
    private final double diameter;

    private CircularRegion(Position center, double radius) {
        this.center = center;
        this.radius = radius;
        this.diameter = radius * 2;
    }

    /**
     * Determines if the specified {@link Position} is within the region
     * @param pos target position
     * @return true if the position is in the region
     */
    public boolean inRegion(Position pos) {
        Objects.requireNonNull(pos, "pos");
        return pos.distanceSquared(this.center) < this.radius * this.radius;
    }

    /**
     * The circumference of the region
     * @return the circumference
     */
    public double getCircumference() {
        return 2 * Math.PI * this.radius;
    }

    /**
     * Get the circumference {@link BlockPosition} of the region
     * @return the {@link BlockPosition}s
     */
    public @NotNull Set<BlockPosition> getOuterBlockPositions() {
        Set<BlockPosition> positions = new HashSet<>((int) getCircumference());
        for (int degree = 0; degree < 360; degree++) {
            float radian = FastMath.toRadians(degree);

            double x = FastMath.cos(radian) * this.radius;
            double z = FastMath.sin(radian) * this.radius;

            positions.add(this.center.add((int) x, 0, (int) z).floor());
        }
        return Collections.unmodifiableSet(positions);
    }

    @Override
    public @NotNull JElement serialize() {
        return new JObject()
                .add("center", this.center.serialize())
                .add("radius", this.radius);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CircularRegion)) {
            return false;
        }
        CircularRegion other = (CircularRegion) o;
        return Double.compare(other.radius, this.radius) == 0 &&
                center.equals(other.center);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.center, this.radius);
    }

    @Override
    public String toString() {
        return "CircularRegion(center=" + this.getCenter() + ", radius=" + this.getRadius() + ")";
    }
}