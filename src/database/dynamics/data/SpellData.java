package database.dynamics.data;

import com.zaxxer.hikari.HikariDataSource;
import database.dynamics.AbstractDAO;
import entity.monster.Monster;
import fight.spells.*;
import game.world.World;
import kernel.Main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SpellData extends AbstractDAO<Spell> {
    public SpellData(HikariDataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void load(Object obj) {
    }

    @Override
    public boolean update(Spell obj) {
        return false;
    }

    public void load() {
        Result result = null,result2 = null,result3=null;
        try {
            result = getData("SELECT  * from spells");
            ResultSet RS = result.resultSet;
            boolean modif = false;
            while (RS.next()) {
                int id = RS.getInt("id");
                Spell spell = null;
                if (World.world.getSort(id) != null) {
                    spell = World.world.getSort(id);
                    spell.setInfos(RS.getInt("sprite"), RS.getString("spriteinfo"), RS.getInt("type"), RS.getInt("duration"));
                    modif = true;
                }
                else {
                    spell = new Spell(id, RS.getString("name"), RS.getInt("sprite"), RS.getString("spriteinfo"), RS.getInt("type"), RS.getInt("duration"));
                    World.world.addSort(spell);
                }
                spell.getSortsStats().clear();

                result2 = getData("SELECT  * from spells_grade where spellID="+id);
                ResultSet RS2 = result2.resultSet;
                while (RS2.next()) {
                    int spellid = RS2.getInt("spellID");
                    int gradeid = RS2.getInt("gradeID");
                    String[] sForbiddenStates = RS2.getString("statesForbidden").split(";");
                    ArrayList<Integer> ForbiddenStates= new ArrayList<>();
                    for (String forbiddenState : sForbiddenStates) {
                        ForbiddenStates.add(Integer.parseInt(forbiddenState));
                    }

                    SpellGrade spellgrade = new SpellGrade(spellid,gradeid,RS2.getInt("paCost"),RS2.getInt("poMin"),RS2.getInt("poMax"),RS2.getInt("ratioCC"),RS2.getInt("ratioEC"),RS2.getBoolean("isLine"),RS2.getBoolean("needLOS"),RS2.getBoolean("needEmptyC"),RS2.getBoolean("isPoModif"),RS2.getInt("maxByTurn"),RS2.getInt("maxByTarget"),RS2.getInt("CD"),RS2.getInt("lvlLearn"),RS2.getBoolean("endTurn"),ForbiddenStates,RS2.getInt("stateNeed"));

                    result3 = getData("SELECT  * from spells_effect where spellID="+id+" and gradeID="+gradeid);
                    ResultSet RS3 = result3.resultSet;
                    while (RS3.next()) {
                        boolean isCCeffect = RS3.getBoolean("isCCeffect");
                        //SpellEffect se = new SpellEffect(RS3.getInt("effectID"),RS3.getInt("min"),RS3.getInt("max"),RS3.getInt("args"),RS3.getInt("turn"),RS3.getInt("chance"),RS3.getString("jet"),RS3.getString("area"),RS3.getString("onHit"),RS3.getInt("effectTarget"),RS3.getInt("spellID"));
                        Effect se2 = null;
                        if(isCCeffect)
                            se2 = new Effect(RS3.getInt("effectID"),spellid,gradeid,RS3.getInt("min"),RS3.getInt("max"),RS3.getInt("args"),RS3.getString("area"),RS3.getInt("chance"),RS3.getInt("turn"),true,RS3.getString("jet"),RS3.getInt("effectTarget"),RS3.getString("onHit"));
                        else
                            se2 = new Effect(RS3.getInt("effectID"),spellid,gradeid,RS3.getInt("min"),RS3.getInt("max"),RS3.getInt("args"),RS3.getString("area"),RS3.getInt("chance"),RS3.getInt("turn"),false,RS3.getString("jet"),RS3.getInt("effectTarget"),RS3.getString("onHit"));

                        /*if(isCCeffect)
                            spellgrade.addCCSpellEffect(se);
                        else
                            spellgrade.addSpellEffect(se);*/

                        spellgrade.addEffectSpell(se2);
                    }
                    close(result3);
                    spellgrade.setTypeSwitchSpellEffects();
                    spell.addSortStats(gradeid,spellgrade);
                }

                close(result2);
            }
            if (modif)
                for (Monster monster : World.world.getMonstres())
                    monster.getGrades().values().forEach(Monster.MobGrade::refresh);
        } catch (SQLException e) {
            super.sendError("SortData load", e);
            Main.INSTANCE.stop("unknown");
        } finally {
            close(result);
        }
    }

}
