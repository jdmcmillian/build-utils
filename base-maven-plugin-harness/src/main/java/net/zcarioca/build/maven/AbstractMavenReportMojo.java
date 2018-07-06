package net.zcarioca.build.maven;

import net.zcarioca.build.report.AbstractSystemReportRenderer;
import net.zcarioca.build.report.ReportBuilder;
import net.zcarioca.build.report.ReportRendererFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.ReaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public abstract class AbstractMavenReportMojo<R extends AbstractSystemReportRenderer> extends AbstractMavenMojo {

    private static final String DEFAULT_COUNTRY = "US";
    private static final String DEFAULT_LANGUAGE = "en";

    @Parameter(defaultValue = "${project.reporting.outputDirectory}", readonly = true, required = true)
    protected File outputDirectory;
    @Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}", readonly = true)
    private String inputEncoding;
    @Parameter(property = "outputEncoding", defaultValue = "${project.reporting.outputEncoding}", readonly = true)
    private String outputEncoding;
    @Parameter(defaultValue = "${user.language}")
    private String userLanguage;

    @Parameter(defaultValue = "${user.country}")
    private String userCountry;

    @Component
    private Renderer renderer;

    @Override
    protected void executeMojo() throws MojoExecutionException, MojoFailureException {
        generateFinalReport(executeReportMojo(), getOutputFileName());
    }

    protected void generateFinalReport(final ReportRendererFactory rendererFactory,
                                       final String outputFileName) throws MojoExecutionException {
        try {
            new ReportBuilder()
                    .resourceBundle(getBundle())
                    .locale(userLanguage, userCountry)
                    .encoding(getOutputEncoding())
                    .generatorName(getGeneratorName())
                    .includeToc(true)
                    .renderer(renderer)
                    .templateProperties(getTemplateProperties())
                    .writeReport(new File(outputDirectory, outputFileName), rendererFactory);
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected abstract ReportRendererFactory executeReportMojo() throws MojoExecutionException, MojoFailureException;

    protected abstract String getOutputFileName();

    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle("messages", getLocale());
    }

    protected String getOutputEncoding() {
        return outputEncoding == null ? ReaderFactory.UTF_8 : outputEncoding;
    }

    private final String getGeneratorName() throws MojoExecutionException {
        try (final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("generator.txt")) {
            return IOUtils.toString(inputStream, getInputEncoding()) + getGoalName();
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private Map<String, Object> getTemplateProperties() {
        final Map<String, Object> templateProperties = new HashMap<>();
        templateProperties.put("project", project);
        templateProperties.put("inputEncoding", getInputEncoding());
        templateProperties.put("outputEncoding", getOutputEncoding());
        // Put any of the properties in directly into the Velocity context
        for (final Map.Entry<Object, Object> entry : project.getProperties().entrySet()) {
            templateProperties.put((String) entry.getKey(), entry.getValue());
        }
        return templateProperties;
    }

    protected Locale getLocale() {
        final String language = StringUtils.defaultString(userLanguage, DEFAULT_LANGUAGE);
        final String country = StringUtils.defaultString(userCountry, DEFAULT_COUNTRY);
        final Locale locale = new Locale(language, country);
        return locale;
    }

    protected String getInputEncoding() {
        return inputEncoding == null ? ReaderFactory.ISO_8859_1 : inputEncoding;
    }

    protected abstract String getGoalName();

}
