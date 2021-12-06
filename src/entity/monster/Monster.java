package entity.monster;

import area.map.GameCase;
import area.map.GameMap;
import client.other.Stats;
import common.Formulas;
import database.Database;
import entity.monster.boss.MaitreCorbac;
import fight.Fight;
import fight.Fighter;
import fight.spells.Spell;
import fight.spells.Spell.SortStats;
import fight.spells.SpellEffect;
import game.world.World;
import game.world.World.Drop;
import kernel.Config;
import kernel.Constant;
import object.GameObject;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Monster {
    private int id;
    private String name;
    private int gfxId;
    private int align;
    private String colors;
    private int ia = 0;
    private int minKamas;
    private int maxKamas;
    private Map<Integer, MobGrade> grades = new HashMap<>();
    private ArrayList<Drop> drops = new ArrayList<>();
    private boolean isCapturable;
    private int aggroDistance = 0;

    public Monster(int id,String name, int gfxId, int align, String colors,
                   String thisGrades, String thisSpells, String thisStats,
                   String thisStatsInfos, String thisPdvs, String thisPoints,
                   String thisInit, int minKamas, int maxKamas, String thisXp, int ia,
                   boolean capturable, int aggroDistance) {
        this.id = id;
        this.name = name;
        this.gfxId = gfxId;
        this.align = align;
        this.colors = colors;
        this.minKamas = minKamas;
        this.maxKamas = maxKamas;
        this.ia = ia;
        this.isCapturable = capturable;
        this.aggroDistance = aggroDistance;
        int G = 1;

        for (int n = 0; n < 12; n++) {
            try {
                //Grades
                String[] split = thisGrades.split("\\|");
                String grade = split[n];
                String[] infos = grade.split("@");
                int level = Integer.parseInt(infos[0]);
                String resists = infos[1];
                //Stats
                String stats = thisStats.split("\\|")[n];
                //Spells
                String spells = "";
                if (!thisSpells.equalsIgnoreCase("||||")
                        && !thisSpells.equalsIgnoreCase("")
                        && !thisSpells.equalsIgnoreCase("-1")) {
                    spells = thisSpells.split("\\|")[n];
                    if (spells.equals("-1"))
                        spells = "";
                }
                //PDVMax//init
                int pdvmax = 1;
                int init = 1;

                try {
                    pdvmax = Integer.parseInt(thisPdvs.split("\\|")[n]);
                    init = Integer.parseInt(thisInit.split("\\|")[n]);
                } catch (Exception e) {
                    e.printStackTrace();
                    World.world.logger.error("#1# Erreur lors du chargement du monstre (template) : "
                             + id);
                }
                //PA / PM
                int PA = 3;
                int PM = 3;
                int xp = 10;

                try {
                    String[] pts = thisPoints.split("\\|")[n].split(";");
                    try {
                        PA = Integer.parseInt(pts[0]);
                        PM = Integer.parseInt(pts[1]);
                        xp = Integer.parseInt(thisXp.split("\\|")[n]);
                    } catch (Exception e1) {
                        World.world.logger.error("#2# Erreur lors du chargement du monstre (template) : "
                                + id);
                        e1.printStackTrace();
                    }
                } catch (Exception e) {
                    World.world.logger.error("#3# Erreur lors du chargement du monstre (template) : "
                            + id);
                    e.printStackTrace();
                }
                grades.put(G, new MobGrade(this, G, level, PA, PM, resists, stats, thisStatsInfos, spells, pdvmax, init, xp, n));
                G++;
            } catch (Exception e) {
                // ok, pour les dopeuls ...
                //TODO: Enlever toutes les erreurs
            }
        }
    }

    public void setInfos(int gfxId, int align, String colors,
                         String thisGrades, String thisSpells, String thisStats,
                         String thisStatsInfos, String thisPdvs, String thisPoints,
                         String thisInit, int minKamas, int maxKamas, String thisXp, int ia,
                         boolean capturable, int aggroDistance) {
        this.gfxId = gfxId;
        this.align = align;
        this.colors = colors;
        this.minKamas = minKamas;
        this.maxKamas = maxKamas;
        this.ia = ia;
        this.isCapturable = capturable;
        this.aggroDistance = aggroDistance;
        int G = 1;
        grades.clear();
        for (int n = 0; n < 12; n++) {
            try {
                //Grades
                String grade = thisGrades.split("\\|")[n];
                String[] infos = grade.split("@");
                int level = Integer.parseInt(infos[0]);
                String resists = infos[1];
                //Stats
                String stats = thisStats.split("\\|")[n];
                //Spells
                String spells = thisSpells.split("\\|")[n];
                if (spells.equals("-1"))
                    spells = "";
                //PDVMax//init
                int pdvmax = 1;
                int init = 1;

                try {
                    pdvmax = Integer.parseInt(thisPdvs.split("\\|")[n]);
                    init = Integer.parseInt(thisInit.split("\\|")[n]);
                } catch (Exception e) {
                    e.printStackTrace();
                    World.world.logger.error("#4# Erreur lors du chargement du monstre (template) : "
                            + id);
                }
                //PA / PM
                int PA = 3;
                int PM = 3;
                int xp = 10;

                try {
                    String[] pts = thisPoints.split("\\|")[n].split(";");
                    try {
                        PA = Integer.parseInt(pts[0]);
                        PM = Integer.parseInt(pts[1]);
                        xp = Integer.parseInt(thisXp.split("\\|")[n]);
                    } catch (Exception e1) {
                        World.world.logger.error("#5# Erreur lors du chargement du monstre (template) : "
                                + id);
                        e1.printStackTrace();
                    }
                } catch (Exception e) {
                    World.world.logger.error("#6# Erreur lors du chargement du monstre (template) : "
                            + id);
                    e.printStackTrace();
                }
                grades.put(G, new MobGrade(this, G, level, PA, PM, resists, stats, thisStatsInfos, spells, pdvmax, init, xp, n));
                G++;
            } catch (Exception e) {
                // ok pour les dopeuls
                e.printStackTrace();
            }
        }
    }

    public void deleteDrop(int id) {
        Drop remove = null;
        for (Drop d : drops) {
            if (d.getObjectId() == id) {
                remove = d;
                break;
            }
        }
        if (remove != null) {
            /*World.world.getObjTemplate(id).delMobQueDropea(this.id)*/
            drops.remove(remove);
        }
    }

    public MobGrade getGrade(int gradevalue) {
        int graderandom = 1;
        for (Entry<Integer, MobGrade> grade : getGrades().entrySet()) {
            if (graderandom == gradevalue)
                return grade.getValue();
            else
                graderandom++;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public enum TipoGrupo {
        FIJO, NORMAL, SOLO_UNA_PELEA, HASTA_QUE_MUERA
    }
    public Boolean modifyStats(Byte grade, String packet) {
        try {
            MobGrade mob;
            if(grades.get((int)grade) != null) {
                mob = grades.get((int) grade);
            }
            else {
                return false;
            }
            String[] split = packet.split(Pattern.quote("|"));
            String[] stats = split[0].split(",");
            for (String stat : stats) {
                try {
                    var a = stat.split(":");
                    mob.stats.put(Integer.parseInt(a[0]), Integer.parseInt(a[1]));
                } catch (Exception ignored) {
                }
            }
            Monster m = World.world.getMonstre(mob.template.id);
            mob.changeStats(split[0]);
            mob.pdvMax = Integer.parseInt(split[1]);
            mob.baseXp = Integer.parseInt(split[2]);
            m.minKamas = Integer.parseInt(split[3]);
            m.maxKamas = Integer.parseInt(split[4]);
            var s = strStatsTodosMobs().split("~");
            Database.getDynamics().getMonsterData().updateMonsterStats(id, s[0], s[1], s[2], split[3], split[4]);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public String strStatsTodosMobs() { // se usa mas q todo para el panel mobs, pero minkamas y maxkamas son int para sql
        StringBuilder strStats = new StringBuilder();
        StringBuilder strPDV = new StringBuilder();
        StringBuilder strExp = new StringBuilder();
        StringBuilder strMinKamas = new StringBuilder();
        StringBuilder strMaxKamas = new StringBuilder();
        var e = false;
        for (int i = 1 ; i < 6; i++) {
            MobGrade mob;
            if(grades.get(i) != null) {
                mob = grades.get(i);
            }
            else{
                break;
            }
            if (e) {
                strStats.append("|");
                strPDV.append("|");
                strExp.append("|");
                strMinKamas.append("|");
                strMaxKamas.append("|");
            }
            strStats.append(mob.stringStatsActualizado());
            strPDV.append(mob.pdvMax);
            strExp.append(mob.baseXp);
            strMinKamas.append(mob.template.minKamas);
            strMaxKamas.append(mob.template.maxKamas);
            e = true;
        }
        return (strStats.toString() + "~" + strPDV.toString() + "~" + strExp.toString() + "~" + strMinKamas.toString() + "~"
                + strMaxKamas.toString());
    }

    public int getId() {
        return this.id;
    }

    public int getGfxId() {
        return this.gfxId;
    }

    public int getAlign() {
        return this.align;
    }

    public String getColors() {
        return this.colors;
    }

    public int getIa() {
        return this.ia;
    }

    public int getMinKamas() {
        return this.minKamas;
    }

    public int getMaxKamas() {
        return this.maxKamas;
    }

    public Map<Integer, MobGrade> getGrades() {
        return this.grades;
    }

    public void addDrop(Drop D) {
        this.drops.add(D);
    }

    public ArrayList<Drop> getDrops() {
        return this.drops;
    }

    public boolean isCapturable() {
        return this.isCapturable;
    }

    public int getAggroDistance() {
        return this.aggroDistance;
    }

    public MobGrade getGradeByLevel(int lvl) {
        if(this.getGrades() == null) return null;
        for (MobGrade grade : this.getGrades().values())
            if (grade != null && grade.getLevel() == lvl)
                return grade;
        return null;
    }

    public MobGrade getRandomGrade() {
        if(this.getGrades() == null) return null;
        int randomgrade = (int) (Math.random() * (6 - 1)) + 1, graderandom = 1;

        for (MobGrade grade : this.getGrades().values()) {
            if (grade != null && graderandom == randomgrade) return grade;
            else graderandom++;
        }

        return null;
    }

    public static class MobGroup {
        public final static MaitreCorbac MAITRE_CORBAC = new MaitreCorbac();

        private int id;
        private int cellId;
        private int orientation = 2;
        private int align = -1;
        private short starBonus;
        private int aggroDistance = 0;
        private int subarea = -1;
        private boolean changeAgro = false;
        private boolean isFix = false;
        private boolean isExtraGroup = false;
        private Map<Integer, MobGrade> mobs = new HashMap<Integer, MobGrade>();
        private String condition = "";
        private Timer condTimer;
        private ArrayList<GameObject> objects;

        public MobGroup(int Aid, int Aalign, ArrayList<MobGrade> possibles,
                        GameMap Map, int cell, int fixSize, int minSize, int maxSize,
                        MobGrade extra) {

            id = Aid;
            align = Aalign;
            //D�termination du nombre de mob du groupe
            int rand = 0;
            int nbr = 0;
            //region Nombre de monstre
            if (fixSize > 0 && fixSize < 9) {
                nbr = fixSize;
            } else if (minSize != -1 && maxSize != -1 && maxSize != 0
                    && (minSize < maxSize)) {
                if (minSize == 3 && maxSize == 8) {
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 25) //3: 25%
                    {
                        nbr = 3;
                    } else if (rand < 48) //4:23%
                    {
                        nbr = 4;
                    } else if (rand < 51) //5:20%
                    {
                        nbr = 5;
                    } else if (rand < 85) //6:17%
                    {
                        nbr = 6;
                    } else if (rand < 95) //7:10%
                    {
                        nbr = 7;
                    } else
                    //8:5%
                    {
                        nbr = 8;
                    }
                } else if (minSize == 1 && maxSize == 3) { // 21 - normalement tout astrub
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 40) //1: 40%
                    {
                        nbr = 1;
                    } else if (rand < 75)//2: 35%
                    {
                        nbr = 2;
                    } else
                    //3: 25%
                    {
                        nbr = 3;
                    }
                } else if (minSize == 1 && maxSize == 5) {
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 30) //3: 30%
                    {
                        nbr = 1;
                    } else if (rand < 53) //4:23%
                    {
                        nbr = 2;
                    } else if (rand < 73) //5:20%
                    {
                        nbr = 3;
                    } else if (rand < 90) //6:17%
                    {
                        nbr = 4;
                    } else
                    //8:10%
                    {
                        nbr = 5;
                    }
                } else if (minSize == 1 && maxSize == 4) {
                    rand = Formulas.getRandomValue(0, 99);
                    if (rand < 35) //3: 35%
                    {
                        nbr = 1;
                    } else if (rand < 61) //4:26%
                    {
                        nbr = 2;
                    } else if (rand < 82) //5:21%
                    {
                        nbr = 3;
                    } else
                    //8:18%
                    {
                        nbr = 4;
                    }
                } else {
                    nbr = Formulas.getRandomValue(minSize, maxSize);
                }
            } else if (minSize == -1) {
                switch (maxSize) {
                    case 0:
                        return;
                    case 1:
                        nbr = 1;
                        break;
                    case 2:
                        nbr = Formulas.getRandomValue(1, 2); //1:50%	2:50%
                        break;
                    case 3:
                        nbr = Formulas.getRandomValue(1, 3); //1:33.3334%	2:33.3334%	3:33.3334%
                        break;
                    case 4:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 22) //1:22%
                            nbr = 1;
                        else if (rand < 48) //2:26%
                            nbr = 2;
                        else if (rand < 74) //3:26%
                            nbr = 3;
                        else
                            //4:26%
                            nbr = 4;
                        break;
                    case 5:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 15) //1:15%
                            nbr = 1;
                        else if (rand < 35) //2:20%
                            nbr = 2;
                        else if (rand < 60) //3:25%
                            nbr = 3;
                        else if (rand < 85) //4:25%
                            nbr = 4;
                        else
                            //5:15%
                            nbr = 5;
                        break;
                    case 6:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 10) //1:10%
                            nbr = 1;
                        else if (rand < 25) //2:15%
                            nbr = 2;
                        else if (rand < 45) //3:20%
                            nbr = 3;
                        else if (rand < 65) //4:20%
                            nbr = 4;
                        else if (rand < 85) //5:20%
                            nbr = 5;
                        else
                            //6:15%
                            nbr = 6;
                        break;
                    case 7:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 9) //1:9%
                            nbr = 1;
                        else if (rand < 20) //2:11%
                            nbr = 2;
                        else if (rand < 35) //3:15%
                            nbr = 3;
                        else if (rand < 55) //4:20%
                            nbr = 4;
                        else if (rand < 75) //5:20%
                            nbr = 5;
                        else if (rand < 91) //6:16%
                            nbr = 6;
                        else
                            //7:9%
                            nbr = 7;
                        break;
                    default:
                        rand = Formulas.getRandomValue(0, 99);
                        if (rand < 9) //1:9%
                            nbr = 1;
                        else if (rand < 20) //2:11%
                            nbr = 2;
                        else if (rand < 33) //3:13%
                            nbr = 3;
                        else if (rand < 50) //4:17%
                            nbr = 4;
                        else if (rand < 67) //5:17%
                            nbr = 5;
                        else if (rand < 80) //6:13%
                            nbr = 6;
                        else if (rand < 91) //7:11%
                            nbr = 7;
                        else
                            //8:9%
                            nbr = 8;
                        break;
                }
            } else {
                switch (minSize) {
                    case 1:
                        rand = Formulas.getRandomValue(1, 8);
                        switch (rand) {
                            case 1:
                                nbr = 1;
                                break;
                            case 2:
                                nbr = 2;
                                break;
                            case 3:
                                nbr = 3;
                                break;
                            case 4:
                                nbr = 4;
                                break;
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 2:
                        rand = Formulas.getRandomValue(2, 8);
                        switch (rand) {
                            case 2:
                                nbr = 2;
                                break;
                            case 3:
                                nbr = 3;
                                break;
                            case 4:
                                nbr = 4;
                                break;
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 3:
                        rand = Formulas.getRandomValue(3, 8);
                        switch (rand) {
                            case 3:
                                nbr = 3;
                                break;
                            case 4:
                                nbr = 4;
                                break;
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 4:
                        rand = Formulas.getRandomValue(4, 8);
                        switch (rand) {
                            case 4:
                                nbr = 4;
                                break;
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 5:
                        rand = Formulas.getRandomValue(5, 8);
                        switch (rand) {
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 6:
                        rand = Formulas.getRandomValue(6, 8);
                        switch (rand) {
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 7:
                        rand = Formulas.getRandomValue(7, 8);
                        switch (rand) {
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                    case 8:
                        nbr = 8;
                        break;
                    default:
                        rand = Formulas.getRandomValue(1, 8);
                        switch (rand) {
                            case 1:
                                nbr = 1;
                                break;
                            case 2:
                                nbr = 2;
                                break;
                            case 3:
                                nbr = 3;
                                break;
                            case 4:
                                nbr = 4;
                                break;
                            case 5:
                                nbr = 5;
                                break;
                            case 6:
                                nbr = 6;
                                break;
                            case 7:
                                nbr = 7;
                                break;
                            case 8:
                                nbr = 8;
                                break;
                        }
                        break;
                }
            }
            //endregion
            int guid = -1;
            boolean haveSameAlign = false;

            if (extra != null) {
                isExtraGroup = true;
                nbr--;
                this.mobs.put(guid, extra);
                guid--;
            }
            //On v�rifie qu'il existe des monstres de l'alignement demand� pour �viter les boucles infinies
            for (MobGrade mob : possibles)
                if (mob.getTemplate().getAlign() == this.align)
                    haveSameAlign = true;
            if (!haveSameAlign)
                return;//S'il n'y en a pas
            for (int a = 0; a < nbr; a++) {
                MobGrade Mob = null;
                do {
                    int random = Formulas.getRandomValue(0, possibles.size() - 1);//on prend un mob au hasard dans le tableau
                    Mob = possibles.get(random).getCopy();
                }
                while (Mob.getTemplate().getAlign() != this.align);

                this.mobs.put(guid, Mob);
                if (Mob.getTemplate().getAggroDistance() > this.aggroDistance)
                    this.aggroDistance = Mob.getTemplate().getAggroDistance();
                guid--;
            }

            this.cellId = (cell == -1 ? Map.getRandomFreeCellId() : cell);

            while (Map.containsForbiddenCellSpawn(this.cellId))
                this.cellId = Map.getRandomFreeCellId();
            if (this.cellId == 0)
                return;
            this.orientation = (Formulas.getRandomValue(0, 3) * 2) + 1;
            this.isFix = false;
            this.starBonus = 0;
        }

        public MobGroup(int id, int cellId, String groupData, String objects, short stars) {
            this.id = id;
            this.align = Constant.ALIGNEMENT_NEUTRE;
            this.cellId = cellId;
            this.isFix = false;
            this.orientation = (Formulas.getRandomValue(0, 3) * 2) + 1;
            this.starBonus = stars;

            int guid = -1;

            for (String data : groupData.split(";")) {
                if (data.equalsIgnoreCase(""))
                    continue;
                String[] infos = data.split(",");

                try {
                    int idMonster = Integer.parseInt(infos[0]);
                    int min = Integer.parseInt(infos[1]);
                    int max = Integer.parseInt(infos[2]);
                    Monster m = World.world.getMonstre(idMonster);
                    List<MobGrade> mgs = new ArrayList<MobGrade>();
                    //on ajoute a la liste les grades possibles

                    for (MobGrade MG : m.getGrades().values())
                        if (MG.level >= min && MG.level <= max)
                            mgs.add(MG);
                    if (mgs.isEmpty())
                        continue;
                    //On prend un grade au hasard entre 0 et size -1 parmis les mobs possibles
                    this.mobs.put(guid, mgs.get(Formulas.getRandomValue(0, mgs.size() - 1)));
                    if (m.getAggroDistance() > this.aggroDistance)
                        this.aggroDistance = m.getAggroDistance();
                    guid--;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for(Entry<Integer, MobGrade> mob :this.mobs.entrySet())// kralamour
                if(mob.getValue().getTemplate().getId() == 423)
                    this.orientation = 3;

            if(!objects.isEmpty()) {
                for (String value : objects.split(",")) {
                    final GameObject gameObject = World.world.getGameObject(Integer.parseInt(value));
                    if (gameObject != null)
                        this.objects.add(gameObject);
                }
            }
        }

        public MobGroup(int id, int cellId, String groupData) {
            this.id = id;
            this.align = Constant.ALIGNEMENT_NEUTRE;

            this.cellId = cellId;
            this.isFix = true;
            int guid = -1;
            boolean star = false;
            for (String data : groupData.split(";")) {
                if (data.equalsIgnoreCase(""))
                    continue;
                String[] infos = data.split(",");
                try {
                    int idMonster = Integer.parseInt(infos[0]);
                    int min = Integer.parseInt(infos[1]);
                    int max = Integer.parseInt(infos[2]);
                    Monster m = World.world.getMonstre(idMonster);
                    List<MobGrade> mgs = new ArrayList<MobGrade>();
                    //on ajoute a la liste les grades possibles

                    for (MobGrade MG : m.getGrades().values()) {
                        if (MG.getBaseXp() != 0)
                            star = true;
                        if (MG.level >= min && MG.level <= max)
                            mgs.add(MG);
                    }
                    if (mgs.isEmpty())
                        continue;
                    //On prend un grade au hasard entre 0 et size -1 parmis les mobs possibles
                    this.mobs.put(guid, mgs.get(Formulas.getRandomValue(0, mgs.size() - 1)));
                    if (m.getAggroDistance() > this.aggroDistance)
                        this.aggroDistance = m.getAggroDistance();
                    guid--;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.orientation = (Formulas.getRandomValue(0, 3) * 2) + 1;
            this.starBonus = (short) (star ? 0 : -1);
        }

        public void setSubArea(int sa) {
            this.subarea = sa;
        }

        public void changeAgro() {
            if (!changeAgro) {
                if (this.haveMineur()) {
                    // 29 : sous-terrain
                    // 96 : exploitation mini�re d'astrub
                    // 31 : passage vers brakmar
                    if (this.subarea != 29 && this.subarea != 96
                            && this.subarea != 31) {
                        this.removeAgro(118);
                    }
                }
            }
            changeAgro = true;
        }

        public void removeAgro(int id) {
            this.aggroDistance = 0;
            for (Entry<Integer, MobGrade> e : this.mobs.entrySet()) {
                MobGrade mb = e.getValue();
                if (mb.template.getId() != id) {
                    if (mb.template.getAggroDistance() > this.aggroDistance) {
                        this.aggroDistance = mb.template.getAggroDistance();
                    }
                }
            }
        }

        public boolean haveMineur() {
            for (Entry<Integer, MobGrade> e : this.mobs.entrySet()) {
                MobGrade mb = e.getValue();
                if (mb.template.getId() == 118) {
                    return true;
                }
            }
            return false;
        }

        public int getId() {
            return this.id;
        }

        public int getCellId() {
            return this.cellId;
        }

        public void setCellId(int cellId) {
            this.cellId = cellId;
        }

        public int getOrientation() {
            return this.orientation;
        }

        public void setOrientation(int orientation) {
            this.orientation = orientation;
        }

        public int getAlignement() {
            return this.align;
        }

        public void addStarBonus() {
            if(this.getStarBonus() >= 200) {
                this.starBonus = 200;
            } else {
                this.starBonus += 15;
            }
        }

        public int getStarBonus() {
            return this.starBonus == -1 ? 0 : this.starBonus;
        }

        public int getAggroDistance() {
            return this.aggroDistance;
        }

        public boolean isFix() {
            return this.isFix;
        }

        public void setIsFix(boolean isFix) {
            this.isFix = isFix;
        }

        public boolean getIsExtraGroup() {
            return this.isExtraGroup;
        }

        public Map<Integer, MobGrade> getMobs() {
            return this.mobs;
        }

        public MobGrade getMobGradeById(int id) {
            return this.mobs.get(id);
        }

        public String getCondition() {
            return this.condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public void startCondTimer() {
            this.condTimer = new Timer();
            condTimer.schedule(new TimerTask() {
                public void run() {
                    condition = "";
                }
            }, 60000 * 10);
        }

        public void stopConditionTimer() {
            try {
                this.condTimer.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<GameObject> getObjects() {
            if(this.objects == null && Config.INSTANCE.getHEROIC())
                this.objects = new ArrayList<>();
            else if(!Config.INSTANCE.getHEROIC())
                return new ArrayList<>();
            return objects;
        }

        public String parseGM() {
            StringBuilder mobIDs = new StringBuilder();
            StringBuilder mobGFX = new StringBuilder();
            StringBuilder mobLevels = new StringBuilder();
            StringBuilder colors = new StringBuilder();
            StringBuilder toreturn = new StringBuilder();
            long totalExp = 0;
            boolean isFirst = true;
            if (this.mobs.isEmpty())
                return "";

            for (Entry<Integer, MobGrade> entry : this.mobs.entrySet()) {
                if (!isFirst) {
                    mobIDs.append(",");
                    mobGFX.append(",");
                    mobLevels.append(",");
                }
                mobIDs.append(entry.getValue().getTemplate().getId());
                mobGFX.append(entry.getValue().getTemplate().getGfxId()).append("^").append(entry.getValue().getSize());
                mobLevels.append(entry.getValue().getLevel());
                colors.append(entry.getValue().getTemplate().getColors()).append(";0,0,0,0;");
                totalExp += entry.getValue().baseXp;

                isFirst = false;
            }

            totalExp = (long) (totalExp * (starBonus / 100f + Config.INSTANCE.getRATE_XP()));
            toreturn.append("+").append(this.cellId).append(";").append(this.orientation).append(";");
            toreturn.append(getStarBonus());// bonus en pourcentage (�toile/20%) // Actuellement 1%/min
            toreturn.append(";").append(this.id).append(";").append(mobIDs).append(";-3;").append(mobGFX).append(";")
                    .append(mobLevels).append(";").append(totalExp).append(";").append(colors);
            return toreturn.toString();
        }

        public void setStarBonus(int i) {
            this.starBonus = (short) i;

        }
    }

    public static class MobGrade {
        private static int pSize = 2;
        private Monster template;
        private int grade;
        private int level;
        private int pdv;
        private int pdvMax;
        private int inFightId;
        private int init;
        private int pa;
        private int pm;
        private int size;
        private int baseXp = 10;
        private GameCase fightCell;
        private ArrayList<SpellEffect> fightBuffs = new ArrayList<SpellEffect>();
        public Map<Integer, Integer> stats = new HashMap<Integer, Integer>();
        private Map<Integer, SortStats> spells = new HashMap<Integer, SortStats>();
        private ArrayList<Integer> statsInfos = new ArrayList<Integer>();

        public MobGrade(Monster template, int grade, int level, int pa, int pm, String resists, String stats, String statsInfos, String allSpells, int pdvMax, int aInit, int xp, int n) {
            this.size = 100 + n * pSize;
            this.template = template;
            this.grade = grade;
            this.level = level;
            this.pdvMax = pdvMax;
            this.pdv = pdvMax;
            this.pa = pa;
            this.pm = pm;
            this.baseXp = xp;
            this.init = aInit;
            this.stats.clear();
            this.spells.clear();

            String[] resist = resists.split(";"), stat = stats.split(","), statInfos = statsInfos.split(";");

            for (String str : statInfos)
                this.statsInfos.add(Integer.parseInt(str));

            try {
                if(resist.length > 3) {
                    this.stats.put(Constant.STATS_ADD_RP_NEU, Integer.parseInt(resist[0]));
                    this.stats.put(Constant.STATS_ADD_RP_TER, Integer.parseInt(resist[1]));
                    this.stats.put(Constant.STATS_ADD_RP_FEU, Integer.parseInt(resist[2]));
                    this.stats.put(Constant.STATS_ADD_RP_EAU, Integer.parseInt(resist[3]));
                    this.stats.put(Constant.STATS_ADD_RP_AIR, Integer.parseInt(resist[4]));
                    this.stats.put(Constant.STATS_ADD_AFLEE, Integer.parseInt(resist[5]));
                    this.stats.put(Constant.STATS_ADD_MFLEE, Integer.parseInt(resist[6]));
                } else {
                    String[] split = resist[0].split(",");
                    this.stats.put(-1, Integer.parseInt(split[0]));
                    this.stats.put(-100, Integer.parseInt(split[1]));
                    this.stats.put(Constant.STATS_ADD_AFLEE, Integer.parseInt(resist[1]));
                    this.stats.put(Constant.STATS_ADD_MFLEE, Integer.parseInt(resist[2]));
                }

                this.stats.put(Constant.STATS_ADD_FORC, Integer.parseInt(stat[0]));
                this.stats.put(Constant.STATS_ADD_SAGE, Integer.parseInt(stat[1]));
                this.stats.put(Constant.STATS_ADD_INTE, Integer.parseInt(stat[2]));
                this.stats.put(Constant.STATS_ADD_CHAN, Integer.parseInt(stat[3]));
                this.stats.put(Constant.STATS_ADD_AGIL, Integer.parseInt(stat[4]));
                this.stats.put(Constant.STATS_ADD_DOMA, Integer.parseInt(statInfos[0]));
                this.stats.put(Constant.STATS_ADD_PERDOM, Integer.parseInt(statInfos[1]));
                this.stats.put(Constant.STATS_ADD_SOIN, Integer.parseInt(statInfos[2]));
                this.stats.put(Constant.STATS_CREATURE, Integer.parseInt(statInfos[3]));
            } catch (Exception e) {
                World.world.logger.error("#1# Erreur lors du chargement du grade du monstre (template) : " + template.getId());
                e.printStackTrace();
            }

            if (!allSpells.equalsIgnoreCase("")) {
                String[] spells = allSpells.split(";");

                for (String str : spells) {
                    if (str.equals("")) continue;
                    String[] spellInfo = str.split("@");
                    int id, lvl;

                    try {
                        id = Integer.parseInt(spellInfo[0]);
                        lvl = Integer.parseInt(spellInfo[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (id == 0 || lvl == 0) continue;
                    Spell spell = World.world.getSort(id);
                    if (spell == null) continue;
                    SortStats spellStats = spell.getStatsByLevel(lvl);
                    if (spellStats == null) continue;
                    this.spells.put(id, spellStats);
                }
            }
        }

        private MobGrade(Monster template, int grade, int level, int pdv,
                         int pdvMax, int pa, int pm,
                         Map<Integer, Integer> stats,
                         ArrayList<Integer> statsInfos,
                         Map<Integer, SortStats> spells, int xp, int n) {
            this.size = 100 + n * pSize;
            this.template = template;
            this.grade = grade;
            this.level = level;
            this.pdv = pdv;
            this.pdvMax = pdvMax;
            this.pa = pa;
            this.pm = pm;
            this.stats = stats;
            this.statsInfos = statsInfos;
            this.spells = spells;
            this.inFightId = -1;
            this.baseXp = xp;
        }

        public MobGrade getCopy() {
            Map<Integer, Integer> newStats = new HashMap<Integer, Integer>();
            newStats.putAll(this.stats);
            int n = (this.size - 100) / pSize;
            return new MobGrade(this.template, this.grade, this.level, this.pdv, this.pdvMax, this.pa, this.pm, newStats, this.statsInfos, this.spells, this.baseXp, n);
        }

        public void replaceStatsInfos(int s) {
            statsInfos.clear();
            statsInfos.add(s);
        }

        public void refresh() {
            if (this.spells.isEmpty())
                return;
            String spells = "";

            for (Entry<Integer, SortStats> entry : this.spells.entrySet()) {
                spells += (spells.isEmpty() ? entry.getKey() + ","
                        + entry.getValue().getLevel() : ";" + entry.getKey()
                        + "," + entry.getValue().getLevel());
            }

            this.spells.clear();

            if (!spells.equalsIgnoreCase("")) {
                for (String split : spells.split("\\;")) {
                    int id = Integer.parseInt(split.split("\\,")[0]);
                    this.spells.put(id, World.world.getSort(id).getStatsByLevel(Integer.parseInt(split.split("\\,")[1])));
                }
            }
        }

        public int getSize() {
            return this.size;
        }

        public Monster getTemplate() {
            return this.template;
        }

        public int getGrade() {
            return this.grade;
        }

        public int getLevel() {
            return this.level;
        }

        public int getPdv() {
            return this.pdv;
        }

        public void setPdv(int pdv) {
            this.pdv = pdv;
        }

        public int getPdvMax() {
            return this.pdvMax;
        }

        public int getInFightID() {
            return this.inFightId;
        }

        public void setInFightID(int i) {
            this.inFightId = i;
        }

        public int getInit() {
            return this.init;
        }

        public int getPa() {
            return this.pa;
        }

        public int getPm() {
            return this.pm;
        }

        public int getBaseXp() {
            return this.baseXp;
        }

        public GameCase getFightCell() {
            return this.fightCell;
        }

        public void setFightCell(GameCase cell) {
            this.fightCell = cell;
        }

        public ArrayList<SpellEffect> getBuffs() {
            return this.fightBuffs;
        }

        public Stats getStats() {
            if(this.getTemplate().getId() == 42 && !stats.containsKey(Constant.STATS_CREATURE))
                stats.put(Constant.STATS_CREATURE, 5);
            if(this.stats.get(-1) != null) {
                Map<Integer, Integer> stats = new HashMap<>();
                stats.putAll(this.stats);
                stats.remove(-1); stats.remove(-100);

                int random = Formulas.getRandomValue(210, 214);
                int one = this.stats.get(-1), all = this.stats.get(-100);

                stats.put(Constant.STATS_ADD_RP_NEU, (random == Constant.STATS_ADD_RP_NEU ? one : all));
                stats.put(Constant.STATS_ADD_RP_TER, (random == Constant.STATS_ADD_RP_TER ? one : all));
                stats.put(Constant.STATS_ADD_RP_FEU, (random == Constant.STATS_ADD_RP_FEU ? one : all));
                stats.put(Constant.STATS_ADD_RP_EAU, (random == Constant.STATS_ADD_RP_EAU ? one : all));
                stats.put(Constant.STATS_ADD_RP_AIR, (random == Constant.STATS_ADD_RP_AIR ? one : all));

                return new Stats(stats);
            }
            return new Stats(this.stats);
        }

        public Map<Integer, SortStats> getSpells() {
            return this.spells;
        }

        public void modifStatByInvocator(Fighter caster) {
            float taux = (1.0F + (caster.getLvl()) / 100.0F);
            int life = Math.round(this.pdvMax * taux);
            this.pdv = life;
            this.pdvMax = this.pdv;
            int force = this.stats.get(Constant.STATS_ADD_FORC) * (int) taux;
            int intel = this.stats.get(Constant.STATS_ADD_INTE) * (int) taux;
            int agili = this.stats.get(Constant.STATS_ADD_AGIL) * (int) taux;
            int sages = this.stats.get(Constant.STATS_ADD_SAGE) * (int) taux;
            int chanc = this.stats.get(Constant.STATS_ADD_CHAN) * (int) taux;
            this.stats.put(Constant.STATS_ADD_FORC, force);
            this.stats.put(Constant.STATS_ADD_INTE, intel);
            this.stats.put(Constant.STATS_ADD_AGIL, agili);
            this.stats.put(Constant.STATS_ADD_SAGE, sages);
            this.stats.put(Constant.STATS_ADD_CHAN, chanc);
        }
        public void modifStatByFightDifficulty(int Difficulty,Fight fight) {
            int taux = 1 ;
            switch (Difficulty){
                case 0 :
                    taux = 1;
                    break;
                case 1 :
                    taux = 2;
                    break;
                case 2 :
                case 3 :
                    taux = 4;
                    break;
                default:
                    taux = 1;
                    break;
            }

            if(Difficulty < 3) {
                int life = this.pdvMax;

                if (taux > 1) {
                    life = this.pdvMax * taux;
                }

                this.pdv = life;
                this.pdvMax = this.pdv;

                int feu = 1000 + (500 * this.getLevel());
                int intel = 1000 + (500 * this.getLevel());
                int agi = 1000 + (500 * this.getLevel());
                int sagesse = 1000 + (500 * this.getLevel());
                int chance = 1000 + (500 * this.getLevel());
                int resistance = 9 * this.getLevel();

                int force = this.stats.get(Constant.STATS_ADD_FORC) * (int) taux;
                //int intel = this.stats.get(Constant.STATS_ADD_INTE) * (int) taux;
                int agili = this.stats.get(Constant.STATS_ADD_AGIL) * (int) taux;
                int sages = this.stats.get(Constant.STATS_ADD_SAGE) * (int) taux;
                int chanc = this.stats.get(Constant.STATS_ADD_CHAN) * (int) taux;

                //this.stats.clear();
                this.stats.put(Constant.STATS_ADD_FORC, this.stats.get(Constant.STATS_ADD_FORC) * taux);
                this.stats.put(Constant.STATS_ADD_INTE, this.stats.get(Constant.STATS_ADD_INTE) * taux);
                this.stats.put(Constant.STATS_ADD_AGIL, this.stats.get(Constant.STATS_ADD_AGIL) * taux);
                this.stats.put(Constant.STATS_ADD_SAGE, this.stats.get(Constant.STATS_ADD_SAGE) * taux);
                this.stats.put(Constant.STATS_ADD_CHAN, this.stats.get(Constant.STATS_ADD_CHAN) * taux);
                if (this.getStats().getEffect(Constant.STATS_ADD_DOMA) == 0) {
                    this.stats.put(Constant.STATS_ADD_DOMA, (taux * taux));
                } else {
                    this.stats.put(Constant.STATS_ADD_DOMA, this.stats.get(Constant.STATS_ADD_DOMA) * taux);
                }
                if (this.getStats().getEffect(Constant.STATS_ADD_DOMA2) == 0) {
                    this.stats.put(Constant.STATS_ADD_DOMA2, (taux * taux));
                } else {
                    this.stats.put(Constant.STATS_ADD_DOMA2, this.stats.get(Constant.STATS_ADD_DOMA2) * taux);
                }
                if (this.getStats().getEffect(Constant.STATS_ADD_SOIN) == 0) {
                    this.stats.put(Constant.STATS_ADD_SOIN, (taux * taux));
                } else {
                    this.stats.put(Constant.STATS_ADD_SOIN, this.stats.get(Constant.STATS_ADD_SOIN) * taux);
                }
                if (this.getStats().getEffect(Constant.STATS_ADD_INIT) == 0) {
                    this.stats.put(Constant.STATS_ADD_INIT, (taux * taux));
                } else {
                    this.stats.put(Constant.STATS_ADD_INIT, this.stats.get(Constant.STATS_ADD_SOIN) * taux);
                }

                this.stats.put(Constant.STATS_ADD_RP_NEU, this.stats.get(Constant.STATS_ADD_RP_NEU) + (taux * taux));
                this.stats.put(Constant.STATS_ADD_RP_FEU, this.stats.get(Constant.STATS_ADD_RP_FEU) + (taux * taux));
                this.stats.put(Constant.STATS_ADD_RP_EAU, this.stats.get(Constant.STATS_ADD_RP_EAU) + (taux * taux));
                this.stats.put(Constant.STATS_ADD_RP_AIR, this.stats.get(Constant.STATS_ADD_RP_AIR) + (taux * taux));
                this.stats.put(Constant.STATS_ADD_RP_TER, this.stats.get(Constant.STATS_ADD_RP_TER) + (taux * taux));
                this.pa = this.pa + taux;
                this.pm = this.pm + taux;

                this.stats.put(Constant.STATS_ADD_PO, taux);
                this.stats.put(Constant.STATS_CREATURE, this.stats.get(Constant.STATS_CREATURE) + taux);
            }
            else{
               Map<Integer, Fighter> Joueurs = fight.getTeam0();
               Map<Integer, Fighter> Monstres = fight.getTeam1();
               ArrayList<Fighter> Joueurs2 = new ArrayList<>(), Monstres2 = new ArrayList<>();

                Joueurs2.addAll(Joueurs.values());
                Monstres2.addAll(Monstres.values());
                int levelMaxMonstre = fight.LvlMaxJoueurs(Monstres2);
                int levelMoyenJoueurs = fight.LvlMoyenJoueurs(Joueurs2);
                int leveldepartMonstre = this.level;
                int levelarrivemonstre = leveldepartMonstre;

                if(levelMaxMonstre < levelMoyenJoueurs) {

                    int life = this.pdvMax;
                    int lifeperlevel = life/leveldepartMonstre;


                    if (lifeperlevel > 30) {
                        lifeperlevel = 30;
                    }


                    // On fait un ratio des stats par level
                    double ratioForce = getRatioStatifExist(Constant.STATS_ADD_FORC, taux);
                    double ratioIntel = getRatioStatifExist(Constant.STATS_ADD_INTE, taux);
                    double ratioChance = getRatioStatifExist(Constant.STATS_ADD_CHAN, taux);
                    double ratioAgil = getRatioStatifExist(Constant.STATS_ADD_AGIL, taux);
                    double ratioSages = getRatioStatifExist(Constant.STATS_ADD_SAGE, taux);
                    double ratioDomma = getRatioPoidifExist(Constant.STATS_ADD_DOMA, 0.5);
                    double ratioSoin = getRatioPoidifExist(Constant.STATS_ADD_SOIN, 0.25);
                    double ratioResNeu = getRatioPoidifExist(Constant.STATS_ADD_RP_NEU, taux);
                    if(ratioResNeu>0.25)
                        ratioResNeu = 0.25;
                    double ratioResTer = getRatioPoidifExist(Constant.STATS_ADD_RP_TER, taux);
                    if(ratioResTer>0.25)
                        ratioResTer = 0.25;
                    double ratioResFeu = getRatioPoidifExist(Constant.STATS_ADD_RP_FEU, taux);
                    if(ratioResFeu>0.25)
                        ratioResFeu = 0.25;
                    double ratioResEau = getRatioPoidifExist(Constant.STATS_ADD_RP_EAU, taux);
                    if(ratioResEau>0.25)
                        ratioResEau = 0.25;
                    double ratioResAgi = getRatioPoidifExist(Constant.STATS_ADD_RP_AIR, taux);
                    if(ratioResAgi>0.25)
                        ratioResAgi = 0.25;
                    double ratioPO = getRatioPoidifExist(Constant.STATS_ADD_PO, 0.05);
                    double ratioCrea = getRatioPoidifExist(Constant.STATS_CREATURE, 0.1);

                    //double[] x = new double[]{ratioForce,ratioIntel,ratioChance,ratioAgil,ratioSages,ratioDomma,ratioSoin,ratioIni,ratioResNeu,ratioResTer,ratioResFeu,ratioResEau,ratioResAgi};

                    if (levelMaxMonstre == leveldepartMonstre) {
                        this.level = levelMoyenJoueurs;
                        levelarrivemonstre = levelMoyenJoueurs;
                    } else {
                        levelarrivemonstre = levelMoyenJoueurs - leveldepartMonstre;
                        this.level = levelMoyenJoueurs - leveldepartMonstre;
                    }

                    int diff = levelarrivemonstre - leveldepartMonstre;
                    System.out.println(diff +" lvl de diff");

                    this.stats.put(Constant.STATS_ADD_FORC, (int) Math.round(this.level * ratioForce));
                    this.stats.put(Constant.STATS_ADD_INTE, (int) Math.round(this.level * ratioIntel));
                    this.stats.put(Constant.STATS_ADD_CHAN, (int) Math.round(this.level * ratioChance));
                    this.stats.put(Constant.STATS_ADD_AGIL, (int) Math.round(this.level * ratioAgil));
                    this.stats.put(Constant.STATS_ADD_SAGE, (int) Math.round(this.level * ratioSages));
                    this.stats.put(Constant.STATS_ADD_DOMA, (int) Math.round(this.level * ratioDomma));
                    this.stats.put(Constant.STATS_ADD_SOIN, (int) Math.round(this.level * ratioSoin));
                    this.stats.put(Constant.STATS_ADD_RP_NEU, (int) Math.round(this.stats.get(Constant.STATS_ADD_RP_NEU) + Math.abs(diff*ratioResNeu)));
                    this.stats.put(Constant.STATS_ADD_RP_TER, (int) Math.round(this.stats.get(Constant.STATS_ADD_RP_TER) + Math.abs(diff*ratioResTer)));
                    this.stats.put(Constant.STATS_ADD_RP_FEU, (int) Math.round(this.stats.get(Constant.STATS_ADD_RP_FEU) + Math.abs(diff*ratioResFeu)));
                    this.stats.put(Constant.STATS_ADD_RP_EAU, (int) Math.round(this.stats.get(Constant.STATS_ADD_RP_EAU) + Math.abs(diff*ratioResEau)));
                    this.stats.put(Constant.STATS_ADD_RP_AIR, (int) Math.round(this.stats.get(Constant.STATS_ADD_RP_AIR) + Math.abs(diff*ratioResAgi)));
                    this.stats.put(Constant.STATS_ADD_PO, (int) Math.round(this.level * ratioPO));
                    this.stats.put(Constant.STATS_CREATURE, (int) Math.round(this.level * ratioCrea));

                    this.pdv = lifeperlevel * this.level;
                    this.pdvMax = this.pdv;


                    System.out.println(this.stats.get(Constant.STATS_ADD_FORC) +" force");
                    System.out.println(this.stats.get(Constant.STATS_ADD_INTE)+" intel");
                    System.out.println(this.stats.get(Constant.STATS_ADD_CHAN)+" cha");
                    System.out.println(this.stats.get(Constant.STATS_ADD_AGIL)+" agi");
                    System.out.println(this.stats.get(Constant.STATS_ADD_SAGE)+" sagesse");
                    System.out.println(this.stats.get(Constant.STATS_ADD_DOMA)+" dommage");
                    System.out.println(this.stats.get(Constant.STATS_ADD_SOIN)+" soin");
                    System.out.println(this.stats.get(Constant.STATS_ADD_RP_NEU)+" Re neu");
                    System.out.println(this.stats.get(Constant.STATS_ADD_RP_TER)+" Re ter");
                    System.out.println(this.stats.get(Constant.STATS_ADD_RP_FEU)+" Re feu");
                    System.out.println(this.stats.get(Constant.STATS_ADD_RP_EAU)+" Re eau");
                    System.out.println(this.stats.get(Constant.STATS_ADD_RP_AIR)+" Re air");
                    System.out.println(this.stats.get(Constant.STATS_ADD_PO)+" PO");
                    System.out.println(this.stats.get(Constant.STATS_CREATURE)+" Crea");


                    //int ratioForce = Math.round(this.stats.get(Constant.STATS_ADD_FORC) / this.getLevel()) ;
                    int pas = this.pa;
                    int pms = this.pm;

                    int maxPatoAdd = pas / 2;
                    int maxPmtoAdd = pms / 2;

                    float tranche = 200 / maxPatoAdd;
                    float tranche2 = 200 / maxPmtoAdd;
                    float tranche3 = 200 / maxPmtoAdd;
                    float tranche4 = 200 / maxPmtoAdd;

                    int nombrePA = Math.round(diff / tranche);
                    if (nombrePA < 1) {
                        nombrePA = 1;
                    }
                    int nombrePM = Math.round(diff / tranche2);
                    if (nombrePA < 1) {
                        nombrePM = 1;
                    }

                    this.pa = pas + nombrePA;
                    this.pm = pms + nombrePM;
                    System.out.println(this.pa+ " PA");
                    System.out.println(this.pm+ " PM");
                }
                else{
                    int life = this.pdvMax;

                    if (taux > 1) {
                        life = this.pdvMax * taux;
                    }

                    this.pdv = life;
                    this.pdvMax = this.pdv;

                    int feu = 1000 + (500 * this.getLevel());
                    int intel = 1000 + (500 * this.getLevel());
                    int agi = 1000 + (500 * this.getLevel());
                    int sagesse = 1000 + (500 * this.getLevel());
                    int chance = 1000 + (500 * this.getLevel());
                    int resistance = 9 * this.getLevel();

                    int force = this.stats.get(Constant.STATS_ADD_FORC) * (int) taux;
                    //int intel = this.stats.get(Constant.STATS_ADD_INTE) * (int) taux;
                    int agili = this.stats.get(Constant.STATS_ADD_AGIL) * (int) taux;
                    int sages = this.stats.get(Constant.STATS_ADD_SAGE) * (int) taux;
                    int chanc = this.stats.get(Constant.STATS_ADD_CHAN) * (int) taux;

                    //this.stats.clear();
                    this.stats.put(Constant.STATS_ADD_FORC, this.stats.get(Constant.STATS_ADD_FORC) * taux);
                    this.stats.put(Constant.STATS_ADD_INTE, this.stats.get(Constant.STATS_ADD_INTE) * taux);
                    this.stats.put(Constant.STATS_ADD_AGIL, this.stats.get(Constant.STATS_ADD_AGIL) * taux);
                    this.stats.put(Constant.STATS_ADD_SAGE, this.stats.get(Constant.STATS_ADD_SAGE) * taux);
                    this.stats.put(Constant.STATS_ADD_CHAN, this.stats.get(Constant.STATS_ADD_CHAN) * taux);
                    if (this.getStats().getEffect(Constant.STATS_ADD_DOMA) == 0) {
                        this.stats.put(Constant.STATS_ADD_DOMA, (taux * taux));
                    } else {
                        this.stats.put(Constant.STATS_ADD_DOMA, this.stats.get(Constant.STATS_ADD_DOMA) * taux);
                    }
                    if (this.getStats().getEffect(Constant.STATS_ADD_DOMA2) == 0) {
                        this.stats.put(Constant.STATS_ADD_DOMA2, (taux * taux));
                    } else {
                        this.stats.put(Constant.STATS_ADD_DOMA2, this.stats.get(Constant.STATS_ADD_DOMA2) * taux);
                    }
                    if (this.getStats().getEffect(Constant.STATS_ADD_SOIN) == 0) {
                        this.stats.put(Constant.STATS_ADD_SOIN, (taux * taux));
                    } else {
                        this.stats.put(Constant.STATS_ADD_SOIN, this.stats.get(Constant.STATS_ADD_SOIN) * taux);
                    }
                    if (this.getStats().getEffect(Constant.STATS_ADD_INIT) == 0) {
                        this.stats.put(Constant.STATS_ADD_INIT, (taux * taux));
                    } else {
                        this.stats.put(Constant.STATS_ADD_INIT, this.stats.get(Constant.STATS_ADD_SOIN) * taux);
                    }

                    this.stats.put(Constant.STATS_ADD_RP_NEU, this.stats.get(Constant.STATS_ADD_RP_NEU) + (taux * taux));
                    this.stats.put(Constant.STATS_ADD_RP_FEU, this.stats.get(Constant.STATS_ADD_RP_FEU) + (taux * taux));
                    this.stats.put(Constant.STATS_ADD_RP_EAU, this.stats.get(Constant.STATS_ADD_RP_EAU) + (taux * taux));
                    this.stats.put(Constant.STATS_ADD_RP_AIR, this.stats.get(Constant.STATS_ADD_RP_AIR) + (taux * taux));
                    this.stats.put(Constant.STATS_ADD_RP_TER, this.stats.get(Constant.STATS_ADD_RP_TER) + (taux * taux));
                    this.pa = this.pa + taux;
                    this.pm = this.pm + taux;

                    this.stats.put(Constant.STATS_ADD_PO, taux);
                    this.stats.put(Constant.STATS_CREATURE, this.stats.get(Constant.STATS_CREATURE) + taux);

                }
            }
            //Stats stats3 = new Stats(stats2);
            //this.stats = stats3;
            //System.out.println( "Stats : " +  this.getPa() + " PA " + this.getPm() + " PM");

        }

        public int getRatioStatifExist(int stat,int taux){
            int ratio = 0;
            if(this.getStats().getEffect(stat) != 0) {
                ratio = Math.round(this.stats.get(stat) / this.getLevel());
            }
            else{
                ratio = taux;
            }
            return ratio;
        }

        public double getRatioPoidifExist(int stat,double ratio2){
            double ratio = 0;
            if(this.getStats().getEffect(stat) != 0) {
                 ratio = Math.round(this.stats.get(stat) / this.getLevel());
            }
            else{
                 ratio = ratio2;
            }
            return ratio;
        }

        public void changeStats(String statInfos) {
            this.stats.clear();
            String[] stats = statInfos.split(",");
            for(String s : stats)
            {
                String[] details = s.split(":");
                this.stats.put(Integer.parseInt(details[0]), Integer.parseInt(details[1]));
            }
        }

        public String stringStatsActualizado() {
            StringBuilder strStats = new StringBuilder();
            strStats.append(Constant.STATS_ADD_PA).append(":").append(getPa()).append(",");
            strStats.append(Constant.STATS_ADD_PM).append(":").append(getPm()).append(",");
            stats.forEach((key, value) -> {
                switch (key) {
                    case Constant.STATS_ADD_PA:
                    case Constant.STATS_ADD_PM:
                    case Constant.STATS_CREATURE:
                    case Constant.STATS_ADD_CHAN:
                    case Constant.STATS_ADD_AGIL:
                    case Constant.STATS_ADD_FORC:
                    case Constant.STATS_ADD_INTE :
                    case Constant.STATS_ADD_INIT :
                    {
                        if (!strStats.isEmpty()) {
                            strStats.append(",");
                        }
                        strStats.append(key).append(":").append(value);
                    }
                }
            });
            strStats.append(",").append(Constant.STATS_ADD_INIT).append(":").append(getInit());
            return strStats.toString();
        }

        public String getStringResi() {
            String Resi ="";
            Resi = this.stats.get(Constant.STATS_ADD_RP_NEU) + "," + this.stats.get(Constant.STATS_ADD_RP_TER) + "," + this.stats.get(Constant.STATS_ADD_RP_FEU) + "," + this.stats.get(Constant.STATS_ADD_RP_EAU) + "," +this.stats.get(Constant.STATS_ADD_RP_AIR);
            return Resi;
        }


    }
}
