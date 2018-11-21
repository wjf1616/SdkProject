package com.chatsdk.model.mail.monster;

import java.util.List;

/**
 * Created by user on 2018/3/28.
 */

public class BossRewardParams {
    private String uid;
    private int killCount;
    private String killRate;
    private boolean killBossLimit;
    private List<RateRewardParams> killReward;
    private List<RateRewardParams> hurtReward;

    public List<RateRewardParams> getHurtReward() {
        return hurtReward;
    }

    public void setHurtReward(List<RateRewardParams> hurtReward) {
        this.hurtReward = hurtReward;
    }

    public int getKillCount() {
        return killCount;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    public String getKillRate() {
        return killRate;
    }

    public void setKillRate(String killRate) {
        this.killRate = killRate;
    }

    public List<RateRewardParams> getKillReward() {
        return killReward;
    }

    public void setKillReward(List<RateRewardParams> killReward) {
        this.killReward = killReward;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isKillBossLimit() {
        return killBossLimit;
    }

    public void setKillBossLimit(boolean killBossLimit) {
        this.killBossLimit = killBossLimit;
    }
}
