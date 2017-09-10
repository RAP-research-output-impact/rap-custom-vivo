package dk.dtu.adm.rap.controller;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Model;
import dk.dtu.adm.rap.utils.StoreUtils;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
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
import java.util.HashMap;


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
                jo.put("by_department", getCoPubsByDepartment(uri));
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


    @Path("/org/{vid}/by-dept")
    @GET
    @Produces("application/json")
    public Response getCoPubByDept(@PathParam("vid") String vid, @Context Request request) {
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
            Model tmpModel = deptModel(uri);
            String rq = getQuery("SELECT ?d ?name (COUNT(DISTINCT ?p) as ?num)\n" +
                    "WHERE {\n" +
                    "?d a tmp:Dtu ;\n" +
                    "tmp:name ?label ;\n" +
                    "tmp:related ?org .\n" +
                    "?org tmp:pub ?p .\n" +
                    "OPTIONAL {?d tmp:pname ?pName }\n" +
                    "BIND( if(bound(?pName),?pName,?label) as ?name )" +
                    "\n" +
                    "}\n" +
                    "GROUP BY ?d ?name\n" +
                    "ORDER BY DESC(?num) ?name");
            log.debug("Dept query:\n" + rq);
            ArrayList<HashMap> depts = this.storeUtils.getFromModel(rq, tmpModel);
            JSONArray out = new JSONArray();
            for (HashMap dept: depts) {
                String deptUri = dept.get("d").toString();
                String exOrgRq = "SELECT ?org ?name (COUNT(DISTINCT ?p) as ?num)\n" +
                        "WHERE {\n" +
                        "?org a tmp:Ext ;\n" +
                        "tmp:name ?name ;\n" +
                        "tmp:pub ?p .\n" +
                        "?d a tmp:Dtu ;\n" +
                        "tmp:related ?org .\n" +
                        "\n" +
                        "}\n" +
                        "GROUP BY ?org ?name\n" +
                        "ORDER BY DESC(?num) ?name";
                ParameterizedSparqlString q2 = this.storeUtils.getQuery(exOrgRq);
                q2.setIri("d", deptUri);
                String exOrgQuery = q2.toString();
                log.debug("External Dept query:\n" + exOrgQuery);
                ArrayList<HashMap> subOrgs = null;
                try {
                    subOrgs = this.storeUtils.getFromModelJSON(exOrgQuery, tmpModel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dept.put("sub_orgs", new JSONArray(subOrgs));
                out.put(dept);
            }
            JSONObject jo = new JSONObject();
            try {
                jo.put("summary", getSummary(uri));
                jo.put("departments", out);
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
                "    SELECT (COUNT(DISTINCT ?pub) as ?coPubTotal) " +
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
        final ArrayList<String> outArray = new ArrayList<String>();
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

    private ArrayList getCoPubsByDepartment(String orgUri) {
        log.debug("Running copub by department query");
        String rq = "" +
                "SELECT DISTINCT ?dtuSubOrg ?dtuSubOrgName ?otherOrgs (COUNT(DISTINCT ?pub) as ?number)\n" +
                "WHERE {\n" +
                "?pub a wos:Publication ;\n" +
                "\t\tvivo:relatedBy ?dtuAddress .\n" +
                "\t?dtuAddress a wos:Address ;\n" +
                "\t\tvivo:relates ?dtuSubOrg, d:org-technical-university-of-denmark.\n" +
                "\t?dtuSubOrg a wos:SubOrganization ;\n" +
                "\t\twos:subOrganizationName ?dtuSubOrgName .\n" +
                "\t{\n" +
                "\t\tselect ?pub  (group_concat(distinct ?subOrgName ; separator = \", \") as ?otherOrgs)\n" +
                "\t\twhere {\n" +
                "\t\t\t?org a foaf:Organization ;\n" +
                "\t\t\t\tvivo:relatedBy ?address .\n" +
                "\t\t\t?address a wos:Address ;\n" +
                "\t\t\t\tvivo:relates ?subOrg .\n" +
                "\t\t\t?subOrg a wos:SubOrganization ;\n" +
                "\t\t\t\twos:subOrganizationName ?subOrgName .\n" +
                "\t\t\t?pub a wos:Publication ;\n" +
                "\t\t\t\tvivo:relatedBy ?address, ?dtuAddress .\n" +
                "\t\t}\n" +
                "\t\tGROUP BY ?pub\n" +
                "\t}\n" +
                "\t}\n" +
                "\tGROUP BY ?dtuSubOrg ?dtuSubOrgName ?otherOrgs\n" +
                "\tORDER BY ?dtuSubOrgName ?number\n";
        ParameterizedSparqlString q2 = this.storeUtils.getQuery(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Related categories query:\n" + query);
        return this.storeUtils.getFromStoreJSON(query);
    }

    private ArrayList getWorldwidePubs() {
        log.debug("Querying for country codes for copublication");
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


    private Model deptModel(String externalOrgUri) {
        String rq = "CONSTRUCT {\n" +
                "?dtuSubOrg a tmp:Dtu ;\n" +
                "tmp:name ?dtuSubOrgName ;\n" +
                "tmp:pname ?pName ;\n" +
                "tmp:related ?externalSubOrg .\n" +
                "?externalSubOrg a tmp:Ext ;\n" +
                "tmp:name ?externalSubOrgName ;\n" +
                "tmp:pub ?pub ;\n" +
                "tmp:related ?dtuSubOrg .\n" +
                "}\n" +
                "WHERE {\n" +
                "?pub a wos:Publication ;\n" +
                "vivo:relatedBy ?dtuAddress, ?otherAddress .\n" +
                "?dtuAddress a wos:Address ;\n" +
                "vivo:relates ?dtuSubOrg, d:org-technical-university-of-denmark .\n" +
                "?otherAddress a wos:Address ;\n" +
                "vivo:relates ?externalSubOrg, ?uOrg .\n" +
                "?uOrg a wos:UnifiedOrganization ;\n" +
                "rdfs:label ?unifiedName .\n" +
                "?dtuSubOrg a wos:SubOrganization ;\n" +
                "vivo:relatedBy ?dtuAddress  ;\n" +
                "wos:subOrganizationName ?dtuSubOrgName .\n" +
                "?externalSubOrg a wos:SubOrganization ;\n" +
                "vivo:relatedBy ?otherAddress ;\n" +
                "wos:subOrganizationName ?externalSubOrgName \n." +
                "OPTIONAL { ?dtuSubOrg wos:preferredName ?pName }" +
                "}";
        ParameterizedSparqlString query = this.storeUtils.getQuery(rq);
        query.setIri("?uOrg", externalOrgUri);
        String raw = query.toString();
        log.debug("Dept Model query:\n" + rq);
        return this.storeUtils.getModelFromStore(raw);
    }

    private String getQuery(String raw) {
        ParameterizedSparqlString ps = this.storeUtils.getQuery(raw);
        return ps.toString();
    }


}
