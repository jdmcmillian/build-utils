package net.zcarioca.build.report;

import org.apache.maven.doxia.sink.Sink;

import java.util.function.BiFunction;

public interface ReportRendererFactory extends BiFunction<Sink, ReportBuilder, AbstractSystemReportRenderer> {

    @Override
    default AbstractSystemReportRenderer apply(final Sink sink, final ReportBuilder rReportBuilder) {
        return createReportRenderer(sink, rReportBuilder);
    }

    AbstractSystemReportRenderer createReportRenderer(Sink sink, ReportBuilder builder);
}
