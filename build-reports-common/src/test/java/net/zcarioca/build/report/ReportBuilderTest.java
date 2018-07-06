package net.zcarioca.build.report;

import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

public class ReportBuilderTest {

    @ClassRule
    public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testReportBuilder() throws IOException {
        final File outputFile = temporaryFolder.newFile();

        final ReportRendererFactory factory = (sink, reportBuilder) -> new TestReportRenderer(sink, reportBuilder);

        new ReportBuilder()
                .includeToc(true)
                .generatorName("test-generator")
                .encoding(StandardCharsets.UTF_8.name())
                .locale("en", "US")
                .resourceBundle("test-messages")
                .writeReport(outputFile, factory);

        assertTrue("The file " + outputFile.getAbsolutePath() + " is not found", outputFile.exists());
        assertTrue("The file " + outputFile.getAbsolutePath() + " is empty", outputFile.length() > 0);

        try (final InputStream input = new FileInputStream(outputFile)) {
            System.out.println(IOUtils.toString(input, StandardCharsets.UTF_8));
        }
    }


}
