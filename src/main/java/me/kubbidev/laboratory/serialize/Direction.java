package me.kubbidev.laboratory.serialize;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.kubbidev.laboratory.util.gson.GsonSerializable;
import me.kubbidev.laboratory.util.gson.builder.JObject;
import org.jetbrains.annotations.NotNull;

/**
 * An immutable and serializable direction object
 */
@Getter
public final class Direction implements GsonSerializable {
    public static final Direction ZERO = Direction.of(0.0f, 0.0f);

    public static Direction deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("yaw"));
        Preconditions.checkArgument(object.has("pitch"));

        float yaw = object.get("yaw").getAsFloat();
        float pitch = object.get("pitch").getAsFloat();

        return of(yaw, pitch);
    }

    public static Direction of(float yaw, float pitch) {
        return new Direction(yaw, pitch);
    }

    private final float yaw;
    private final float pitch;

    private Direction(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public @NotNull JObject serialize() {
        return new JObject()
                .add("yaw", this.yaw)
                .add("pitch", this.pitch);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Direction)) {
            return false;
        }
        Direction other = (Direction) o;
        return Float.compare(this.getYaw(), other.getYaw()) == 0 &&
                Float.compare(this.getPitch(), other.getPitch()) == 0;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;

        result = result * PRIME + Float.floatToIntBits(this.getYaw());
        result = result * PRIME + Float.floatToIntBits(this.getPitch());

        return result;
    }

    @Override
    public String toString() {
        return "Direction(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
    }
}