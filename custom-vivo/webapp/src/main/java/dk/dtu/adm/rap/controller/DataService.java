package dk.dtu.adm.rap.controller;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.vocabulary.XSD;
import dk.dtu.adm.rap.utils.StoreUtils;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.ArrayList;


@Path("/report/")
public class DataService {

    @Context
    private HttpServletRequest httpRequest;

    private static final Log log = LogFactory.getLog(DataService.class.getName());
    private static String namespace;
    private StoreUtils storeUtils;

    @Path("/org/{vid}")
    @GET
    @Produces("application/json")
    public Response getOrg(@PathParam("vid") String vid, @Context Request request) {
        VitroRequest vreq = new VitroRequest(httpRequest);

        if (!LoginStatusBean.getBean(vreq).isLoggedIn()) {
            return Response.status(403).type("text/plain").entity("Restricted to authenticated users").build();
        }

        ConfigurationProperties props = ConfigurationProperties.getBean(httpRequest);
        namespace = props.getProperty("Vitro.defaultNamespace");
        String wosDataVersion = props.getProperty("wos.dataVersion");
        Boolean cacheActive = Boolean.parseBoolean(props.getProperty("wos.cacheActive"));
        String uri = namespace + vid;

        //setup storeUtils
        this.storeUtils = new StoreUtils();
        this.storeUtils.setRdfService(namespace, vreq.getRDFService());

        ResponseBuilder builder = null;
        EntityTag etag = new EntityTag(wosDataVersion + uri);
        if (cacheActive.equals(true) && wosDataVersion != null) {
            log.info("Etag caching active");
            builder = request.evaluatePreconditions(etag);
        }

        // cached resource did change -> serve updated content
        if (builder == null) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("summary", getSummary(uri));
                jo.put("categories", getRelatedPubCategories(uri));
                jo.put("org_totals", getSummaryPubCount(uri));
                jo.put("top_categories", getTopCategories(uri));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String outJson = jo.toString();
            builder = Response.ok(outJson);
        }

        builder.tag(etag);
        return builder.build();

        //return Response.status(200).entity(jo.toString()).cacheControl(cc).build();
    }

    @Path("/worldmap")
    @GET
    @Produces("application/json")
    public Response getWorldMap(@Context Request request) {
        VitroRequest vreq = new VitroRequest(httpRequest);

        if (!LoginStatusBean.getBean(vreq).isLoggedIn()) {
            return Response.status(403).type("text/plain").entity("Restricted to authenticated users").build();
        }

        ConfigurationProperties props = ConfigurationProperties.getBean(httpRequest);
        namespace = props.getProperty("Vitro.defaultNamespace");
        String wosDataVersion = props.getProperty("wos.dataVersion");
        Boolean cacheActive = Boolean.parseBoolean(props.getProperty("wos.cacheActive"));

        //setup storeUtils
        this.storeUtils = new StoreUtils();
        this.storeUtils.setRdfService(namespace, vreq.getRDFService());

        ResponseBuilder builder = null;
        EntityTag etag = new EntityTag(wosDataVersion + "worldmap");
        if (cacheActive.equals(true) && wosDataVersion != null) {
            log.info("Etag caching active");
            builder = request.evaluatePreconditions(etag);
        }

        // cached resource did change -> serve updated content
        if (builder == null) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("summary", getWorldwidePubs());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String outJson = jo.toString();
            builder = Response.ok(outJson);
        }

        builder.tag(etag);
        return builder.build();
    }


    @Path("/country/{cCode}")
    @GET
    @Produces("application/json")
    public Response getCountry(@PathParam("cCode") String cCode, @Context Request request) {
        VitroRequest vreq = new VitroRequest(httpRequest);

        if (!LoginStatusBean.getBean(vreq).isLoggedIn()) {
            return Response.status(403).type("text/plain").entity("Restricted to authenticated users").build();
        }

        ConfigurationProperties props = ConfigurationProperties.getBean(httpRequest);
        namespace = props.getProperty("Vitro.defaultNamespace");
        String wosDataVersion = props.getProperty("wos.dataVersion");
        Boolean cacheActive = Boolean.parseBoolean(props.getProperty("wos.cacheActive"));

        //setup storeUtils
        this.storeUtils = new StoreUtils();
        this.storeUtils.setRdfService(namespace, vreq.getRDFService());

        ResponseBuilder builder = null;
        EntityTag etag = new EntityTag(wosDataVersion + "country" + cCode);
        if (cacheActive.equals(true) && wosDataVersion != null) {
            log.info("Etag caching active");
            builder = request.evaluatePreconditions(etag);
        }

        // cached resource did change -> serve updated content
        if (builder == null) {
            JSONObject jo = new JSONObject();
            try {
                jo.put("orgs", getCoPubsCountry(cCode.toUpperCase()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String outJson = jo.toString();
            builder = Response.ok(outJson);
        }

        builder.tag(etag);
        return builder.build();
    }

    private Object getSummary(String orgUri) {
        String rq = "SELECT \n" +
                "      ?name\n" +
                "      ?overview\n" +
                "      ?coPubTotal\n" +
                "      ?categories\n" +
                "      ?orgTotal\n" +
                "      ?orgCitesTotal\n" +
                "      ((?orgCitesTotal / ?orgTotal) as ?orgImpact)\n" +
                "      ?dtuTotal\n" +
                "      ?dtuCitesTotal\n" +
                "      ((?dtuCitesTotal / ?dtuTotal) as ?dtuImpact)\n" +
                "WHERE {\n" +
                "  ?org rdfs:label ?name .\n" +
                "  OPTIONAL {  ?org vivo:overview ?overview }\n" +
                "{\n" +
                "    SELECT (COUNT(?pub) as ?coPubTotal) " +
                "   WHERE {\n" +
                "    ?org a foaf:Organization ; \n" +
                "       vivo:relatedBy ?address .\n" +
                "   ?address a wos:Address ;\n" +
                "       vivo:relates ?pub .\n" +
                "   ?pub a wos:Publication .\n" +
                "   }\n" +
                "  }\n" +
                "  {\n" +
                "      SELECT (COUNT( DISTINCT ?cat) as ?categories) " +
                "     WHERE {\n" +
                "      ?org a foaf:Organization ;\n" +
                "          vivo:relatedBy ?address .\n" +
                "      ?address a wos:Address ;\n" +
                "         vivo:relates ?pub .\n" +
                "      ?pub a wos:Publication ;\n" +
                "           wos:hasCategory ?cat .\n" +
                "      }\n" +
                "  }\n" +
                "  {\n" +
                "      select (sum(?number) as ?orgTotal)\n" +
                "      where {\n" +
                "         ?tc a wos:InCitesPubPerYear ;\n" +
                "             wos:number ?number ;\n" +
                "             vivo:relatedBy ?org .\n" +
                "   \t  }\n" +
                "  }\n" +
                "  {\n" +
                "    select (sum(?number) as ?orgCitesTotal)\n" +
                "    where {\n" +
                "       ?tc a wos:InCitesCitesPerYear ;\n" +
                "           wos:number ?number ;\n" +
                "           vivo:relatedBy ?org .\n" +
                "   }\n" +
                "  }\n" +
                "  {\n" +
                "      select (sum(?number) as ?dtuTotal)\n" +
                "      where {\n" +
                "         ?tc a wos:InCitesPubPerYear ;\n" +
                "             wos:number ?number ;\n" +
                "             vivo:relatedBy d:org-technical-university-of-denmark .\n" +
                "   \t  }\n" +
                "  }\n" +
                "  {\n" +
                "    select (sum(?number) as ?dtuCitesTotal)\n" +
                "    where {\n" +
                "       ?tc a wos:InCitesCitesPerYear ;\n" +
                "           wos:number ?number ;\n" +
                "           vivo:relatedBy d:org-technical-university-of-denmark .\n" +
                "    }\n" +
                "  }\n" +
                "}";
        ParameterizedSparqlString q2 = this.storeUtils.getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Summary pub count query:\n" + query);
        ArrayList summary = this.storeUtils.getFromStoreJSON(query);
        return summary.get(0);
    }

    private ArrayList getSummaryPubCount(final String orgUri) {
        log.debug("Running summary pub count query");
        final ArrayList outArray = new ArrayList<String>();
        String rq = "" +
                "select ?number ?year\n" +
                "where {\n" +
                "   ?pc a wos:InCitesPubPerYear ;\n" +
                "       wos:number ?number ;\n" +
                "       wos:year ?year ;\n" +
                "       vivo:relatedBy ?org .\n" +
                "}\n" +
                "ORDER BY DESC(?year)";
        ParameterizedSparqlString q2 = this.storeUtils.getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Summary pub count query:\n" + query);
        return this.storeUtils.getFromStoreJSON(query);
    }

    private ArrayList getTopCategories(final String orgUri) {
        log.debug("Running top category query");
        String rq = "select ?cat ?name ?number\n" +
                "where {\n" +
                "  ?count a wos:InCitesTopCategory ;\n" +
                "         wos:number ?number ;\n" +
                "         vivo:relates ?org ;\n" +
                "         vivo:relates ?cat .\n" +
                "  ?cat a wos:Category ;\n" +
                "       rdfs:label ?name .\n" +
                "}\n" +
                "ORDER BY DESC(?number)";
        ParameterizedSparqlString q2 = this.storeUtils.getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Top category query:\n" + query);
        return this.storeUtils.getFromStoreJSON(query);
    }

    private ArrayList getRelatedPubCategories(String orgUri) {
        log.debug("Running org category query");
        String rq = "" +
                "SELECT ?category (SAMPLE(?label) as ?name) (COUNT(distinct ?pub) as ?number)\n" +
                "WHERE { \n" +
                "?org a foaf:Organization ; \n" +
                "       vivo:relatedBy ?address . \n" +
                "?address a wos:Address ; \n" +
                "       vivo:relates ?pub .\n" +
                "?pub a wos:Publication ; \n" +
                "    vivo:hasPublicationVenue ?venue ; \n" +
                "    wos:hasCategory ?category . \n" +
                "?category rdfs:label ?label . \n" +
                "}\n" +
                "GROUP BY ?category ?label\n" +
                "ORDER BY DESC(?number) ?label";
        ParameterizedSparqlString q2 = this.storeUtils.getQuery(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Related categories query:\n" + query);
        return this.storeUtils.getFromStoreJSON(query);
    }

    private ArrayList getWorldwidePubs() {
        log.debug("Querying ofr country codes for copublication");
        String rq = "select ?code (COUNT(DISTINCT ?pub) as ?publications)\n" +
                "    where {\n" +
                "        ?org a wos:UnifiedOrganization ;\n" +
                "            obo:RO_0001025 ?country ;\n" +
                "            vivo:relatedBy ?address .\n" +
                "      ?country a vivo:Country ;\n" +
                "               geo:codeISO3 ?code .\n" +
                "      ?address a wos:Address ;\n" +
                "      vivo:relates ?pub .\n" +
                "      ?pub a wos:Publication .\n" +
                "    FILTER (?org != d:org-technical-university-of-denmark)\n" +
                "    }\n" +
                "    GROUP by ?code\n" +
                "    ORDER BY DESC(?publications)" ;
        ParameterizedSparqlString q2 = this.storeUtils.getQuery(rq);
        String query = q2.toString();
        log.debug("Country pubs query:\n" + query);
        return this.storeUtils.getFromStoreJSON(query);
    }

    private ArrayList getCoPubsCountry(String countryCode) {
        log.debug("Running query to find copubs by country and org");
        String rq = "select ?org ?name (COUNT(DISTINCT ?pub) as ?publications)" +
                "where {\n" +
                "  ?org a wos:UnifiedOrganization ;\n" +
                "       rdfs:label ?name ;\n" +
                "       vivo:relatedBy ?address ;\n" +
                "       obo:RO_0001025 ?country .\n" +
                "  ?country a vivo:Country ;\n" +
                "           geo:codeISO3 ?countryCode^^<http://www.w3.org/2001/XMLSchema#string> .\n" +
                "  ?address a wos:Address ;\n" +
                "           vivo:relates ?pub .\n" +
                "  ?pub a wos:Publication .\n" +
                "  FILTER (?org != d:org-technical-university-of-denmark)\n" +
                "}\n" +
                "GROUP BY ?org ?name \n" +
                "ORDER BY DESC(?publications)";
        ParameterizedSparqlString q2 = this.storeUtils.getQuery(rq);
        q2.setLiteral("countryCode", countryCode);
        String query = q2.toString();
        log.debug("Related categories query:\n" + query);
        return this.storeUtils.getFromStoreJSON(query);
    }


}