package fight.spells;

import kernel.Constant;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

public class EffectConstant {


    public static final int EFFECTID_DEROBADE = 9;
    public static final int EFFECTID_REVERSECHANCE = 79;

    public static final int EFFECTID_REDUCEDAMAGE = 105;
    public static final int EFFECTID_RETURNSPELL = 106;
    public static final int EFFECTID_RETURNDAMAGE = 107;
    public static final int EFFECTID_AP_POISON = 131;
    public static final int EFFECTID_PASSTURN = 140;
    public static final int EFFECTID_INVISIBLE = 150;
    public static final int EFFECTID_REDUCEDAMAGE2 = 265;
    public static final int EFFECTID_SACRIFICE = 765;
    public static final int EFFECTID_EROSION =776;
    public static final int EFFECTID_MINIMUMDAMAGE = 781;
    public static final int EFFECTID_MAXIMUMDAMAGE = 782;


    public static final int[] IS_DIRECTDAMMAGE_EFFECT = {82,85,86,87,88,89,91,92,93,94,95,96,97,98,99,100,109,275,276,277,278,279,670,671,672};
    public static final int[] NONEEDTARGET_EFFECT = {4,180,181,185,200,400,401,402,780,50,51,120,109,783};
    public static final int[] NEEDALLTARGET_EFFECT = {90};

    //Elements
    public static final int ELEMENT_NULL = -1;
    public static final int ELEMENT_NEUTRE = 0;
    public static final int ELEMENT_TERRE = 1;
    public static final int ELEMENT_EAU = 2;
    public static final int ELEMENT_FEU = 3;
    public static final int ELEMENT_AIR = 4;


    public static final int[] TRIGGER_ONATTAK_EAU = {85,91,96,275};
    public static final int[] TRIGGER_ONATTAK_TERRE = {86,92,97,276};
    public static final int[] TRIGGER_ONATTAK_AIR = {87,93,98,277};
    public static final int[] TRIGGER_ONATTAK_FEU = {88,94,99,278};
    public static final int[] TRIGGER_ONATTAK_NEUTRE = {89,95,100,670,671,672,279};
    public static final int[] TRIGGER_ONATTAK_NOELEM = {82,109};
    public static final int[] TRIGGER_ONATTAK_TEMP = ArrayUtils.addAll(TRIGGER_ONATTAK_EAU,TRIGGER_ONATTAK_FEU);
    public static final int[] TRIGGER_ONATTAK_TEMP2 = ArrayUtils.addAll(TRIGGER_ONATTAK_TEMP,TRIGGER_ONATTAK_AIR);
    public static final int[] TRIGGER_ONATTAK_TEMP1 = ArrayUtils.addAll(TRIGGER_ONATTAK_TEMP2,TRIGGER_ONATTAK_TERRE);
    public static final int[] TRIGGER_ONATTAK_TEMP0 = ArrayUtils.addAll(TRIGGER_ONATTAK_TEMP1,TRIGGER_ONATTAK_NEUTRE);
    public static final int[] TRIGGER_ONATTAK_DAMMAGE = ArrayUtils.addAll(TRIGGER_ONATTAK_TEMP0,TRIGGER_ONATTAK_NOELEM);
    public static final int[] TRIGGER_ONATTAK_RETPA = {84,101,168};
    public static final int[] TRIGGER_ONATTAK_RETPM = {127,169,77};
    public static final int[] TRIGGER_ONATTAK_RET = ArrayUtils.addAll(TRIGGER_ONATTAK_RETPM,TRIGGER_ONATTAK_RETPA);
    public static final int[] TRIGGER_ONHEAL = {81,108,143,646};

    public static Map<Integer, int[]> ALL_TRIGGERS = getAllTriggers();

    public static Map<Integer, int[]> getAllTriggers() {
        Map<Integer, int[]> test = new HashMap<>();
        test.put(0, TRIGGER_ONATTAK_NEUTRE);
        test.put(1, TRIGGER_ONATTAK_EAU);
        test.put(2, TRIGGER_ONATTAK_TERRE);
        test.put(3, TRIGGER_ONATTAK_AIR);
        test.put(4, TRIGGER_ONATTAK_FEU);
        test.put(5, TRIGGER_ONATTAK_DAMMAGE);
        test.put(6, TRIGGER_ONATTAK_RET);
        test.put(7, TRIGGER_ONATTAK_RETPA);
        test.put(8, TRIGGER_ONATTAK_RETPM);
        test.put(9, TRIGGER_ONHEAL);
        return test;
    }

    //Effects
    public static final int STATS_ADD_PM2 = 78;
    public static final int STATS_REM_PA = 101;
    public static final int STATS_ADD_VIE = 110;
    public static final int STATS_ADD_PA = 111;
    public static final int STATS_ADD_DOMA = 112;
    public static final int STATS_MULTIPLY_DOMMAGE = 114;
    public static final int STATS_ADD_CC = 115;
    public static final int STATS_REM_PO = 116;
    public static final int STATS_ADD_PO = 117;
    public static final int STATS_ADD_FORC = 118;
    public static final int STATS_ADD_AGIL = 119;
    public static final int STATS_ADD_PA2 = 120;
    public static final int STATS_ADD_DOMA2 = 121;
    public static final int STATS_ADD_EC = 122;
    public static final int STATS_ADD_CHAN = 123;
    public static final int STATS_ADD_SAGE = 124;
    public static final int STATS_ADD_VITA = 125;
    public static final int STATS_ADD_INTE = 126;
    public static final int STATS_REM_PM = 127;
    public static final int STATS_ADD_PM = 128;
    public static final int STATS_ADD_PERDOM = 138;
    public static final int STATS_ADD_ENERGIE = 139;
    public static final int STATS_ADD_PDOM = 142;
    public static final int STATS_REM_DOMA = 145;
    public static final int STATS_REM_CHAN = 152;
    public static final int STATS_REM_VITA = 153;
    public static final int STATS_REM_AGIL = 154;
    public static final int STATS_REM_INTE = 155;
    public static final int STATS_REM_SAGE = 156;
    public static final int STATS_REM_FORC = 157;
    public static final int STATS_ADD_PODS = 158;
    public static final int STATS_REM_PODS = 159;
    public static final int STATS_ADD_AFLEE = 160;
    public static final int STATS_ADD_MFLEE = 161;
    public static final int STATS_REM_AFLEE = 162;
    public static final int STATS_REM_MFLEE = 163;
    public static final int STATS_ADD_MAITRISE = 165;
    public static final int STATS_REM_PA2 = 168;
    public static final int STATS_REM_PM2 = 169;
    public static final int STATS_REM_CC = 171;
    public static final int STATS_ADD_INIT = 174;
    public static final int STATS_REM_INIT = 175;
    public static final int STATS_ADD_PROS = 176;
    public static final int STATS_REM_PROS = 177;
    public static final int STATS_ADD_SOIN = 178;
    public static final int STATS_REM_SOIN = 179;
    public static final int STATS_CREATURE = 182;
    public static final int STATS_ADD_PRED = 184;
    public static final int STATS_ADD_MRED = 184;
    public static final int STATS_ADD_RP_TER = 210;
    public static final int STATS_ADD_RP_EAU = 211;
    public static final int STATS_ADD_RP_AIR = 212;
    public static final int STATS_ADD_RP_FEU = 213;
    public static final int STATS_ADD_RP_NEU = 214;
    public static final int STATS_REM_RP_TER = 215;
    public static final int STATS_REM_RP_EAU = 216;
    public static final int STATS_REM_RP_AIR = 217;
    public static final int STATS_REM_RP_FEU = 218;
    public static final int STATS_REM_RP_NEU = 219;
    public static final int STATS_RETDOM = 220;
    public static final int STATS_TRAPDOM = 225;
    public static final int STATS_TRAPPER = 226;
    public static final int STATS_ADD_R_FEU = 240;
    public static final int STATS_ADD_R_NEU = 241;
    public static final int STATS_ADD_R_TER = 242;
    public static final int STATS_ADD_R_EAU = 243;
    public static final int STATS_ADD_R_AIR = 244;
    public static final int STATS_REM_R_FEU = 245;
    public static final int STATS_REM_R_NEU = 246;
    public static final int STATS_REM_R_TER = 247;
    public static final int STATS_REM_R_EAU = 248;
    public static final int STATS_REM_R_AIR = 249;
    public static final int STATS_ADD_RP_PVP_TER = 250;
    public static final int STATS_ADD_RP_PVP_EAU = 251;
    public static final int STATS_ADD_RP_PVP_AIR = 252;
    public static final int STATS_ADD_RP_PVP_FEU = 253;
    public static final int STATS_ADD_RP_PVP_NEU = 254;
    public static final int STATS_REM_RP_PVP_TER = 255;
    public static final int STATS_REM_RP_PVP_EAU = 256;
    public static final int STATS_REM_RP_PVP_AIR = 257;
    public static final int STATS_REM_RP_PVP_FEU = 258;
    public static final int STATS_REM_RP_PVP_NEU = 259;
    public static final int STATS_ADD_R_PVP_TER = 260;
    public static final int STATS_ADD_R_PVP_EAU = 261;
    public static final int STATS_ADD_R_PVP_AIR = 262;
    public static final int STATS_ADD_R_PVP_FEU = 263;
    public static final int STATS_ADD_R_PVP_NEU = 264;

    public static final int STATS_ADD_XP=605;
    public static final int STATS_ADD_XPJOB=614;

    public static final int STATS_ADD_ERO=1009;
    public static final int STATS_REM_ERO=1010;
    public static final int STATS_ADD_R_ERO=1011;
    public static final int STATS_REM_R_ERO=1012;
    public static final int STATS_REM_PA3 = 2100;
    public static final int STATS_REM_RENVOI = 2111;
    public static final int STATS_REM_INVO = 2112;
    public static final int STATS_REM_TRAPDOM = 2113;
    public static final int STATS_REM_TRAPPER = 2114;
    public static final int STATS_ADD_FINALDMG = 2008;
    public static final int STATS_REM_FINALDMG = 2009;

    //Liste des stats négatives
    public static final int[] STATS_NEGATIVE = {STATS_REM_DOMA, STATS_REM_CHAN,STATS_REM_VITA,
            STATS_REM_AGIL, STATS_REM_INTE, STATS_REM_SAGE, STATS_REM_FORC, STATS_REM_PODS, STATS_REM_AFLEE,
            STATS_REM_CC, STATS_REM_SOIN, STATS_REM_INIT,STATS_REM_PA,STATS_REM_PA2,STATS_REM_PM,STATS_REM_PM2,
            STATS_REM_MFLEE, STATS_REM_PO, STATS_REM_PROS,
            STATS_REM_R_AIR, STATS_REM_R_EAU, STATS_REM_R_FEU, STATS_REM_R_NEU, STATS_REM_R_TER,
            STATS_REM_RP_AIR, STATS_REM_RP_EAU,STATS_REM_RP_FEU,STATS_REM_RP_NEU,STATS_REM_RP_TER,
            STATS_REM_RP_PVP_AIR,STATS_REM_RP_PVP_EAU,STATS_REM_RP_PVP_FEU,STATS_REM_RP_PVP_NEU,STATS_REM_RP_PVP_TER
    };

    public static final int[] EFFECTS_HEAL = {};
    public static final int[] EFFECTS_DAMMAGE = {};
    public static final int[] EFFECTS_RET_AP = {};
    public static final int[] EFFECTS_RET_MP = {};

    // Buff de Sort Classe
    public static final int STATS_SPELL_ADD_PO = 281;
    public static final int STATS_SPELL_PO_MODIF = 282;
    public static final int STATS_SPELL_ADD_DOM = 283;
    public static final int STATS_SPELL_ADD_HEAL = 284;
    public static final int STATS_SPELL_REM_PA = 285;
    public static final int STATS_SPELL_REM_DELAY = 286;
    public static final int STATS_SPELL_ADD_CRIT = 287;
    public static final int STATS_SPELL_LINE_LAUNCH = 288;
    public static final int STATS_SPELL_LOS = 289;
    public static final int STATS_SPELL_ADD_LAUNCH = 290;
    public static final int STATS_SPELL_ADD_PER_TARGET = 291;
    public static final int STATS_SPELL_FIXE_DURATION_DELAY = 292;
    public static final int STATS_SPELL_ADD_BASE_DAMAGE = 293;
    public static final int STATS_SPELL_REM_PO = 294;
    public static final int STATS_PO_MODIFIABLE_SPELL = 282; //11a#idSpell en Hexa#0#0#0d0+idSpell
    public static final int STATS_ADD_DO_SPELL = 283; //11b#0#0#+Do en Hexa#0d0+idSpell
    public static final int STATS_REDUCE_SPELL_DELAY = 286; //11e#0#0#+Nb Delais Reduit en Hexa#0d0+idSpell
    public static final int STATS_ADD_CC_SPELL = 287;//11f#0#0#+CC en Hexa#0d0+idSpell
    public static final int STATS_LAUNCHABLE_IN_LINE = 288; //120#idSpellEnHexa#0#0#0d0+idSpell


    //ETAT
    public static final int ETAT_NEUTRE = 0;
    public static final int ETAT_SAOUL = 1;
    public static final int ETAT_CAPT_AME = 2;
    public static final int ETAT_PORTEUR = 3;
    public static final int ETAT_PEUREUX = 4;
    public static final int ETAT_DESORIENTE = 5;
    public static final int ETAT_ENRACINE = 6;
    public static final int ETAT_PESANTEUR = 7;
    public static final int ETAT_PORTE = 8;
    public static final int ETAT_MOTIV_SYLVESTRE = 9;
    public static final int ETAT_APPRIVOISEMENT = 10;
    public static final int ETAT_CHEVAUCHANT = 11;
    public static final int ETAT_ENCRE_PRIMAIRE = 31;
    public static final int ETAT_ENCRE_SECONDAIRE = 32;
    public static final int ETAT_ENCRE_TERTIDIAIRE = 33;
    public static final int ETAT_ENCRE_QUATENAIRE = 34;
    public static final int ETAT_LOURD = 63;

    //Elements
    public static final int EFFECT_TYPE_SPELL = 0;
    public static final int EFFECT_TYPE_CAC = 1;
    public static final int EFFECT_TYPE_CONSOMMABLE=2;

    public static int getElemSwitchEffect(int EffectID){
        int elem = Constant.ELEMENT_NULL;
        switch (EffectID){
            case 91:
            case 96:
                elem = Constant.ELEMENT_EAU;
                break;
            case 92:
            case 97:
                elem = Constant.ELEMENT_TERRE;
                break;
            case 93:
            case 98:
                elem = Constant.ELEMENT_AIR;
                break;
            case 94:
            case 99:
                elem = Constant.ELEMENT_FEU;
                break;
            case 95:
            case 100:
                elem = Constant.ELEMENT_NEUTRE;
                break;
            default:
                break;
        }
        return elem;
    }

}
