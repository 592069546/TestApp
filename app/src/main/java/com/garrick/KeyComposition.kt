package com.garrick

/**
 * Decompose accented characters.
 *
 *
 * For example, [decompose(&#39;é&#39;)][.decompose] returns `"\u0301e"`.
 *
 *
 * This is useful for injecting key events to generate the expected character ([android.view.KeyCharacterMap.getEvents]
 * KeyCharacterMap.getEvents()} returns `null` with input `"é"` but works with input `"\u0301e"`).
 *
 *
 * See [diacritical dead key characters](https://source.android.com/devices/input/key-character-map-files#behaviors).
 */
object KeyComposition {
    private const val KEY_DEAD_GRAVE = "\u0300"
    private const val KEY_DEAD_ACUTE = "\u0301"
    private const val KEY_DEAD_CIRCUMFLEX = "\u0302"
    private const val KEY_DEAD_TILDE = "\u0303"
    private const val KEY_DEAD_UMLAUT = "\u0308"

    private val COMPOSITION_MAP = mapOf(
        'À' to grave('A'),
        'È' to grave('E'),
        'Ì' to grave('I'),
        'Ò' to grave('O'),
        'Ù' to grave('U'),
        'à' to grave('a'),
        'è' to grave('e'),
        'ì' to grave('i'),
        'ò' to grave('o'),
        'ù' to grave('u'),
        'Ǹ' to grave('N'),
        'ǹ' to grave('n'),
        'Ẁ' to grave('W'),
        'ẁ' to grave('w'),
        'Ỳ' to grave('Y'),
        'ỳ' to grave('y'),
        'Á' to acute('A'),
        'É' to acute('E'),
        'Í' to acute('I'),
        'Ó' to acute('O'),
        'Ú' to acute('U'),
        'Ý' to acute('Y'),
        'á' to acute('a'),
        'é' to acute('e'),
        'í' to acute('i'),
        'ó' to acute('o'),
        'ú' to acute('u'),
        'ý' to acute('y'),
        'Ć' to acute('C'),
        'ć' to acute('c'),
        'Ĺ' to acute('L'),
        'ĺ' to acute('l'),
        'Ń' to acute('N'),
        'ń' to acute('n'),
        'Ŕ' to acute('R'),
        'ŕ' to acute('r'),
        'Ś' to acute('S'),
        'ś' to acute('s'),
        'Ź' to acute('Z'),
        'ź' to acute('z'),
        'Ǵ' to acute('G'),
        'ǵ' to acute('g'),
        'Ḉ' to acute('Ç'),
        'ḉ' to acute('ç'),
        'Ḱ' to acute('K'),
        'ḱ' to acute('k'),
        'Ḿ' to acute('M'),
        'ḿ' to acute('m'),
        'Ṕ' to acute('P'),
        'ṕ' to acute('p'),
        'Ẃ' to acute('W'),
        'ẃ' to acute('w'),
        'Â' to circumflex('A'),
        'Ê' to circumflex('E'),
        'Î' to circumflex('I'),
        'Ô' to circumflex('O'),
        'Û' to circumflex('U'),
        'â' to circumflex('a'),
        'ê' to circumflex('e'),
        'î' to circumflex('i'),
        'ô' to circumflex('o'),
        'û' to circumflex('u'),
        'Ĉ' to circumflex('C'),
        'ĉ' to circumflex('c'),
        'Ĝ' to circumflex('G'),
        'ĝ' to circumflex('g'),
        'Ĥ' to circumflex('H'),
        'ĥ' to circumflex('h'),
        'Ĵ' to circumflex('J'),
        'ĵ' to circumflex('j'),
        'Ŝ' to circumflex('S'),
        'ŝ' to circumflex('s'),
        'Ŵ' to circumflex('W'),
        'ŵ' to circumflex('w'),
        'Ŷ' to circumflex('Y'),
        'ŷ' to circumflex('y'),
        'Ẑ' to circumflex('Z'),
        'ẑ' to circumflex('z'),
        'Ã' to tilde('A'),
        'Ñ' to tilde('N'),
        'Õ' to tilde('O'),
        'ã' to tilde('a'),
        'ñ' to tilde('n'),
        'õ' to tilde('o'),
        'Ĩ' to tilde('I'),
        'ĩ' to tilde('i'),
        'Ũ' to tilde('U'),
        'ũ' to tilde('u'),
        'Ẽ' to tilde('E'),
        'ẽ' to tilde('e'),
        'Ỹ' to tilde('Y'),
        'ỹ' to tilde('y'),
        'Ä' to umlaut('A'),
        'Ë' to umlaut('E'),
        'Ï' to umlaut('I'),
        'Ö' to umlaut('O'),
        'Ü' to umlaut('U'),
        'ä' to umlaut('a'),
        'ë' to umlaut('e'),
        'ï' to umlaut('i'),
        'ö' to umlaut('o'),
        'ü' to umlaut('u'),
        'ÿ' to umlaut('y'),
        'Ÿ' to umlaut('Y'),
        'Ḧ' to umlaut('H'),
        'ḧ' to umlaut('h'),
        'Ẅ' to umlaut('W'),
        'ẅ' to umlaut('w'),
        'Ẍ' to umlaut('X'),
        'ẍ' to umlaut('x'),
        'ẗ' to umlaut('t')
    )

    @JvmStatic
    fun decompose(c: Char): String? = COMPOSITION_MAP[c]

    private fun grave(c: Char): String = KEY_DEAD_GRAVE + c

    private fun acute(c: Char): String = KEY_DEAD_ACUTE + c

    private fun circumflex(c: Char): String = KEY_DEAD_CIRCUMFLEX + c

    private fun tilde(c: Char): String = KEY_DEAD_TILDE + c

    private fun umlaut(c: Char): String = KEY_DEAD_UMLAUT + c
}