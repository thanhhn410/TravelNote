package vn.travelnote.io

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Trinh ghi XLSX toi gian, viet thang ra OOXML.
 * Muc dich: giu APK duoi 20MB (Apache POI se lam phinh app them hon 10MB).
 */
sealed class Cell {
    data class Text(val v: String) : Cell()
    data class Num(val v: Double, val money: Boolean = false) : Cell()
    data class Head(val v: String) : Cell()
    object Empty : Cell()
}

class Sheet(name: String) {
    val name: String = sanitize(name)
    val rows = ArrayList<List<Cell>>()

    fun row(cells: List<Cell>) {
        rows.add(cells)
    }

    fun row(vararg cells: Cell) {
        rows.add(cells.toList())
    }

    fun blank() {
        rows.add(emptyList())
    }

    private fun sanitize(n: String): String {
        val cleaned = n.replace(Regex("[\\\\/*?\\[\\]:]"), " ").trim()
        return if (cleaned.length > 31) cleaned.substring(0, 31) else cleaned.ifBlank { "Sheet" }
    }
}

object Xlsx {

    fun write(out: OutputStream, sheets: List<Sheet>) {
        val zip = ZipOutputStream(out)
        zip.putNextEntry(ZipEntry("[Content_Types].xml"))
        zip.write(contentTypes(sheets.size).toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("_rels/.rels"))
        zip.write(rootRels().toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("xl/workbook.xml"))
        zip.write(workbook(sheets).toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels"))
        zip.write(workbookRels(sheets.size).toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        zip.putNextEntry(ZipEntry("xl/styles.xml"))
        zip.write(styles().toByteArray(Charsets.UTF_8))
        zip.closeEntry()

        sheets.forEachIndexed { i, s ->
            zip.putNextEntry(ZipEntry("xl/worksheets/sheet${i + 1}.xml"))
            zip.write(sheetXml(s).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        zip.finish()
        zip.flush()
    }

    private fun contentTypes(n: Int): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        sb.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
        sb.append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
        sb.append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
        sb.append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>")
        sb.append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>")
        for (i in 1..n) {
            sb.append("<Override PartName=\"/xl/worksheets/sheet$i.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>")
        }
        sb.append("</Types>")
        return sb.toString()
    }

    private fun rootRels(): String =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
            "</Relationships>"

    private fun workbook(sheets: List<Sheet>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        sb.append("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ")
        sb.append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>")
        sheets.forEachIndexed { i, s ->
            sb.append("<sheet name=\"").append(esc(s.name)).append("\" sheetId=\"").append(i + 1)
                .append("\" r:id=\"rId").append(i + 1).append("\"/>")
        }
        sb.append("</sheets></workbook>")
        return sb.toString()
    }

    private fun workbookRels(n: Int): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        sb.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">")
        for (i in 1..n) {
            sb.append("<Relationship Id=\"rId$i\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet$i.xml\"/>")
        }
        sb.append("<Relationship Id=\"rId${n + 1}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>")
        sb.append("</Relationships>")
        return sb.toString()
    }

    private fun styles(): String =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
            "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"#,##0\"/></numFmts>" +
            "<fonts count=\"2\">" +
            "<font><sz val=\"11\"/><color theme=\"1\"/><name val=\"Calibri\"/></font>" +
            "<font><b/><sz val=\"11\"/><color theme=\"1\"/><name val=\"Calibri\"/></font>" +
            "</fonts>" +
            "<fills count=\"3\">" +
            "<fill><patternFill patternType=\"none\"/></fill>" +
            "<fill><patternFill patternType=\"gray125\"/></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFD9EAD3\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
            "</fills>" +
            "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>" +
            "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
            "<cellXfs count=\"3\">" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" +
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>" +
            "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>" +
            "</cellXfs>" +
            "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
            "</styleSheet>"

    private fun sheetXml(s: Sheet): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
        sb.append("<cols><col min=\"1\" max=\"1\" width=\"14\" customWidth=\"1\"/>")
        sb.append("<col min=\"2\" max=\"3\" width=\"22\" customWidth=\"1\"/>")
        sb.append("<col min=\"4\" max=\"9\" width=\"16\" customWidth=\"1\"/></cols>")
        sb.append("<sheetData>")
        s.rows.forEachIndexed { r, cells ->
            val rowNum = r + 1
            sb.append("<row r=\"").append(rowNum).append("\">")
            cells.forEachIndexed { c, cell ->
                val ref = colName(c) + rowNum
                when (cell) {
                    is Cell.Empty -> {}
                    is Cell.Head -> sb.append("<c r=\"").append(ref).append("\" s=\"1\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                        .append(esc(cell.v)).append("</t></is></c>")
                    is Cell.Text -> sb.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                        .append(esc(cell.v)).append("</t></is></c>")
                    is Cell.Num -> sb.append("<c r=\"").append(ref).append("\"")
                        .append(if (cell.money) " s=\"2\"" else "")
                        .append("><v>").append(trimNum(cell.v)).append("</v></c>")
                }
            }
            sb.append("</row>")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun trimNum(d: Double): String {
        return if (d == d.toLong().toDouble()) d.toLong().toString()
        else String.format(java.util.Locale.US, "%.4f", d)
    }

    private fun colName(index: Int): String {
        var i = index
        val sb = StringBuilder()
        while (i >= 0) {
            sb.insert(0, ('A' + (i % 26)))
            i = i / 26 - 1
        }
        return sb.toString()
    }

    private fun esc(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
