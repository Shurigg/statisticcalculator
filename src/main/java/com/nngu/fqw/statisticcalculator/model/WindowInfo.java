package com.nngu.fqw.statisticcalculator.model;

public class WindowInfo {

    private Boolean hasAttack;
    private Double minSigmaDeviation;

    public WindowInfo() {
    }

    public WindowInfo(Boolean hasAttack, Double minSigmaDeviation) {
        this.hasAttack = hasAttack;
        this.minSigmaDeviation = minSigmaDeviation;
    }

    public Boolean getHasAttack() {
        return hasAttack;
    }

    public void setHasAttack(Boolean hasAttack) {
        this.hasAttack = hasAttack;
    }

    public Double getMinSigmaDeviation() {
        return minSigmaDeviation;
    }

    public void setMinSigmaDeviation(Double minSigmaDeviation) {
        this.minSigmaDeviation = minSigmaDeviation;
    }

    @Override
    public String toString() {
        return "WindowInfo{" +
                "hasAttack=" + hasAttack +
                ", minDeviation=" + minSigmaDeviation +
                '}';
    }
}
