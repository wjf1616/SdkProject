package com.chatsdk.model.mail.missile;

import com.chatsdk.model.mail.battle.ArmyParams;
import com.chatsdk.model.mail.battle.RewardParams;
import com.chatsdk.model.mail.battle.UserParams;

import java.util.List;


/**
 * Created by lzh on 16/9/7.
 */
public class MissileMailContents
{
    private String					uid;
    private int						type;
    private String					createTime;
    private UserParams              defUser;
    private UserParams				atkUser;
    private AttackInfoParams        attackInfo;
    private MineInfoParam           mineInfo;
    private String					isSuccess;
    private String					isAtt;
    private String					missileAnti;
    private int						delCityDef;
    private int						missileId;

    private int						mineId;


    private String				    targetPointId;
    private int						targetPointType;

    private int                     delChip;

    private List<RewardParams>      delResource;
    private List<ArmyParams>        dead;

    private List<ArmyParams>        hurt;


    private List<UserParams>        defUserList;

    private List<DeadListParams>        deadList;



    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public String getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(String createTime)
    {
        this.createTime = createTime;
    }

    public UserParams getDefUser()
    {
        return defUser;
    }

    public void setDefUser(UserParams defUser)
    {
        this.defUser = defUser;
    }

    public UserParams getAtkUser()
    {
        return atkUser;
    }

    public void setAtkUser(UserParams atkUser)
    {
        this.atkUser = atkUser;
    }

    public String getIsSuccess()
    {
        return isSuccess;
    }

    public void setIsSuccess(String isSuccess)
    {
        this.isSuccess = isSuccess;
    }

    public String getTargetPointId()
    {
        return targetPointId;
    }

    public void setTargetPointId(String targetPointId)
    {
        this.targetPointId = targetPointId;
    }

    public String getIsAtt()
    {
        return isAtt;
    }

    public void setIsAtt(String isAtt)
    {
        this.isAtt = isAtt;
    }

    public String getMissileAnti() {
        return missileAnti;
    }

    public void setMissileAnti(String missileAnti) {
        this.missileAnti = missileAnti;
    }

    public int getDelCityDef()
    {
        return delCityDef;
    }

    public void setDelCityDef(int delCityDef)
    {
        this.delCityDef = delCityDef;
    }

    public int getTargetPointType()
    {
        return targetPointType;
    }

    public void setTargetPointType(int targetPointType)
    {
        this.targetPointType = targetPointType;
    }

    public int getMissileId()
    {
        return missileId;
    }

    public void setMissileId(int missileId)
    {
        this.missileId = missileId;
    }

    public int getMineId()
    {
        return mineId;
    }

    public void setMineId(int mineId)
    {
        this.mineId = mineId;
    }

    public int getDelChip()
    {
        return delChip;
    }

    public void setDelChip(int delChip)
    {
        this.delChip = delChip;
    }

    public List<RewardParams> getDelResource()
    {
        return delResource;
    }

    public void setDelResource(List<RewardParams> delResource)
    {
        this.delResource = delResource;
    }

    public AttackInfoParams getAttackInfo() {
        return attackInfo;
    }

    public void setAttackInfo(AttackInfoParams attackInfo) {
        this.attackInfo = attackInfo;
    }

    public MineInfoParam getMineInfo() {
        return mineInfo;
    }

    public void setMineInfo(MineInfoParam mineInfo) {
        this.mineInfo = mineInfo;
    }
    
    public List<ArmyParams> getDead() {
        return dead;
    }

    public void setDead(List<ArmyParams> dead) {
        this.dead = dead;
    }

    public List<ArmyParams> getHurt() {
        return hurt;
    }

    public void setHurt(List<ArmyParams> hurt) {
        this.hurt = hurt;
    }

    public List<UserParams> getDefUserList() {
        return defUserList;
    }

    public void setDefUserList(List<UserParams> defUserList) {
        this.defUserList = defUserList;
    }


    public List<DeadListParams> getDeadList() {
        return deadList;
    }

    public void setDeadList(List<DeadListParams> deadList) {
        this.deadList = deadList;
    }

}
