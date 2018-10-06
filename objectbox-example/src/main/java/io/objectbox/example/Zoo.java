package io.objectbox.example;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class Zoo {

    @Id
    private long id;
    private String name;

    public Zoo() {}

    public Zoo(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "\nname:" + getName();
    }

    // A Zoo can have many Animals
    @Backlink
    ToMany<Animal> animals;

}