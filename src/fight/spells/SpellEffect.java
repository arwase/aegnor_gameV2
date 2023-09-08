package fight.spells;

import area.map.GameCase;
import client.Player;
import common.Formulas;
import common.PathFinding;
import common.SocketManager;
import entity.monster.Monster;
import entity.monster.Monster.MobGrade;
import fight.Fight;
import fight.Fighter;
import fight.traps.Glyph;
import fight.traps.Trap;
import game.GameServer;
import game.world.World;
import kernel.Constant;
import org.apache.commons.lang3.ArrayUtils;
import util.TimerWaiter;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SpellEffect {

	private final int durationFixed;
	private int effectID;
	private int turns = 0;
	private String jet = "0d0+0";
	private int chance = 100;
	private String args;
	private String info;
	private String area;
	private int minvalue = 0;
	private int maxvalue = 0;
	private int fixvalue = 0;
	private Fighter caster = null;
	private int spell = 0;
	private int spellLvl = 1;
	private boolean debuffable = true;
	private int duration = 0;
	private int effectTarget = 0;
	private GameCase cell = null;
	private String onHitCondition="";
	private int onHit=0;
	private boolean isBuff = false;
	private int dammageDone = 0;

	// Un vrai spell effect
	public SpellEffect(int aID, int min, int max, int args, int turn, int chance, String jet, String Area, String OnHit, int EffectTarget, int aspell) {
		this.effectID = aID;
		this.minvalue = min;
		this.maxvalue = max;
		this.durationFixed = turn;

		if(this.maxvalue == -1)
			this.maxvalue = this.minvalue;

		this.info = args+"";
		this.turns = turn;
		if(turn>0)
			this.isBuff = true;
		this.chance = chance;
		this.jet = jet;
		this.area = Area;

		if(!OnHit.equals("-1")){
			String[] sOnHit = OnHit.split(";");
			this.onHit = Integer.parseInt(sOnHit[0]);
			this.onHitCondition = sOnHit[1];
		}
		else{
			this.onHit = 0;
			this.onHitCondition = "";
		}
		this.effectTarget = EffectTarget;
		if(jet.equals("")){
			this.args = this.minvalue+";"+this.maxvalue+";"+this.info+";"+this.turns+";"+this.chance+";0d0+"+this.minvalue;
		}
		else{
			this.args = this.minvalue+";"+this.maxvalue+";"+this.info+";"+this.turns+";"+this.chance+";"+this.jet;
		}
		this.spell = aspell;
	}

	// Effet d'arme ???
	public SpellEffect(int aID, String aArgs, int aSpell, int aSpellLevel) {
		effectID = aID;
		args = aArgs;
		spell = aSpell;
		spellLvl = aSpellLevel;
		durationFixed = 0;
		try {
			fixvalue = Integer.parseInt(args.split(";")[0]);
			minvalue = Integer.parseInt(args.split(";")[1]);
			maxvalue = Integer.parseInt(args.split(";")[2]);
			if(maxvalue == -1)
				maxvalue = minvalue;
			turns = Integer.parseInt(args.split(";")[3]);
			chance = Integer.parseInt(args.split(";")[4]);
			jet = args.split(";")[5];
		} catch (Exception ignored) {
		}
	}

	// ADD UN BUFF A EFFET ON HIT
	public SpellEffect(int id, int value2, int turns2, Fighter aCaster, int aspell) {
		effectID = id;
		fixvalue = value2;
		duration = turns2;
		caster = aCaster;
		spell = aspell;
		this.durationFixed = turns2;
	}

	// ADD UN BUFF VALEUR FIX
	public SpellEffect(int id, int value2, int aduration, int turns2, boolean debuff, Fighter aCaster, String args2, int aspell) {
		effectID = id;
		fixvalue = value2;
		turns = turns2;
		debuffable = debuff;
		caster = aCaster;
		duration = aduration;
		this.durationFixed = duration;
		args = args2;
		spell = aspell;
		try {
			minvalue = Integer.parseInt(args.split(";")[0]);
			maxvalue = Integer.parseInt(args.split(";")[1]);
			if(maxvalue == -1)
				maxvalue = minvalue;

			jet = args.split(";")[5];
		}
		catch (Exception ignored) {}
	}

	public String getInfo() {return this.info; }

	public String getOnHitCondition() {return this.onHitCondition; }

	public int getEffectTarget(){
		return this.effectTarget;
	}

	public String getAreaEffect(){
		return this.area;
	}

	public int getDuration() {
		return duration;
	}

	public static ArrayList<Fighter> getTargets(SpellEffect SE, Fight fight, ArrayList<GameCase> cells) {
		ArrayList<Fighter> cibles = new ArrayList<Fighter>();
		for (GameCase aCell : cells) {
			if (aCell == null) continue;
			Fighter f = aCell.getFirstFighter();
			if (f == null) continue;
			cibles.add(f);
		}
		return cibles;
	}

	// A SUPPRIMER AU FUR ET A MESURE
	public static int applyOnHitBuffs(int finalDommage, Fighter target, Fighter caster, Fight fight, int elementId) {
		for (int id : Constant.ON_HIT_BUFFS) {
			for (SpellEffect buff : target.getBuffsByEffectID(id)) {
				switch (id) {
					case 114:
						if (buff.spell == 521) finalDommage = finalDommage * 2;
						break;
					case 138:
						if (buff.getSpell() == 1039) {
							int stats = 0;
							if (elementId == Constant.ELEMENT_AIR)
								stats = 217;
							else if (elementId == Constant.ELEMENT_EAU)
								stats = 216;
							else if (elementId == Constant.ELEMENT_FEU)
								stats = 218;
							else if (elementId == Constant.ELEMENT_NEUTRE)
								stats = 219;
							else if (elementId == Constant.ELEMENT_TERRE)
								stats = 215;
							int val = target.getBuff(stats).getFixvalue();
							int turns = target.getBuff(stats).getTurn();
							int duration = target.getBuff(stats).getDurationFixed();
							String args = target.getBuff(stats).getArgs();
							for (int i : Constant.getOppositeStats(stats))
								target.addBuff(i, val, turns, duration, true, buff.getSpell(), args, caster, true);
							target.addBuff(stats, val, duration, turns, true, buff.getSpell(), args, caster, true);

						}
						break;
					case 9://Derobade
						//Si pas au cac (distance == 1)
						int d = PathFinding.getDistanceBetween(fight.getMap(), target.getCell().getId(), caster.getCell().getId());
						if (d > 1) continue;
						int chan = buff.getFixvalue();
						int c = Formulas.getRandomValue(0, 99);
						if (c + 1 >= chan) continue;//si le deplacement ne s'applique pas
						int nbrCase = 0;
						try {
							nbrCase = Integer.parseInt(buff.getArgs().split(";")[1]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (nbrCase == 0) continue;
						int exCase = target.getCell().getId();
						int newCellID = PathFinding.newCaseAfterPush(fight, caster.getCell(), target.getCell(), nbrCase);
						if (newCellID < 0)//S'il a été bloqué
						{
							int a = -newCellID;
							a = nbrCase - a;
							newCellID = PathFinding.newCaseAfterPush(fight, caster.getCell(), target.getCell(), a);
							if (newCellID == 0)
								continue;
							if (fight.getMap().getCase(newCellID) == null)
								continue;
						}
						target.getCell().getFighters().clear();
						target.setCell(fight.getMap().getCase(newCellID));
						target.getCell().addFighter(target);
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, target.getId() + "", target.getId() + "," + newCellID, elementId);

						Trap.doTraps(target.getFight(), target);
						//si le joueur a bouger
						if (exCase != newCellID)
							finalDommage = 0;
						break;

					case 79://chance éca
						try {
							String[] infos = buff.getArgs().split(";");
							int coefDom = Integer.parseInt(infos[0]);
							int coefHeal = Integer.parseInt(infos[1]);
							int chance = Integer.parseInt(infos[2]);
							int jet = Formulas.getRandomValue(0, 99);

							if (jet < chance)//Soin
							{
								finalDommage = -(finalDommage * coefHeal);
								if (-finalDommage > (target.getPdvMax() - target.getPdv()))
									finalDommage = -(target.getPdvMax() - target.getPdv());
							} else//Dommage
								finalDommage = finalDommage * coefDom;
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;

					case 107://renvoie Dom
						if (target.getId() == caster.getId()) break;

						if (caster.hasBuff(765))//sacrifice
						{
							if (caster.getBuff(765) != null && !caster.getBuff(765).getCaster().isDead()) {
								buff.applyEffect_765B(fight, caster);
								caster = caster.getBuff(765).getCaster();
							}
						}

						String[] args = buff.getArgs().split(";");
						float coef = 1 + (target.getTotalStats().getEffect(Constant.STATS_ADD_SAGE) / 100);
						int renvoie = 0;
						try {
							if (Integer.parseInt(args[1]) != -1) {
								renvoie = (int) (coef * Formulas.getRandomValue(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
							} else {
								renvoie = (int) (coef * Integer.parseInt(args[0]));
							}
						} catch (Exception e) {
							return finalDommage;
						}
						if (renvoie > finalDommage) renvoie = finalDommage;
						finalDommage -= renvoie;
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 107, "-1", target.getId() + "," + renvoie, elementId);
						if (renvoie > caster.getPdv()) renvoie = caster.getPdv();
						if (finalDommage < 0) finalDommage = 0;
						caster.removePdv(caster, renvoie);
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId() + "", caster.getId() + ",-" + renvoie, elementId);
						break;
					/*case 606://Chatiment (ancien)
						int stat = buff.getValue();
						int jet = Formulas.getRandomJet(buff.getJet(), caster, target);
						target.addBuff(stat, jet, -1, -1, false, buff.getSpell(), buff.getArgs(), caster, true);
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, stat, caster.getId() + "", target.getId() + "," + jet + "," + -1, elementId);
						break;
					case 607://Chatiment (ancien)
					case 608:
					case 609:
					case 611:
						stat = buff.getValue();
						jet = Formulas.getRandomJet(buff.getJet(), caster, target);
						target.addBuff(stat, jet, -1, -1, false, buff.getSpell(), buff.getArgs(), caster, true);
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, stat, caster.getId() + "", target.getId() + "," + jet + "," + -1, elementId);
						break;*/
					case 788://Chatiments
						int taux = (caster.getPlayer() == null ? 1 : 2), gain = finalDommage / taux, max = 0;
						int stat = buff.getFixvalue();

						try {
							max = Integer.parseInt(buff.getArgs().split(";")[1]);
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}

						//on retire au max possible la valeur déjà gagné sur le chati
						int oldValue = (target.getChatiValue().get(stat) == null ? 0 : target.getChatiValue().get(stat));
						max -= oldValue;
						//Si gain trop grand, on le reduit au max
						if (gain > max) gain = max;
						//On met a jour les valeurs des chatis
						int newValue = oldValue + gain;

						if (stat == 125) {
							SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, stat, caster.getId() + "", target.getId() + "," + gain + "," + 5, elementId);
							target.setPdv(target.getPdv() + gain);
							if(target.getPlayer() != null) SocketManager.GAME_SEND_STATS_PACKET(target.getPlayer());
						} else {
							target.getChatiValue().put(stat, newValue);
							target.addBuff(stat, gain, 5, 1, false, buff.getSpell(), buff.getArgs(), caster, true);

							SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, stat, caster.getId() + "", target.getId() + "," + gain + "," + 5, elementId);
						}
						target.getChatiValue().put(stat, newValue);
						break;

					default:
						break;
				}
			}
		}
		return finalDommage;
	}

	public int decrementDuration() {
		duration -= 1;
		return duration;
	}

	public int getTurn() {
		return turns;
	}

	public void setTurn(int turn) {
		this.turns = turn;
	}

	public boolean isDebuffabe() {
		return debuffable;
	}

	public int getEffectID() {
		return effectID;
	}

	public void setEffectID(int id) {
		effectID = id;
	}

	public String getJet() {
		return jet;
	}

	public int getMaxValue() {
		return maxvalue;
	}

	public int getMinValue() {
		return minvalue;
	}

	public int getChance() {
		return chance;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String newArgs) {
		args = newArgs;
	}

	public int getMaxMinSpell(Fighter fighter, int value) {
		int val = value;
		if (fighter.hasBuff(782)) {
			int max = Integer.parseInt(args.split(";")[1]);
			if (max == -1)
				max = Integer.parseInt(args.split(";")[0]);
			val = max;
		} else if (fighter.hasBuff(781))
			val = Integer.parseInt(args.split(";")[0]);
		return val;
	}

	public void applyBeginingBuff(Fight fight, Fighter fighter) {
		ArrayList<Fighter> targets = new ArrayList<>();
		targets.add(fighter);
		this.turns = 0;
		this.applyToFight(fight, this.caster, targets, false);
	}

	public void applyToFight(Fight fight, Fighter perso, GameCase Cell, ArrayList<Fighter> cibles) {
		cell = Cell;
		applyToFight(fight, perso, cibles, false);
	}

	private int getDurationFixed() {
		return this.durationFixed;
	}

	public Fighter getCaster() {
		return caster;
	}

	public int getSpell() {
		return spell;
	}

	public void applyToFight(Fight fight, Fighter acaster, ArrayList<Fighter> cibles, boolean isCaC) {
		/*try {
			if (turns != 0 && turns != -1)//Si ce n'est pas un buff qu'on applique en début de tour
				turns = turns;
		} catch (NumberFormatException ignored) {}*/
		caster = acaster;

		try {
			jet = args.split(";")[5];
		} catch (Exception ignored) {}

		if (caster.getPlayer() != null) {
			Player perso = caster.getPlayer();
			if (perso.getObjectsClassSpell().containsKey(spell)) {
				int modi = 0;
				if (effectID == 108)
					modi = perso.getValueOfClassObject(spell, 284);
				else if (effectID >= 91 && effectID <= 100)
					modi = perso.getValueOfClassObject(spell, 283);
				String jeta = jet.split("\\+")[0];
				int bonus = Integer.parseInt(jet.split("\\+")[1]) + modi;
				jet = jeta + "+" + bonus;
			}
		}

		if(!cibles.isEmpty()) {
			ArrayList<Fighter> cible2 = new ArrayList<>();
			cibles.addAll(cible2);

			for (Fighter Cible : cibles) {
				if (!Cible.isDead()) {
					cible2.add(Cible);
				}
			}
			cibles.clear();
			cibles.addAll(cible2);
		}

		// On va retenir les dégats infligé la dedans
		this.dammageDone = 0;

		// LES EFFETS QUI N'ONT PAS BESOIN DE TARGET
		if(ArrayUtils.contains(Constant.NONEEDTARGET_EFFECT,effectID)){
			switch (effectID){
				case 4://Fuite/Bond du félin/ Bond du iop / téléport
					applyEffect_4(fight);
					break;
				case 50://Porter
					applyEffect_50(fight);
					break;
				case 51://jeter
					applyEffect_51(fight);
					break;
				case 109://Dommage pour le lanceur
					applyEffect_109(fight);
					break;
				case 120://Bonus PA
					applyEffect_120(fight);
					break;
				case 180://Double du sram
					applyEffect_180(fight);
					break;
				case 181://Invoque une créature
				case 200: //Contrôle Invocation
					applyEffect_181(fight);
					break;
				case 185://Invoque une creature statique
					applyEffect_185(fight);
					break;
				case 400://Créer un  piège
					applyEffect_400(fight);
					break;
				case 401://Créer une glyphe
					applyEffect_401(fight);
					break;
				case 402://Glyphe des Blop
					applyEffect_402(fight);
					break;
				case 780://laisse spirituelle
					applyEffect_780(fight);
					break;
				case 783://Pousse jusqu'a la case visé sans do pou
					applyEffect_783(fight);
					break;
				case 784://Raulebaque
					applyEffect_784(fight);
					break;
				default:
					GameServer.a("Pas d'effet de spell " + effectID + " Dev pour le spell " + spell + " sans target necessaire");
					break;
			}
		}
		else{
			// Cas particulier DO POU
			switch (spell) {
				case 73://Piége répulsif
				case 418://Fléche de dispersion
				case 151://Soufle
				case 165://FlÃ¨che enflammé
					cibles = this.trierCibles(cibles, fight);
					break;
			}

			for(Fighter target :cibles){
				if(ArrayUtils.contains(Constant.IS_DIRECTDAMMAGE_EFFECT,effectID) && turns == 0 ) {
					if (caster.isHide())
						caster.unHide(spell);

					if (target.hasBuff(765))//sacrifice
					{
						if (target.getBuff(765) != null && !target.getBuff(765).getCaster().isDead()) {
							applyEffect_765B(fight, target);
							target = target.getBuff(765).getCaster();
						}
					}

					if(!isCaC) {
						//si la cible a le buff renvoie de sort et que le sort peut etre renvoyer
						if (target.hasBuff(106) && target.getBuffValue(106) >= spellLvl && spell != 0) {
							SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 106, target.getId() + "", target.getId() + ",1", this.effectID);
							//le lanceur devient donc la cible
							target = caster;
						}
					}
				}

				if (this.onHit <= 0) {
					switch (effectID) {
						case 5://Repousse de X case
							applyEffect_5(target, fight);
							break;
						case 6://Attire de X case
							applyEffect_6(target, fight);
							break;
						case 8://Echange les place de 2 joueur
							applyEffect_8(target, fight);
							break;
						case 9://Esquive une attaque en reculant de 1 case
							applyEffect_9(target);
							break;
						case 77://Vol de PM
							applyEffect_77(target, fight);
							break;
						case 78://Bonus PM
							applyEffect_78(target, fight);
							break;
						case 79:// + X chance(%) dommage subis * Y sinon soigné de dommage *Z
							applyEffect_79(target);
							break;
						case 81:// Cura, PDV devueltos
							applyEffect_81(target, fight, isCaC);
							break;
						case 82://Vol de Vie fixe
							applyEffect_82(target, fight);
							break;
						case 84://Vol de PA
							applyEffect_84(target, fight);
							break;
						case 85://Dommage Eau %vie
							applyEffect_85(target, fight);
							break;
						case 86://Dommage Terre %vie
							applyEffect_86(target, fight);
							break;
						case 87://Dommage Air %vie
							applyEffect_87(target, fight);
							break;
						case 88://Dommage feu %vie
							applyEffect_88(target, fight);
							break;
						case 89://Dommage neutre %vie
							applyEffect_89(target, fight);
							break;
						case 90://Donne X% de sa vie
							applyEffect_90(target, fight);
							break;
						case 91://Vol de Vie Eau
							applyEffect_91(target, fight, isCaC);
							break;
						case 92://Vol de Vie Terre
							applyEffect_92(target, fight, isCaC);
							break;
						case 93://Vol de Vie Air
							applyEffect_93(target, fight, isCaC);
							break;
						case 94://Vol de Vie feu
							applyEffect_94(target, fight, isCaC);
							break;
						case 95://Vol de Vie neutre
							applyEffect_95(target, fight, isCaC);
							break;
						case 96://Dommage Eau
							applyEffect_96(target, fight, isCaC);
							break;
						case 97://Dommage Terre
							applyEffect_97(target, fight, isCaC);
							break;
						case 98://Dommage Air
							applyEffect_98(target, fight, isCaC);
							break;
						case 99://Dommage feu
							applyEffect_99(target, fight, isCaC);
							break;
						case 100://Dommage neutre
							applyEffect_100(target, fight, isCaC);
							break;
						case 101://Retrait PA
							applyEffect_101(target, fight);
							break;
						case 106://Renvoie de sort
							applyEffect_106(target);
							break;
						case 107://Renvoie de dom
							applyEffect_107(target);
							break;
						case 108://Soin
							applyEffect_108(target, fight, isCaC);
							break;
						case 111://+ X PA
							applyEffect_111(target, fight);
							break;
						case 116://Malus PO
						case 117://Bonus PO
							applyEffect_PO(target, fight);
							break;
						case 105://Dommages réduits de X
						case 110://+ X vie
						case 112://+Dom
						case 114://Multiplie les dommages par X
						case 115://+Cc
						case 118://Bonus force
						case 119://Bonus Agilité
						case 121://+Dom
						case 122://+EC
						case 123://+Chance
						case 124://+Sagesse
						case 125://+Vitalité
						case 126://+Intelligence
						case 138://%dom
						case 142://Dommages physique
						case 144:// - Dommages (pas bosté)
						case 145://Malus Dommage
						case 152:// - Chance
						case 153:// - Vita
						case 154:// - Agi
						case 155:// - Intel
						case 156:// - Sagesse
						case 157:// - Force
						case 160:// + Esquive PA
						case 161:// + Esquive PM
						case 162:// - Esquive PA
						case 163:// - Esquive PM
						case 171://Malus CC
						case 176:// + prospection
						case 177:// - prospection
						case 178:// + soin
						case 179:// - soin
						case 182://+ Crea Invoc
						case 183://Resist Magique
						case 184://Resist Physique
						case 186://Diminue les dommages %
						case 210://Resist % terre
						case 211://Resist % eau
						case 212://Resist % air
						case 213://Resist % feu
						case 214://Resist % neutre
						case 215://Faiblesse % terre
						case 216://Faiblesse % eau
						case 217://Faiblesse % air
						case 218://Faiblesse % feu
						case 219://Faiblesse % neutre
						case 265://Reduit les Dom de X
							applyEffect_Buff(target, fight);
							break;
						case 127://Retrait PM
							applyEffect_127(target, fight);
							break;
						case 128://+PM
							applyEffect_128(target, fight);
							break;
						case 130://Vol de kamas
							applyEffect_130(fight, target);
							break;
						case 131://Poison : X Pdv  par PA
							applyEffect_131(target);
							break;
						case 132://Enleve les envoutements
							applyEffect_132(target, fight);
							break;
						case 140://Passer le tour
							applyEffect_140(target);
							break;
						case 141://Tue la cible
							applyEffect_141(fight, target);
							break;
						case 143:// PDV rendu (soin sans boost de intel mais juste de +soin ???)
							applyEffect_143(target, fight);
							break;
						case 149://Change l'apparence
							applyEffect_149(fight, target);
							break;
						case 150://Invisibilité
							applyEffect_150(fight, target);
							break;
						case 164:// Daños reducidos en x%
						case 220:// Renvoie dommage
							applyEffect_BuffMinValue(target);
							break;
						case 165:// Maîtrises
							applyEffect_165();
							break;
						case 168://Perte PA non esquivable
							applyEffect_168(target, fight);
							break;
						case 169://Perte PM non esquivable
							applyEffect_169(target, fight);
						case 202://Perception
							applyEffect_202(fight, target);
							break;
						case 266://Vol Chance
							applyEffect_266(fight, target);
							break;
						case 267://Vol vitalité
							applyEffect_267(fight, target);
							break;
						case 268://Vol agitlité
							applyEffect_268(fight, target);
							break;
						case 269://Vol intell
							applyEffect_269(fight, target);
							break;
						case 270://Vol sagesse
							applyEffect_270(fight, target);
							break;
						case 275://TODO : pas dev
							break;
						case 276://TODO : pas dev
							break;
						case 277://TODO : pas dev
							break;
						case 278://TODO : pas dev
							break;
						case 279://TODO : pas dev
							break;
						case 281://Augmente la portée du sort
						case 282://Rend la portée du sort #1 modifiable
						case 283://+#3 de dommages sur le sort #1
						case 284://+#3 de soins sur le sort #1
						case 285://Réduit de #3 le coût en PA du sort #1
						case 286://Réduit de #3 le délai de relance du sort #1
						case 287://#3 aux CC sur le sort #1
						case 288://Désactive le lancer en ligne du sort #1
						case 289://Désactive la ligne de vue du sort #1
						case 290://Augmente de #3 le nombre de lancer maximal par tour du sort #1
						case 291://Augmente de #3 le nombre de lancer maximal par cible du sort #1
						case 292://Fixe à #3 le délai de relance du sort #1
						case 293://Augmente les dégâts de base du sort #1 de #3
						case 294://Diminue la portée du sort #1 de #3
						case 776:// %erosion
							applyEffect_SpellBuff(target);
							break;
						case 320://Vol de PO
							applyEffect_320(fight, target);
							break;
						case 405://tue une invoque pour en ajouter une
							applyEffect_405(target, fight);
							break;
						case 666://Pas d'effet complémentaire
							break;
						case 670: // TODO : pas dev mais jamais utilisé par dofus
							break;
						case 671://Dommages : X% de la vie de l'attaquant (neutre) - Juste au lanceur non ???
							applyEffect_671(target, fight);
							break;
						case 672://Dommages : X% de la vie de l'attaquant (neutre) - A la cellule visé
							applyEffect_672(target, fight);
							break;
						case 765://Sacrifice
							applyEffect_765(target);
							break;
						case 781://Minimize les effets aléatoires
						case 782://Maximise les effets aléatoires
							applyEffect_781(target, fight);
							break;
						case 786://Soin pendant l'attaque
							applyEffect_786(target, fight);
							break;
						case 787://Applique un sort sur la cible
							applyEffect_787(target, fight);
							break;
						case 788://Chatiment de X sur Y tours
							applyEffect_788(target);
							break;
						case 950://Applique Etat X
							applyEffect_950(fight, target);
							break;
						case 951://Enleve Etat X
							applyEffect_951(fight, target);
							break;
						default:
							GameServer.a("Pas d'effet de spell " + effectID + " Dev pour le spell " + spell);
							break;
					}

					if(this.dammageDone > 0){								// ou (100)
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()+ "", target.getId() + "," + -(this.dammageDone), this.effectID);
					}
				} else {
					switch (effectID) {
						case 950://Etat X
						case 951://Enleve l'Etat X
							target.addStateOnHit(this, target, true);
							break;
						case 111:
						case 128:
							target.addBuffOnHit(this, target, true);
							// Don PA/PM
							break;
						case 210:
						case 211:
						case 212:
						case 213:
						case 214:
						case 215:
						case 216:
						case 217:
						case 218:
						case 219:
						case 220:
							target.addBuffOnHit(this, target, true);
							// Resistance et faiblesse
							break;
						default:
							target.addBuffOnHit(this, target, true);
							break;
					}
				}

				if (target.getPdv() <= 0) {
					fight.onFighterDie(target, target);
				}

				if(!target.isDead())
					applyOnHitBuffEffect(fight, target, effectID,acaster,this.dammageDone);

			}
		}

	}


	// Nouvelle fonction
	private void applyOnHitBuffEffect(Fight fight, Fighter target, int EffectID, Fighter caster, int DegatInflige) {

		Collections.sort(target.getOnHitBuff(), Comparator.comparingInt(BuffOnHitEffect::getEffectID));
		Collections.reverse(target.getOnHitBuff());
		// TODO : Virer les anciennes gestion des OnHit bien Moche
		// Nouvelle gestion des effect OnHit
		for ( BuffOnHitEffect onHitEffect : target.getOnHitBuff() ){
			switch (onHitEffect.getBuffType()){
				case 1 : { // Dégat infligé
					if( (onHitEffect.getBuffOnHitConditions().contains(-1) && ArrayUtils.contains(Constant.IS_DIRECTDAMMAGE_EFFECT,EffectID) && turns == 0 ) || onHitEffect.getBuffOnHitConditions().contains(EffectID)){
						// -1 a appliqué sans condition d'élément
						// ou a appliqué si l'effet est dans le tableau des effets attendu
						SpellEffect toApply = onHitEffect.getSpellEffectToApply();
						switch (toApply.getEffectID()){
							case 950 :
								int etatId = toApply.getFixvalue();
								target.setState(etatId, toApply.getTurn()  );
								target.addBuff(toApply.getEffectID(), etatId, toApply.getTurn() , 1, false, toApply.getSpell(), "", target, true);
								SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId() + "", target.getId() + "," + etatId + ",1", toApply.getEffectID() );
								break;
							case 951 :
								if (!target.haveState(toApply.getFixvalue()))
									continue;
								//on enleve l'�tat
								int etatId2 = toApply.getFixvalue();
								target.setState(etatId2, 0);
								target.debuffState(etatId2);
								SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 951, caster.getId() + "", target.getId() + "," + toApply.getFixvalue() + ",0", toApply.getEffectID());
								break;
							default :
								target.addBuff(toApply.getEffectID(), toApply.getMinValue(), toApply.getTurn(), toApply.getTurn(), true, toApply.getSpell(), "", target, true);
								SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, toApply.getEffectID(), caster.getId()+ "", target.getId() + "," + toApply.getMinValue() + "," + toApply.getTurn(), toApply.getEffectID());
								break;
						}
					}
					else{
						//System.out.println(" pas les conditons " + EffectID);
					}
					break;
				}
				case 2 : { // ret PA/PM
					if(onHitEffect.getBuffOnHitConditions().contains(-1) || onHitEffect.getBuffOnHitConditions().contains(EffectID)){
						SpellEffect toApply = onHitEffect.getSpellEffectToApply();
						target.addBuff(toApply.getEffectID(), toApply.getMinValue(), toApply.getTurn(), toApply.getTurn(), true, toApply.getSpell(), "", target, true);
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, toApply.getEffectID(), caster.getId()+ "", target.getId() + "," + toApply.getMinValue() + "," + toApply.getTurn(), toApply.getEffectID());
					}
					break;
				}
				case 3 : { // Si soin


				}
				default:
					break;
			}
		}
	}


	// On bond
	private void applyEffect_4(Fight fight) {
		if (cell.isWalkable(true) && !fight.isOccuped(cell.getId()))//Si la case est prise, on va �viter que les joueurs se montent dessus *-*
		{
			caster.getCell().getFighters().clear();
			caster.setCell(cell);
			caster.getCell().addFighter(caster);

			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getId()
					+ "", caster.getId() + "," + cell.getId());

			this.checkTraps(fight, caster, (short) 1200);

		} else {
			GameServer.a("Case déjà occupé");
		}
		return;
	}

	// On pousse
	private int applyEffect_5(Fighter target, Fight fight) {
		/*if (cibles.size() == 1 && spell == 120 || spell == 310)
			if (!cibles.get(0).isDead())
				caster.setOldCible(cibles.get(0));*/

				boolean next = false;
				if (target.getMob() != null)
					for (int i : Constant.STATIC_INVOCATIONS)
						if (i == target.getMob().getTemplate().getId())
							next = true;

				if (target.haveState(6) || next)
					return 0;

				GameCase cell = this.cell;

				if (target.getCell().getId() == this.cell.getId() || spell == 73)
					cell = caster.getCell();

				int newCellId = PathFinding.newCaseAfterPush(fight, cell, target.getCell(), minvalue);

				if (newCellId == 0)
					return 0;

				if (newCellId < 0) {
					int a = -newCellId, factor = Formulas.getRandomJet(Constant.DO_POU_DOMMAGE);
					double b = (caster.isInvocation() ? caster.getInvocator().getLvl() : caster.getLvl()) / 50;
					if (b < 0.1) b = 0.1;

					int finalDmg = (int) (factor * b * a);

					if (finalDmg < 1) finalDmg = 1;
					if (finalDmg > target.getPdv()) finalDmg = target.getPdv();

					if (target.hasBuff(184)) {
						finalDmg = finalDmg - target.getBuff(184).getFixvalue();//Réduction physique
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(184).getFixvalue());
					}
					if (target.hasBuff(105)) {
						finalDmg = finalDmg - target.getBuff(105).getFixvalue();//Immu
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId() + "", target.getId() + "," + target.getBuff(105).getFixvalue());
					}
					if (finalDmg > 0) {
						if(finalDmg > 200) finalDmg = Formulas.getRandomValue(189, 211);
						target.removePdv(caster, finalDmg);
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId() + "", target.getId() + ",-" + finalDmg);
						if (target.getPdv() <= 0) {
							fight.onFighterDie(target, caster);
							/*if (target.canPlay() && target.getPlayer() != null) fight.endTurn(false);
							else if (target.canPlay()) target.setCanPlay(false);*/
							return 0;
						}
					}
					a = minvalue - a;
					newCellId = PathFinding.newCaseAfterPush(fight, caster.getCell(), target.getCell(), a);

					char dir = PathFinding.getDirBetweenTwoCase(cell.getId(), target.getCell().getId(), fight.getMap(), true);
					GameCase nextCase = fight.getMap().getCase(PathFinding.GetCaseIDFromDirrection(target.getCell().getId(), dir, fight.getMap(), true));

					if (nextCase != null && nextCase.getFirstFighter() != null) {
						Fighter wallTarget = nextCase.getFirstFighter();
						finalDmg = finalDmg / 2;
						if (finalDmg < 1) finalDmg = 1;
						if (finalDmg > 0) {
							wallTarget.removePdv(caster, finalDmg);
							SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId() + "", wallTarget.getId() + ",-" + finalDmg);
							if (wallTarget.getPdv() <= 0)
								fight.onFighterDie(wallTarget, caster);
						}
					}

					if (newCellId == 0)
						return 0;
					if (fight.getMap().getCase(newCellId) == null)
						return 0;
				}

				target.getCell().getFighters().clear();
				target.setCell(fight.getMap().getCase(newCellId));
				target.getCell().addFighter(target);

				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, caster.getId() + "", target.getId() + "," + newCellId);

				Trap.doTraps(fight, target);

		return 0;
	}

	public int getFixvalue() {
		return fixvalue;
	}

	// On attire
	private void applyEffect_6(Fighter target, Fight fight) {

				if (target.haveState(Constant.ETAT_ENRACINE))
					return;

				GameCase eCell = cell;
				//Si meme case
				if (target.getCell().getId() == cell.getId()) {
					//on prend la cellule caster
					eCell = caster.getCell();
				}
				int newCellID = PathFinding.newCaseAfterPush(fight, eCell, target.getCell(), -minvalue);
				if (newCellID == 0)
					return ;

				if (newCellID < 0)//S'il a �t� bloqu�
				{
					int a = -(minvalue + newCellID);
					newCellID = PathFinding.newCaseAfterPush(fight, caster.getCell(), target.getCell(), a);
					if (newCellID == 0)
						return ;

					if (fight.getMap().getCase(newCellID) == null)
						return ;
				}

				target.getCell().getFighters().clear();
				target.setCell(fight.getMap().getCase(newCellID));
				target.getCell().addFighter(target);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, caster.getId() + "", target.getId() + "," + newCellID);
				this.checkTraps(fight, target, (short) 1500);

		return ;
	}

	// On échange de place
	private int applyEffect_8(Fighter target, Fight fight) {

		if (target == null)
			return 0;//ne devrait pas arriver
		if (target.haveState(Constant.ETAT_ENRACINE))
			return 0;//Stabilisation
		if (caster.haveState(Constant.ETAT_PESANTEUR))
			return 0;//Pesanteur
		if (target.haveState(Constant.ETAT_PESANTEUR))
			return 0;//Pesanteur
		if (target.haveState(Constant.ETAT_PORTE)) {
			caster.getPlayer().sendMessage("Il n'est pas possible d'utiliser ce sort avec quelqu'un qui est dans l'état porté");
			return 0;
		}
		if (target.haveState(Constant.ETAT_PORTEUR)) {
			caster.getPlayer().sendMessage("Il n'est pas possible d'utiliser ce sort avec quelqu'un qui est dans l'état porteur");
			return 0;
		}
		if (caster.haveState(Constant.ETAT_PORTE)){
			caster.getPlayer().sendMessage("Il n'est pas possible d'utiliser ce sort car tu es dans l'état porté");
			return 0;
		}
		if (caster.haveState(Constant.ETAT_PORTEUR)){
			caster.getPlayer().sendMessage("Il n'est pas possible d'utiliser ce sort car tu es dans l'état porteur");
			return 0;
		}

		switch (spell) {
			case 438://Transpo
				//si les 2 joueurs ne sont pas dans la meme team, on ignore
				if (target.getTeam() != caster.getTeam())
					return 0;
				break;

			case 445://Coop
				//si les 2 joueurs sont dans la meme team, on ignore
				if (target.getTeam() == caster.getTeam())
					return 0;
				break;
			case 449://D�tour
				break;
			default:
				break;
		}

		//on enleve les persos des cases
		target.getCell().getFighters().clear();
		caster.getCell().getFighters().clear();
		//on retient les cases
		GameCase exTarget = target.getCell();
		GameCase exCaster = caster.getCell();
		//on �change les cases
		target.setCell(exCaster);
		caster.setCell(exTarget);
		//on ajoute les fighters aux cases
		target.getCell().addFighter(target);
		caster.getCell().addFighter(caster);

		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getId()
				+ "", target.getId() + "," + exCaster.getId());
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getId()
				+ "", caster.getId() + "," + exTarget.getId());

		this.checkTraps(fight, target, (short) 1500);
		this.checkTraps(fight, caster, (short) 1500);

		return 0;
	}

	// Dérobade
	private int applyEffect_9(Fighter target) {
		target.addBuff(effectID, minvalue, turns, 1, true, spell, args, caster, true);
		return 0;
	}

	private void applyEffect_50(Fight fight) {
		//Porter
		Fighter target = cell.getFirstFighter();
		if (target == null) return;
		if (target.getMob() != null)
			for (int i : Constant.STATIC_INVOCATIONS)
				if (i == target.getMob().getTemplate().getId())
					return;
		if (target.haveState(6)) return;//Stabilisation

		//on enleve le porté de sa case
		target.getCell().getFighters().clear();
		//on lui définie sa nouvelle case
		target.setCell(caster.getCell());

		//on applique les états
		target.setState(Constant.ETAT_PORTE, 1);
		caster.setState(Constant.ETAT_PORTEUR, 1);
		//on lie les 2 Fighter
		target.setHoldedBy(caster);
		caster.setIsHolding(target);

		//on envoie les packets
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 50, caster.getId() + "", "" + target.getId(), this.effectID);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, target.getId() + "", target.getId() + "," + Constant.ETAT_PORTE + ",1");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId() + "", caster.getId() + "," + Constant.ETAT_PORTEUR + ",1");
		TimerWaiter.addNext(() -> fight.setCurAction(""), 1500, TimeUnit.MILLISECONDS);
	}

	private void applyEffect_51(final Fight fight) {
		//Si case pas libre
		if (!cell.isWalkable(true) || cell.getFighters().size() > 0) return;
		Fighter target = caster.getIsHolding();
		if (target == null) return;

		//on ajoute le porté a sa case
		target.setCell(cell);
		target.getCell().addFighter(target);

		//on enleve les états
		target.setState(Constant.ETAT_PORTE,0,caster.getId()); //infinite duration
		caster.setState(Constant.ETAT_PORTEUR,0,caster.getId()); //infinite duration

		//on dé-lie les 2 Fighter
		target.setHoldedBy(null);
		caster.setIsHolding(null);

		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 51, caster.getId() + "", cell.getId() + "");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, target.getId() + "", target.getId() + "," + Constant.ETAT_PORTE + ",0");
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId() + "", caster.getId() + "," + Constant.ETAT_PORTEUR + ",0");

		this.checkTraps(fight, target, (short) 500);
	}

	private void applyEffect_77(Fighter target, Fight fight) {  // Vol PM Corrigé
		int value = Formulas.getRandomJet(jet, caster);
		int num = 0;

		int val = Formulas.getPointsLost('a', value, caster, target);

		if (val < value)
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 309, caster.getId() + "", target.getId() + "," + (value - val), this.effectID);

		if (val < 1)
			return;

		if(turns ==0)
			target.addBuff(effectID, val, 1, 1, true, spell, args, caster, true);
		else
			target.addBuff(effectID, val, turns, turns, true, spell, args, caster, true);

		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_REM_PM, caster.getId() + "", target.getId() + ",-" + val + "," + turns, this.effectID);
		num += val;

		if (num != 0) {
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 78, caster.getId() + "", caster.getId() + "," + num+ "," + turns, this.effectID);

			if(turns != 0)
				caster.addBuff(78, num, turns, turns, false, spell, args, caster, false);

			if (caster.canPlay())
				caster.setCurPm(fight, num);
		}
	}

	private void applyEffect_78(Fighter target, Fight fight)//Bonus PM
	{
		int val = Formulas.getRandomJet(jet, caster);
		if (val == -1) {
			GameServer.a("Valeur de bonus PA negative " + spell);
			return;
		}

			target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns, this.effectID);

	}

	private void applyEffect_79(Fighter target) {
		if (turns < 1)
			return;//Je vois pas comment, vraiment ...

		target.addBuff(effectID, -1, turns, 0, true, spell, args, caster, true);//on applique un buff
	}

	private void applyEffect_81(Fighter target, Fight fight, boolean isCaC) { // heal
		if (turns == 0) {
			String jet = this.getJet();
			int heal = 0;
			if (jet.equals("0d0+*")) {
				heal = this.getMinValue();
			} else {
				heal = Formulas.getRandomJet(this.getJet(), caster,target);
			}

			// Why ??????   heal = getMaxMinSpell(cible, heal);
			int pdvMax = target.getPdvMax();
			int healFinal = Formulas.calculFinalHeal(caster, heal, isCaC,spell);

			if ((healFinal + target.getPdv()) > pdvMax)
				healFinal = pdvMax - target.getPdv();
			if (healFinal < 1)
				healFinal = 0;
			target.removePdv(caster, -healFinal);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 81, caster.getId() + "", target.getId() + "," + healFinal, this.effectID);

		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);
		}
	}

	private void applyEffect_82(Fighter target, Fight fight) {
		if (turns == 0) {

				int dmg = Formulas.getRandomJet(jet, caster, target);
				int finalDommage = dmg;
				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_NULL);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				/*finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage, this.effectID);*/

				//Vol de vie
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal, this.effectID);

				this.dammageDone = finalDommage;
		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_84(Fighter target, Fight fight) { // Vol PA corrigé !
		int value = Formulas.getRandomJet(jet, caster);
		int num = 0;

		int val = Formulas.getPointsLost('a', value, caster, target);
		if (val < value)
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 308, caster.getId() + "", target.getId() + "," + (value - val), this.effectID);

		if (val < 1)
			return;

		if(turns ==0) // Pour les cas de vol pendant 1 tour
			target.addBuff(effectID, val, 1, 1, true, spell, args, caster, true);
		else
			target.addBuff(effectID, val, turns, turns, true, spell, args, caster, true);

		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_REM_PA, caster.getId() + "", target.getId() + ",-" + val + "," + turns, this.effectID);
		num += val;

		if (num != 0) {
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 120, caster.getId() + "", caster.getId() + "," + num + "," + turns, this.effectID);

			if(turns !=0)
				caster.addBuff(120, num, turns, turns, true, spell, args, caster, false);

			//Gain de PA pendant le tour de jeu
			if (caster.canPlay())
				caster.setCurPa(fight, num);
		}
	}

	private void applyEffect_85(Fighter target, Fight fight) {
		if (turns == 0) {
				int resP = target.getTotalStats().getEffect(Constant.STATS_ADD_RP_EAU);
				int resF = target.getTotalStats().getEffect(Constant.STATS_ADD_R_EAU);
				if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_EAU);
					resF += target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_EAU);
				}
				int dmg = Formulas.getRandomJet(jet, caster, target);//%age de pdv inflig�
				int val = caster.getPdv() / 100 * dmg;//Valeur des d�gats
				//retrait de la r�sist fixe
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
				val -= reduc;
				if (val < 0)
					val = 0;

				val = applyOnHitBuffs(val, target, caster, fight, Constant.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

				if (val > target.getPdv())
					val = target.getPdv();//Target va mourrir
				target.removePdv(caster, val);
			// TODO : a géré différemment

				/*int cura = val;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura, this.effectID);
				}
				val = -(val);*/
				/*SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val, this.effectID);*/

			this.dammageDone = val;
		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_86(Fighter target, Fight fight) {
		if (turns == 0) {
				int resP = target.getTotalStats().getEffect(Constant.STATS_ADD_RP_TER);
				int resF = target.getTotalStats().getEffect(Constant.STATS_ADD_R_TER);
				if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_TER);
					resF += target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_TER);
				}
				int dmg = Formulas.getRandomJet(jet, caster, target);//%age de pdv inflig�
				int val = caster.getPdv() / 100 * dmg;//Valeur des d�gats
				//retrait de la r�sist fixe
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
				val -= reduc;
				if (val < 0)
					val = 0;

				val = applyOnHitBuffs(val, target, caster, fight, Constant.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

				if (val > target.getPdv())
					val = target.getPdv();//Target va mourrir
				target.removePdv(caster, val);

			// TODO : a géré différemment

				/*int cura = val;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura, this.effectID);
				}
				val = -(val);*/
				/*SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val, this.effectID);*/


			this.dammageDone = val;
		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_87(Fighter target, Fight fight) {
		if (turns == 0) {

			/*if (spell == 1009)
					if (LaunchedSpell.haveEffectTarget(fight.getTeam0(), target, 108) <= 0)
						continue;*/

				int resP = target.getTotalStats().getEffect(Constant.STATS_ADD_RP_AIR);
				int resF = target.getTotalStats().getEffect(Constant.STATS_ADD_R_AIR);
				if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_AIR);
					resF += target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_AIR);
				}
				int dmg = Formulas.getRandomJet(jet, caster, target);//%age de pdv inflig�
				int val = caster.getPdv() / 100 * dmg;//Valeur des d�gats
				//retrait de la r�sist fixe
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
				val -= reduc;
				if (val < 0)
					val = 0;

				val = applyOnHitBuffs(val, target, caster, fight, Constant.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

				if (val > target.getPdv())
					val = target.getPdv();//Target va mourrir
				target.removePdv(caster, val);

			// TODO : a géré différemment
				/*int cura = val;

				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura, this.effectID);
				}

				val = -(val);*/
				/*SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val, this.effectID);*/

			this.dammageDone = val;
		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_88(Fighter target, Fight fight) {
		if (turns == 0) {
				int resP = target.getTotalStats().getEffect(Constant.STATS_ADD_RP_FEU);
				int resF = target.getTotalStats().getEffect(Constant.STATS_ADD_R_FEU);
				if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_FEU);
					resF += target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_FEU);
				}
				int dmg = Formulas.getRandomJet(jet, caster, target);//%age de pdv inflig�
				int val = caster.getPdv() / 100 * dmg;//Valeur des d�gats
				//retrait de la r�sist fixe
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
				val -= reduc;
				if (val < 0)
					val = 0;

				val = applyOnHitBuffs(val, target, caster, fight, Constant.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

				if (val > target.getPdv())
					val = target.getPdv();//Target va mourrir
				target.removePdv(caster, val);
			// TODO : a géré différemment
				/*int cura = val;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura, this.effectID);
				}
				val = -(val);*/
				/*SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val, this.effectID);*/

					/*if (target.canPlay() && target.getPlayer() != null)
						fight.endTurn(false);
					else if (target.canPlay())
						target.setCanPlay(false);*/

			this.dammageDone = val;
		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff

		}
	}

	private void applyEffect_89(Fighter target, Fight fight) {
		if (turns == 0) {

				int resP = target.getTotalStats().getEffect(Constant.STATS_ADD_RP_NEU);
				int resF = target.getTotalStats().getEffect(Constant.STATS_ADD_R_NEU);
				if (target.getPlayer() != null)//Si c'est un joueur, on ajoute les resists bouclier
				{
					resP += target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_NEU);
					resF += target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_NEU);
				}
				int dmg = Formulas.getRandomJet(jet, caster, target);//%age de pdv inflig�
				int val = caster.getPdv() / 100 * dmg;//Valeur des d�gats
				//retrait de la r�sist fixe
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;//Reduc %resis
				val -= reduc;
				int armor = 0;
				for (SpellEffect SE : target.getBuffsByEffectID(105)) {
					int intell = target.getTotalStats().getEffect(Constant.STATS_ADD_INTE);
					int carac = target.getTotalStats().getEffect(Constant.STATS_ADD_FORC);
					int value = SE.getFixvalue();
					int a = value
							* (100 + (int) (intell / 2) + (int) (carac / 2))
							/ 100;
					armor += a;
				}
				if (armor > 0) {
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 105, caster.getId()
							+ "", target.getId() + "," + armor, this.effectID);
					val = val - armor;
				}
				if (val < 0)
					val = 0;

				val = applyOnHitBuffs(val, target, caster, fight, Constant.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

				if (val > target.getPdv())
					val = target.getPdv();//Target va mourrir
				target.removePdv(caster, val);

			// TODO : a géré différemment
				/*int cura = val;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura, this.effectID);
				}
				val = -(val);*/
				/*SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val, this.effectID);*/

			this.dammageDone = val;
		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_90(Fighter target, Fight fight) {
		if (turns == 0)//Si Direct
		{
			int pAge = Formulas.getRandomJet(jet, caster);
			int val = pAge * (caster.getPdv() / 100);
			//Calcul des Doms recus par le lanceur
			int finalDommage = applyOnHitBuffs(val, caster, caster, fight, Constant.ELEMENT_NULL);//S'il y a des buffs sp�ciaux

			if (finalDommage > caster.getPdv())
				finalDommage = caster.getPdv();//Caster va mourrir
			caster.removePdv(caster, finalDommage);
			/*finalDommage = -(finalDommage);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
					+ "", caster.getId() + "," + finalDommage, this.effectID);*/

			//Application du soin

				if ((val + target.getPdv()) > target.getPdvMax())
					val = target.getPdvMax() - target.getPdv();//Target va mourrir
				target.removePdv(caster, -val);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + ",+" + val, this.effectID);

			this.dammageDone = finalDommage;

		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_91(Fighter target, Fight fight,
								boolean isCaC)//vole eau
	{
		if (turns == 0) {
			// dégats
			int dmg = Formulas.getRandomJet(jet, caster, target);
			int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_EAU, dmg, false, isCaC, spell);
			// TODO : Virer les appel a ceci.
			finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_EAU);//S'il y a des buffs sp�ciaux

			if (finalDommage > target.getPdv())
				finalDommage = target.getPdv();//Target va mourrir
			target.removePdv(caster, finalDommage);
			/*finalDommage = -(finalDommage);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
					+ "", target.getId() + "," + finalDommage, this.effectID);*/

			// Soin vol de vie
			int heal = (int) (-finalDommage) / 2;
			if ((caster.getPdv() + heal) > caster.getPdvMax())
				heal = caster.getPdvMax() - caster.getPdv();
			caster.removePdv(caster, -heal);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
					+ "", caster.getId() + "," + heal, this.effectID);

			this.dammageDone = finalDommage;
		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_92(Fighter target, Fight fight,
								boolean isCaC)//vole terre
	{
		if (turns == 0) {
			// dégats
				int dmg = Formulas.getRandomJet(jet, caster, target);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_TERRE, dmg, false, isCaC, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_TERRE);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				/*finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage, this.effectID);*/

			// Soin vol de vie
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal, this.effectID);

			this.dammageDone = finalDommage;

		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_93(Fighter target, Fight fight,
								boolean isCaC)//vole air
	{

		if (turns == 0) {
				int dmg = Formulas.getRandomJet(jet, caster, target);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_AIR, dmg, false, isCaC, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_AIR);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				/*finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage, this.effectID);*/

				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal, this.effectID);


			this.dammageDone = finalDommage;
		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}

	}

	private void applyEffect_94(Fighter target, Fight fight,
								boolean isCaC) {
		if (turns == 0) {
			//Vol de vie dégat
				int dmg = Formulas.getRandomJet(jet, caster, target);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_FEU, dmg, false, isCaC, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_FEU);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				/*finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage, this.effectID);*/

				//Vol de vie soin
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal, this.effectID);

			this.dammageDone = finalDommage;
		} else {

				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff

		}
	}

	private void applyEffect_95(Fighter target, Fight fight,
								boolean isCaC) { // Vol neutre ?

		if (turns == 0) {
				int dmg = Formulas.getRandomJet(jet, caster, target);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_NEUTRE, dmg, false, isCaC, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_NEUTRE);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);
				/*finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage, this.effectID);*/

				// Vol de vie neutre
				int heal = (int) (-finalDommage) / 2;
				if ((caster.getPdv() + heal) > caster.getPdvMax())
					heal = caster.getPdvMax() - caster.getPdv();
				caster.removePdv(caster, -heal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
						+ "", caster.getId() + "," + heal, this.effectID);

				this.dammageDone = finalDommage;
		} else {
				target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_96(Fighter target, Fight fight,
								boolean isCaC)//dmg eau
	{
		if (turns == 0) {

				int dmg = Formulas.getRandomJet(jet, caster, target);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_EAU, dmg, false, isCaC, spell);

				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_EAU);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);

				/*int cura = finalDommage;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura, this.effectID);
				}*/
				/*finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage, this.effectID);*/


			this.dammageDone = finalDommage;

		} else {
			target.addBuff(effectID, 0, turns, 1, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_97(Fighter target, Fight fight,
								boolean isCaC)//dmg terre
	{
		if (turns == 0) {
				int dmg = Formulas.getRandomJet(jet, caster, target);

				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_TERRE, dmg, false, isCaC, spell);
				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_TERRE);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);

				/*int cura = finalDommage;

				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura, this.effectID);
				}
				finalDommage = -(finalDommage);

				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage, this.effectID);*/

				this.dammageDone = finalDommage;

		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_98(Fighter target, Fight fight,
								boolean isCaC)//dmg air
	{
		if (turns == 0) {
				int dmg = Formulas.getRandomJet(jet, caster, target);

				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_AIR, dmg, false, isCaC, spell);
				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_AIR);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir

				target.removePdv(caster, finalDommage);

				/*int cura = finalDommage;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura, this.effectID);
				}
				finalDommage = -(finalDommage);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + finalDommage, this.effectID);*/

			this.dammageDone = finalDommage;

		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_99(Fighter target, Fight fight,
								boolean isCaC)//dmg feu
	{
		int dmg = Formulas.getRandomJet(jet, caster, target);
		if (turns == 0) {

				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_FEU, dmg, false, isCaC, spell);
				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_FEU);//S'il y a des buffs sp�ciaux
				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);

				/*int cura = finalDommage;
				if (target.hasBuff(786)) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura, this.effectID);
				}*/



			this.dammageDone = finalDommage;

		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_100(Fighter target, Fight fight,
								 boolean isCaC) { //dmg neutre
		if (turns <= 0) {

				int dmg = Formulas.getRandomJet(jet, caster, target);
				int finalDommage = Formulas.calculFinalDommage(fight, caster, target, Constant.ELEMENT_NEUTRE, dmg, false, isCaC, spell);
				finalDommage = applyOnHitBuffs(finalDommage, target, caster, fight, Constant.ELEMENT_NEUTRE);//S'il y a des buffs sp�ciaux

				if (finalDommage > target.getPdv())
					finalDommage = target.getPdv();//Target va mourrir
				target.removePdv(caster, finalDommage);

			/*	SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + -(finalDommage), this.effectID);*/

			this.dammageDone = finalDommage;
		} else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_101(Fighter target, Fight fight) {
		int value = Formulas.getRandomJet(jet, caster, target);
		int remove = Formulas.getPointsLost('a', value, caster, target);

		if ((value - remove) > 0)
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 308, caster.getId() + "", target.getId() + "," + (value - remove), this.effectID);

		if (remove > 0) {
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 101, target.getId() + "", target.getId() + ",-" + remove, this.effectID);

			if(turns != 0)
				target.addBuff(effectID, remove, turns, turns, false, spell, args, caster, false);
		}

		if (fight.getFighterByOrdreJeu() == target)
			fight.setCurFighterPa(fight.getCurFighterPa() - remove);

	}


	//TODO : rajouter la notion de chance de renvoi dans Info!
	private void applyEffect_106(Fighter target) {
		int val = -1;
		try {
			val = this.getMaxValue();//Niveau de sort max
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (val == -1)
			return;

		this.duration = turns;
		target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
	}

	private void applyEffect_107(Fighter target) {
		if (turns < 1)
			return;//Je vois pas comment, vraiment ...
		else {
			target.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_108(Fighter cible, Fight fight, boolean isCaC) {// healcion

		//TODO : Why ???? if (isCaC) return;

		if (turns <= 0) {
			String jet = this.getJet();
			int heal = 0;
			if (jet.equals("0d0+*")) {
				heal = this.getMinValue();
			} else {
				heal = Formulas.getRandomJet(this.getJet(), caster,cible);
			}

			// Why ??????   heal = getMaxMinSpell(cible, heal);
			int pdvMax = cible.getPdvMax();
			int healFinal = Formulas.calculFinalHeal(caster, heal, isCaC,spell);

				if ((healFinal + cible.getPdv()) > pdvMax)
					healFinal = pdvMax - cible.getPdv();
				if (healFinal < 1)
					healFinal = 0;
				cible.removePdv(caster, -healFinal);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getId() + "", cible.getId() + "," + healFinal, this.effectID);

		} else {
			 cible.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);
		}
	}

	private void applyEffect_109(Fight fight)//Dommage pour le lanceur (fixes)
	{
		if (turns <= 0) {
			int dmg = Formulas.getRandomJet(jet, caster);
			int finalDommage = Formulas.calculFinalDommage(fight, caster, caster, Constant.ELEMENT_NULL, dmg, false, false, spell);

			finalDommage = applyOnHitBuffs(finalDommage, caster, caster, fight, Constant.ELEMENT_NULL);//S'il y a des buffs sp�ciaux
			if (finalDommage > caster.getPdv())
				finalDommage = caster.getPdv();//Caster va mourrir
			caster.removePdv(caster, finalDommage);
			/*finalDommage = -(finalDommage);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
					+ "", caster.getId() + "," + finalDommage, this.effectID);*/

			this.dammageDone = finalDommage;
		} else {
			caster.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);//on applique un buff
		}
	}

	private void applyEffect_111(Fighter target, Fight fight) { // Don PA
		int val = Formulas.getRandomJet(jet, caster);
		if (val == -1) {
			GameServer.a("Valeur jet negatif " + spell);
			return;
		}

		target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);

		//Gain de PA pendant le tour de jeu
		if (target.canPlay() && target == caster)
			target.setCurPa(fight, val);

		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", target.getId() + "," + val + "," + turns, this.effectID);

	}

	private void applyEffect_PO(Fighter target, Fight fight)//Malus PO
	{
		int val = Formulas.getRandomJet(jet, caster);
		if (val == -1) {
			GameServer.a("Valeur jet negatif " + spell);
			return;
		}

		target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()+ "", target.getId() + "," + val + "," + turns, this.effectID);

		// TODO : on est sur que c'est utile ????
		/*if (target.canPlay() && target == caster)
			target.getTotalStats().addOneStat(effectID, val);*/

	}

	private void applyEffect_Buff(Fighter target, Fight fight)//Bonus Force
	{
		int val = Formulas.getRandomJet(jet, caster);
		if (val == -1) {
			GameServer.a("Valeur jet negatif " + spell);
			return;
		}

		target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId() + "", target.getId() + "," + val + "," + turns, this.effectID);
	}


	private void applyEffect_120(Fight fight) // Bonus PA au lanceur si chance!
	{
		int val = Formulas.getRandomJet(jet, caster);
		if (val == -1) {
			GameServer.a("Valeur jet negatif " + spell);
			return;
		}

		if(this.getChance() != 0 && this.getChance() !=100){
			Random random = new Random();
			int randomint = random.nextInt(101);
			if( randomint <= this.getChance() ) {
				caster.addBuff(effectID, val, turns, turns, true, spell, args, caster, true);
				caster.setCurPa(fight, val);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
						+ "", caster.getId() + "," + val + "," + turns, this.effectID);
			}
		}
		else{
			caster.addBuff(effectID, val, turns, turns, true, spell, args, caster, true);
			caster.setCurPa(fight, val);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", caster.getId() + "," + val + "," + turns, this.effectID);
		}
	}


	private void applyEffect_127(Fighter target, Fight fight) { //retraitPM
		int value = Formulas.getRandomJet(jet,caster,target);
		int retrait = Formulas.getPointsLost('m', value, caster, target);

		if ((value - retrait) > 0)
		   	SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 309, caster.getId()+ "", target.getId() + "," + (value - retrait), this.effectID);

		if (retrait > 0) {
			   target.addBuff(effectID, retrait, turns, turns, false, spell, args, caster, true);
			   SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, this.effectID, target.getId() + "", target.getId() + ",-" + retrait, this.effectID);
		}

		if(target.canPlay())
			target.setCurPm(fight, -retrait);


	}

	private void applyEffect_128(Fighter target, Fight fight) { // Gain PM
		int val = Formulas.getRandomJet(jet, caster);
		if (val == -1) {
			GameServer.a("Valeur jet negatif " + spell);
			return;
		}

		target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);

		//Gain de PM pendant le tour de jeu
		if (target.canPlay())
			target.setCurPm(fight, val);

		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()+ "", target.getId() + "," + val + "," + turns, this.effectID);
	}

	private void applyEffect_130(Fight fight, Fighter target) { // Vol de kamas ca n'existe pas en buff
		int kamas = Formulas.getRandomJet(jet, caster, target);
		if (caster.getPlayer() == null) return;

		if (target.getPlayer() != null) {
					target.getPlayer().addKamas(-kamas);
					if (target.getPlayer().getKamas() < 0)
						target.getPlayer().setKamas(0);
		}
		else {
					if (target.getMob() != null) {
						return;
					}

					if (target.isInvocation()) {
						return;
					}
		}

		caster.getPlayer().addKamas(kamas);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 130, caster.getId() + "", kamas + "", this.effectID);

	}

	private void applyEffect_131(Fighter target) {
		target.addBuff(effectID, minvalue, turns, 1, true, spell, args, caster, true);
	}

	private void applyEffect_132(Fighter target, Fight fight) {

		target.debuff();
		if (target.isHide()) target.unHide(spell);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 132, caster.getId() + "", target.getId() + "", this.effectID);

		target.toRebuff =true;

	}


	private void applyEffect_140(Fighter target) { // Passe le prochain tour
		target.addBuff(effectID, 0, 0, 0, true, spell, args, caster, true);
	}

	private void applyEffect_141(Fight fight, Fighter target) { // On tue tout simplement
		fight.onFighterDie(target, target);
	}

	private void applyEffect_143(Fighter cible, Fight fight) { // Du soin fixe qui ne prend en compte que les bonus /malus en soin (sans l'intel)
		if (turns <= 0) {
			int val = Formulas.getRandomJet(jet, caster);
			if(val == -1)return;

			int heals = caster.getTotalStats().getEffect(178) - caster.getTotalStats().getEffect(179);
			int healfinal = val + heals;
			if (healfinal < 1)
				healfinal = 0;
			if ((healfinal + cible.getPdv()) > cible.getPdvMax())
				healfinal = cible.getPdvMax() - cible.getPdv();
			if (healfinal < 1)
				healfinal = 0;

			cible.removePdv(caster, -healfinal);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 108, caster.getId()
					+ "", cible.getId() + "," + healfinal, this.effectID);

		} else {
			cible.addBuff(effectID, 0, turns, 0, true, spell, args, caster, true);
		}
	}



	// Change l'apparence
	private void applyEffect_149(Fight fight, Fighter target) {
		int id = -1;

		try {
			id = Integer.parseInt(info);
		} catch (Exception e) {
			e.printStackTrace();
		}

			if (spell == 686) {
				if (target.getPlayer() != null
						&& target.getPlayer().getSexe() == 1
						|| target.getMob() != null
						&& target.getMob().getTemplate().getId() == 547)
					id = 8011;
			}

			if (id == -1)
				id = target.getDefaultGfx();

			target.addBuff(effectID, id, turns, 1, true, spell, args, caster, true);

			int defaut = target.getDefaultGfx();
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()+ "", target.getId() + "," + defaut + "," + id + ","+ (target.canPlay() ? turns + 1 : turns), this.effectID);

	}

	private void applyEffect_150(Fight fight, Fighter target) { // BUFF NO VALUE NEEDED
		if (turns == 0)
			return;

		/*if (spell == 547 || spell == 546 || spell == 548 || spell == 525) {
			caster.addBuff(effectID, 0, 3, 1, true, spell, args, caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId()
					+ "", caster.getId() + "," + (3 - 1), this.effectID);
			return;
		}*/

		target.addBuff(effectID, 0, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, caster.getId() + "", target.getId() + "," + (turns - 1), this.effectID);

	}


	private void applyEffect_BuffMinValue(Fighter target) { // BUFF MIN VALUE NEEDED ONLY
		int val = minvalue;
		if (val == -1) return;

		target.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
	}

	private void applyEffect_165() { // BUFF MAX VALUE NEEDED ONLY
		int value = maxvalue;
		if (value == -1) return;

		caster.addBuff(effectID, value, turns, 1, true, spell, args, caster, true);
	}

	// Refait a tester
	private void applyEffect_168(Fighter cible, Fight fight) {// - PA, no esquivables
		int value = Formulas.getRandomJet(jet,caster,cible);
		cible.addBuff(effectID, value, turns, turns, true, spell, args, caster, true);

		if (turns <= 1 || duration <= 1)
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, effectID, cible.getId()+ "", cible.getId() + ",-" + value, this.effectID);

		if (fight.getFighterByOrdreJeu() == cible)
			fight.setCurFighterPa(fight.getCurFighterPa() - value);

	}

	// Refait a tester
	private void applyEffect_169(Fighter cible, Fight fight) { // - PM, no esquivables
		int value = Formulas.getRandomJet(jet,caster,cible);
		cible.addBuff(effectID, value, turns, turns, true, spell, args, caster, true);
		if (turns <= 1 || duration <= 1)
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 169, cible.getId() + "", cible.getId() + ",-" + value, this.effectID);

		if (fight.getFighterByOrdreJeu() == cible)
			fight.setCurFighterPm(fight.getCurFighterPm() - value);

	}

	private void applyEffect_180(Fight fight)//invocation
	{
		int cell = this.cell.getId();
		if (!this.cell.getFighters().isEmpty())
			return;

		int id = fight.getNextLowerFighterGuid();
		Player clone = Player.ClonePerso(caster.getPlayer(), -id - 10000, (caster.getPlayer().getMaxPdv() - ((caster.getLvl() - 1) * 5 + 50)));
		clone.setFight(fight);

		Fighter fighter = new Fighter(fight, clone);
		fighter.fullPdv();
		fighter.setTeam(caster.getTeam());
		fighter.setInvocator(caster);

		fight.getMap().getCase(cell).addFighter(fighter);
		fighter.setCell(fight.getMap().getCase(cell));

		fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(caster) + 1), fighter);
		fight.addFighterInTeam(fighter, caster.getTeam());

		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 180, caster.getId() + "", fighter.getGmPacket('+', true).substring(3), this.effectID);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", fight.getGTL(), this.effectID);

		Trap.doTraps(fight, fighter);
	}

	private void applyEffect_181(Fight fight)//invocation
	{
		Fighter current = this.caster;
		int cell = this.cell.getId();
		if(cell == current.getCell().getId())
		{
				int cellToCheck1 = PathFinding.getCaseIdWithPo(cell, 'b', 1);
				int cellToCheck2 = PathFinding.getCaseIdWithPo(cell, 'd', 1);
				int cellToCheck3 = PathFinding.getCaseIdWithPo(cell, 'f', 1);
				int cellToCheck4 = PathFinding.getCaseIdWithPo(cell, 'h', 1);
				if(fight.getMap().getCase(cellToCheck1) != null & fight.getMap().getCase(cellToCheck1).getFighters().isEmpty() & fight.getMap().getCase(cellToCheck1).isWalkable(false, true, -1))
				{
					cell = cellToCheck1;
				}else if(fight.getMap().getCase(cellToCheck2) != null & fight.getMap().getCase(cellToCheck2).getFighters().isEmpty() & fight.getMap().getCase(cellToCheck2).isWalkable(false, true, -1))
				{
					cell = cellToCheck2;
				} else if(fight.getMap().getCase(cellToCheck3) != null & fight.getMap().getCase(cellToCheck3).getFighters().isEmpty() & fight.getMap().getCase(cellToCheck3).isWalkable(false, true, -1))
				{
					cell = cellToCheck3;
				} else if(fight.getMap().getCase(cellToCheck4) != null & fight.getMap().getCase(cellToCheck4).getFighters().isEmpty() & fight.getMap().getCase(cellToCheck4).isWalkable(false, true, -1))
				{
					cell = cellToCheck4;
				}
		}
		if (!fight.getMap().getCase(cell).getFighters().isEmpty())
			return;

		int id = -1, level = -1;

		try {
			String mobs = minvalue+"", levels = maxvalue+"";

			if (mobs.contains(":")) {
				String[] split = mobs.split(":");
				id = Integer.parseInt(split[Formulas.getRandomValue(0, split.length - 1)]);
			} else {
				id = Integer.parseInt(mobs);
			}

			if (levels.contains(":")) {
				String[] split = levels.split(":");
				level = Integer.parseInt(split[Formulas.getRandomValue(0, split.length - 1)]);
			} else {
				level = Integer.parseInt(levels);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		MobGrade MG = null;
		Monster monster = World.world.getMonstre(id);
		if(monster == null) return;
		try {
			MG = monster.getGradeByLevel(level);
			if(MG == null) {
				MG = monster.getRandomGrade();
				if(MG == null) return;
			}
			MG = MG.getCopy();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (id == -1 || level == -1 || MG == null)
			return;

		MG.setInFightID(fight.getNextLowerFighterGuid());

		Fighter F;
		if(caster.getPlayer() != null) {
			boolean leadercontrolinvo = false;
			if (caster.getPlayer().getSlaveLeader() != null) {
				if (fight.isInFight(caster.getPlayer().getSlaveLeader())) {
					leadercontrolinvo = caster.getPlayer().getSlaveLeader().controleinvo;
				}
			}
			if ( (caster.getPlayer().controleinvo || leadercontrolinvo )  &&  id != 285 && id != 262) {
				Player mobControlable = Player.createInvoControlable(caster.getFight().getSigIDFighter(), MG, caster);
				F = new Fighter(fight, mobControlable);
				F.setControllable(true);
				F.setTeam(caster.getTeam());
				caster.getPlayer().addCompagnon(F.getPlayer());
				mobControlable.setFight(fight);
			} else{
				MG.modifStatByInvocator(caster);
				F = new Fighter(fight, MG);
				F.setTeam(caster.getTeam());
			}
		}
		else{
			F = new Fighter(fight, MG);
			F.setTeam(caster.getTeam());
		}

		F.setInvocator(caster);
		fight.getMap().getCase(cell).addFighter(F);
		F.setCell(fight.getMap().getCase(cell));
		F.fullPdv();
		fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(caster) + 1), F);
		fight.addFighterInTeam(F, caster.getTeam());
		String gm = F.getGmPacket('+', true).substring(3);
		String gtl = fight.getGTL();
		try {
			if (this.caster.getMob() != null)
				Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 181, caster.getId() + "", gm, this.effectID);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", gtl, this.effectID);
		caster.nbrInvoc++;

		this.checkTraps(fight, F, (short) 1200);

	}

	private void applyEffect_185(Fight fight) {
		int monster = -1, level = -1;

		try {
			monster = Integer.parseInt(args.split(";")[0]);
			level = Integer.parseInt(args.split(";")[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		MobGrade mobGrade;

		try {
			mobGrade = World.world.getMonstre(monster).getGradeByLevel(level).getCopy();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		if (monster == -1 || level == -1 || mobGrade == null)
			return;

		if (monster == 556 && this.caster.getPlayer() != null)
			mobGrade.modifStatByInvocator(this.caster);

		int id = fight.getNextLowerFighterGuid();
		mobGrade.setInFightID(id);

		Fighter fighter = new Fighter(fight, mobGrade);
		fighter.setTeam(this.caster.getTeam());
		fighter.setInvocator(this.caster);

		fight.getMap().getCase(this.cell.getId()).addFighter(fighter);
		fighter.setCell(fight.getMap().getCase(this.cell.getId()));
		fight.addFighterInTeam(fighter, this.caster.getTeam());
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 181, this.caster.getId() + "", fighter.getGmPacket('+', true).substring(3), this.effectID);
	}



	private void applyEffect_202(Fight fight, Fighter target) {
		// unhide des personnages
		if (target.isHide()) {
			if (target != caster)
				target.unHide(spell);
		}
		for (Trap p : fight.getAllTrapsinAera(cell.getId(), this.area)) {
				p.setIsUnHide(caster);
				p.appear(caster);
		}
	}

	private void applyEffect_266(Fight fight, Fighter target) {
		int val = Formulas.getRandomJet(jet, caster);
		int vol = 0;

		target.addBuff(Constant.STATS_REM_CHAN, val, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_REM_CHAN, caster.getId()
					+ "", target.getId() + "," + val + "," + turns, this.effectID);
		vol += val;

		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constant.STATS_ADD_CHAN, vol, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_ADD_CHAN, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns, this.effectID);
	}

	private void applyEffect_267(Fight fight, Fighter target) {
		int val = Formulas.getRandomJet(jet, caster);
		int vol = 0;

		target.addBuff(Constant.STATS_REM_VITA, val, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_REM_VITA, caster.getId()
					+ "", target.getId() + "," + val + "," + turns, this.effectID);
		vol += val;

		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constant.STATS_ADD_VITA, vol, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_ADD_VITA, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns, this.effectID);
	}

	private void applyEffect_268(Fight fight, Fighter target) {
		int val = Formulas.getRandomJet(jet, caster);
		int vol = 0;

		target.addBuff(Constant.STATS_REM_AGIL, val, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_REM_AGIL, caster.getId()
					+ "", target.getId() + "," + val + "," + turns, this.effectID);
		vol += val;

		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constant.STATS_ADD_AGIL, vol, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_ADD_AGIL, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns, this.effectID);
	}

	private void applyEffect_269(Fight fight, Fighter target) {
		int val = Formulas.getRandomJet(jet, caster);
		int vol = 0;

			target.addBuff(Constant.STATS_REM_INTE, val, turns, 1, true, spell, args, caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_REM_INTE, caster.getId()
					+ "", target.getId() + "," + val + "," + turns, this.effectID);
			vol += val;

		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constant.STATS_ADD_INTE, vol, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_ADD_INTE, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns, this.effectID);
	}

	private void applyEffect_270(Fight fight, Fighter target) {
		int val = Formulas.getRandomJet(jet, caster);
		int vol = 0;

			target.addBuff(Constant.STATS_REM_SAGE, val, turns, 1, true, spell, args, caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_REM_SAGE, caster.getId()
					+ "", target.getId() + "," + val + "," + turns, this.effectID);
			vol += val;

		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constant.STATS_ADD_SAGE, vol, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_ADD_SAGE, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns, this.effectID);
	}

	private void applyEffect_271(Fight fight, Fighter target) {
		int val = Formulas.getRandomJet(jet, caster);
		int vol = 0;

			target.addBuff(Constant.STATS_REM_FORC, val, turns, 1, true, spell, args, caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_REM_FORC, caster.getId()
					+ "", target.getId() + "," + val + "," + turns, this.effectID);
			vol += val;

		if (vol == 0)
			return;
		//on ajoute le buff
		caster.addBuff(Constant.STATS_ADD_FORC, vol, turns, 1, true, spell, args, caster, true);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_ADD_FORC, caster.getId()
				+ "", caster.getId() + "," + vol + "," + turns, this.effectID);
	}

	private void applyEffect_SpellBuff(Fighter target) {
		target.addBuff(effectID, fixvalue, turns, 1, true, spell, args, caster, true);
	}

	private void applyEffect_320(Fight fight, Fighter target) {
		int value = 1;
		try {
			value = Integer.parseInt(args.split(";")[0]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		int num = 0;

			target.addBuff(Constant.STATS_REM_PO, value, turns, 0, true, spell, args, caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_REM_PO, caster.getId()
					+ "", target.getId() + "," + value + "," + turns, this.effectID);
			num += value;

		if (num != 0) {
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, Constant.STATS_ADD_PO, caster.getId()
					+ "", caster.getId() + "," + num + "," + turns, this.effectID);
			caster.addBuff(Constant.STATS_ADD_PO, num, 1, 0, true, spell, args, caster, true);
			//Gain de PO pendant le tour de jeu
			if (caster.canPlay())
				caster.getTotalStats().addOneStat(Constant.STATS_ADD_PO, num);
		}
	}

	private void applyEffect_400(Fight fight) {
		if (!cell.isWalkable(true))
			return;//Si case pas marchable
		if (cell.getFirstFighter() != null && !cell.getFirstFighter().isHide())
			return;//Si la case est prise par un joueur

		//Si la case est prise par le centre d'un piege
		for (Trap p : fight.getAllTraps())
			if (p.getCell().getId() == cell.getId())
				return;

		String[] infos = args.split(";");
		int spellID = Short.parseShort(infos[0]);
		int level = Byte.parseByte(infos[1]);
		String po = this.getAreaEffect();
		byte size = (byte) World.world.getCryptManager().getIntByHashedValue(po.charAt(1));
		SpellGrade TS = World.world.getSort(spellID).getStatsByLevel(level);
		final Trap g = new Trap(fight, caster, cell, size, TS, spell, (byte) level);
		fight.getAllTraps().add(g);
		int unk = g.getColor();
		int team = caster.getTeam() + 1;
		String str = "GDZ+" + cell.getId() + ";" + size + ";" + unk;
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, team, 999, caster.getId() + "", str, this.effectID);
		str = "GDC" + cell.getId() + ";Haaaaaaaaz3005;";
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, team, 999, caster.getId() + "", str, this.effectID);
	}

	private void applyEffect_401(Fight fight) {
		if (!cell.isWalkable(false))
			return;//Si case pas marchable
		if (cell.getFirstFighter() != null && caster != cell.getFirstFighter())
			return;//Si la case est prise par un joueur

		int spellID = this.minvalue;
		int level = this.maxvalue;
		int color = Integer.parseInt(this.info);
		byte duration = (byte) this.turns;
		SpellGrade TS = World.world.getSort(spellID).getStatsByLevel(level);

		Glyph g = new Glyph(fight, caster, cell, this.area, TS, duration, spell,color);
		fight.getAllGlyphs().add(g);
		g.appear();

	}

	private void applyEffect_402(Fight fight) {
		if (!cell.isWalkable(true))
			return;//Si case pas marchable

		int spellID = this.minvalue;
		int level = this.maxvalue;
		int color = Integer.parseInt(this.info);
		byte duration = (byte) this.turns;
		SpellGrade TS = World.world.getSort(spellID).getStatsByLevel(level);
		Glyph g = new Glyph(fight, caster, cell, this.area, TS, duration, spell,color);
		fight.getAllGlyphs().add(g);
		g.appear();
		/*String str = "GDZ+" + cell.getId() + ";" + size + ";" + unk;
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId()
				+ "", str, this.effectID);
		str = "GDC" + cell.getId() + ";Haaaaaaaaa3005;";
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId()
				+ "", str, this.effectID);*/
	}

	private void applyEffect_405(Fighter target,Fight fight) {
		// D'abord il tue l'invoque si il y en a une dans la zone
		fight.onFighterDie(target, target);

		applyEffect_181(fight);

	}

	private void applyEffect_671(Fighter target, Fight fight) {
				int resP = target.getTotalStats().getEffect(Constant.STATS_ADD_RP_NEU), resF = target.getTotalStats().getEffect(Constant.STATS_ADD_R_NEU);
				if (target.getPlayer() != null) {
					resP += target.getTotalStats().getEffect(Constant.STATS_ADD_RP_PVP_NEU);
					resF += target.getTotalStats().getEffect(Constant.STATS_ADD_R_PVP_NEU);
				}

				int dmg = Formulas.getRandomJet(jet, caster, target);// % de pdv
				dmg = getMaxMinSpell(target, dmg);
				int val = caster.getPdv() / 100 * dmg;// Valor de da�os
				val -= resF;
				int reduc = (int) (((float) val) / (float) 100) * resP;// Reduc
				// %resis
				val -= reduc;
				if (val < 0)
					val = 0;
				val = applyOnHitBuffs(val, target, caster, fight, Constant.ELEMENT_NULL);
				if (val > target.getPdv())
					val = target.getPdv();
				target.removePdv(caster, val);
				int cura = val;
				if (target.hasBuff(786) && target.getBuff(786) != null) {
					if ((cura + caster.getPdv()) > caster.getPdvMax())
						cura = caster.getPdvMax() - caster.getPdv();
					caster.removePdv(caster, -cura);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, target.getId()
							+ "", caster.getId() + ",+" + cura, this.effectID);
				}
				/*val = -(val);
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
						+ "", target.getId() + "," + val, this.effectID);*/

				this.dammageDone = val;
	}

	private void applyEffect_672(Fighter target, Fight fight) {
		//Punition
		//Formule de barge ? :/ Clair que ca punie ceux qui veulent l'utiliser x_x
		double val = ((double) Formulas.getRandomJet(jet, caster) / (double) 100);
		int pdvMax = caster.getPdvMaxOutFight();
		double pVie = (double) caster.getPdv() / (double) caster.getPdvMax();
		double rad = (double) 2 * Math.PI * (double) (pVie - 0.5);
		double cos = Math.cos(rad);
		double taux = (Math.pow((cos + 1), 2)) / (double) 4;
		double dgtMax = val * pdvMax;
		int dgt = (int) (taux * dgtMax);

			int finalDommage = applyOnHitBuffs(dgt, target, caster, fight, Constant.ELEMENT_NEUTRE);//S'il y a des buffs sp�ciaux
			int resi = target.getTotalStats().getEffect(Constant.STATS_ADD_RP_NEU);
			int retir = 0;
			if (resi > 2) {
				retir = (finalDommage * resi) / 100;
				finalDommage = finalDommage - retir;
			}
			if (resi < -2) {
				retir = ((-finalDommage) * (-resi)) / 100;
				finalDommage = finalDommage + retir;
			}
			if (finalDommage > target.getPdv())
				finalDommage = target.getPdv();//Target va mourrir
			target.removePdv(caster, finalDommage);
			/*finalDommage = -(finalDommage);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 100, caster.getId()
					+ "", target.getId() + "," + finalDommage, this.effectID);

			if (target.getPdv() <= 0) {
				fight.onFighterDie(target, target);
				if (target.canPlay() && target.getPlayer() != null)
					fight.endTurn(false);
				else if (target.canPlay())
					target.setCanPlay(false);
			}*/
			this.dammageDone = finalDommage;
	}

	private void applyEffect_765(Fighter target) {
		target.addBuff(effectID, 0, turns, 1, true, spell, args, caster, true);
	}

	private void applyEffect_765B(Fight fight, Fighter target) {
		Fighter sacrified = target.getBuff(765).getCaster();
		GameCase cell1 = sacrified.getCell();
		GameCase cell2 = target.getCell();

		sacrified.getCell().getFighters().clear();
		target.getCell().getFighters().clear();
		sacrified.setCell(cell2);
		sacrified.getCell().addFighter(sacrified);
		target.setCell(cell1);
		target.getCell().addFighter(target);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, target.getId() + "", target.getId() + "," + cell1.getId(), this.effectID);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, sacrified.getId() + "", sacrified.getId() + "," + cell2.getId(), this.effectID);
	}

	private void applyEffect_776(ArrayList<Fighter> objetivos, Fight pelea) {
		int val = Formulas.getRandomJet(jet, caster);
		if (val == -1) {
			return;
		}
		for (Fighter objetivo : objetivos) {
			objetivo.addBuff(effectID, val, turns, 1, true, spell, args, caster, true);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(pelea, 7, effectID, caster.getId()
					+ "", objetivo.getId() + "," + val + "," + turns, this.effectID);
		}
	}

	private void applyEffect_780(Fight fight) {
		Fighter target = null;

		for (Fighter fighter : fight.getDeadList().values())
			if (!fighter.hasLeft() && fighter.getTeam() == caster.getTeam())
				target = fighter;
		if (target == null) return;

		fight.addFighterInTeam(target, target.getTeam());
		target.setIsDead(false);
		target.getFightBuff().clear();
		if (target.isInvocation())
			fight.getOrderPlaying().add((fight.getOrderPlaying().indexOf(target.getInvocator()) + 1), target);

		target.setCell(cell);
		target.getCell().addFighter(target);

		target.fullPdv();
		int percent = (100 - minvalue) * target.getPdvMax() / 100;
		target.removePdv(caster, percent);

		String gm = target.getGmPacket('+', true).substring(3);
		String gtl = fight.getGTL();
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 181, target.getId() + "", gm, this.effectID);
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, target.getId() + "", gtl, this.effectID);
		if (!target.isInvocation())
			SocketManager.GAME_SEND_STATS_PACKET(target.getPlayer());

		fight.removeDead(target);
	}

	private void applyEffect_781(Fighter target, Fight fight) {
		target.addBuff(effectID, 1, turns, turns, debuffable, spell, args, caster, true);
	}

	private void applyEffect_782(ArrayList<Fighter> cibles, Fight fight) {
		for (Fighter target : cibles) {
			target.addBuff(effectID, 1, turns, turns, debuffable, spell, args, caster, true);
		}
	}

	private void applyEffect_783(Fight fight) { 	// POUSSE NO DO POU


		GameCase ccase = caster.getCell();
		//On calcule l'orientation entre les 2 cases
		char dir = PathFinding.getDirBetweenTwoCase(ccase.getId(), cell.getId(), fight.getMap(), true);
		//On calcule l'id de la case a coté du lanceur dans la direction obtenue
		int tcellID = PathFinding.GetCaseIDFromDirrection(ccase.getId(), dir, fight.getMap(), true);
		//on prend la case corespondante
		GameCase tcase = fight.getMap().getCase(tcellID);

		if (tcase == null)
			return;
		//S'il n'y a personne sur la case, on arrete
		if (tcase.getFighters().isEmpty())
			return;
		//On prend le Fighter ciblé
		Fighter target = tcase.getFirstFighter();
		//On verifie qu'il peut aller sur la case ciblé en ligne droite
		int c1 = tcellID, limite = 0;
		if (target.getMob() != null)
			for (int i : Constant.STATIC_INVOCATIONS)
				if (i == target.getMob().getTemplate().getId())
					return;

		while (true) {
			if (PathFinding.GetCaseIDFromDirrection(c1, dir, fight.getMap(), true) == cell.getId())
				break;
			if (PathFinding.GetCaseIDFromDirrection(c1, dir, fight.getMap(), true) == -1)
				return;
			c1 = PathFinding.GetCaseIDFromDirrection(c1, dir, fight.getMap(), true);
			limite++;
			if (limite > 50)
				return;
		}
		GameCase newCell = PathFinding.checkIfCanPushEntity(fight, ccase.getId(), cell.getId(), dir);

		if (newCell != null)
			cell = newCell;

		target.getCell().getFighters().clear();
		target.setCell(cell);
		target.getCell().addFighter(target);

		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 5, caster.getId() + "", target.getId() + "," + cell.getId(), this.effectID);
		Trap.doTraps(fight, target);
	}

	private void applyEffect_784(Fight fight) { // Rollback
		Map<Integer, GameCase> origPos = fight.getRholBack(); // les positions de début de combat

		ArrayList<Fighter> list = fight.getFighters(3); // on copie la liste des fighters
		for (int i = 1; i < list.size(); i++)   // on boucle si tout le monde est à la place
			if (!list.isEmpty())                 // d'un autre
				for (Fighter F : list) {
					if (F == null || F.isDead() || !origPos.containsKey(F.getId())) {
						continue;
					}
					if (F.getCell().getId() == origPos.get(F.getId()).getId()) {
						continue;
					}
					if (origPos.get(F.getId()).getFirstFighter() == null) {
						F.getCell().getFighters().clear();
						F.setCell(origPos.get(F.getId()));
						F.getCell().addFighter(F);
						SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, F.getId() + "", F.getId() + "," + F.getCell().getId(), this.effectID);
					}
				}
	}

	// TODO a reprendre si c'est utile
	private void applyEffect_786(Fighter target, Fight pelea) {
		target.addBuff(effectID, Integer.parseInt(info), turns, 1, true, spell, args, caster, true);
	}

	// TODO a reprendre si c'est utile
	private void applyEffect_787(Fighter target, Fight pelea) { // applique un sort sur la cible
		int hechizoID = -1;
		int hechizoNivel = -1;
		try {
			hechizoID = Integer.parseInt(args.split(";")[0]);
			hechizoNivel = Integer.parseInt(args.split(";")[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Spell hechizo = World.world.getSort(hechizoID);
		ArrayList<SpellEffect> EH = hechizo.getStatsByLevel(hechizoNivel).getEffects();
		for (SpellEffect eh : EH) {
				target.addBuff(eh.effectID, minvalue, 1, 1, true, eh.spell, eh.args, caster, true);
		}
	}

	// TODO a reprendre les chatiment avec le nouveau Système OnHit
	private void applyEffect_788(Fighter target) {
		target.addBuff(effectID, minvalue, turns, 1, false, spell, args, target, true);
	}

	private void applyEffect_950(Fight fight, Fighter target) {
			int id = -1;
			try {
				id = Integer.parseInt(info);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (id == -1)
				return;


				if (turns <= 0) {
					target.setState(id, turns);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
							+ "", target.getId() + "," + id + ",1", this.effectID);
				} else {
					target.setState(id, turns);
					SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId()
							+ "", target.getId() + "," + id + ",1", this.effectID);
					target.addBuff(effectID, id, turns, 1, false, spell, args, target, true);
				}


	}

	private void applyEffect_951(Fight fight, Fighter target) {

			int id = -1;
			try {
				id = Integer.parseInt(info);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (id == -1)
				return;

				//Si la cible n'a pas l'�tat
				if (!target.haveState(id))
					return;
				//on enleve l'�tat
				target.setState(id, 0);

				// Gestion des cas ou l'état est un buff et qu'il est débuffable par un autre sort(Notamment Picole et Lait de Bambou)
				target.debuffState(id);

				//on envoie le packet
				SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 950, caster.getId() + "", target.getId() + "," + id + ",0", this.effectID);
	}

	/*private void applyEffect_1000(Fight fight, ArrayList<Fighter> cibles) {
		String[] infos = args.split(";");
		int spellID = Short.parseShort(infos[0]);
		int level = Byte.parseByte(infos[1]);
		byte duration = 1;
		SpellGrade TS = World.world.getSort(spellID).getStatsByLevel(level);
		GameCase celll = null;
		int casenbr = 0;
		boolean quatorze = false;
		for (GameCase entry : fight.getMap().getCases()) {
			casenbr = casenbr + 1;

			if (casenbr == 14 && quatorze) {
				quatorze = false;
				casenbr = 0;
			}
			if (casenbr == 15) {
				quatorze = true;
				casenbr = 0;
			}
			if (quatorze)
				continue;
			celll = entry;
			if (celll == null)
				continue;
			switch (celll.getId()) {
				case 28:
				case 57:
				case 86:
				case 115:
				case 144:
				case 173:
				case 202:
				case 231:
				case 260:
				case 289:
				case 318:
				case 347:
				case 376:
				case 405:
				case 434:
				case 463:
					continue;
			}
			if (!celll.isWalkable(false))
				continue;
			Glyph g = new Glyph(fight, caster, celll, (byte) 0, TS, duration, spell);
			fight.getAllGlyphs().add(g);

			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", "GDZ+" + celll.getId() + ";" + 0 + ";" + g.getColor(), this.effectID);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", "GDC" + celll.getId() + ";Haaaaaaaaa3005;", this.effectID);
		}
	}

	private void applyEffect_1001(Fight fight, ArrayList<Fighter> cibles) {
		String[] infos = args.split(";");
		int spellID = Short.parseShort(infos[0]);
		int level = Byte.parseByte(infos[1]);
		byte duration = 1;
		SpellGrade TS = World.world.getSort(spellID).getStatsByLevel(level);
		GameCase celll = null;
		int casenbr = 0;
		boolean quatorze = false;
		for (GameCase entry : fight.getMap().getCases()) {
			casenbr = casenbr + 1;

			if (casenbr == 14 && quatorze == true) {
				quatorze = false;
				casenbr = 0;
			}
			if (casenbr == 15) {
				quatorze = true;
				casenbr = 0;
			}
			if (quatorze == false)
				continue;
			celll = entry;
			if (celll == null)
				continue;
			if (!celll.isWalkable(false))
				continue;
			Glyph g = new Glyph(fight, caster, celll, (byte) 0, TS, duration, spell);
			fight.getAllGlyphs().add(g);
			int unk = g.getColor();
			String str = "GDZ+" + celll.getId() + ";" + 0 + ";" + unk;
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str, this.effectID);
			str = "GDC" + celll.getId() + ";Haaaaaaaaa3005;";
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str, this.effectID);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str, this.effectID);
			str = "GDC" + celll.getId() + ";Haaaaaaaaa3005;";
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str, this.effectID);
		}
	}

	private void applyEffect_1002(Fight fight, ArrayList<Fighter> cibles) {
		String[] infos = args.split(";");
		int spellID = Short.parseShort(infos[0]);
		int level = Byte.parseByte(infos[1]);
		byte duration = 100;
		SpellGrade TS = World.world.getSort(spellID).getStatsByLevel(level);

		if (cell.isWalkable(true) && !fight.isOccuped(cell.getId())) {
			caster.getCell().getFighters().clear();
			caster.setCell(cell);
			caster.getCell().addFighter(caster);
			Trap.doTraps(caster.getFight(), caster);
			SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 4, caster.getId() + "", caster.getId() + "," + cell.getId(), this.effectID);
		}

		Glyph g = new Glyph(fight, caster, cell, (byte) 0, TS, duration, spell);
		fight.getAllGlyphs().add(g);
		int unk = g.getColor();
		String str = "GDZ+" + cell.getId() + ";" + 0 + ";" + unk;
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str, this.effectID);
		str = "GDC" + cell.getId() + ";Haaaaaaaaa3005;";
		SocketManager.GAME_SEND_GA_PACKET_TO_FIGHT(fight, 7, 999, caster.getId() + "", str, this.effectID);
	}*/

	private ArrayList<Fighter> trierCibles(ArrayList<Fighter> cibles, Fight fight) {
		ArrayList<Fighter> array = new ArrayList<>();
		int max = -1;
		int distance;

		for (Fighter f : cibles) {
			distance = PathFinding.getDistanceBetween(fight.getMap(), this.cell.getId(), f.getCell().getId());
			if (distance > max)
				max = distance;
		}

		for (int i = max; i >= 0; i--) {
			Iterator<Fighter> it = cibles.iterator();
			while (it.hasNext()) {
				Fighter f = it.next();
				distance = PathFinding.getDistanceBetween(fight.getMap(), this.cell.getId(), f.getCell().getId());
				if (distance == i) {
					array.add(f);
					it.remove();
				}
			}
		}

		return array;
	}

	/**
	 * Cette fonction doit etre utilise <b>uniquement lorsque</b> il faut attendre la fin d'une animation niveau client.<br>
	 * Exemple une <b>teleportation , attirance , invocation ou bien le fait d'etre jete par un panda.</b><br>
	 * Lorsque quelqu'un est pousse il n'est pas necessaire d'appeler cette fonction car le client prend en charge la possibilite de pousser a plusieurs repprises.
	 *
	 * @param fight L'instance de la class Fight
	 * @param fighter Le joueur qu'il faut appliquer le reseau de pieges ou bien juste les pieges
	 * @param time Temps que mets l'animation de tp/jeter/attirer niveau client
	 *
	 * @author Sarazar928Ghost
	 */
	private void checkTraps(Fight fight, Fighter fighter, short time) {
		// Il est sur un piege qui pousse ?
		final boolean isPushing = Trap.checkPushingTraps(fight, fighter);

		// Si il n'est pas dans un reseau
		if(!isPushing) {
			Trap.doTraps(fight, fighter);
			return;
		}

		// Il est dans un reseau
		fight.setTraped(true);

		TimerWaiter.addNext(() -> {
			Trap.doTraps(fight, fighter);
			fight.removeTraped();
		}, time, TimeUnit.MILLISECONDS);
	}


}