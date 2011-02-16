package nu.fgv.register.reports.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import nu.fgv.register.reports.util.XmlDataSource;

/**
 * An XML JasperReports interface.
 */
@Service
public class GeneratorService {
    private static final String TYPE_PDF = "pdf";
    private static final String TYPE_XML = "xml";
    private static final String TYPE_RTF = "rtf";
    private static final String TYPE_XLS = "xls";
    private static final String TYPE_CSV = "csv";
    private static final String TYPE_DOCX = "docx";
    private static final String TYPE_ODT = "odt";
    private static final String TYPE_ODS = "ods";

    private String reportSuffix;
    private String reportPrefix;
    
    @Required
    public void setReportSuffix(String reportSuffix) {
        this.reportSuffix = reportSuffix;
    }

    @Required
    public void setReportPrefix(String reportPrefix) {
        this.reportPrefix = reportPrefix;
    }

    private InputStream getReport(String report) throws IOException {
        return new ClassPathResource(reportPrefix + report + "." + reportSuffix).getInputStream();
    }

    /**
     * Generate report.
     *
     * @return The report
     */
    public byte[] generate(String report, String outputFormat, String inputData, String selectCriteria) {
        try {
            JasperPrint jasperPrint = JasperFillManager.fillReport(getReport(report), null, new XmlDataSource(new ByteArrayInputStream(inputData.getBytes("UTF-8")), selectCriteria));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            if (TYPE_PDF.equals(outputFormat)) {
                JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            } else if (TYPE_XML.equals(outputFormat)) {
                JasperExportManager.exportReportToXmlStream(jasperPrint, outputStream);
            } else if (TYPE_RTF.equals(outputFormat)) {
                JRRtfExporter rtfExporter = new JRRtfExporter();
                rtfExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                rtfExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                rtfExporter.exportReport();
            } else if (TYPE_XLS.equals(outputFormat)) {
                JRXlsExporter xlsExporter = new JRXlsExporter();
                xlsExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                xlsExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                xlsExporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
                xlsExporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
                xlsExporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
                xlsExporter.exportReport();
            } else if (TYPE_CSV.equals(outputFormat)) {
                JRCsvExporter csvExporter = new JRCsvExporter();
                csvExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                csvExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                csvExporter.exportReport();
            } else if (TYPE_DOCX.equals(outputFormat)) {
                JRDocxExporter docxExporter = new JRDocxExporter();
                docxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                docxExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
            } else if (TYPE_ODT.equals(outputFormat)) {
                JROdtExporter odtExporter = new JROdtExporter();
                odtExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                odtExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
            } else if (TYPE_ODS.equals(outputFormat)) {
                JROdsExporter odsExporter = new JROdsExporter();
                odsExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                odsExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
            } else {
                throw new RuntimeException("Unknown format");
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
