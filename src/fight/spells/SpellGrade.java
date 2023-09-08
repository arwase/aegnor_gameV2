package fight.spells;

import area.map.GameCase;
import common.Formulas;
import common.PathFinding;
import fight.Challenge;
import fight.Fight;
import fight.Fighter;
import game.world.World;
import kernel.Constant;
import kernel.Main;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class SpellGrade {

    private int spellID;
    private int level;
    private int PACost;
    private int minPO;
    private int maxPO;
    private int TauxCC;
    private int TauxEC;
    private boolean isLineLaunch;
    private boolean hasLDV;
    private boolean isEmptyCell;
    private boolean isModifPO;
    private int maxLaunchbyTurn;
    private int maxLaunchbyByTarget;
    private int coolDown;
    private int reqLevel;
    private boolean isEcEndTurn;
    private ArrayList<SpellEffect> effects = new ArrayList<>();
    private ArrayList<SpellEffect> CCeffects  = new ArrayList<>();
    private ArrayList<Integer> statesForbidden;
    private int stateRequire;
    private int type=0;

    public SpellGrade(int spellid, int gradeid, int paCost, int poMin, int poMax, int ratioCC, int ratioEC, boolean isLine, boolean needLOS, boolean needEmptyC, boolean isPoModif, int maxByTurn, int maxByTarget, int cd, int lvlLearn, boolean endTurn, ArrayList<Integer> forbiddenStates, int stateNeed) {
        this.spellID = spellid;
        this.level = gradeid;
        this.PACost = paCost;
        this.minPO = poMin;
        this.maxPO = poMax;
        this.TauxCC = ratioCC;
        this.TauxEC = ratioEC;
        this.isLineLaunch = isLine;
        this.hasLDV = needLOS;
        this.isEmptyCell = needEmptyC;
        this.isModifPO = isPoModif;
        this.maxLaunchbyTurn = maxByTurn;
        this.maxLaunchbyByTarget = maxByTarget;
        this.coolDown = cd;
        this.reqLevel = lvlLearn;
        this.isEcEndTurn = endTurn;
        this.statesForbidden = forbiddenStates;
        this.stateRequire = stateNeed;
    }

    public void addSpellEffect(SpellEffect sp){
        this.effects.add(sp);
    }

    public void addCCSpellEffect(SpellEffect sp){
        this.CCeffects.add(sp);
    }

    public int getSpellID() {
        return spellID;
    }

    public int getStateRequire() {
        return stateRequire;
    }

    public ArrayList<Integer> getStatesForbidden() {
        return statesForbidden;
    }

    public Spell getSpell() {
        return World.world.getSort(spellID);
    }

    public int getSpriteID() {
        return getSpell().getSpriteID();
    }

    public String getSpriteInfos() {
        return getSpell().getSpriteInfos();
    }

    public int getLevel() {
        return level;
    }

    public int getPACost() {
        return PACost;
    }

    public int getMinPO() {
        return minPO;
    }

    public int getMaxPO() {
        return maxPO;
    }

    public int getTauxCC() {
        return TauxCC;
    }

    public int getTauxEC() {
        return TauxEC;
    }

    public boolean isLineLaunch() {
        return isLineLaunch;
    }

    public boolean hasLDV() {
        return hasLDV;
    }

    public boolean isEmptyCell() {
        return isEmptyCell;
    }

    public boolean isModifPO() {
        return isModifPO;
    }

    public int getMaxLaunchbyTurn() {
        return maxLaunchbyTurn;
    }

    public int getMaxLaunchByTarget() {
        return maxLaunchbyByTarget;
    }

    public int getCoolDown() {
        return coolDown;
    }

    public int getReqLevel() {
        return reqLevel;
    }

    public boolean isEcEndTurn() {
        return isEcEndTurn;
    }

    public ArrayList<SpellEffect> getEffects() {
        return effects;
    }

    public ArrayList<SpellEffect> getCCeffects() {
        return CCeffects;
    }

    public int getTypeSwitchSpellEffects() {
        /*String stype = "";
        switch (this.type){
            case 0: stype= "offensif";
                break;
            case 1: stype= "buff";
                break;
            case 2: stype= "invocation";
                break;
            case 3: stype= "heal";
                break;
            case 4: stype= "trap";
                break;
            case 5: stype= "mouvement";
                break;
            case 6: stype= "useless";
                break;
        }
        System.out.println("Le systeme a décidé que le sort " + getSpell().getName() + " était majoritairement de type " +  stype);*/
        return this.type;
    }

    public void setTypeSwitchSpellEffects() {
        int majorType = 0;
        int[] tableau = new int[7];
        for(SpellEffect effect : this.getEffects()) {
            // Effet de buff

            if(Arrays.stream(Constant.SPELLEFFECT_BUFF).anyMatch(value -> value == effect.getEffectID())){
                if(effect.getEffectID() == 950 || effect.getEffectID() == 951) {
                    tableau[1] += 6000;
                }
                tableau[1] += 10+Formulas.getMiddleJet(effect);
                continue;
            }
            else if(Arrays.stream(Constant.SPELLEFFECT_DEBUFF).anyMatch(value -> value == effect.getEffectID())){
                tableau[0] += 10+Formulas.getMiddleJet(effect);
                continue;
            }
            else if(Arrays.stream(Constant.SPELLEFFECT_DAMMAGE).anyMatch(value -> value == effect.getEffectID())){
                if(effect.getEffectID() == 141) {
                    tableau[0] += 10000;
                }
                tableau[0] += 10 + Formulas.getMiddleJet(effect);
                continue;
            }
            else if(Arrays.stream(Constant.SPELLEFFECT_HEAL).anyMatch(value -> value == effect.getEffectID())){
                tableau[3] += 10+Formulas.getMiddleJet(effect);
                continue;
            }
            else if(Arrays.stream(Constant.SPELLEFFECT_INVO).anyMatch(value -> value == effect.getEffectID())){
                tableau[2] += 15000+Formulas.getMiddleJet(effect);
                continue;
            }
            else if(Arrays.stream(Constant.SPELLEFFECT_MOUVEMENT).anyMatch(value -> value == effect.getEffectID())){
                tableau[5] += 10000+Formulas.getMiddleJet(effect);
                continue;
            }
            else if(Arrays.stream(Constant.SPELLEFFECT_TRAP).anyMatch(value -> value == effect.getEffectID())){
                tableau[4] += 5000+Formulas.getMiddleJet(effect);
                continue;
            }
            else if(Arrays.stream(Constant.SPELLEFFECT_USELESS).anyMatch(value -> value == effect.getEffectID())){
                tableau[6] += 0;
                continue;
            }
            else{
                System.out.println(effect.getEffectID() + " pas pris en compte dans le calcul car pas typé");
                continue;
            }
        }

        int idMaxValue = -1;
        int maxValue = Integer.MIN_VALUE;

        for (int id = 0; id < 7; id++) {
            if (tableau[id] > maxValue) {
                maxValue = tableau[id];
                idMaxValue = id;
            }
        }
        majorType =idMaxValue;

        this.type = majorType;
    }

    public void applySpellEffectToFight(Fight fight, Fighter perso,
                                        GameCase cell, boolean isCC, boolean isTrap) {
        ArrayList<SpellEffect> effets;
        if (isCC)
            effets = CCeffects;
        else
            effets = effects;

        int jetChance = 0;
        if (this.getSpell().getSpellID() == 101) // Si c'est roulette
        {
            jetChance = Formulas.getRandomValue(0, 75);
            if (jetChance % 2 == 0)
                jetChance++;
        } else if (this.getSpell().getSpellID() == 574) // Si c'est Ouverture hasardeuse fant�me
            jetChance = Formulas.getRandomValue(0, 96);
        else if (this.getSpell().getSpellID() == 574) // Si c'est Ouverture hasardeuse
            jetChance = Formulas.getRandomValue(0, 95);
        else if (this.getSpell().getSpellID() == 913) // Si c'est Ouverture hasardeuse
            jetChance = Formulas.getRandomValue(0, 95);
        else
            jetChance = Formulas.getRandomValue(0, 99);
        int curMin = 0;
        int num = 0;

        for (SpellEffect SE : effets) {
            try {
                if (fight.getState() >= Constant.FIGHT_STATE_FINISHED)
                    return;
                if (SE.getChance() != 0 && SE.getChance() != 100)// Si pas 100%
                {
                    if (jetChance <= curMin
                            || jetChance >= (SE.getChance() + curMin)) {
                        curMin += SE.getChance();
                        num++;
                        continue;
                    }
                    curMin += SE.getChance();
                }


                ArrayList<GameCase> cells = PathFinding.getCellListFromAreaString(fight.getMap(), cell.getId(), perso.getCell().getId(), SE.getAreaEffect());
                ArrayList<GameCase> finalCells = new ArrayList<GameCase>();

                int TE = SE.getEffectTarget();

                for (GameCase C : cells) {
                    if (C == null)
                        continue;
                    Fighter F = C.getFirstFighter();
                    if (F == null)
                        continue;
                    // Ne touches pas les alli�s : 1
                    if (((TE & 1) == 1) && (F.getTeam() == perso.getTeam()))
                        continue;
                    // Ne touche pas le lanceur : 2
                    if ((((TE >> 1) & 1) == 1) && (F.getId() == perso.getId()))
                        continue;
                    // Ne touche pas les ennemies : 4
                    if ((((TE >> 2) & 1) == 1) && (F.getTeam() != perso.getTeam()))
                        continue;
                    // Ne touche pas les combatants (seulement invocations) : 8
                    if ((((TE >> 3) & 1) == 1) && (!F.isInvocation()))
                        continue;
                    // Ne touche pas les invocations : 16
                    if ((((TE >> 4) & 1) == 1) && (F.isInvocation()))
                        continue;
                    // N'affecte que le lanceur : 32
                    if ((((TE >> 5) & 1) == 1) && (F.getId() != perso.getId()))
                        continue;
                    // N'affecte que les alliés (pas le lanceur) : 64
                    if ((((TE >> 6) & 1) == 1) && (F.getTeam() != perso.getTeam() || F.getId() == perso.getId()))
                        continue;
                    // N'affecte PERSONNE : 1024
                    if ((((TE >> 10) & 1) == 1))
                        continue;

                    // Si pas encore eu de continue, on ajoute la case, tout le monde : 0
                    finalCells.add(C);
                }
                // Si le sort n'affecte que le lanceur et que le lanceur n'est
                // pas dans la zone

                if (((TE >> 5) & 1) == 1)
                    if (!finalCells.contains(perso.getCell()))
                        finalCells.add(perso.getCell());
                ArrayList<Fighter> cibles = SpellEffect.getTargets(SE, fight, finalCells);


                TE = SE.getEffectTarget();
                for (GameCase C : cells) {
                    if (C == null)
                        continue;
                    Fighter F = C.getFirstFighter();
                    if (F == null)
                        continue;
                    // Ne touches pas les alli�s : 1
                    if (((TE & 1) == 1) && (F.getTeam() == perso.getTeam()))
                        continue;
                    // Ne touche pas le lanceur : 2
                    if ((((TE >> 1) & 1) == 1) && (F.getId() == perso.getId()))
                        continue;
                    // Ne touche pas les ennemies : 4
                    if ((((TE >> 2) & 1) == 1) && (F.getTeam() != perso.getTeam()))
                        continue;
                    // Ne touche pas les combatants (seulement invocations) : 8
                    if ((((TE >> 3) & 1) == 1) && (!F.isInvocation()))
                        continue;
                    // Ne touche pas les invocations : 16
                    if ((((TE >> 4) & 1) == 1) && (F.isInvocation()))
                        continue;
                    // N'affecte que le lanceur : 32
                    if ((((TE >> 5) & 1) == 1) && (F.getId() != perso.getId()))
                        continue;
                    // N'affecte que les alliés (pas le lanceur) : 64
                    if ((((TE >> 6) & 1) == 1) && (F.getTeam() != perso.getTeam() || F.getId() == perso.getId()))
                        continue;
                    // N'affecte PERSONNE : 1024
                    if ((((TE >> 10) & 1) == 1))
                        continue;

                    // Si pas encore eu de continue, on ajoute la case, tout le monde : 0
                    finalCells.add(C);
                }


                if ((fight.getType() != Constant.FIGHT_TYPE_CHALLENGE)
                        && (fight.getAllChallenges().size() > 0)) {
                    for (Map.Entry<Integer, Challenge> c : fight.getAllChallenges().entrySet()) {
                        if (c.getValue() == null)
                            continue;
                        c.getValue().onFightersAttacked(cibles, perso, SE, this.getSpellID(), isTrap);
                    }
                }
                SE.applyToFight(fight, perso, cell, cibles);

                num++;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

}
