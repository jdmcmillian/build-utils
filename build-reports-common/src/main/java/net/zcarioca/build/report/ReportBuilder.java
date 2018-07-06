package net.zcarioca.build.report;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.maven.doxia.site.decoration.DecorationModel;
import org.apache.maven.doxia.siterenderer.DefaultSiteRenderer;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.siterenderer.RendererException;
import org.apache.maven.doxia.siterenderer.RenderingContext;
import org.apache.maven.doxia.siterenderer.SiteRenderingContext;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static net.zcarioca.build.common.Validators.requireNotBlank;
import static net.zcarioca.build.common.Validators.requireNotNull;

public class ReportBuilder {
    private static final String DOXIA_TEMPLATE = "org/apache/maven/doxia/siterenderer/resources/default-site.vm";

    ResourceBundle resourcBundle;
    Locale locale;
    Charset encoding;
    boolean includeToc;
    String generatorName;
    Map<String, Object> templateProperties;
    Renderer renderer;

    public ReportBuilder() {
        renderer = new DefaultSiteRenderer();
        ((DefaultSiteRenderer) renderer).enableLogging(new ConsoleLogger());
    }

    public ReportBuilder renderer(final Renderer renderer) {
        requireNotNull(renderer, "Parameter 'renderer' is null");
        this.renderer = renderer;
        return this;
    }

    public ReportBuilder generatorName(final String generatorName) {
        requireNotBlank(generatorName, "Parameter 'generatorName' is null or blank");
        this.generatorName = generatorName;
        return this;
    }

    public ReportBuilder locale(final String language, final String country) {
        requireNotBlank(language, "Parameter 'language' is null or blank");
        return locale(new Locale(language, country));
    }

    public ReportBuilder locale(final Locale locale) {
        requireNotNull(locale, "Parameter 'locale' is null");
        this.locale = locale;
        return this;
    }

    public ReportBuilder encoding(final String encoding) {
        requireNotBlank(encoding, "Parameter 'encoding' is null or blank");
        return encoding(Charset.forName(encoding));
    }

    public ReportBuilder encoding(final Charset encoding) {
        requireNotNull(encoding, "Parameter 'encoding' is null");
        this.encoding = encoding;
        return this;
    }

    public ReportBuilder resourceBundle(final String pathToMessages) {
        requireNotBlank(pathToMessages, "Parameter 'pathToMessages' is null or blank");
        return resourceBundle(ResourceBundle.getBundle(pathToMessages));
    }

    public ReportBuilder resourceBundle(final ResourceBundle resourceBundle) {
        requireNotNull(resourceBundle, "Parameter 'resourceBundle' is null");
        resourcBundle = resourceBundle;
        return this;
    }

    public ReportBuilder includeToc(final boolean include) {
        includeToc = include;
        return this;
    }

    public ReportBuilder templateProperties(final Map<String, Object> templateProperties) {
        this.templateProperties = templateProperties;
        return this;
    }

    public void writeReport(final File outputFile, final ReportRendererFactory rendererFactory) throws IOException {
        final File parent = outputFile.getParentFile();
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Unable to create directory: " + parent.getAbsolutePath());
            }
        }
        if (!parent.isDirectory()) {
            throw new IOException("Not a directory: " + parent.getAbsolutePath());
        }
        validateState();

        final SiteRenderingContext siteRenderingContext = new SiteRenderingContext();
        siteRenderingContext.setDecoration(new DecorationModel());
        siteRenderingContext.setTemplateName(DOXIA_TEMPLATE);
        siteRenderingContext.setTemplateProperties(getTemplateProperties());
        siteRenderingContext.setLocale(locale);

        final RenderingContext renderingContext = new RenderingContext(parent, outputFile.getName(), generatorName);
        final SiteRendererSink siteRendererSink = new CustomSiteSink(renderingContext);

        // auto-closeable is only used to close the sink
        rendererFactory.apply(siteRendererSink, this).render();
        siteRendererSink.close();

        System.out.println("Title: " + siteRendererSink.getTitle());
        System.out.println("Authors: " + siteRendererSink.getAuthors());
        System.out.println("Head: " + siteRendererSink.getHead());
        System.out.println("Body: " + siteRendererSink.getBody());


        try (final BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(outputFile, encoding))) {
            renderer.mergeDocumentIntoSite(writer, siteRendererSink, siteRenderingContext);
        } catch (final RendererException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private void validateState() {
        requireNotNull(encoding, () -> new IllegalStateException("Parameter 'encoding' has not been set"));
        requireNotNull(resourcBundle, () -> new IllegalStateException("Parameter 'resourceBundle' has not been set"));
        requireNotNull(locale, () -> new IllegalStateException("Parameter 'locale' has not been set"));
        requireNotBlank(generatorName, () -> new IllegalStateException("Parameter 'generatorName' has not been set"));
    }

    private Map<String, Object> getTemplateProperties() {
        return Optional.ofNullable(templateProperties).orElseGet(() -> {
            final Map<String, Object> templateProperties = new HashMap<>();
            templateProperties.put("outputEncoding", encoding.name());
            return templateProperties;
        });

    }

}
