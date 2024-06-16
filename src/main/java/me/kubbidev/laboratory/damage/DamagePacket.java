package me.kubbidev.laboratory.damage;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class DamagePacket implements Cloneable {

    @Getter
    @Setter
    private DamageType[] types;

    @Getter
    private double value;
    private double additiveModifiers;
    private double multiplicativeModifiers = 1;

    @Getter
    @Setter
    private @Nullable Element element;

    public DamagePacket(double value, DamageType... types) {
        this(value, null, types);
    }

    public DamagePacket(double value, @Nullable Element element, DamageType... types) {
        this.value = value;
        this.types = types;
        this.element = element;
    }

    /**
     * Directly edits the damage packet value.
     *
     * @param value New damage value
     */
    public void setValue(double value) {
        Preconditions.checkArgument(value >= 0, "Value cannot be negative");
        this.value = value;
    }

    /**
     * Register a multiplicative damage modifier.
     * <p>
     * This is used for critical strikes which modifier should
     * NOT stack up with damage boosting statistics.
     *
     * @param coefficient Multiplicative coefficient. 1.5 will
     *                    increase final damage by 50%
     */
    public void multiplicativeModifier(double coefficient) {
        Preconditions.checkArgument(coefficient >= 0, "Coefficient cannot be negative");
        this.multiplicativeModifiers *= coefficient;
    }

    public void additiveModifier(double multiplier) {
        this.additiveModifiers += multiplier;
    }

    /**
     * @return Final value of the damage packet taking into account
     * all the damage modifiers that have been registered
     */
    public double getFinalValue() {
        // Make sure the returned value is positive
        return this.value * Math.max(0, 1 + this.additiveModifiers) * this.multiplicativeModifiers;
    }

    /**
     * @return Checks if the current packet has that damage type
     */
    public boolean hasType(DamageType type) {
        for (DamageType checked : this.types) {
            if (checked == type)
                return true;
        }
        return false;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public DamagePacket clone() {
        DamagePacket clone = new DamagePacket(this.value, this.types);
        clone.additiveModifiers = this.additiveModifiers;
        clone.multiplicativeModifiers = this.multiplicativeModifiers;
        clone.element = this.element;
        return clone;
    }

}