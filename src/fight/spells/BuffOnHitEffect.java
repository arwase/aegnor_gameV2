package fight.spells;

import fight.Fight;
import fight.Fighter;

import java.util.ArrayList;

public class BuffOnHitEffect extends SpellEffect{

    public static final int TYPE_DIRECT = 0;
    public static final int TYPE_ON_HIT = 1;


    private int buffType;
    private ArrayList<Integer> onHitConditions; // Nouvel attribut
    private SpellEffect onHitEffectBuff; // Nouvel attribut
    private int duration; // Nouveau paramètre
    private boolean debuffable = true;

    public BuffOnHitEffect(int id, int value2, int turns2, Fighter aCaster, int aspell, int customDuration, int buffType, String onHitConditions,SpellEffect OnHitEffectBuff, boolean debuff) {
        super(id, value2, turns2, aCaster, aspell);

        this.debuffable = debuff;
        this.duration = customDuration;
        this.buffType = buffType;
        this.onHitConditions = new ArrayList<>();
        for(String sEffect : onHitConditions.split(",") ){
            this.onHitConditions.add(Integer.parseInt(sEffect));
        }
        this.onHitEffectBuff = OnHitEffectBuff;

    }

    public BuffOnHitEffect(int id, int value2, int turns2, Fighter aCaster, int aspell, int customDuration, boolean debuff) {
        super(id, value2, turns2, aCaster, aspell);
        this.debuffable = debuff;
        this.duration = customDuration;
        this.buffType = TYPE_DIRECT;
    }

    public SpellEffect getSpellEffectToApply(){
        return this.onHitEffectBuff;
    }

    // Ajoutez des méthodes spécifiques aux effets de buff si nécessaire
    public ArrayList<Integer> getBuffOnHitConditions(){
        return this.onHitConditions;
    }

    public int getBuffType() {
        return buffType;
    }

    public void setBuffType(int buffType) {
        this.buffType = buffType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int customDuration) {
        this.duration = customDuration;
    }

    public int decrementDuration() {
        duration -= 1;
        return duration;
    }


    public void applyOnHitBuffsEffect(Fighter target, Fighter caster, Fight fight, int elementId) {



    }

}


