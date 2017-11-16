package com.riskitbiskit.bakingcompanion.Main;

public class Recipe {
    private int id;
    private String name;
    private String image;

    public Recipe(int id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }
}