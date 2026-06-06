package me.RedEagle3.rankProgressionProxy.Models;

import java.util.List;

public class RankData {

    private final String rankName;
    private final int index;
    private final long requiredMinutes;
    private final List<String> rewards;
    private final String icon;
    private final String color;

    public RankData(String rankName, int index, long requiredMinutes, List<String> rewards, String icon, String color) {
        this.rankName = rankName;
        this.index = index;
        this.requiredMinutes = requiredMinutes;
        this.rewards = rewards;
        this.icon = icon;
        this.color = color;
    }

    // Getters:
    public String getRankName() {return rankName;}
    public int getIndex() {return index;}
    public long getRequiredMinutes() {return requiredMinutes;}
    public List<String> getRewards() {return rewards;}
    public String getIcon() { return icon; }
    public String getColor() { return color; }
}