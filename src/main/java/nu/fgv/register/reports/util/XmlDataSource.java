package nu.fgv.register.reports.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.beanutils.ConvertUtils;
import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.util.JRXmlUtils;

public class XmlDataSource implements JRDataSource {

    private Document document;
    private String selectExpression;
    @SuppressWarnings("unchecked")
    private List nodeList;
    private int nodeListLength;
    private Object currentNode;
    private int currentNodeIndex = -1;

    public XmlDataSource() {
    }

    public XmlDataSource(Document document, String selectExpression) throws JRException {
        this.document = document;
        this.selectExpression = selectExpression;
        moveFirst();
    }

    public XmlDataSource(String uri, String selectExpression) throws JRException {
        this(XmlDataSource.loadDocumentFromFile(uri), selectExpression);
    }

    public XmlDataSource(InputStream in) throws JRException {
        this(in, ".");
    }

    public XmlDataSource(InputStream in, String selectExpression) throws JRException {
        this(JRXmlUtils.parse(new InputSource(in)), selectExpression);
    }

    public void moveFirst() throws JRException {
        if (document == null) {
            throw new JRException("document cannot be not null");
        }
        if (selectExpression == null) {
            throw new JRException("selectExpression cannot be not null");
        }

        try {
            currentNode = null;
            currentNodeIndex = -1;
            nodeListLength = 0;
            nodeList = getXpathNodeList(document, selectExpression);
            nodeListLength = nodeList.size();
        } catch (Exception e) {
            throw new JRException("XPath selection failed. Expression: " + selectExpression, e);
        }
    }

    @Override
    public boolean next() throws JRException {
        if (currentNodeIndex == nodeListLength - 1) {
            return false;
        }

        currentNode = nodeList.get(++currentNodeIndex);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        if (currentNode == null) {
            return null;
        }

        String expression = jrField.getDescription();
        if (expression == null || expression.length() == 0) {
            return null;
        }

        Object value = null;
        Class valueClass = jrField.getValueClass();

        if (Object.class != valueClass) {
            String text = null;

            try {
                text = getXpathStringValue(currentNode, expression);
            } catch (Exception e) {
                throw new JRException("XPath selection failed. Expression: " + expression, e);
            }

            if (text != null) {
                if (String.class == valueClass) {
                    value = text;
                } else {
                    value = ConvertUtils.convert(text.trim(), valueClass);
                }
            }
        }

        return value;
    }

    protected static Document loadDocumentFromFile(String file) throws JRException {
        Document doc = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new File(file));
        } catch (Exception e) {
            throw new JRException("Input file:" + file + " was not found.");
        }

        return doc;
    }

    @SuppressWarnings("unchecked")
    protected List getXpathNodeList(Object doc, String domxpath) throws Exception {
        try {
            DOMXPath xpath = new DOMXPath(domxpath);
            Object ro = xpath.evaluate(doc);
            if (ro instanceof List) {
                return (List) xpath.evaluate(doc);
            } else {
                List results = new ArrayList();
                results.add(ro);
                return results;
            }
        } catch (JaxenException e) {
            throw e;
        }
    }

    protected String getXpathStringValue(Object node, String domxpath) throws Exception {
        try {
            DOMXPath xpath = new DOMXPath(domxpath);
            String value = xpath.stringValueOf(node);
            if (value.length() == 0) {
                return null;
            } else {
                return value;
            }
        } catch (JaxenException e) {
            throw e;
        }
    }

    public XmlDataSource subDataSource(String selectExpr) throws JRException {
        Document doc = subDocument();
        return new XmlDataSource(doc, selectExpr);
    }

    public Document subDocument() throws JRException {
        if (currentNode == null) {
            throw new JRException("No node available. Iterate or rewind the data source.");
        }

        Document doc = JRXmlUtils.createDocument((org.w3c.dom.Node) currentNode);
        return doc;
    }

    public XmlDataSource dataSource(String selectExpr) throws JRException {
        return new XmlDataSource(document, selectExpr);
    }

}
