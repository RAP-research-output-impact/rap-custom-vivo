package dk.dtu.adm.rap.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Model;

import dk.dtu.adm.rap.utils.StoreUtils;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

public class CoPubsByDtuResearcher extends CoPubsHttpServlet {

    private static final long serialVersionUID = 1L;
    private String TEMPLATE = "copubs-by-dtu-researcher.ftl";
    private static final String PUB_MODEL_QUERY = "coPubByDtuResearcher/getModel.rq";
    private static final String PUB_MODEL_QUERY_WITH_PARTNER_RESEARCHER = 
            "coPubByDtuResearcher/getModelWithPartnerResearcher.rq";
    private static final String PUB_QUERY = "coPubByDtuResearcher/getPubs.rq";
    private final static Log log = LogFactory.getLog(CoPubsByCategory.class);

    @Override
    ResponseValues processRequest(VitroRequest vreq, StoreUtils storeUtils,
            String namespace, String localName) {
        String dtuResearcherUri = namespace + localName;
        String collab = vreq.getParameter("collab");
        String collabUri = namespace + collab ;
        String partnerResearcherUri = vreq.getParameter("partnerResearcherUri");
        Map<String, Object> body = new HashMap<String, Object>();
        Integer startYear = parseInt(vreq.getParameter("startYear"));
        Integer endYear = parseInt(vreq.getParameter("endYear"));
        Model pubsModel = getPubModel(collabUri, dtuResearcherUri, partnerResearcherUri, 
                storeUtils, startYear, endYear);
        log.info("Pubs model has " + pubsModel.size() + " statements");
        ArrayList<HashMap> pubs = getPubs(pubsModel, storeUtils);
        body.put("name", getResourceName(dtuResearcherUri, storeUtils));
        if(partnerResearcherUri != null) {
            body.put("partnerResearcherName", getResourceName(
                    partnerResearcherUri, storeUtils));
        }
        body.put("collabOrg", getResourceName(collabUri, storeUtils));
        body.put("pubs", pubs);
        return new TemplateResponseValues(TEMPLATE, body);
    }
    
    private Model getPubModel(String collabUri, String dtuResearcherUri, 
            String partnerResearcherUri, StoreUtils storeUtils, 
            Integer startYear, Integer endYear) {
        String rq = (partnerResearcherUri == null) ? readQuery(PUB_MODEL_QUERY) 
                : readQuery(PUB_MODEL_QUERY_WITH_PARTNER_RESEARCHER);
        ParameterizedSparqlString prq = storeUtils.getQuery(rq);
        prq = addYearFilters(prq, startYear, endYear);
        prq.setIri("dtuResearcher", dtuResearcherUri);
        prq.setIri("collab", collabUri);
        if(partnerResearcherUri != null) {
            prq.setIri("partnerResearcher", partnerResearcherUri);
        }
        log.info("Pub model query:\n " + prq.toString());
        return storeUtils.getModelFromStore(prq.toString());
    }
    
    private ArrayList<HashMap> getPubs(Model pubModel, StoreUtils storeUtils) {
        String rq = readQuery(PUB_QUERY);
        String processedRq =  getQuery(rq, storeUtils);
        log.info("Pubs query:\n " + processedRq);
        ArrayList<HashMap> out = new ArrayList<HashMap>();
        for (HashMap pub: storeUtils.getFromModel(processedRq, pubModel)) {
            out.add(pub);
        }
        return out;
    }

}
