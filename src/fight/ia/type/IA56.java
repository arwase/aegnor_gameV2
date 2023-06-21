package fight.ia.type;

import fight.Fight;
import fight.Fighter;
import fight.ia.AbstractNeedSpell;
import fight.ia.util.Function;
import fight.spells.Spell;

/**
 * Created by Locos on 04/10/2015.
 */
public class IA56 extends AbstractNeedSpell  {

    public IA56(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);
    }

    @Override
    public void apply() {
        if (!this.stop && this.fighter.canPlay() && this.count > 0) {
            int time = 100, maxPo = 1;
            boolean action = false;

            for(Spell.SortStats spellStats : this.highests)
                if(spellStats.getMaxPO() > maxPo)
                    maxPo = spellStats.getMaxPO();

            Fighter ennemy = Function.getInstance().getNearestEnnemy(this.fight, this.fighter);
            Fighter L = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1 + this.fighter.getBuffValue(117));// pomax +1;
            Fighter C = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;

            if(maxPo == 1) L = null;
            if(C != null && C.isHide()) C = null;
            if(L != null && L.isHide()) L = null;

            if(this.fighter.getCurPa(this.fight) > 0)
            {
                int value = Function.getInstance().attackIfPossibleCMAttirance(this.fight, this.fighter, this.fighter.getCurPa(this.fight));

                if(value != 0) {
                   // System.out.println("On peut pas attirer, ni taper cac");
                    time = value;
                    action = true;
                }
                else{
                    if(!Function.getInstance().attackCacIfPossibleCM(this.fight, this.fighter, this.fighter.getCurPa(this.fight)))
                    {
                        time = 1000;
                        action = true;
                    }
                }
            }
            if(this.fighter.getCurPm(this.fight) > 0) {
                int value = Function.getInstance().moveenfaceIfPossible(this.fight, this.fighter, ennemy, maxPo + 1 + this.fighter.getBuffValue(117));
                //System.out.println("On essaie de se mettre en face");
                if(value != 0) {
                    //System.out.println("On peut pas ???");
                    //System.out.println(value + "Resultat");
                    time = value;
                    action = true;
                    L = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 1, maxPo + 1 + this.fighter.getBuffValue(117));// pomax +1;
                    C = Function.getInstance().getNearestEnnemynbrcasemax(this.fight, this.fighter, 0, 2);//2 = po min 1 + 1;
                    if(maxPo == 1)
                        L = null;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0) {
                //System.out.println("On se buff");
                if (Function.getInstance().buffIfPossible(this.fight, this.fighter, this.fighter, this.buffs)) {
                    time = 1000;
                    action = true;
                }
            }
            if(this.fighter.getCurPm(this.fight) > 0) {
                //System.out.println("On se rapproche");
                if(Function.getInstance().moveNearIfPossible(fight, this.fighter, ennemy))
                {
                    time = 1000;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0)
            {
                //System.out.println("Si on est en ligne");
                int value = Function.getInstance().attackIfPossibleCMAttirance(this.fight, this.fighter, this.fighter.getCurPa(this.fight));
                if(value != 0) {
                    time = value;
                    action = true;
                }
                else{

                    if(!Function.getInstance().attackCacIfPossibleCM(this.fight, this.fighter, this.fighter.getCurPa(this.fight)))
                    {
                        time = 1000;
                        action = true;
                    }
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && L != null && C == null) {
                //System.out.println("On l'est pas, On tape");
                int value = Function.getInstance().attackIfPossibleCM1(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && C != null) {
                //System.out.println("On retape si on a des PA");
                int value = Function.getInstance().attackIfPossibleCM1(this.fight, this.fighter, this.cacs);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }
            if(this.fighter.getCurPa(this.fight) > 0 && L != null && C == null) {
                int value = Function.getInstance().attackIfPossible(this.fight, this.fighter, this.highests);
                //System.out.println("Inutile non ?" + value);
                if(value != 0) {
                    time = value;
                    action = true;
                }
            }
            if(this.fighter.getCurPm(this.fight) > 0) {

                int value = Function.getInstance().moveenfaceIfPossible(this.fight, this.fighter, ennemy, maxPo + 1 + this.fighter.getBuffValue(117));
                //System.out.println("Bizarrement on essaie encore de se mettre en face ?" + value);
                if(value != 0) time = value;
            }

            if(this.fighter.getCurPa(this.fight) == 0 && this.fighter.getCurPm(this.fight) == 0) this.stop = true;
            addNext(this::decrementCount, time);
        } else {
            this.stop = true;
        }
    }
}