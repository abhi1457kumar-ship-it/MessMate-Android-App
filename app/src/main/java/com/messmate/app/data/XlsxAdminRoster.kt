package com.messmate.app.data

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipInputStream

/** Reads 10-digit IDs from column A of the first worksheet in an Excel .xlsx workbook. */
internal object XlsxAdminRoster {
    fun readIds(input: InputStream): List<String> {
        val workbookParts = mutableMapOf<String, ByteArray>()
        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "xl/sharedStrings.xml" || entry.name == "xl/worksheets/sheet1.xml") {
                    workbookParts[entry.name] = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        val sheet = workbookParts["xl/worksheets/sheet1.xml"]
            ?: error("The selected file isn't a supported Excel workbook.")
        val sharedStrings = workbookParts["xl/sharedStrings.xml"]?.let(::readSharedStrings).orEmpty()
        return readFirstColumn(sheet, sharedStrings)
    }

    private fun readSharedStrings(xml: ByteArray): List<String> {
        val parser = Xml.newPullParser().apply { setInput(xml.inputStream(), null) }
        val strings = mutableListOf<String>()
        var insideString = false
        var insideText = false
        val current = StringBuilder()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "si" -> {
                        insideString = true
                        current.clear()
                    }
                    "t" -> if (insideString) insideText = true
                }
                XmlPullParser.TEXT -> if (insideString && insideText) current.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "t" -> insideText = false
                    "si" -> {
                        strings += current.toString()
                        insideString = false
                    }
                }
            }
            event = parser.next()
        }
        return strings
    }

    private fun readFirstColumn(xml: ByteArray, sharedStrings: List<String>): List<String> {
        val parser = Xml.newPullParser().apply { setInput(xml.inputStream(), null) }
        val ids = linkedSetOf<String>()
        var cellRef = ""
        var cellType = ""
        var isFirstColumn = false
        var inCellValue = false
        val cellValue = StringBuilder()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "c" -> {
                        cellRef = parser.getAttributeValue(null, "r").orEmpty()
                        cellType = parser.getAttributeValue(null, "t").orEmpty()
                        isFirstColumn = cellRef.matches(Regex("A[0-9]+"))
                        cellValue.clear()
                    }
                    "v", "t" -> if (isFirstColumn) inCellValue = true
                }
                XmlPullParser.TEXT -> if (isFirstColumn && inCellValue) cellValue.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "v", "t" -> inCellValue = false
                    "c" -> {
                        if (isFirstColumn && cellRef != "A1") {
                            val raw = cellValue.toString().trim()
                            val value = if (cellType == "s") {
                                sharedStrings.getOrNull(raw.toIntOrNull() ?: -1).orEmpty()
                            } else {
                                raw
                            }
                            if (value.length == 10 && value.all { it in '0'..'9' }) ids += value
                        }
                        isFirstColumn = false
                    }
                }
            }
            event = parser.next()
        }
        return ids.toList()
    }
}
