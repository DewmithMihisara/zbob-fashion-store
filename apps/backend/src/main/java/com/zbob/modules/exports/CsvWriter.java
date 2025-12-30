package com.zbob.modules.exports;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/** Writes RFC 4180 CSV. Cells that a spreadsheet would run as a formula are made into plain text. */
public final class CsvWriter {

    private final Writer out;

    public CsvWriter(Writer out) {
        this.out = out;
    }

    public void row(List<?> cells) throws IOException {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                out.write(',');
            }
            out.write(cell(cells.get(i)));
        }
        out.write("\r\n");
    }

    static String cell(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        // A leading = + - @ or tab would make Excel or Sheets treat customer-entered text as a formula.
        if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0 && !(value instanceof Number)) {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            text = "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    public static String money(int cents) {
        return "%d.%02d".formatted(cents / 100, cents % 100);
    }
}
