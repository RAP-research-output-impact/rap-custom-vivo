package dk.dtu.adm.rap.utils;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by ted on 5/20/17.
 */
public class StoreUtils {

    private static final Log log = LogFactory.getLog(StoreUtils.class);
    private RDFService rdfService;
    private String namespace;

    public void setRdfService(String namespace, RDFService service) {
        this.rdfService = service;
        this.namespace = namespace;
    }



    public ArrayList<HashMap> getFromModel(String selectQuery, Model tmpModel) {
        ArrayList<HashMap> outRows = new ArrayList<HashMap>();
        try {
            QueryExecution qexec = QueryExecutionFactory.create(selectQuery, tmpModel);
            try {
                ResultSet results = qexec.execSelect();
                while ( results.hasNext() ) {
                    HashMap<String,String> thisItem = new HashMap();
                    QuerySolution soln = results.nextSolution();
                    for (String var: results.getResultVars()) {
                        //verify value exits
                        RDFNode val = soln.get(var);
                        if (val == null) {
                            continue;
                        }
                        // convert literals to lexical value
                        // convert uris to strings
                        if (val.isLiteral()) {
                            thisItem.put(var, val.asLiteral().getValue().toString());
                        } else {
                            thisItem.put(var, val.asResource().getURI());
                        }
                    }
                    //outItem = new JSONObject(thisItem);
                    outRows.add(thisItem);
                }
            } finally {
                qexec.close();
            }
        } catch (QueryParseException e) {
            log.warn(e);
            return outRows;
        }
        return outRows;
    }

    public ArrayList<HashMap> getFromStore(String selectQuery) {
        final ArrayList<HashMap> outRows = new ArrayList<HashMap>();
        try {
            ResultSet result = RDFServiceUtils.sparqlSelectQuery(selectQuery, this.rdfService);
            while(result.hasNext()) {
                HashMap thisItem = new HashMap();
                QuerySolution soln = result.nextSolution();
                Iterator iter = soln.varNames();
                while(iter.hasNext()) {
                    String name = (String)iter.next();
                    RDFNode val = soln.get(name);
                    if(val != null) {
                        if (val.isLiteral()) {
                            thisItem.put(name, val.asLiteral().getValue());
                        } else {
                            thisItem.put(name, val.asResource().getURI());
                        }
                    }
                }
                outRows.add(thisItem);
            }
        } catch (Throwable var10) {
            log.error(var10, var10);
        }
        return outRows;
    }

    public Model getModelFromStore(String constructQuery) {
        Model results = ModelFactory.createDefaultModel();
        try {
            rdfService.sparqlConstructQuery(constructQuery, results);
        } catch (RDFServiceException e) {
            e.printStackTrace();
        }
        return results;
    }

    public ArrayList getFromStoreJSON(String selectQuery) {
        final ArrayList<JSONObject> outRows = new ArrayList<JSONObject>();
        try {
            ResultSet result = RDFServiceUtils.sparqlSelectQuery(selectQuery, this.rdfService);
            while(result.hasNext()) {
                JSONObject thisItem = new JSONObject();
                QuerySolution soln = result.nextSolution();
                Iterator iter = soln.varNames();
                while(iter.hasNext()) {
                    String name = (String)iter.next();
                    RDFNode val = soln.get(name);
                    if(val != null) {
                        if (val.isLiteral()) {
                            thisItem.put(name, val.asLiteral().getValue());
                        } else {
                            thisItem.put(name, val.asResource().getURI());
                        }
                    }
                }
                outRows.add(thisItem);
            }
        } catch (Throwable var10) {
            log.error(var10, var10);
        }
        return outRows;
    }

    //setup query string here. only need to set prefixes once.
    public ParameterizedSparqlString getQuery(String raw) {
        ParameterizedSparqlString q2 = new ParameterizedSparqlString();
        q2.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        q2.setNsPrefix("vivo", "http://vivoweb.org/ontology/core#");
        q2.setNsPrefix("bibo", "http://purl.org/ontology/bibo/");
        q2.setNsPrefix("wos", "http://webofscience.com/ontology/wos#");
        q2.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        q2.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        q2.setNsPrefix("geo", "http://aims.fao.org/aos/geopolitical.owl#");
        q2.setNsPrefix("obo", "http://purl.obolibrary.org/obo/");
        q2.setNsPrefix("tmp", "http://localhost/tmp");
        q2.setNsPrefix("d", this.namespace);
        q2.setCommandText(raw);
        return q2;
    }

    public String getLocalName(String uri) {
        return uri.replace(this.namespace, "");
    }
}
