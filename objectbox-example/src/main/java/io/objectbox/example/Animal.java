package io.objectbox.example;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class Animal {

    @Id(assignable = true) private long id;
    private String name;
    private boolean flying;
    private boolean swimming;
    private boolean walking;

    public Animal() {}

    public Animal(String name, boolean flying, boolean swimming, boolean walking) {
        this.name = name;
        this.flying = flying;
        this.swimming = swimming;
        this.walking = walking;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public boolean isSwimming() {
        return swimming;
    }

    public void setSwimming(boolean swimming) {
        this.swimming = swimming;
    }

    public boolean isWalking() {
        return walking;
    }

    public void setWalking(boolean walking) {
        this.walking = walking;
    }

    @Override
    public String toString() {
        return "\nname:" + getName() + ", isFlying:" + isFlying() + ", isSwimming:" + isSwimming() + ", isWalking:" + isWalking();
    }

    // an Animal belongs to one Zoo
    ToOne<Zoo> zoo;

}