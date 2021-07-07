package common

import area.map.GameCase
import area.map.GameMap
import kernel.Constant
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import kotlin.experimental.and

class Encryptador {
    private val HASH = charArrayOf(
        'a',
        'b',
        'c',
        'd',
        'e',
        'f',
        'g',
        'h',
        'i',
        'i',
        'j',
        'k',
        'l',
        'm',
        'n',
        'o',
        'p',  // 15
        'q',
        'r',
        's',
        't',
        'u',
        'v',
        'w',
        'x',
        'y',
        'z',
        'A',
        'B',
        'C',
        'D',
        'E',
        'F',
        'G',
        'H',
        'I',
        'J',
        'K',
        'L',
        'M',  // 38
        'N',
        'O',
        'P',
        'Q',
        'R',
        'S',
        'T',
        'U',
        'V',
        'W',
        'X',
        'Y',
        'Z',
        '0',
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',  // 61
        '-',
        '_'
    ) // q = 16, N = 40, - = 63 _ = 64
    private val HEX_CHARS =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    fun decifrarMapData(key2: String, preData: String): String {
        var key = key2
        var data = preData
        try {
            key = prepareKey(key)
            data = decypherData(preData, key, checksum(key).toString() + "")
        } catch (ignored: Exception) {
        }
        return data
    }
    fun prepareKey(d: String): String {
        val _loc3 = StringBuilder("")
        var _loc4 = 0
        while (_loc4 < d.length) {
            _loc3.append(d.substring(_loc4, _loc4.plus(2)).toInt(16).toChar())
            _loc4 += 2
        }
        return StringBuilder(unescape(_loc3.toString())).toString()
    }

    private fun checksum(s: String): Char {
        var _loc3 = 0
        var _loc4 = 0
        while (_loc4 < s.length) {
            _loc3 = _loc3.plus(s.codePointAt(_loc4).div(16))
            _loc4++
        }
        return HEX_CHARS[_loc3.div(16)]
    }
    fun decypherData(d: String, k: String, checksum: String): String {
        val c = checksum.toInt(16).times(2)
        val _loc5 = StringBuilder()
        val _loc6 = k.length
        var _loc7 = 0
        var _loc9 = 0
        while (_loc9 < d.length) {
            _loc5.append(
                (d.substring(
                    _loc9,
                    _loc9.plus(2)
                ).toInt(16).xor(k.codePointAt((_loc7.plus(c)) % _loc6))).toChar()
            )
            _loc7++
            _loc9 = _loc9.plus(2)
        }
        return StringBuilder(unescape(_loc5.toString())).toString()
    }

    private fun unescape(s1: String): String {
        var s = s1
        try {
            s = URLDecoder.decode(s, StandardCharsets.UTF_8.toString())
        } catch (ignored: Exception) {
        }
        return s
    }

    fun decompilarMapaData(mapa: GameMap) {
        try {
            var activo: Boolean
            var lineaDeVista: Boolean
            var tieneObjInteractivo: Boolean
            var caminable: Byte
            var level: Byte
            var slope: Byte
            var objInteractivo: Short
            var f: Short = 0
            while (f < mapa.mapData.length) {
                val celdaData =
                    StringBuilder(mapa.mapData.substring(f.toInt(), f.plus(10)))
                val celdaInfo = ArrayList<Byte>()
                for (element in celdaData) {
                    celdaInfo.add(getNumeroPorValorHash(element))
                }
                activo = celdaInfo[0].and(32).toInt().shr(5) != 0
                lineaDeVista = celdaInfo[0].and(1) != 0.toByte()
                tieneObjInteractivo = celdaInfo[7].and(2).toInt().shr(1) != 0
                caminable = celdaInfo[2].and(56).toInt().shr(3).toByte() // 0 = no, 1 = medio, 4 = si
                level = celdaInfo[1].and(15)
                slope = celdaInfo[4].and(60).toInt().shr(2).toByte()
                objInteractivo =
                    celdaInfo[0].and(2).toInt().shl(12).plus(celdaInfo[7].and(1).toInt().shl(12))
                        .plus(celdaInfo[8].toInt().shl(6)).plus(celdaInfo[9]).toShort()
                val celdaID = (f.div(10)).toShort()
                var movemiento = true
                if(caminable.toInt() == 0)
                {
                    movemiento = false
                }
                val celda = GameCase(
                    mapa,
                    celdaID.toInt(),
                    movemiento,
                    lineaDeVista,
                    level,
                    slope,
                    activo,
                    if (tieneObjInteractivo) objInteractivo.toInt() else -1
                )
                mapa.addCases(celda)
                //celda.celdaNornmal()
                if (tieneObjInteractivo && objInteractivo.toInt() != -1) {
                    mapa.trabajos?.let { getTrabajosPorOI(objInteractivo.toInt(), it) }
                }
                f = f.plus(10).toShort()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun getNumeroPorValorHash(c: Char): Byte {
        for (a in HASH.indices) {
            if (HASH[a].equals(c)) {
                return a.toByte()
            }
        }
        return -1
    }

    private fun noRepetirEnArray(array: ArrayList<Int>, i: Int) {
        if (!array.contains(i)) {
            array.add(i)
            array.trimToSize()
        }
    }

    fun getTrabajosPorOI(oi: Int, array: ArrayList<Int>) {
        when (oi) {
            7019 -> noRepetirEnArray(array, 23)
            7013 -> {
                noRepetirEnArray(array, 17)
                noRepetirEnArray(array, 149)
                noRepetirEnArray(array, 148)
                noRepetirEnArray(array, 15)
                noRepetirEnArray(array, 16)
                noRepetirEnArray(array, 147)
            }
            7018 -> noRepetirEnArray(array, 110)
            7028 -> noRepetirEnArray(array, 151)
            7022 -> noRepetirEnArray(array, 135)
            7023 -> noRepetirEnArray(array, 134)
            7024 -> noRepetirEnArray(array, 133)
            7025 -> noRepetirEnArray(array, 132)
            7001 -> {
                noRepetirEnArray(array, 109)
                noRepetirEnArray(array, 27)
            }
            7016, 7014 -> noRepetirEnArray(array, 63)
            7015 -> {
                noRepetirEnArray(array, 123)
                noRepetirEnArray(array, 64)
            }
            7036 -> {
                noRepetirEnArray(array, 165)
                noRepetirEnArray(array, 166)
                noRepetirEnArray(array, 167)
            }
            7011 -> {
                noRepetirEnArray(array, 13)
                noRepetirEnArray(array, 14)
            }
            7037 -> {
                noRepetirEnArray(array, 163)
                noRepetirEnArray(array, 164)
            }
            7002 -> noRepetirEnArray(array, 32)
            7005 -> noRepetirEnArray(array, 48)
            7003 -> noRepetirEnArray(array, 101)
            7008, 7009, 7010 -> {
                noRepetirEnArray(array, 12)
                noRepetirEnArray(array, 11)
            }
            7039 -> {
                noRepetirEnArray(array, 182)
                noRepetirEnArray(array, 171)
            }
            7038 -> {
                noRepetirEnArray(array, 169)
                noRepetirEnArray(array, 168)
            }
            7007 -> {
                noRepetirEnArray(array, 47)
                noRepetirEnArray(array, 122)
            }
            7012 -> {
                noRepetirEnArray(array, 18)
                noRepetirEnArray(array, 19)
                noRepetirEnArray(array, 20)
                noRepetirEnArray(array, 21)
                noRepetirEnArray(array, 65)
                noRepetirEnArray(array, 66)
                noRepetirEnArray(array, 67)
                noRepetirEnArray(array, 142)
                noRepetirEnArray(array, 143)
                noRepetirEnArray(array, 144)
                noRepetirEnArray(array, 145)
                noRepetirEnArray(array, 146)
            }
            7020 -> {
                noRepetirEnArray(array, 1)
                noRepetirEnArray(array, 113)
                noRepetirEnArray(array, 115)
                noRepetirEnArray(array, 116)
                noRepetirEnArray(array, 117)
                noRepetirEnArray(array, 118)
                noRepetirEnArray(array, 119)
                noRepetirEnArray(array, 120)
            }
            7027 -> noRepetirEnArray(array, 156)
        }
    }
}