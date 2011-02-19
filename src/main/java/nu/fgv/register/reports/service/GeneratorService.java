package nu.fgv.register.reports.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
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
    private static final String TYPE_HTML = "html";
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
    public byte[] generate(String report, String outputFormat, String locale, String inputData, String selectCriteria) {
        try {
            ResourceBundle resourceBundle = null;
            if (locale == null) {
                resourceBundle = ResourceBundle.getBundle(report);
            } else {
                resourceBundle = ResourceBundle.getBundle(report, new Locale(locale));
            }
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, resourceBundle);
            JasperPrint jasperPrint = JasperFillManager.fillReport(getReport(report), parameters, new XmlDataSource(new ByteArrayInputStream(inputData.getBytes("UTF-8")), selectCriteria));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            if (TYPE_PDF.equals(outputFormat)) {
                JRPdfExporter pdfExporter = new JRPdfExporter();
                pdfExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                pdfExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                pdfExporter.exportReport();
            } else if (TYPE_XML.equals(outputFormat)) {
                JRXmlExporter xmlExporter = new JRXmlExporter();
                xmlExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                xmlExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                xmlExporter.exportReport();
            } else if (TYPE_HTML.equals(outputFormat)) {
                JRXhtmlExporter htmlExporter = new JRXhtmlExporter();
                htmlExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                htmlExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                htmlExporter.exportReport();
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
                docxExporter.exportReport();
            } else if (TYPE_ODT.equals(outputFormat)) {
                JROdtExporter odtExporter = new JROdtExporter();
                odtExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                odtExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                odtExporter.exportReport();
            } else if (TYPE_ODS.equals(outputFormat)) {
                JROdsExporter odsExporter = new JROdsExporter();
                odsExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                odsExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
                odsExporter.exportReport();
            } else {
                throw new RuntimeException("Unknown format");
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
