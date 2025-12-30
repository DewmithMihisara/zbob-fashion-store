package com.zbob.modules.exports;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvWriterTest {

    @Test
    void quotesCommasQuotesAndNewlines() {
        assertThat(CsvWriter.cell("a,b")).isEqualTo("\"a,b\"");
        assertThat(CsvWriter.cell("say \"hi\"")).isEqualTo("\"say \"\"hi\"\"\"");
        assertThat(CsvWriter.cell("line\nbreak")).isEqualTo("\"line\nbreak\"");
    }

    @Test
    void neutralisesFormulas() {
        assertThat(CsvWriter.cell("=HYPERLINK(\"http://evil\")")).startsWith("\"'=HYPERLINK");
        assertThat(CsvWriter.cell("+1")).isEqualTo("'+1");
        assertThat(CsvWriter.cell("@cmd")).isEqualTo("'@cmd");
    }

    @Test
    void leavesNegativeNumbersAlone() {
        assertThat(CsvWriter.cell(-5)).isEqualTo("-5");
    }

    @Test
    void writesEmptyCellsForNull() throws Exception {
        StringWriter out = new StringWriter();
        new CsvWriter(out).row(Arrays.asList("a", null, 3));

        assertThat(out.toString()).isEqualTo("a,,3\r\n");
    }

    @Test
    void formatsMoney() {
        assertThat(CsvWriter.money(3550)).isEqualTo("35.50");
        assertThat(List.of(CsvWriter.money(5))).containsExactly("0.05");
    }
}
