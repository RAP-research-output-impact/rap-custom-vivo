package dk.dtu.adm.rap.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
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
    
    abstract ResponseValues processRequest(VitroRequest vreq, StoreUtils storeUtils, 
            String namespace, String localName);
        
}
