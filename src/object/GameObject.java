package object;

import area.map.entity.Trunk;
import client.Account;
import client.Player;
import client.other.Stats;
import common.Formulas;
import common.SocketManager;
import database.Database;
import entity.mount.Mount;
import entity.pet.Pet;
import entity.pet.PetEntry;
import fight.spells.SpellEffect;
import game.world.World;
import game.world.World.Couple;
import hdv.HdvEntry;
import job.JobAction;
import kernel.Constant;
import object.entity.Fragment;

import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameObject {

    protected ObjectTemplate template;
    protected int quantity = 1;
    protected int position = Constant.ITEM_POS_NO_EQUIPED;
    protected int guid;
    protected int obvijevanPos;
    protected int obvijevanLook;
    protected int obvijevanId;
    protected int puit;
    protected int rarity;
    private Stats Stats = new Stats();
    private ArrayList<SpellEffect> Effects = new ArrayList<>();
    private ArrayList<String> SortStats = new ArrayList<>();
    private Map<Integer, String> txtStats = new HashMap<>();
    private Map<Integer, Integer> SoulStats = new HashMap<>();
    private int mimibiote;

    public byte modification = -1;

    public GameObject(int Guid, int template, int qua, int pos, String strStats, int puit,int rarity, int mimibiote) {
        this.guid = Guid;
        this.template = World.world.getObjTemplate(template);
        this.quantity = qua;
        this.position = pos;
        this.puit = puit;
        this.rarity = rarity;
        this.mimibiote = mimibiote;
        Stats = new Stats();
        this.parseStringToStats(strStats, false);
    }

    public GameObject(int Guid) {
        this.guid = Guid;
        this.template = World.world.getObjTemplate(8378);
        this.quantity = 1;
        this.position = -1;
        this.puit = 0;
        this.rarity = 0;
    }

    public GameObject(int Guid, int template, int qua, int pos, Stats stats, ArrayList<SpellEffect> effects, Map<Integer, Integer> _SoulStat, Map<Integer, String> _txtStats, int puit, int rarity, int mimibiote) {
        this.guid = Guid;
        this.template = World.world.getObjTemplate(template);
        this.quantity = qua;
        this.position = pos;
        this.Stats = stats;
        this.Effects = effects;
        this.SoulStats = _SoulStat;
        this.txtStats = _txtStats;
        this.obvijevanPos = 0;
        this.obvijevanLook = 0;
        this.obvijevanId = 0;
        this.puit = puit;
        this.rarity = rarity;
        this.mimibiote = mimibiote;
    }

    public static GameObject getCloneObjet(GameObject obj, int qua) {
        Map<Integer, Integer> maps = new HashMap<>();
        maps.putAll(obj.getStats().getMap());
        Stats newStats = new Stats(maps);

        GameObject ob = new GameObject(Database.getStatics().getObjectData().getNextId(), obj.getTemplate().getId(), qua, Constant.ITEM_POS_NO_EQUIPED, newStats, obj.getEffects(), obj.getSoulStat(), obj.getTxtStat(), obj.getPuit(),obj.getRarity(), obj.getMimibiote());
        ob.modification = 0;
        return ob;
    }

    public int getMimibiote()
    {
        return mimibiote;
    }

    public void setMimibiote(int mimibiote)
    {
        this.mimibiote = mimibiote;
    }

    public int setId() {
        this.guid = Database.getStatics().getObjectData().getNextId();
        return this.getGuid();
    }

    public int getPuit() {
        return this.puit;
    }

    public void setPuit(int puit) {
        this.puit = puit;
    }

    public int getObvijevanPos() {
        return obvijevanPos;
    }

    public void setObvijevanPos(int pos) {
        obvijevanPos = pos;
        this.setModification();
    }

    public int getObvijevanLook() {
        return obvijevanLook;
    }

    public void setObvijevanLook(int look) {
        obvijevanLook = look;
        this.setModification();
    }

    public void setObvijevanId(int id) {
        this.obvijevanId = id;
    }
    public int getObvijevanId() {
        return this.obvijevanId;
    }

    public void setModification() {
        if(this.modification == -1)
            this.modification = 1;
    }
    public String stringObjetoConGuiño() {
        StringBuilder str = new StringBuilder();
        try {
            str.append(Integer.toHexString(guid)).append("~")
                    .append(Integer.toHexString(getTemplate().getId())).append("~").append(
                    Integer.toHexString(quantity))
                    .append("~");
                            if (position == Constant.ITEM_POS_NO_EQUIPED)
                                str.append("");
                            else
                            {
                                str.append(Integer.toHexString(position));
                            }
                    str.append("~").append(parseStatsString()).append("~").append(getTemplate().getPrice() / 10);
            str.append(";");
        } catch (Exception e) {
            System.out.println("OBJETO BUG stringObjetoConGuiño " + guid + " Exception: " + e.toString());
        }
        return str.toString();
    }

    public void parseStringToStats(String strStats, boolean save) {
        if(this.template != null & this.template.getId() == 7010) return;
        String dj1 = "";
        if (!strStats.equalsIgnoreCase("")) {

            for (String split : strStats.split(",")) {
                try {
                    if (split.equalsIgnoreCase(""))
                        continue;
                    if (split.substring(0, 3).equalsIgnoreCase("325") && (this.getTemplate().getId() == 10207 || this.getTemplate().getId() == 10601)) {
                        txtStats.put(Constant.STATS_DATE, split.substring(3) + "");
                        continue;
                    }

                    if (split.substring(0, 3).equalsIgnoreCase("3dc")) {// Si c'est une rune de signature crï¿½e
                        txtStats.put(Constant.STATS_SIGNATURE, split.split("#")[4]);
                        continue;
                    }
                    if (split.substring(0, 3).equalsIgnoreCase("3d9")) {// Si c'est une rune de signature modifiï¿½
                        txtStats.put(Constant.STATS_CHANGE_BY, split.split("#")[4]);
                        continue;
                    }

                    String[] stats = split.split("#");
                    int id = Integer.parseInt(stats[0], 16);
                    if (id == Constant.STATS_PETS_DATE
                            && this.getTemplate().getType() == Constant.ITEM_TYPE_CERTIFICAT_CHANIL) {
                        txtStats.put(id, split.substring(3));
                        continue;
                    }
                    if(id == Constant.STATS_PETS_PDV || id == Constant.STATS_PETS_POIDS)
                    {
                        if(stats[3].contains("1d"))
                        {
                            txtStats.put(id, stats[3].substring(0,2));
                        }
                        else {
                            txtStats.put(id, stats[3]);
                        }
                        continue;
                    }
                    if (id == Constant.STATS_CHANGE_BY || id == Constant.STATS_NAME_TRAQUE || id == Constant.STATS_OWNER_1) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (id == Constant.STATS_GRADE_TRAQUE || id == Constant.STATS_ALIGNEMENT_TRAQUE || id == Constant.STATS_NIVEAU_TRAQUE) {
                        txtStats.put(id, stats[3]);
                        continue;
                    }
                    if (id == Constant.STATS_PETS_SOUL) {
                        SoulStats.put(Integer.parseInt(stats[1], 16), Integer.parseInt(stats[3], 16)); // put(id_monstre, nombre_tuï¿½)
                        continue;
                    }
                    if (id == Constant.STATS_NAME_DJ) {
                        dj1 += (!dj1.isEmpty() ? "," : "") + stats[3];
                        txtStats.put(Constant.STATS_NAME_DJ, dj1);
                        continue;
                    }
                    if(id == Constant.COMPATIBLE_AVEC) {
                        txtStats.put(Constant.COMPATIBLE_AVEC, stats[4]);
                        continue;
                    }

                    if (id == 997 || id == 996) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (this.template != null && this.template.getId() == 77 && id == Constant.STATS_PETS_DATE) {
                        txtStats.put(id, split.substring(3));
                        continue;
                    }
                    if (id == Constant.STATS_DATE) {
                        txtStats.put(id, stats[3]);
                        continue;
                    }
                    if(id == Constant.APPARAT_ITEM) {
                        txtStats.put(id, stats[3]);
                        continue;
                    }
                    //Si stats avec Texte (Signature, apartenance, etc)//FIXME
                    if (id != Constant.STATS_RESIST  && (id < 970 || id > 974) && (!stats[3].equals("") && (!stats[3].equals("0") || id == Constant.STATS_PETS_DATE || id == Constant.STATS_PETS_PDV || id == Constant.STATS_PETS_POIDS || id == Constant.STATS_PETS_EPO || id == Constant.STATS_PETS_REPAS))) {//Si le stats n'est pas vide et (n'est pas ï¿½gale ï¿½ 0 ou est de type familier)
                        if (!(this.getTemplate().getType() == Constant.ITEM_TYPE_CERTIFICAT_CHANIL && id == Constant.STATS_PETS_DATE)) {

                            txtStats.put(id, stats[3]);
                            continue;
                        }
                    }
                    if (id == Constant.STATS_RESIST && this.getTemplate() != null && this.getTemplate().getType() == 93) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }
                    if (id == Constant.STATS_RESIST) {
                        txtStats.put(id, stats[4]);
                        continue;
                    }

                    if (id >= 281 && id <= 294) {
                        int jet = Integer.parseInt(stats[1], 16);
                        Stats.addOneStat(id, jet);
                        continue;
                    }

                    if(id >= 970 && id <= 974)
                    {
                        int jet = Integer.parseInt(stats[3]);
                        if(id == Constant.OBVIJEVANT_SKIN)
                        {
                            setObvijevanLook(Integer.parseInt(stats[3], 16));
                        } else if(id == Constant.REAL_TYPE)
                        {
                            setObvijevanPos(Integer.parseInt(stats[3], 16));
                        } else if(id == 970)
                        {
                            setObvijevanId(Integer.parseInt(stats[3], 16));
                        }
                        txtStats.put(id, String.valueOf(jet));
                        Stats.addOneStat(id, jet);
                        continue;
                    }

                    boolean follow1 = true;
                    switch (id) {
                        case 110:
                        case 139:
                        case 605:
                        case 614:
                            String min = stats[1];
                            String max = stats[2];
                            String jet = stats[4];
                            String args = min + ";" + max + ";-1;-1;0;" + jet;
                            Effects.add(new SpellEffect(id, args, 0, -1));
                            follow1 = false;
                            break;
                    }
                    if (!follow1) {
                        continue;
                    }

                    boolean follow2 = true;
                    for (int a : Constant.ARMES_EFFECT_IDS) {
                        if (a == id) {
                            Effects.add(new SpellEffect(id, stats[1] + ";" + stats[2] + ";-1;-1;0;" + stats[4], 0, -1));
                            follow2 = false;
                        }
                    }
                    if (!follow2)
                        continue;//Si c'ï¿½tait un effet Actif d'arme ou une signature

                    String statsString = stats[0];
                    // Gestyion des dommages bizarres


                    int statMax = JobAction.getStatBaseMaxLegendaire(this.template, statsString );

                    if(this.getTemplate().getType() == Constant.ITEM_TYPE_FAMILIER){

                            //System.out.println("C'est un familier " + this.getTemplate().getName());

                            //statMax =  JobAction.getStatBaseMaxLegendaire(this.template, statsString );
                            Pet pets = World.world.getPets(this.template.getId() );
                            statMax = pets.getMax();
                            if (statMax < Integer.parseInt(stats[1], 16) && this.getTemplate().getId() != 6894) {
                                //System.out.println(this.getTemplate().getName() + " " +statMax + " plus petit que " + Integer.parseInt(stats[1], 16));
                                System.out.println("["+ this.getGuid() +"]"+ "!! Familier illégal :"+  this.template.getName() + " On ignore la stat "+ id + " Car valeur " + Integer.parseInt(stats[1], 16) + " alors que max " + statMax);
                                txtStats.put(Constant.STATS_CHANGE_BY, "Arwase [corrigé]");
                                Stats.addOneStat(id, statMax);
                            }
                            else{
                                Stats.addOneStat(id, Integer.parseInt(stats[1], 16) );
                            }

                    }



                    if( statMax == 0 && statsString.equals("70")){
                        statsString = "79";
                        statMax = JobAction.getStatBaseMaxLegendaire(this.template, statsString );
                        id=121;
                    }

                    if( statMax == 0 && statsString.equals("79")  ) {
                        statsString = "70";
                        statMax = JobAction.getStatBaseMaxLegendaire(this.template, statsString );
                        id=112;
                    }

                    int poidmax = 151;
                    double poiduni = JobAction.getPwrPerEffet(id);
                    int poidligne = (int) Math.round( Integer.parseInt(stats[1], 16) *poiduni);


                    if(this.getTemplate().getType() != Constant.ITEM_TYPE_FAMILIER &&  this.getTemplate().getType() != Constant.ITEM_TYPE_FANTOME_FAMILIER  &&  this.getTemplate().getType() != Constant.ITEM_TYPE_CERTIFICAT_CHANIL) {
                        if(poidligne <= poidmax) {
                            Stats.addOneStat(id, Integer.parseInt(stats[1], 16));
                        }
                        else {
                            if(statMax >= Integer.parseInt(stats[1], 16) ) {
                                Stats.addOneStat(id, Integer.parseInt(stats[1], 16));
                            }
                            else {
                                System.out.println("["+ this.getGuid() +"]"+ "!! Item illégal :"+  this.template.getName() + " On ignore la stat "+ id + " Car valeur " + Integer.parseInt(stats[1], 16) + " Donc ligne a " + poidligne + " alors que max " + statMax);
                                txtStats.put(Constant.STATS_CHANGE_BY, "Arwase [Use-faille]");
                            }
                        }
                    }
                    else {
                        if(this.getTemplate().getType() != Constant.ITEM_TYPE_FAMILIER) {
                            Stats.addOneStat(id, Integer.parseInt(stats[1], 16));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if(save)
            this.setModification();
    }

    public void addTxtStat(int i, String s) {
        txtStats.put(i, s);
        this.setModification();
    }

    public static String whoGotObject(GameObject gameObject) {
        int id = gameObject.getGuid();

        Collection<Account> compte = World.world.getAccounts();
        Player Owner = null;
        String Origine = null;


        //Pour tous les comptes
        for (Account s : compte) {
            boolean test = false;
            // On test la banques du compte
            if (s.getBank().contains(gameObject)) {
                Owner = Account.getFirstPlayer(s);
                Origine = "banque du joueur :" + s.getId();
                continue;
            }

            // On test les hvds du compte
            Map<Integer, ArrayList<HdvEntry>> hdvs = World.world.getMyItems(s.getId());
            for (Entry<Integer, ArrayList<HdvEntry>> iteminhdv : hdvs.entrySet()) {
                ArrayList<HdvEntry> items = iteminhdv.getValue();
                if (items.contains(gameObject)) {
                    Owner = Account.getFirstPlayer(s);
                    Origine = "En vente hdv :" + iteminhdv.getKey()  + " joueur " + Owner.getId() ;
                    continue;
                }
            }

            if (Origine != null) {
                continue;
            }
            // On test les  joueurs du compte
            Map<Integer, Player> players = s.getPlayers();
            for (Entry<Integer, Player> joueurs : players.entrySet()) {
                Player joueur = joueurs.getValue();

                Map<Integer, GameObject> objectsOfPlayer = joueur.getItems();
                for (Entry<Integer, GameObject> item : objectsOfPlayer.entrySet()) {
                    if (item.getValue() == gameObject) {
                        Owner = joueur;
                        Origine = "inventaire du joueur" + joueur.getId();
                        continue;
                    }

                }

                Map<Integer, Integer> objectsventeofPlayer = joueur.getStoreItems();
                for (Entry<Integer, Integer> itemenvente : objectsventeofPlayer.entrySet()) {
                    if (itemenvente.getValue() == gameObject.getGuid()) {
                        Owner = joueur;
                        Origine = "inventaire de vente du joueur";
                        continue;
                    }
                }

            }

        }

        // Pour tous les coffres
        if (Origine == null){
            Map<Integer, Trunk> alltrunks = World.world.getTrunks();
            for (Entry<Integer, Trunk> trunks : alltrunks.entrySet()) {
                int idOwner = trunks.getValue().getOwnerId();
                int idcoffre = trunks.getValue().getId();

                Map<Integer, GameObject> ObjectinTrunks = trunks.getValue().getObject();

                for (Entry<Integer, GameObject> object : ObjectinTrunks.entrySet()) {
                    if (object.getValue() == gameObject) {
                        Owner = World.world.getPlayer(idOwner);
                        Origine = "Dans un coffre " + idcoffre + " Appartenant a " + idOwner;
                        continue;
                    }
                }
            }
       }
        //Pour tous les percepteurs



        return Origine;
    }

    public static Collection<GameObject> getunkownOriginObject(){
        Collection<GameObject> objCOll = new ArrayList();
        CopyOnWriteArrayList<GameObject> allobjct = World.world.getGameObjects();

        for (GameObject object : allobjct) {
            String lol = whoGotObject(object);

            if(lol == null){
                //System.out.println( object.getGuid() +" - Pas trouvé" );
                objCOll.add(object);
            }
            else{
                System.out.println( object.getGuid() +" - " +lol);
            }
        }

        return  objCOll;
    }

    public String getTraquedName() {
        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            if (Integer.toHexString(entry.getKey()).compareTo("3dd") == 0) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Stats getStats() {
        return Stats;
    }

    public void setStats(Stats SS) {
        Stats = SS;
        this.setModification();
    }

    public String stringObjectMimibiote()
    {
        StringBuilder str = new StringBuilder(String.valueOf(Integer.toHexString(this.guid))
        + "~" + Integer.toHexString(getTemplate().getId())
        + "~" + Integer.toHexString(getQuantity()) + "~"
                + ((getPosition() == -1) ? "" : Integer.toHexString(getPosition()) + "~"
        + this.parseStatsString()));

        return str.toString();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            quantity = 0;
        }
        if (quantity >= 100000)
        {
            quantity = 1;
        }
        this.quantity = quantity;
        this.setModification();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.setModification();
        this.position = position;
    }

    public ObjectTemplate getTemplate() {
        return template;
    }

    public void setTemplate(int Tid) {
        this.setModification();
        this.template = World.world.getObjTemplate(Tid);
    }

    public int getGuid() {
        return guid;
    }

    public Map<Integer, Integer> getSoulStat() {
        return SoulStats;
    }

    public Map<Integer, String> getTxtStat() {
        return txtStats;
    }

    public void setExchangeIn(Player player) {

        this.setModification();
    }
    public Mount setMountStats(Player player, Mount mount, boolean castrated) {
        if(mount == null)
            mount = new Mount(Constant.getMountColorByParchoTemplate(this.getTemplate().getId()), player.getId(), false);
        if(castrated) mount.setCastrated();

        this.clearStats();
        this.getStats().addOneStat(995, - (mount.getId()));
        this.getTxtStat().put(996, player.getName());
        this.getTxtStat().put(997, mount.getName());
        this.setModification();
        return mount;
    }

    public void attachToPlayer(Player player) {
        this.getTxtStat().put(Constant.STATS_OWNER_1, player.getName());
        SocketManager.GAME_SEND_UPDATE_OBJECT_DISPLAY_PACKET(player, this);
        this.setModification();
    }

    public boolean isAttach() {
        boolean ok = this.getTxtStat().containsKey(Constant.STATS_OWNER_1);

        if(ok) {
            Player player = World.world.getPlayerByName(this.getTxtStat().get(Constant.STATS_OWNER_1));
            if(player != null) player.send("BN");
        }

        return ok;
    }

    public String parseItem() {
        String posi = position == Constant.ITEM_POS_NO_EQUIPED ? "" : Integer.toHexString(position);
        return Integer.toHexString(guid) + "~"
                + Integer.toHexString(template.getId()) + "~"
                + Integer.toHexString(quantity) + "~" + posi + "~"
                + parseStatsString() +  "~"
                + rarity + ";";
    }

    public String parseItemNormal() {
        String posi = position == Constant.ITEM_POS_NO_EQUIPED ? "" : Integer.toHexString(position);
        return guid + "~"
                + template.getId() + "~"
                + quantity + "~" + posi + "~"
                + parseStatsString() +  "~"
                + rarity + ";";
    }

    public String getParamStatTexto(int stat, int parametro) {
        try {
            String s = getTxtStat().get(stat);
            if (!s.isEmpty()) {
                if (s.split("#").length > parametro - 1) {
                    return s.split("#")[parametro - 1];
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    public String parseStatsString() {
        if (getTemplate().getType() == 83) //Si c'est une pierre d'ï¿½me vide
            return getTemplate().getStrTemplate();
        StringBuilder stats = new StringBuilder();
        boolean isFirst = true;

       /* if ((getTemplate().getPanoId() >= 81 && getTemplate().getPanoId() <= 92)
                || (getTemplate().getPanoId() >= 201 && getTemplate().getPanoId() <= 212)) {
            for (String spell : SortStats) {
                if (!isFirst) {
                    stats.append(",");
                }
                String[] sort = spell.split("#");
                int spellid = Integer.parseInt(sort[1], 16);
                stats.append(sort[0]).append("#").append(sort[1]).append("#0#").append(sort[3]).append("#").append(World.world.getSort(spellid));
                isFirst = false;
            }
        }*/

            for (SpellEffect SE : Effects) {
                if (!isFirst)
                    stats.append(",");
                String[] infos = SE.getArgs().split(";");

                try {
                    switch (SE.getEffectID()) {
                        case 614:
                            stats.append(Integer.toHexString(SE.getEffectID())).append("#0#0#").append(infos[0]).append("#").append(infos[5]);
                            break;

                        default:
                            stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#").append(infos[1]).append("#").append(infos[5]);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                isFirst = false;
            }

            for (Entry<Integer, String> entry : txtStats.entrySet()) {
                if (!isFirst)
                    stats.append(",");
                if (template.getType() == 77 || template.getType() == 90) {
                    if (entry.getKey() == Constant.STATS_PETS_PDV)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                    if (entry.getKey() == Constant.STATS_PETS_EPO)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                    if (entry.getKey() == Constant.STATS_PETS_REPAS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());

                    if (entry.getKey() == Constant.STATS_PETS_POIDS) {
                        int corpu = 0;
                        int corpulence = 0;
                        String c = entry.getValue();
                        if (c != null && !c.equalsIgnoreCase("")) {
                            try {
                                corpulence = Integer.parseInt(c);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (corpulence != 0)
                            corpu = 7;
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(corpu)).append("#").append(corpulence > 0 ? corpu : 0).append("#").append(Integer.toHexString(corpu));
                    }
                    if (entry.getKey() == Constant.STATS_PETS_DATE
                            && template.getType() == 77) {
                        if (entry.getValue().contains("#"))
                            stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
                        else {
                            stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
                            System.out.println("date " + Formulas.convertToDate(Long.parseLong(entry.getValue())) + " " + Integer.toHexString(entry.getKey()));
                        }
                    }
                } else if(entry.getKey() == Constant.APPARAT_ITEM) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(Integer.toHexString(Integer.parseInt(entry.getValue())));
                }
                else if ((entry.getKey() == 970) || (entry.getKey() == 971) || (entry.getKey() == 972) || (entry.getKey() == 973) || (entry.getKey() == 974))
                {
                    int value = Integer.parseInt(entry.getValue());
                    int value2 = Integer.parseInt(entry.getValue(), 16);
                    int statid = entry.getKey();
                    if(statid == 970)
                    {
                        stats.append(Integer.toHexString(statid)).append("#0#0#").append(Integer.toHexString(Integer.parseInt(entry.getValue())));
                    }
                    else {
                        stats.append(Integer.toHexString(statid)).append("#0#0#").append(value);
                    }
                    if (statid == 973) setObvijevanPos(value2);
                    if (statid == 972) setObvijevanLook(value);
                    if (statid == 970) setObvijevanId(value);
                } else if (entry.getKey() == Constant.STATS_CHANGE_BY || entry.getKey() == Constant.STATS_NAME_TRAQUE || entry.getKey() == Constant.STATS_OWNER_1) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
                } else if (entry.getKey() == Constant.STATS_GRADE_TRAQUE || entry.getKey() == Constant.STATS_ALIGNEMENT_TRAQUE || entry.getKey() == Constant.STATS_NIVEAU_TRAQUE) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue()).append("#0");
                } else if (entry.getKey() == Constant.STATS_NAME_DJ) {
                    if (entry.getValue().equals("0d0+0"))
                        continue;
                    for (String i : entry.getValue().split(",")) {
                        stats.append(",").append(Integer.toHexString(entry.getKey())).append("#0#0#").append(i);
                    }
                    continue;
                } else if (entry.getKey() == Constant.STATS_DATE) {
                    String item = entry.getValue();
                    if (item.contains("#")) {
                        String date = item.split("#")[3];
                        if (date != null && !date.equalsIgnoreCase(""))
                            stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(date)));
                    } else
                        stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(item)));
                } else if (entry.getKey() == Constant.CAPTURE_MONSTRE) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
                } else if (entry.getKey() == Constant.STATS_PETS_PDV
                        || entry.getKey() == Constant.STATS_PETS_POIDS
                        || entry.getKey() == Constant.STATS_PETS_DATE
                        || entry.getKey() == Constant.STATS_PETS_REPAS) {
                    PetEntry p = World.world.getPetsEntry(this.getGuid());
                    if (p == null) {
                        if (entry.getKey() == Constant.STATS_PETS_PDV)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append("a").append("#0#a");
                        if (entry.getKey() == Constant.STATS_PETS_POIDS)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                        if (entry.getKey() == Constant.STATS_PETS_DATE)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                        if (entry.getKey() == Constant.STATS_PETS_REPAS)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                    } else {
                        if (entry.getKey() == Constant.STATS_PETS_PDV)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getPdv())).append("#0#").append(Integer.toHexString(p.getPdv()));
                        if (entry.getKey() == Constant.STATS_PETS_POIDS)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toString(p.parseCorpulence())).append("#").append(p.getCorpulence() > 0 ? p.parseCorpulence() : 0).append("#").append(Integer.toString(p.parseCorpulence()));
                        if (entry.getKey() == Constant.STATS_PETS_DATE) {
                            stats.append(Integer.toHexString(entry.getKey())).append(p.parseLastEatDate());
                        }
                        if (entry.getKey() == Constant.STATS_PETS_REPAS)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                        if (p.getIsEupeoh()
                                && entry.getKey() == Constant.STATS_PETS_EPO)
                            stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0)).append("#0#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0));
                    }
                } else if (entry.getKey() == Constant.STATS_RESIST
                        && getTemplate().getType() == 93) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate()))).append("#").append(entry.getValue()).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate())));
                } else if (entry.getKey() == Constant.STATS_RESIST) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate()))).append("#").append(entry.getValue()).append("#").append(Integer.toHexString(getResistanceMax(getTemplate().getStrTemplate())));
                } else {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
                }
                isFirst = false;
            }

            for (Entry<Integer, Integer> entry : SoulStats.entrySet()) {
                if (!isFirst)
                    stats.append(",");

                if (this.getTemplate().getType() == 18)
                    stats.append(Integer.toHexString(Constant.STATS_PETS_SOUL)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
                if (entry.getKey() == Constant.STATS_NIVEAU)
                    stats.append(Integer.toHexString(Constant.STATS_NIVEAU)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
                isFirst = false;
            }
            for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {

                int statID = entry.getKey();
                if ((getTemplate().getPanoId() >= 81 && getTemplate().getPanoId() <= 92)
                        || (getTemplate().getPanoId() >= 201 && getTemplate().getPanoId() <= 212) || getTemplate().getId() == 8992 || getTemplate().getId() == 8993) {
                    String[] modificable = template.getStrTemplate().split(",");
                    int cantMod = modificable.length;
                    for (int j = 0; j < cantMod; j++) {
                        String[] mod = modificable[j].split("#");
                        if (Integer.parseInt(mod[0], 16) == statID) {
                            String jet = "0d0+" + Integer.parseInt(mod[1], 16);
                            if (!isFirst) {
                                stats.append(",");
                            }
                            else
                            {
                                if(stats.length() > 0)
                                {
                                    stats = new StringBuilder();
                                }
                            }
                            stats.append(mod[0]).append("#").append(mod[1]).append("#0#").append(mod[3]).append("#").append(jet);
                            isFirst = false;
                        }
                    }
                } else {

                    if (!isFirst)
                        stats.append(",");
                    if (statID == 615) {
                        stats.append(Integer.toHexString(statID)).append("#0#0#").append(Integer.toHexString(entry.getValue()));
                    } else if ((statID == 970) || (statID == 971) || (statID == 972)
                            || (statID == 973) || (statID == 974)) {
                        int jet = entry.getValue();
                        if(statID == 970)
                        {
                            stats.append(Integer.toHexString(statID)).append("#0#0#").append(Integer.toHexString(jet));
                        }
                        else {
                            stats.append(Integer.toHexString(statID)).append("#0#0#").append(jet);
                        }
                        if (statID == 973)
                            setObvijevanPos(Integer.parseInt(String.valueOf(jet), 16));
                        if (statID == 972)
                            setObvijevanLook(jet);
                        if(statID == 970)
                            setObvijevanId(jet);
                    } else  if (statID == Constant.STATS_TURN) {
                        String jet = "0d0+" + entry.getValue();
                        stats.append(Integer.toHexString(statID)).append("#");
                        stats.append("0#0#").append(Integer.toHexString(entry.getValue())).append("#").append(jet);
                    } else {
                            String jet = "0d0+" + entry.getValue();
                            stats.append(Integer.toHexString(statID)).append("#");
                            stats.append(Integer.toHexString(entry.getValue())).append("#0#0#").append(jet);
                            isFirst = false;
                    }
                }
            }
        return stats.toString();
    }

    public String parseStatsStringSansUserObvi() {
        if (getTemplate().getType() == 83) //Si c'est une pierre d'ï¿½me vide
            return getTemplate().getStrTemplate();

        StringBuilder stats = new StringBuilder();
        boolean isFirst = true;

        if (this instanceof Fragment) {
            Fragment fragment = (Fragment) this;
            for (Couple<Integer, Integer> couple : fragment.getRunes()) {
                stats.append((stats.toString().isEmpty() ? couple.first : ";"
                        + couple.first)).append(":").append(couple.second);
            }
            return stats.toString();
        }



        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            if (!isFirst)
                stats.append(",");
            if (template.getType() == 77) {
                if (entry.getKey() == Constant.STATS_PETS_PDV)
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#0#").append(entry.getValue());
                if (entry.getKey() == Constant.STATS_PETS_POIDS)
                    stats.append(Integer.toHexString(entry.getKey())).append("#").append(entry.getValue()).append("#").append(entry.getValue()).append("#").append(entry.getValue());
                if (entry.getKey() == Constant.STATS_PETS_DATE) {
                    if (entry.getValue().contains("#")){
                        stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
                    }
                    else{
                        stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
                    }
                }
                //stats.append(Integer.toHexString(entry.getKey())).append(Formulas.convertToDate(Long.parseLong(entry.getValue())));
            } else if (entry.getKey() == Constant.STATS_DATE) {
                if (entry.getValue().contains("#"))
                    stats.append(Integer.toHexString(entry.getKey())).append(entry.getValue());
                else
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(Long.parseLong(entry.getValue()));
            } else if (entry.getKey() == Constant.STATS_CHANGE_BY || entry.getKey() == Constant.STATS_NAME_TRAQUE || entry.getKey() == Constant.STATS_OWNER_1) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
            } else if (entry.getKey() == Constant.STATS_GRADE_TRAQUE || entry.getKey() == Constant.STATS_ALIGNEMENT_TRAQUE || entry.getKey() == Constant.STATS_NIVEAU_TRAQUE) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue()).append("#0");
            } else if (entry.getKey() == Constant.STATS_NAME_DJ) {
                for (String i : entry.getValue().split(","))
                    stats.append(",").append(Integer.toHexString(entry.getKey())).append("#0#0#").append(i);
            } else if (entry.getKey() == Constant.CAPTURE_MONSTRE) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
            } else if(entry.getKey() == Constant.APPARAT_ITEM) {
                stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(entry.getValue());
            }
            else if(entry.getKey() >= 970 && entry.getKey() <= 974)
            {
                continue;
            } else if (entry.getKey() == Constant.STATS_PETS_PDV
                    || entry.getKey() == Constant.STATS_PETS_POIDS
                    || entry.getKey() == Constant.STATS_PETS_DATE) {



                PetEntry p = World.world.getPetsEntry(this.getGuid());
                if (p == null) {
                    if (entry.getKey() == Constant.STATS_PETS_PDV)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("a").append("#0#a");
                    if (entry.getKey() == Constant.STATS_PETS_POIDS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                    if (entry.getKey() == Constant.STATS_PETS_DATE) {
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#0#0");
                        //System.out.println(" l'item " + this.getGuid() + " : " + this.getTemplate().getName() );
                    }
                } else {
                    if (entry.getKey() == Constant.STATS_PETS_PDV)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getPdv())).append("#0#").append(Integer.toHexString(p.getPdv()));
                    if (entry.getKey() == Constant.STATS_PETS_POIDS)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(p.parseCorpulence()).append("#").append(p.getCorpulence() > 0 ? p.parseCorpulence() : 0).append("#").append(p.parseCorpulence());
                    if (entry.getKey() == Constant.STATS_PETS_DATE) {
                        stats.append(Integer.toHexString(entry.getKey())).append(p.parseLastEatDate());
                    }
                    if (p.getIsEupeoh()
                            && entry.getKey() == Constant.STATS_PETS_EPO)
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0)).append("#0#").append(Integer.toHexString(p.getIsEupeoh() ? 1 : 0));
                }
            } else {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#0#").append(entry.getValue());
            }
            isFirst = false;
        }
        /*
         * if(isCertif && !db) return stats.toString();
         */

        for (SpellEffect SE : Effects) {
            if (!isFirst)
                stats.append(",");

            String[] infos = SE.getArgs().split(";");
            try {
                stats.append(Integer.toHexString(SE.getEffectID())).append("#").append(infos[0]).append("#").append(infos[1]).append("#0#").append(infos[5]);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            isFirst = false;
        }
        for (Entry<Integer, Integer> entry : SoulStats.entrySet()) {
            if (!isFirst)
                stats.append(",");
            stats.append(Integer.toHexString(Constant.STATS_PETS_SOUL)).append("#").append(Integer.toHexString(entry.getKey())).append("#").append("0").append("#").append(Integer.toHexString(entry.getValue()));
            isFirst = false;
        }
        for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {
                if (!isFirst)
                    stats.append(",");

                if (entry.getKey() == 615) {
                    stats.append(Integer.toHexString(entry.getKey())).append("#0#0#").append(Integer.toHexString(entry.getValue()));
                }
                else if (entry.getKey() >= 970 && entry.getKey() <= 974)
                {
                    stats.append(Integer.toHexString(entry.getKey())).append('#').append(entry.getValue()).append("#0#").append(entry.getValue());
                }
                else {
                        String jet = "0d0+" + entry.getValue();
                        stats.append(Integer.toHexString(entry.getKey())).append("#").append(Integer.toHexString(entry.getValue()));
                        stats.append("#0#0#").append(jet);
                }
                isFirst = false;
            }
        return stats.toString();
    }

    public String parseToSave() {

        return parseStatsStringSansUserObvi();
    }

    public String obvijevanOCO_Packet(int pos) {
        String strPos = String.valueOf(pos);
        if (pos == -1)
            strPos = "";
        String upPacket = "OCO";
        upPacket = upPacket + Integer.toHexString(getGuid()) + "~";
        upPacket = upPacket + Integer.toHexString(getTemplate().getId()) + "~";
        upPacket = upPacket + Integer.toHexString(getQuantity()) + "~";
        upPacket = upPacket + strPos + "~";
        upPacket = upPacket + parseStatsString();
        this.setModification();
        return upPacket;
    }

    public void obvijevanNourir(GameObject obj) {
        if (obj == null)
            return;
        for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {
            if (entry.getKey() != 974) // on ne boost que la stat de l'expï¿½rience de l'obvi
                continue;
            if (entry.getValue() > 500) // si le boost a une valeur supï¿½rieure ï¿½ 500 (irrï¿½aliste)
                return;
            entry.setValue(entry.getValue()
                    + obj.getTemplate().getLevel() / 3);
        }
        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            if(entry.getKey() != 974)
            {
                continue;
            }
            if(Integer.parseInt(entry.getValue()) > 500)
            {
                return;
            }
            entry.setValue(String.valueOf(Integer.parseInt(entry.getValue()) + obj.getTemplate().getLevel() / 3));
        }
        this.setModification();
    }

    public void obvijevanChangeStat(int statID, int val) {
        for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {
            if (entry.getKey() != statID)
                continue;
            entry.setValue(val);
        }

        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            if(entry.getKey() != statID)
            {
                continue;
            }
            entry.setValue(String.valueOf(val));
        }
        this.setModification();
    }

    public void removeAllObvijevanStats() {
        setObvijevanPos(0);
        setObvijevanId(0);
        setObvijevanLook(0);
        client.other.Stats StatsSansObvi = new Stats();
        for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {
            int statID = entry.getKey();
            if ((statID == 970) || (statID == 971) || (statID == 972)
                    || (statID == 973) || (statID == 974))
                continue;
            StatsSansObvi.addOneStat(statID, entry.getValue());
        }
        Map<Integer, String> newTxtStats = new HashMap<>();
        for (Entry<Integer, String> entry : txtStats.entrySet()) {
            int statID = entry.getKey();
            if ((statID == 970) || (statID == 971) || (statID == 972)
                    || (statID == 973) || (statID == 974)) {
                continue;
            }
            newTxtStats.put(entry.getKey(), entry.getValue());
        }
        txtStats = newTxtStats;
        Stats = StatsSansObvi;
        this.setModification();
    }

    public void removeAll_ExepteObvijevanStats() {
        setObvijevanPos(0);
        client.other.Stats StatsSansObvi = new Stats();
        for (Entry<Integer, Integer> entry : Stats.getMap().entrySet()) {
            int statID = entry.getKey();
            if ((statID != 971) && (statID != 972) && (statID != 973)
                    && (statID != 974))
                continue;
            StatsSansObvi.addOneStat(statID, entry.getValue());
        }
        Stats = StatsSansObvi;
        this.setModification();
    }

    public String getObvijevanStatsOnly() {
        GameObject obj = getCloneObjet(this, 1);
        obj.removeAll_ExepteObvijevanStats();
        this.setModification();
        return obj.parseStatsStringSansUserObvi();
    }

    /* *********FM SYSTEM********* */

    public Stats generateNewStatsFromTemplate(String statsTemplate,
                                              boolean useMax) {
        Stats itemStats = new Stats(false, null);
        //Si stats Vides
        if (statsTemplate.equals("") || statsTemplate == null)
            return itemStats;

        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            boolean follow = true;

            for (int a : Constant.ARMES_EFFECT_IDS)
                //Si c'est un Effet Actif
                if (a == statID)
                    follow = false;
            if (!follow)
                continue;//Si c'ï¿½tait un effet Actif d'arme

            String jet = "";
            int value = 1;
            try {
                jet = stats[4];
                value = Formulas.getRandomJet(jet);
                if (useMax) {
                    try {
                        //on prend le jet max
                        int min = Integer.parseInt(stats[1], 16);
                        int max = Integer.parseInt(stats[2], 16);
                        value = min;
                        if (max != 0)
                            value = max;
                    } catch (Exception e) {
                        e.printStackTrace();
                        value = Formulas.getRandomJet(jet);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(value > 0) {
                itemStats.addOneStat(statID, value);
            }
        }
        return itemStats;
    }

    public ArrayList<SpellEffect> getEffects() {
        return Effects;
    }

    public ArrayList<SpellEffect> getCritEffects() {
        ArrayList<SpellEffect> effets = new ArrayList<SpellEffect>();
        for (SpellEffect SE : Effects) {
            try {
                boolean boost = true;
                for (int i : Constant.NO_BOOST_CC_IDS)
                    if (i == SE.getEffectID())
                        boost = false;
                String[] infos = SE.getArgs().split(";");
                if (!boost) {
                    effets.add(SE);
                    continue;
                }
                int min = Integer.parseInt(infos[0], 16)
                        + (boost ? template.getBonusCC() : 0);
                int max = Integer.parseInt(infos[1], 16)
                        + (boost ? template.getBonusCC() : 0);
                String jet = "1d" + (max - min + 1) + "+" + (min - 1);
                //exCode: String newArgs = Integer.toHexString(min)+";"+Integer.toHexString(max)+";-1;-1;0;"+jet;
                //osef du minMax, vu qu'on se sert du jet pour calculer les dï¿½gats
                String newArgs = "0;0;0;-1;0;" + jet;
                effets.add(new SpellEffect(SE.getEffectID(), newArgs, 0, -1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return effets;
    }

    public void clearStats() {
        //On vide l'item de tous ces effets
        Stats = new Stats();
        Effects.clear();
        txtStats.clear();
        SortStats.clear();
        SoulStats.clear();
        this.setModification();
    }

    public void refreshStatsObjet(String newsStats) {
        parseStringToStats(newsStats, true);
        this.setModification();
    }

    public int getResistance(String statsTemplate) {
        int Resistance = 0;

        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (Integer.parseInt(stats[0], 16) == Constant.STATS_RESIST) {
                Resistance = Integer.parseInt(stats[2], 16);
            }
        }
        return Resistance;
    }

    public int getResistanceMax(String statsTemplate) {
        int ResistanceMax = 0;

        String[] splitted = statsTemplate.split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (Integer.parseInt(stats[0], 16) == Constant.STATS_RESIST) {
                ResistanceMax = Integer.parseInt(stats[1], 16);
            }
        }
        return ResistanceMax;
    }

    public int getRandomValue(String statsTemplate, int statsId) {
        if (statsTemplate.equals(""))
            return 0;

        String[] splitted = statsTemplate.split(",");
        int value = 0;
        for (String s : splitted) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            if (statID != statsId)
                continue;
            String jet;
            try {
                jet = stats[4];
                value = Formulas.getRandomJet(jet);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return value;
    }

    public String parseFMStatsString(String statsstr, int add,
                                     boolean negatif) {
        String stats = "";
        boolean isFirst = true;
        for (SpellEffect SE : this.Effects) {
            if (!isFirst)
                stats += ",";

            String[] infos = SE.getArgs().split(";");
            try {
                stats += Integer.toHexString(SE.getEffectID()) + "#" + infos[0]
                        + "#" + infos[1] + "#0#" + infos[5];
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            isFirst = false;
        }

        for (Entry<Integer, Integer> entry : this.Stats.getMap().entrySet()) {
            if (!isFirst)
                stats += ",";

            //World.world.logger.trace( ""+Integer.toHexString(entry.getKey())+" "+statsstr +" "+ add+ " "+ entry.getValue());
            if(entry.getKey() < 970 | entry.getKey() > 974) {
                if (Integer.toHexString(entry.getKey()).compareTo(statsstr) == 0) {
                    int newstats = 0;
                    @SuppressWarnings("unused")
                    int newstats2 = 0;
                    //World.world.logger.trace( "Negative ? " + negatif);
                    if (negatif) {
                        newstats = entry.getValue() - add;
                        if (add < 1)
                            continue;
                    } else {
                        newstats = entry.getValue() + add;
                    }
                    String jet = "0d0+" + newstats;
                    stats += Integer.toHexString(entry.getKey()) + "#"
                            + Integer.toHexString(newstats) + "#0#0#"
                            + jet;
                } else {

                    String jet = "0d0+" + entry.getValue();
                    stats += Integer.toHexString(entry.getKey()) + "#"
                            + Integer.toHexString(entry.getValue()) + "#0#0#" + jet;

                    //World.world.logger.trace( ""+entry.getKey()+" "+statsstr +" "+ add+ " "+ entry.getValue());
                }
            } else
            {
                stats += Integer.toHexString(entry.getKey()) + "#0#0#" + entry.getValue();
            }
            isFirst = false;
        }

        for (Entry<Integer, String> entry : this.txtStats.entrySet()) {
            if (!isFirst)
                stats += ",";
                if(entry.getKey() < 970 ||entry.getKey() > 974) {
                    if(entry.getKey() != Constant.APPARAT_ITEM)
                    {
                        stats += Integer.toHexString(entry.getKey()) + "#0#0#" + entry.getValue();
                    }
                    stats += Integer.toHexString(entry.getKey()) + "#0#0#0#"
                            + entry.getValue();
                }
                else{
                    stats += Integer.toHexString(entry.getKey()) + "#0#0#" + entry.getValue();
                }
                isFirst = false;
        }
        // World.world.logger.trace( ""+stats);
        return stats;
    }

    public static byte viewActualStatsItem(GameObject obj, String stats)//retourne vrai si le stats est actuellement sur l'item
    {
        if (!obj.parseStatsString().isEmpty()) { // si l'obj est pas vide
            for (Entry<Integer, Integer> entry : obj.getStats().getMap().entrySet()) { // Toutes les entrées
                if (Integer.toHexString(entry.getKey()).compareTo(stats) > 0)//Effets inutiles
                {
                    if (Integer.toHexString(entry.getKey()).compareTo("98") == 0  // C'est un cas négatif
                            && stats.compareTo("7b") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9a") == 0
                            && stats.compareTo("77") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9b") == 0
                            && stats.compareTo("7e") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("9d") == 0
                            && stats.compareTo("76") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("74") == 0
                            && stats.compareTo("75") == 0) {
                        return 2;
                    } else if (Integer.toHexString(entry.getKey()).compareTo("99") == 0
                            && stats.compareTo("7d") == 0) {
                        return 2;
                    }
                }
                else if (Integer.toHexString(entry.getKey()).compareTo(stats) == 0)//L'effet existe bien !
                {
                    return 1;
                }
            }
            return 0;
        } else {
            return 0;
        }
    }

    public static byte viewBaseStatsItem(GameObject obj, String ItemStats)//retourne vrai si le stats existe de base sur l'item
    {

        String[] splitted = obj.getTemplate().getStrTemplate().split(",");
        for (String s : splitted) {
            String[] stats = s.split("#");
            if (stats[0].compareTo(ItemStats) > 0)//Effets n'existe pas de base
            {
                if (stats[0].compareTo("98") == 0
                        && ItemStats.compareTo("7b") == 0) {
                    return 2; //
                } else if (stats[0].compareTo("9a") == 0
                        && ItemStats.compareTo("77") == 0) {
                    return 2;
                } else if (stats[0].compareTo("9b") == 0
                        && ItemStats.compareTo("7e") == 0) {
                    return 2;
                } else if (stats[0].compareTo("9d") == 0
                        && ItemStats.compareTo("76") == 0) {
                    return 2;
                } else if (stats[0].compareTo("74") == 0
                        && ItemStats.compareTo("75") == 0) {
                    return 2;
                } else if (stats[0].compareTo("99") == 0
                        && ItemStats.compareTo("7d") == 0) {
                    return 2;
                } else {
                }
            } else if (stats[0].compareTo(ItemStats) == 0)//L'effet existe bien !
            {
                return 1;
            }
        }
        return 0;
    }

    public void setNewStats(String statsObjectFm,int statsAdd) {
        int ItemCurrentStats = viewActualStatsItem(this, statsObjectFm);
        String statsStr = "";
        String statStringObj = this.parseStatsString() ;
        boolean negative = false;
        if (ItemCurrentStats == 2) {
            if (statsObjectFm.compareTo("7b") == 0) {
                statsObjectFm = "98";
                negative = true;
            }
            if (statsObjectFm.compareTo("77") == 0) {
                statsObjectFm = "9a";
                negative = true;
            }
            if (statsObjectFm.compareTo("7e") == 0) {
                statsObjectFm = "9b";
                negative = true;
            }
            if (statsObjectFm.compareTo("76") == 0) {
                statsObjectFm = "9d";
                negative = true;
            }
            if (statsObjectFm.compareTo("7c") == 0) {
                statsObjectFm = "9c";
                negative = true;
            }
            if (statsObjectFm.compareTo("7d") == 0) {
                statsObjectFm = "99";
                negative = true;
            }
        }
        if (ItemCurrentStats == 1 || ItemCurrentStats == 2) {
            //World.world.logger.trace( "On est la normal");
            if (statStringObj.isEmpty()) {
                statsStr = statsObjectFm + "#"
                        + Integer.toHexString(statsAdd) + "#0#0#0d0+"
                        + statsAdd;
                this.clearStats();
                this.refreshStatsObjet(statsStr);

            } else {
                statsStr = this.parseFMStatsString(statsObjectFm, statsAdd, negative);
                //World.world.logger.trace( "Etrange "+statsStr);
                this.clearStats();
                this.refreshStatsObjet(statsStr);
                //String test = this.parseStatsString() ;
                // World.world.logger.trace( "Test "+test);
            }
        } else {
            //World.world.logger.trace( "Bizarrement on est la");
            if (statStringObj.isEmpty()) {
                statsStr = statsObjectFm + "#"
                        + Integer.toHexString(statsAdd) + "#0#0#0d0+"
                        + statsAdd;
                this.clearStats();
                this.refreshStatsObjet(statsStr);
            } else {
                statsStr = this.parseFMStatsString(statsObjectFm, statsAdd, negative)
                        + ","
                        + statsObjectFm
                        + "#"
                        + Integer.toHexString(statsAdd)
                        + "#0#0#0d0+"
                        + statsAdd;
                this.clearStats();
                this.refreshStatsObjet(statsStr);
            }
        }
        this.setModification();
    }

    public String parseStringStatsEC_FM(GameObject obj, double poid, int carac) { // Ca c'est pas mal mais peut etre vori le jet perdu en soit
        String stats = "";
        boolean first = false;
        double perte = 0.0;
        for (SpellEffect EH : obj.Effects) {
            if (first)
                stats += ",";
            String[] infos = EH.getArgs().split(";");
            try {
                stats += Integer.toHexString(EH.getEffectID()) + "#" + infos[0]
                        + "#" + infos[1] + "#0#" + infos[5];
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            first = true;
        }
        java.util.Map<Integer, Integer> statsObj = new java.util.HashMap<Integer, Integer>(obj.Stats.getMap());
        java.util.ArrayList<Integer> keys = new ArrayList<Integer>(obj.Stats.getMap().keySet());
        Collections.shuffle(keys);
        int p = 0;
        int key = 0;
        if (keys.size() > 1) {
            for (Integer i : keys) // On cherche un OverFM
            {
                if(i < 970 | i > 974) {
                    //if(i == 121){
                    //	i = 112;
                    //}
                    int value = statsObj.get(i);
                    if (this.isOverFm(i, value)) {
                        key = i;
                        //System.out.println(value + " On est over" + i);
                        break;
                    }
                    p++;
                }
            }
            if (key > 0) // On place l'OverFm en tï¿½te de liste pour ï¿½tre niquï¿½
            {
                keys.remove(p);
                keys.add(p, keys.get(0));
                keys.remove(0);
                keys.add(0, key);
            }
        }
        for (Integer i : keys) {
            //if(i == 121){
            //	i = 112;
            //}
            //System.out.println(i + " On boucle ??");
            int newstats = 0;
            int statID = i;
            int value = statsObj.get(i);


            //System.out.println(value + " La valeur de la stats");
            if (perte > poid || statID == carac) {
                newstats = value;
                //System.out.println(newstats  + "On laisse parce qu'on a deja perdu assez");
            } else if ((statID == 152) || (statID == 154) || (statID == 155)
                    || (statID == 157) || (statID == 116) || (statID == 153)) {

                //System.out.println( " des stats négatif qu'on remet au max");
                float a = (float) (value * poid / 100.0D);

                if (a < 1.0F)
                    a = 1.0F;
                //System.out.println(a  + " une valeur ");
                float chute = value + a;
                //System.out.println(chute + " la chute est de");
                newstats = (int) Math.floor(chute);

                if (newstats > JobAction.getBaseMaxJet(obj.getTemplate().getId(), Integer.toHexString(i)))
                    newstats = JobAction.getBaseMaxJet(obj.getTemplate().getId(), Integer.toHexString(i));

            } else {
                if ((statID == 127) || (statID == 101))
                    continue;

                float chute;

                if (this.isOverFm(statID, value)){ // Gros kick dans la gueulle de l'over FM
                    chute = (float) (value - value
                            * (poid - (int) Math.floor(perte)) * 2 / 100.0D);
                    //System.out.println( " On est over donc chute " + chute );
                }
                else{
                    chute = (float) (value - value
                            * (poid - (int) Math.floor(perte)) / 100.0D);

                    //System.out.println( " On est normal donc chute " + chute );
                }
                if ((chute / (float) value) < 0.75){

                    chute = ((float) value) * 0.75F; // On ne peut pas perdre plus de 25% d'une stat d'un coup
                    //System.out.println( " on a trop chuter donc chute " + chute );
                }
                double chutePwr = (value - chute)
                        * JobAction.getPwrPerEffet(statID);
                //System.out.println( " chute final" + chutePwr );
                //int chutePwrFixe = (int) Math.floor(chutePwr);

                perte += chutePwr;

                /*
                 * if (obj.getPuit() > 0 && chutePwrFixe <= obj.getPuit()) //
                 * S'il y a un puit positif, on annule la baisse { perte +=
                 * chutePwr; chute = value; // On rï¿½initialise
                 * obj.setPuit(obj.getPuit() - chutePwrFixe); // On descend le
                 * puit } else if (obj.getPuit() > 0) // Si le puit est positif,
                 * mais pas suffisant pour annuler { double pwr =
                 * obj.getPuit()/World.getPwrPerEffet(statID); // On calcule
                 * l'annulation possible de la chute chute += (int)
                 * Math.floor(pwr); // On l'a rï¿½ajoute perte +=
                 * (value-chute)*World.getPwrPerEffet(statID); obj.setPuit(0);
                 * // On fixe le puit ï¿½ 0 } else { perte += chutePwr; }
                 */

                newstats = (int) Math.floor(chute);

                //System.out.println( " Nouvelle valur stats " + newstats );
            }
            if (newstats < 1)
                continue;
            String jet = "0d0+" + newstats;
            if (first)
                stats += ",";
            stats += Integer.toHexString(statID) + "#"
                    + Integer.toHexString(newstats) + "#0#0#" + jet;
            first = true;
        }
        for (Entry<Integer, String> entry : obj.txtStats.entrySet()) {
            if (first)
                stats += ",";
            if(entry.getKey() < 970 | entry.getKey() > 974) {
                if(entry.getKey() == Constant.APPARAT_ITEM)
                {
                    stats += Integer.toHexString(entry.getKey()) + "#0#0#" + entry.getValue();
                }
                else {
                    stats += Integer.toHexString((entry.getKey())) + "#0#0#0#"
                            + entry.getValue();
                }
            }
            else{
                stats += Integer.toHexString(entry.getKey()) + "#0#0#" + entry.getValue();
            }
            first = true;
        }
        return stats;
    }

    public boolean isOverFm(int stat, int val) {
        if(stat == 121){
            stat = 112;
        }

        boolean trouve = false;
        String statsTemplate = "";
        statsTemplate = this.template.getStrTemplate();

        if (statsTemplate == null || statsTemplate.isEmpty())
            return false;
        String[] split = statsTemplate.split(",");
        for (String s : split) {
            String[] stats = s.split("#");
            int statID = Integer.parseInt(stats[0], 16);
            //System.out.println("La stat " + statID);
            if (statID != stat)
                continue;

            trouve = true;
            boolean sig = true;
            for (int a : Constant.ARMES_EFFECT_IDS)
                if (a == statID)
                    sig = false;
            if (!sig)
                continue;
            String jet = "";
            int value = 1;
            try {


                jet = stats[4];
                value = Formulas.getRandomJet(jet);

                if(rarity <= 3){
                    try {
                        int min = Integer.parseInt(stats[1], 16);
                        int max = Integer.parseInt(stats[2], 16);
                        value = min;
                        if (max != 0)
                            value = max;
                    } catch (Exception e) {
                        e.printStackTrace();
                        value = Formulas.getRandomJet(jet);
                    }
                }
                else{
                    try {
                        int min = Integer.parseInt(stats[1], 16);
                        int max = Integer.parseInt(stats[2], 16);
                        value = min;
                        if (max != 0)
                            value = Formulas.getRandomJetWithRarity(min,max,5);
                    } catch (Exception e) {
                        e.printStackTrace();
                        value = Formulas.getRandomJet(jet);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            if (val > value){
                //System.out.println("On est over" + val + " "+value);
                return true;
            }

        }
        return !trouve;
    }

    public void setRarity(int rarity) {
        this.rarity = rarity;
    }

    public int getRarity() {
        return rarity;
    }

    public static int getAleaRarity() {
        int rarity = 1;
        int seuil = 50;
        int chance = 0;

        for (int i = 0; i <= 4; i++) {
            chance = Formulas.getRandomValue(0, 100);
            if(chance>seuil) {
                rarity++;
            }
            else {
                break;
            }
        }

        if(rarity>5){
            rarity = 5;
        }
        return rarity;
    }

    public static int getRarityAlea(int difficulty) {
        int rarity = 1;
        switch (difficulty){
            case 0 :
                break;
            case 1:
                rarity = 2;
                break;
            case 2 :
                rarity = 3;
                break;
            default:
                rarity = 1;
                break;
        }

        int seuil = 50;
        int chance = 0;

        for (int i = 0; i <= 4-difficulty; i++) {
            chance = Formulas.getRandomValue(0, 100);
            if(chance>seuil) {
                rarity++;
            }
            else {
                break;
            }
        }

        if(rarity>5){
            rarity = 5;
        }
        return rarity;
    }

    public static int getAleaRarity(long chanceimpact) {
        int rarity = 1;
        int seuil = 50;
        int chance = 0;

        float perc = (float)(chanceimpact*0.01);
        int seuilimpact = (int)Math.floor(seuil * perc);
        for (int i = 0; i <= 4; i++) {
            chance = Formulas.getRandomValue(0, 100);
            if(chance<seuilimpact) {
                rarity++;
            }
            else {
                break;
            }
        }

        if(rarity>5){
            rarity = 5;
        }
        return rarity;
    }

    public ArrayList<String> getSpellStats() {
        return SortStats;
    }

    public boolean isSameStats(GameObject other) {

        if(this.getEffects().size() == 0 && other.getEffects().size() == 0)
        {
            //System.out.println("Bizarrement ici");
            return true;
        }
        else {

            System.out.println(other.getGuid());

            System.out.println(this.getGuid());

            boolean test = false;
            for(SpellEffect effect : this.getEffects()){
                //System.out.println("Item testé Degat "+ effect.getEffectID() + " Degat " + effect.getValue()  );


                for(SpellEffect effect2 : other.getEffects()){
                    //System.out.println("Item comparaison Degat "+ effect2.getEffectID() + " Degat "+ effect2.getArgs()  );
                   // System.out.println("Item de comparaison Degat "+ effect2.getEffectID() + " Degat " + effect2.getJet());
                    if(effect.getEffectID() == effect2.getEffectID() ){
                        //System.out.println("On est la 1 "+ effect2.getEffectID());
                        //System.out.println("On est la 2 "+ effect.getEffectID());
                        if(effect.getArgs().equals(effect2.getArgs())){
                            //System.out.println("On est la 3 "+ effect.getJet());
                            //System.out.println("On est la 4 "+ effect2.getJet());
                            test = true;
                        }
                    }
                }
                if(!test){
                    return test;
                }
            }

            test = false;
            for(SpellEffect effect : other.getEffects()){
                for(SpellEffect effect2 : this.getEffects()){
                    if(effect.getEffectID() == effect2.getEffectID() ){
                        if(effect.getArgs().equals(effect2.getArgs())){
                            test = true;
                        }
                    }
                }
                if(!test){
                    return false;
                }
            }
            //System.out.println("On est la "+ test);
            return test;
        }

    }
}
