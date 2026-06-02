package me.RedEagle3.rankProgression.Models;

public class RankMilestone {

    private final String rankName;
    private final long requiredMillis;
    private final String reward;
    private final int index;
    private final String icon;
    private final String color;

    public RankMilestone(String rankName, long requiredMillis, String reward, int index, String icon, String color) {
        this.rankName = rankName;
        this.requiredMillis = requiredMillis;
        this.reward = reward;
        this.index = index;
        this.icon = icon;
        this. color = color;
    }

    // Getters:
    public String getRankName() {return rankName;}
    public long getRequiredMillis() {return requiredMillis;}
    public String getReward() {return reward;}
    public int getIndex() {return index;}
    public String getIcon() { return icon; }
    public String getColor() { return color; }
}