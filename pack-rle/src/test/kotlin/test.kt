
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class Tests {

    @Test
    fun encode() {
        var text = EncodeParser.create("AAAABABABBBBBaba")
        assertEquals("${Char(147)}A${Char(35)}BABA${Char(148)}B${Char(34)}aba", text.encoded)
        println(text.sizeRed)
        text = EncodeParser.create("A".repeat(112))
        assertEquals("${Char(255)}A", text.encoded)
        println(text.sizeRed)
        text = EncodeParser.create("A".repeat(113))
        assertEquals("${Char(255)}A${Char(32)}A", text.encoded)
        println(text.sizeRed)
        text = EncodeParser.create("A".repeat(113))
        assertEquals("${Char(255)}A${Char(32)}A", text.encoded)
        println(text.sizeRed)
    }

    @Test
    fun decode() {
        var text = DecodeParser.create("${Char(147)}A${Char(35)}BABA${Char(148)}B${Char(34)}aba")
        assertEquals("AAAABABABBBBBaba", text.decoded)
        text = DecodeParser.create("${Char(255)}A")
        assertEquals("A".repeat(112), text.decoded)
        text = DecodeParser.create("${Char(255)}A${Char(32)}A")
        assertEquals("A".repeat(113), text.decoded)
        text = DecodeParser.create("${Char(255)}A${Char(32)}A")
        assertEquals("A".repeat(113), text.decoded)
    }

}