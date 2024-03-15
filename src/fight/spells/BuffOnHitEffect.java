package fight.spells;

import fight.Fight;
import fight.Fighter;

public class BuffOnHitEffect extends Effect{

    public static final int TYPE_DIRECT = 0;
    public static final int TYPE_ON_HIT = 1;


    private int buffType;
    private int[] onHitConditions; // Nouvel attribut
    private Effect onHitEffectBuff; // Nouvel attribut
    private boolean debuffable = true;
    private int impactedTarget = 0;


    public BuffOnHitEffect(int id, int duration, int args1, int args2, int args3, int chance, Fighter caster, int spellID, Effect buffToAddOnHit, int onHitConditions, int impactedTarget) {
        super(id, duration, args1, args2,args3, chance,caster,spellID);
        this.buffType = TYPE_ON_HIT;
        this.onHitConditions = EffectConstant.ALL_TRIGGERS.get(onHitConditions);

        // Ca c'est le sort de boost a déclancher si la condition est remplie
        this.onHitEffectBuff = buffToAddOnHit;
        this.impactedTarget = impactedTarget;
    }


    public Effect getSpellEffectToApply(){
        return this.onHitEffectBuff;
    }

    // Ajoutez des méthodes spécifiques aux effets de buff si nécessaire
    public int[] getBuffOnHitConditions(){
        return this.onHitConditions;
    }

    public int getBuffType() {
        return buffType;
    }

    public int getImpactedTarget() {
        return impactedTarget;
    }

    public void setBuffType(int buffType) {
        this.buffType = buffType;
    }

    /*public int getDuration() {
        return duration;
    }

    public void setDuration(int customDuration) {
        this.duration = customDuration;
    }

    public int decrementDuration() {
        duration -= 1;
        return duration;
    }*/


    public void applyOnHitBuffsEffect(Fighter target, Fighter caster, Fight fight, int elementId) {



    }

}


