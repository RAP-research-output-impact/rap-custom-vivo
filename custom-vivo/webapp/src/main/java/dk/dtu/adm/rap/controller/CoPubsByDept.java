package dk.dtu.adm.rap.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.store.Hash;
import dk.dtu.adm.rap.utils.StoreUtils;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import org.apache.axis.components.logger.LogFactory;
import org.apache.commons.logging.Log;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ted on 9/10/17.
 */
public class CoPubsByDept extends FreemarkerHttpServlet {
    private String TEMPLATE = "copubs-by-dept.ftl";
    private static final Log log = LogFactory.getLog(CoPubsByDept.class.getName());
    private static String namespace;
    private StoreUtils storeUtils;
    private static final String ORG_INFO_QUERY = "coPubByDept/info.rq";
    private static final String PUB_MODEL_QUERY = "coPubByDept/getModel.rq";
    private static final String PUB_META_QUERY = "coPubByDept/getPubs.rq";

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        ConfigurationProperties props = ConfigurationProperties.getBean(vreq);
        namespace = props.getProperty("Vitro.defaultNamespace");
        //setup storeUtils
        this.storeUtils = new StoreUtils();
        this.storeUtils.setRdfService(namespace, vreq.getRDFService());

        String localName = null;
        String path = vreq.getPathInfo();
        if (path != null) {
            String[] pathParts = path.split("/");
            if (pathParts.length >= 1) {
                localName = pathParts[1];
            }
        }
        String orgUri = namespace + localName;

        String collab = vreq.getParameter("collab");
        String collabUri = namespace + collab ;
        ParameterizedSparqlString collabRq = this.storeUtils.getQuery("SELECT ?name WHERE { ?collab rdfs:label ?name }");
        collabRq.setIri("collab", collabUri);
        ArrayList<HashMap> collabOrgInfo = this.storeUtils.getFromStore(collabRq.toString());
        String collabName = collabOrgInfo.get(0).get("name").toString();

        String getOrgs = readQuery(ORG_INFO_QUERY);
        ParameterizedSparqlString orgsRq = this.storeUtils.getQuery(getOrgs);
        orgsRq.setIri("dtuOrg", orgUri);
        String rq = orgsRq.toString();
        log.debug("Meta query:\n" + rq);
        ArrayList<HashMap> meta = this.storeUtils.getFromStore(rq);
        String preferredName = meta.get(0).get("name").toString();

        Model pubsModel = getPubModel(meta, collabUri);
        ArrayList<HashMap> pubs = getPubs(pubsModel);

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("mainOrg", preferredName);
        body.put("collabOrg", collabName);
        body.put("name", preferredName);
        body.put("pubs", pubs);
        return new TemplateResponseValues(TEMPLATE, body);
    }

    private Model getPubModel(ArrayList<HashMap> meta, String collabUri) {
        String rq = readQuery(PUB_MODEL_QUERY);

        String orgString = "";
        for (HashMap row: meta) {
            orgString += "<" + row.get("org") + "> ";
        }
        String fullQuery = rq.replace("orgList", orgString).replace("?collab", "<" + collabUri + ">");
        String processedQuery = getQuery(fullQuery);
        log.debug("Pubs query:\n " + processedQuery);
        return this.storeUtils.getModelFromStore(processedQuery);
    }

    private ArrayList<HashMap> getPubs(Model pubModel) {
        String rq = readQuery(PUB_META_QUERY);
        String processedRq =  getQuery(rq);
        log.debug("Pubs meta query:\n " + processedRq);
        return this.storeUtils.getFromModel(processedRq, pubModel);
    }

    private String getQuery(String raw) {
        ParameterizedSparqlString ps = this.storeUtils.getQuery(raw);
        return ps.toString();
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
}
