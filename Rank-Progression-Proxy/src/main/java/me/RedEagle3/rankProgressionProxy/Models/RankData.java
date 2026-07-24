package me.RedEagle3.rankProgressionProxy.Models;

import java.util.List;

public class RankData {

    private final String rankName;
    private final int index;
    private final long requiredMinutes;
    private final String rewardText;
    private final List<String> rewardCommands;
    private final String icon;
    private final String color;

    public RankData(String rankName, int index, long requiredMinutes, String rewardText, List<String> rewardCommands, String icon, String color) {
        this.rankName = rankName;
        this.index = index;
        this.requiredMinutes = requiredMinutes;
        this.rewardText = rewardText;
        this.rewardCommands = rewardCommands;
        this.icon = icon;
        this.color = color;
    }

    // Getters:
    public String getRankName() {return rankName;}
    public int getIndex() {return index;}
    public long getRequiredMinutes() {return requiredMinutes;}
    public String getRewardText() {return rewardText;}
    public List<String> getRewardCommands() {return rewardCommands;}
    public String getIcon() { return icon; }
    public String getColor() { return color; }
}