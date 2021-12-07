package fight.ia;

import fight.Fight;
import fight.Fighter;
import fight.spells.Spell.SortStats;
import fight.spells.SpellEffect;
import game.world.World;
import guild.Guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Locos on 04/10/2015.
 */
public abstract class AbstractNeedSpell extends AbstractIA {

    protected List<SortStats> buffs, glyphs, invocations, cacs, highests;

    public AbstractNeedSpell(Fight fight, Fighter fighter, byte count) {
        super(fight, fighter, count);

        if(fighter.isCollector())
        {
            Map<String, List<SortStats>> spellPerco = getPercoSpells(fighter);
            if(spellPerco != null) {
                this.buffs = spellPerco.get("BUFF");
                this.glyphs = spellPerco.get("GLYPH");
                this.invocations = spellPerco.get("INVOCATION");
                this.cacs = spellPerco.get("CAC");
                this.highests = spellPerco.get("HIGHEST");
            }
        }
        else {
            this.buffs = AbstractNeedSpell.getListSpellOf(fighter, "BUFF");
            this.glyphs = AbstractNeedSpell.getListSpellOf(fighter, "GLYPH");
            this.invocations = AbstractNeedSpell.getListSpellOf(fighter, "INVOCATION");
            this.cacs = AbstractNeedSpell.getListSpellOf(fighter, "CAC");
            this.highests = AbstractNeedSpell.getListSpellOf(fighter, "HIGHEST");
        }
    }

    private static List<SortStats> getListSpellOf(Fighter fighter, String type) {
        final List<SortStats> spells = new ArrayList<>();
            for (SortStats spell : fighter.getMob().getSpells().values()) {
                if (spells.contains(spell)) continue;
                switch (type) {
                    case "BUFF":
                        if (spell.getSpell().getType() == 1) spells.add(spell);
                        break;
                    case "GLYPH":
                        if (spell.getSpell().getType() == 4) spells.add(spell);
                        break;
                    case "INVOCATION":
                        spells.addAll(spell.getEffects().stream().filter(spellEffect -> spellEffect.getEffectID() == 181).map(spellEffect -> spell).collect(Collectors.toList()));
                        break;
                    case "CAC":
                        if (spell.getSpell().getType() == 0) {
                            boolean effect = false;
                            for (SpellEffect spellEffect : spell.getEffects())
                                if (spellEffect.getEffectID() == 4 || spellEffect.getEffectID() == 6)
                                    effect = true;
                            if (!effect && spell.getMaxPO() < 3) spells.add(spell);
                        }
                        break;
                    case "HIGHEST":
                        if (spell.getSpell().getType() == 0) {
                            boolean effect = false;
                            for (SpellEffect spellEffect : spell.getEffects())
                                if (spellEffect.getEffectID() == 4 || spellEffect.getEffectID() == 6)
                                    effect = true;
                            if (effect && spell.getSpellID() != 805) continue;
                            if (spell.getMaxPO() > 1) spells.add(spell);
                        }
                        break;
                }
            }
        return spells;
    }

    private Map<String, List<SortStats>> getPercoSpells(Fighter fighter) {
        Guild guild = World.world.getGuild(fighter.getCollector().getGuildId());
        if(guild != null)
        {
            Map<Integer, SortStats> spellsPerco = guild.getSpells();
            Map<String, List<SortStats>> spellsExploit = new HashMap<>();
            List<SortStats> spellBuff = new ArrayList<SortStats>();
            List<SortStats> spellHighest = new ArrayList<SortStats>();
            List<SortStats> spellGlyph = new ArrayList<SortStats>();
            List<SortStats> spellInvocation = new ArrayList<SortStats>();
            List<SortStats> spellCac = new ArrayList<SortStats>();
            for(SortStats spell :  spellsPerco.values())
            {
                if(spell != null) {
                    if (spell.getLevel() > 0) {
                        if (spell.getSpellID() == 451 | spell.getSpellID() == 452 | spell.getSpellID() == 453 | spell.getSpellID() == 454 | spell.getSpellID() == 459 | spell.getSpellID() == 461) {
                            spellBuff.add(spell);
                        } else if (spell.getSpellID() == 455 | spell.getSpellID() == 456 | spell.getSpellID() == 457 | spell.getSpellID() == 458 | spell.getSpellID() == 460 | spell.getSpellID() == 462) {
                            spellHighest.add(spell);
                        }
                    }
                }
            }
            spellsExploit.put("BUFF", spellBuff);
            spellsExploit.put("HIGHEST", spellHighest);
            spellsExploit.put("GLYPH", spellGlyph);
            spellsExploit.put("INVOCATION", spellInvocation);
            spellsExploit.put("CAC", spellCac);
            return  spellsExploit;
        }
        return null;
    }
}
