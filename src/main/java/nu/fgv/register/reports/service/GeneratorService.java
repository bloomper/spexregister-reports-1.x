package nu.fgv.register.reports.service;

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
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporterParameter;
import nu.fgv.register.reports.util.XmlDataSource;

/**
 * An XML JasperReports interface.
 * 
 * Takes XML data from the standard input and uses JRXmlDataSource to generate reports in the
 * specified output format using the specified compiled JasperReports design.
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

    private String outputType = null;
    private String compiledDesign = null;
    private String selectCriteria = null;

    /**
     * Constructor.
     *
     * @param outputType The output type
     * @param compiledDesign The compiled design
     * @param selectCriteria The select criteria
     */
    private GeneratorService(String outputType, String compiledDesign, String selectCriteria) {
        this.outputType = outputType;
        this.compiledDesign = compiledDesign;
        this.selectCriteria = selectCriteria;
    }

    /**
     * Main method.
     *
     * @param args The arguments
     */
    public static void main(String[] args) {
        String outputType = null;
        String compiledDesign = null;
        String selectCriteria = null;

        if (args.length != 3) {
            printUsage();
            return;
        }

        for (String arg : args) {
            if (arg.startsWith("-o")) {
                outputType = arg.substring(2);
            } else if (arg.startsWith("-f")) {
                compiledDesign = arg.substring(2);
            } else if (arg.startsWith("-x")) {
                selectCriteria = arg.substring(2);
            }
        }

        GeneratorService jasperInterface = new GeneratorService(outputType, compiledDesign, selectCriteria);
        if (!jasperInterface.generateReport()) {
            System.exit(-1);
        }
    }

    /**
     * Generate report.
     *
     * @return The report
     */
    private boolean generateReport() {
        try {
            JasperPrint jasperPrint = JasperFillManager.fillReport(compiledDesign, null, new XmlDataSource(System.in, selectCriteria));

            if (TYPE_PDF.equals(outputType)) {
                JasperExportManager.exportReportToPdfStream(jasperPrint, System.out);
            } else if (TYPE_XML.equals(outputType)) {
                JasperExportManager.exportReportToXmlStream(jasperPrint, System.out);
            } else if (TYPE_RTF.equals(outputType)) {
                JRRtfExporter rtfExporter = new JRRtfExporter();
                rtfExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                rtfExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, System.out);
                rtfExporter.exportReport();
            } else if (TYPE_XLS.equals(outputType)) {
                JRXlsExporter xlsExporter = new JRXlsExporter();
                xlsExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                xlsExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, System.out);
                xlsExporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.TRUE);
                xlsExporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
                xlsExporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
                xlsExporter.exportReport();
            } else if (TYPE_CSV.equals(outputType)) {
                JRCsvExporter csvExporter = new JRCsvExporter();
                csvExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                csvExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, System.out);
                csvExporter.exportReport();
            } else if (TYPE_DOCX.equals(outputType)) {
                JRDocxExporter docxExporter = new JRDocxExporter();
                docxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                docxExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, System.out);
            } else if (TYPE_ODT.equals(outputType)) {
                JROdtExporter odtExporter = new JROdtExporter();
                odtExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                odtExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, System.out);
            } else if (TYPE_ODS.equals(outputType)) {
                JROdsExporter odsExporter = new JROdsExporter();
                odsExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                odsExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, System.out);
            } else {
                printUsage();
            }
        } catch (JRException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Print usage.
     */
    private static void printUsage() {
        System.out.println("XmlJasperReportsInterface usage:");
        System.out.println("\tjava XmlJasperReportsInterface -oOutputType -fCompiledDesign -xSelectExpression < input.xml > report\n");
        System.out.println("\tOutput types:\t\tpdf | xml | rtf | xls | csv | docx | odt | ods");
        System.out.println("\tCompiled design:\tFilename of the compiled JasperReports design");
        System.out.println("\tSelect expression:\tXPath expression that specifies the select criteria");
        System.out.println("\t\t\t\t(See net.sf.jasperreports.engine.data.JRXmlDataSource for further information)");
        System.out.println("\tStandard input:\t\tXML input data");
        System.out.println("\tStandard output:\tReport generated by JasperReports");
    }
}
