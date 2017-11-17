package com.riskitbiskit.bakingcompanion.Details;

public class Ingredient {
    private double quantity;
    private String measurement;
    private String ingredient;

    public Ingredient(double quantity, String measurement, String ingredient) {
        this.quantity = quantity;
        this.measurement = measurement;
        this.ingredient = ingredient;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getMeasurement() {
        return measurement;
    }

    public String getIngredient() {
        return ingredient;
    }
}