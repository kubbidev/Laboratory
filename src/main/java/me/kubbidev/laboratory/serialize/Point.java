package me.kubbidev.laboratory.serialize;

import com.google.gson.JsonElement;
import lombok.Getter;
import me.kubbidev.laboratory.util.gson.GsonSerializable;
import me.kubbidev.laboratory.util.gson.builder.JElement;
import me.kubbidev.laboratory.util.gson.builder.JObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An immutable and serializable position + direction object
 */
@Getter
public final class Point implements GsonSerializable {

    public static Point deserialize(JsonElement element) {
        Position position = Position.deserialize(element);
        Direction direction = Direction.deserialize(element);

        return of(position, direction);
    }

    public static Point of(Position position, Direction direction) {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(direction, "direction");
        return new Point(position, direction);
    }

    private final Position position;
    private final Direction direction;

    private Point(Position position, Direction direction) {
        this.position = position;
        this.direction = direction;
    }

    public Point add(double x, double y, double z) {
        return this.position.add(x, y, z).withDirection(this.direction);
    }

    public Point subtract(double x, double y, double z) {
        return this.position.subtract(x, y, z).withDirection(this.direction);
    }

    @Override
    public @NotNull JElement serialize() {
        return new JObject()
                .consume(o -> {
                    Consumer<JObject> consumer = object -> {
                        for (Map.Entry<String, JsonElement> e : object.toJson().entrySet()) {
                            if (e == null || e.getKey() == null) {
                                continue;
                            }
                            o.add(e.getKey(), e.getValue());
                        }
                    };
                    consumer.accept(this.position.serialize());
                    consumer.accept(this.direction.serialize());
                });
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Point other)) {
            return false;
        }
        return this.getPosition().equals(other.getPosition()) && this.getDirection().equals(other.getDirection());
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;

        result = result * PRIME + this.getPosition().hashCode();
        result = result * PRIME + this.getDirection().hashCode();

        return result;
    }

    @Override
    public String toString() {
        return "Point(position=" + this.getPosition() + ", direction=" + this.getDirection() + ")";
    }
}