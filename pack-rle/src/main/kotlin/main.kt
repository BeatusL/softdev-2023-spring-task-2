import java.io.File
import kotlinx.cli.*
import java.io.FileInputStream


fun main(args: Array<String>) {
    val parser = ArgParser("pack-rle")
    val type by parser.option(ArgType.String, shortName = "z|-u", description = "Operation type").required()
    val output by
    parser.option(ArgType.String, shortName = "out", description = "Output file name").default("outputname.txt")
    val input by
    parser.option(ArgType.String, shortName = "in", description = "Input file name").default("inputname.txt")

    parser.parse(args)
    val outputFile = File(output)
    val inputFile = File(input)


    if (!inputFile.exists()) {
        println("File does not exist. Please, check the path.")
        kotlin.system.exitProcess(-1)
        }


    when (type) {
        "-z" -> {
            println("Processing...")
            outputFile.bufferedWriter().use {
                it.write(EncodeParser.create(FileInputStream(inputFile)).encoded)
            }
            println("File saved successfully!/nHave a good day!")
        }
        "-u" -> {
            println("Processing...")
            outputFile.bufferedWriter().use {
                it.write(DecodeParser.create(FileInputStream(inputFile)).decoded)
            }
            println("File saved successfully!/nHave a good day!")
        }
        else -> {
            println(
                "(-u) and (-z) are the only available arguments./n" +
                        "Choose one of them or do not choose at all"
            )
            kotlin.system.exitProcess(-1)
        }
    }
}


class EncodeParser private constructor (private val inputStream: FileInputStream) {
    private var index = 0
    private var result = StringBuilder()
    private var text = StringBuilder()
    private var textIsRead = false


    private fun fromIntToString(count: Int, string: String): String {
        var curCount = count
        val localResult = StringBuilder()
        val maxSequenceDif = 113
        val maxSequenceSim = 112
        when {
            count < 0 -> {
                var maxSequenceCounter = 0
                curCount *= -1
                while (curCount > maxSequenceDif) {
                    maxSequenceCounter++
                    localResult.append(Char(144))
                        .append(string.substring(113 * (maxSequenceCounter - 1), 113 * maxSequenceCounter))
                    curCount -= maxSequenceDif
                }
                localResult.append(' ' + curCount - 1).append(string.substring(113 * maxSequenceCounter, string.length))
                return localResult.toString()
            }
            count > 1 -> {
                while (curCount > maxSequenceSim) {       // A * 142 = ~A~A~A!A
                    localResult.append("${Char(255)}$string")       //
                    curCount -= maxSequenceSim
                }
                if (curCount > 1) localResult.append(Char(143) + curCount).append(string)
                else localResult.append(Char(32)).append(string)
            }
            else -> throw IllegalArgumentException()
        }
        return localResult.toString()
    }

    private fun countDif() {
        read()
        var count = 1
        var cdResult = "${text[index]}"
        while (!isOutOfBounds(index + 1) && text[index] != text[index + 1]) {
            if (!isOutOfBounds(index + 2) && text[index + 1] == text[index + 2]) break
            count++
            index++
            cdResult += text[index]
            read()
        }
        result.append(fromIntToString(-count, cdResult))
    }

    private fun countSim() {
        var count = 1
        while (!isOutOfBounds(index + 1) && text[index] == text[index + 1]) {
            count++
            index++
            read()
        }
        result.append(fromIntToString(count, text[index].toString()))
    }

    private fun encode(): String {
        read()
        while (!isOutOfBounds(index)) {
            read()
            if (text[index] == text[index + 1]) {
                countSim()
            }
            else countDif()
            index++
        }
        return String(result)
    }

    private fun read() {
        if (!textIsRead && text.length <= index + 2) {
            val status = inputStream.read()
            if (status != -1) text.append(Char(status).toString())
            else textIsRead = true
        }
    }

    private fun isOutOfBounds(localIndex: Int) = textIsRead && localIndex >= text.length

    val encoded = encode()

    companion object {
        fun create(inputStream: FileInputStream) = EncodeParser(inputStream)
    }
}

class DecodeParser private constructor (private val inputStream: FileInputStream) {

    private var byteArray = ByteArray(2048)
    private val result = StringBuilder()
    private var text = ""
    private var index = 0

    private fun decode(): String {
        read()
        while (true) {
            if (text[index].code in 32..144) {
                appendDiff(text[index].code)
            }
            else {
                println(text[index].code)
                appendSim(text[index].code)
            }
            if (index >= text.length && read() == -1) break
        }
        inputStream.close()
        return result.toString()
    }

    private fun read(): Int {
        val res = inputStream.read(byteArray)
        text = if (res != -1) String(byteArray, 0, res) else ""
        return res
    }

    private fun appendDiff(int: Int) {
        if (index + int - 30 <= text.length) {
            result.append(text.substring(index + 1, index + int - 30))
            index += int - 30
        }
        else {
            result.append(text.substring(index + 1))
            index = index + int - 30 - text.length
            read()
            result.append(text.substring(0, index))
        }
    }
    private fun appendSim(int: Int) {
        if (index + 1 < text.length) {
            result.append(text[index + 1].toString().repeat(int - 143))
            index += 2
        }
        else {
            read()
            result.append(text[0].toString().repeat(int - 143))
            index = 1
        }
    }


    val decoded = decode()
    companion object {
        fun create(inputStream: FileInputStream) = DecodeParser(inputStream)
    }
}
