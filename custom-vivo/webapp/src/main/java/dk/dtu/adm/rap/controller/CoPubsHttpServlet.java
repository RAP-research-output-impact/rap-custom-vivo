package dk.dtu.adm.rap.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.ParameterizedSparqlString;

import dk.dtu.adm.rap.utils.StoreUtils;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;

public abstract class CoPubsHttpServlet extends FreemarkerHttpServlet {

    private static final Log log = LogFactory.getLog(CoPubsHttpServlet.class);
    
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        ConfigurationProperties props = ConfigurationProperties.getBean(vreq);
        String namespace = props.getProperty("Vitro.defaultNamespace");
        //setup storeUtils
        StoreUtils storeUtils = new StoreUtils();
        storeUtils.setRdfService(namespace, vreq.getRDFService());

        String localName = null;
        String path = vreq.getPathInfo();
        if (path != null) {
            String[] pathParts = path.split("/");
            if (pathParts.length >= 1) {
                localName = pathParts[1];
            }
        }
        return processRequest(vreq, storeUtils, namespace, localName);
    }
    
    protected String getResourceName(String resourceUri, StoreUtils storeUtils) {
        if(resourceUri == null) {
            return null;
        }
        if(storeUtils == null) {
            throw new IllegalArgumentException("storeUtils must be supplied");
        }
        ParameterizedSparqlString rq = storeUtils.getQuery(
                "SELECT ?name WHERE { ?resource rdfs:label ?name }");
        rq.setIri("resource", resourceUri);
        ArrayList<HashMap> resultSet = storeUtils.getFromStore(
                rq.toString());
        if(resultSet.isEmpty()) {
            return null;
        } else {
            return resultSet.get(0).get("name").toString();    
        }        
    }
    
    public static String readQuery( String name ) {
        URL qurl = Resources.getResource(name);
        try {
            return Resources.toString(qurl, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    protected String getQuery(String raw, StoreUtils storeUtils) {
        ParameterizedSparqlString ps = storeUtils.getQuery(raw);
        return ps.toString();
    }
    
    protected Integer parseInt(String value) {
        if(value != null) {
            try {
                return Integer.parseInt(value, 10);
            } catch (NumberFormatException nfe) {
                log.trace(nfe, nfe);
                return null;
            }
        } else {
            return null;
        }
    }
    
    protected ParameterizedSparqlString addYearFilters(
            ParameterizedSparqlString ps, 
            Integer startYear, Integer endYear) {
        if(startYear == null) {
            startYear = 1;
        }
        if(endYear == null) {
            endYear = 9999;
        }
        ps.setLiteral("startYear", String.format("%04d", startYear) + "-01-01T00:00:00", 
                XSDDatatype.XSDdateTime);
        ps.setLiteral("endYear", String.format("%04d", endYear) + "-12-31T23:59:59", 
                XSDDatatype.XSDdateTime);
        return ps;
    }
    
    abstract ResponseValues processRequest(VitroRequest vreq, StoreUtils storeUtils, 
            String namespace, String localName);
        
}
