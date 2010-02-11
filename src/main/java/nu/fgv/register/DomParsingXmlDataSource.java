package nu.fgv.register;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.util.JRXmlUtils;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.beanutils.ConvertUtils;

import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.*;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Custom XML datasource to increase performance.
 */
public class DomParsingXmlDataSource implements JRDataSource {
    private Document document;
    private String selectExpression;
    private List nodeList;
    private int nodeListLength;
    private Object currentNode;
    private int currentNodeIndex = -1;

    /**
     * Constructor.
     */
    public DomParsingXmlDataSource() {
    }

    /**
     * Constructor.
     *
     * @param document
     * @param selectExpression
     * @throws JRException
     */
    public DomParsingXmlDataSource(Document document, String selectExpression) throws JRException {
        this.document = document;
        this.selectExpression = selectExpression;
        moveFirst();
    }

    /**
     * Constructor.
     *
     * @param uri
     * @param selectExpression
     * @throws JRException
     */
    public DomParsingXmlDataSource(String uri, String selectExpression) throws JRException {
        this(DomParsingXmlDataSource.loadDocumentFromFile(uri), selectExpression);
    }

    /**
     * Constructor.
     *
     * @param in
     * @throws JRException
     */
    public DomParsingXmlDataSource(InputStream in) throws JRException {
        this(in, ".");
    }

    /**
     * Constructor.
     *
     * @param in
     * @param selectExpression
     * @throws JRException
     */
    public DomParsingXmlDataSource(InputStream in, String selectExpression)
            throws JRException {
        this(JRXmlUtils.parse(new InputSource(in)), selectExpression);
    }

    /**
     * @throws JRException
     */
    public void moveFirst() throws JRException {
        if (document == null)
            throw new JRException("document cannot be not null");
        if (selectExpression == null)
            throw new JRException("selectExpression cannot be not null");

        try {
            currentNode = null;
            currentNodeIndex = -1;
            nodeListLength = 0;
            nodeList = getXpathNodeList(document, selectExpression);
            nodeListLength = nodeList.size();
        } catch (Exception e) {
            throw new JRException("XPath selection failed. Expression: "
                    + selectExpression, e);
        }
    }

    /** {@inheritDoc} */
    public boolean next() throws JRException {
        if (currentNodeIndex == nodeListLength - 1)
            return false;

        currentNode = nodeList.get(++currentNodeIndex);
        return true;
    }

    /** {@inheritDoc} */
    public Object getFieldValue(JRField jrField) throws JRException {
        if (currentNode == null)
            return null;

        String expression = jrField.getDescription();
        if (expression == null || expression.length() == 0)
            return null;

        Object value = null;

        Class valueClass = jrField.getValueClass();

        if (Object.class != valueClass) {
            String text = null;

            try {
                text = getXpathStringValue(currentNode, expression);
            } catch (Exception e) {
                throw new JRException("XPath selection failed. Expression: "
                        + expression, e);
            }

            if (text != null) {
                if (String.class == valueClass)
                    value = text;
                else
                    value = ConvertUtils.convert(text.trim(), valueClass);
            }
        }

        return value;
    }

    /**
     * @param file
     * @return
     * @throws JRException
     */
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

    /**
     * @param doc
     * @param domxpath
     * @return
     * @throws Exception
     */
    protected List getXpathNodeList(Object doc, String domxpath)
            throws Exception {
        try {
            XPath xpath = new DOMXPath(domxpath);
            Object ro = xpath.evaluate(doc);
            if (ro instanceof List)
                return (List) xpath.evaluate(doc);
            else {
                List results = new ArrayList();
                results.add(ro);
                return results;
            }
        } catch (JaxenException je) {
            throw je;
        }
    }

    /**
     * @param node
     * @param domxpath
     * @return
     * @throws Exception
     */
    protected String getXpathStringValue(Object node, String domxpath)
            throws Exception {
        try {
            XPath xpath = new DOMXPath(domxpath);
            String value = xpath.stringValueOf(node);
            if (value.length() == 0)
                return null;
            else
                return value;
        } catch (JaxenException je) {
            throw je;
        }
    }

    /**
     * @param selectExpr
     * @return
     * @throws JRException
     */
    public DomParsingXmlDataSource subDataSource(String selectExpr) throws JRException {
        Document doc = subDocument();
        return new DomParsingXmlDataSource(doc, selectExpr);
    }

    /**
     * @return
     * @throws JRException
     */
    public Document subDocument() throws JRException {
        if (currentNode == null) {
            throw new JRException("No node available. Iterate or rewind the data source.");
        }

        Document doc = JRXmlUtils.createDocument((org.w3c.dom.Node) currentNode);
        return doc;
    }

    /**
     * @param selectExpr
     * @return
     * @throws JRException
     */
    public DomParsingXmlDataSource dataSource(String selectExpr) throws JRException {
        return new DomParsingXmlDataSource(document, selectExpr);
    }
}
