package fight.ia.type;

import common.PathFinding;
import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import fight.spells.Spell.SortStats;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA30 extends AbstractNeedSpell  {

    public IA30(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;
            Fighter ennemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Fighter ennemyBuffed = Function.getInstance().getEnnemyWithBuff(this.fight, this.fighter);

            for(SortStats S : this.highests) {
                if (S != null && S.getMaxPO() > maxPo) {
                    maxPo = S.getMaxPO();
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0)
            {
                if (Function.getInstance().moveToAttackIfPossibleAll(this.fight, this.fighter) != 0) {
                    time += 1000;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0)
            {
                if(ennemyBuffed != null) {
                    if (!Function.getInstance().debuffIfPossiblePerco(this.fight, this.fighter, ennemyBuffed)) {
                        time += 1000;
                    }
                }
                else {
                    if (!Function.getInstance().debuffIfPossiblePerco(this.fight, this.fighter, ennemy)) {
                        time += 1000;
                    }
                }
            }

            if(this.fighter.getCurPa(this.fight) > 0)
            {
                if(Function.getInstance().attackIfPossiblePerco(this.fight, this.fighter, ennemy, this.highests) != 0)
                {
                    time += 1000;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0)
            {
                if(!Function.getInstance().HealIfPossible(this.fight, this.fighter, false))
                {
                    time += 1000;
                }
            }

            if(this.fighter.getCurPm(this.fight) > 0)
            {
                if(Function.getInstance().moveFarIfPossible(this.fight, this.fighter) == 0)
                {
                    time += 1000;
                }
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}