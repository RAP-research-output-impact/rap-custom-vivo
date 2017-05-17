package dk.dtu.adm.rap.controller;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
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
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.ArrayList;
import java.util.HashMap;


@Path("/report/")
public class DataService {

    @Context
    private HttpServletRequest httpRequest;

    private static final Log log = LogFactory.getLog(DataService.class.getName());
    private static String namespace;

    @Path("/org/{vid}")
    @GET
    @Produces("application/json")
    public Response getOrg(@PathParam("vid") String vid, @Context Request request) {
        VitroRequest vreq = new VitroRequest(httpRequest);
        ConfigurationProperties props = ConfigurationProperties.getBean(httpRequest);
        namespace = props.getProperty("Vitro.defaultNamespace");
        String wosDataVersion = props.getProperty("wos.dataVersion");
        Boolean cacheActive = Boolean.parseBoolean(props.getProperty("wos.cacheActive"));
        String uri = namespace + vid;

        if(!LoginStatusBean.getBean(vreq).isLoggedIn()) {
            return Response.status(403).type("text/plain").entity("Restricted to authenticated users").build();
        }

        ResponseBuilder builder = null;
        EntityTag etag = new EntityTag(wosDataVersion + uri);
        if (cacheActive.equals(true) && wosDataVersion != null) {
            log.info("Etag caching active");
            builder = request.evaluatePreconditions(etag);
        }

        // cached resource did change -> serve updated content
        if( builder == null){
            JSONObject jo = new JSONObject(getOrgInfo(uri, vreq));
            ArrayList coPubs = getRelatedPubs(uri, vreq);
            Integer orgPubCount = getOrgPubCount(uri, vreq);
            ArrayList categories = getRelatedPubCategories(uri, vreq);
            ArrayList orgTotal = getSummaryPubCount(uri, vreq);
            Integer totalDTUPubs = getTotalDTUPubs(vreq);
            Integer totalDTUCites = getTotalDTUCites(vreq);
            Integer totalCites = getTotalCites(uri, vreq);
            ArrayList topCategories = getTopCategories(uri, vreq);

            try {
                jo.put("co_pubs", coPubs.size());
                jo.put("total_pubs", orgPubCount);
                jo.put("total_dtu_pubs", totalDTUPubs);
                jo.put("total_dtu_cites", totalDTUCites);
                //jo.put("pubs", new JSONArray(pubs));
                jo.put("categories", new JSONArray(categories));
                jo.put("org_totals", new JSONArray(orgTotal));
                jo.put("total_cites", totalCites);
                jo.put("top_categories", topCategories);
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

    /*  org info */
    @SuppressWarnings("unchecked")
    private static HashMap getOrgInfo(final String orgUri, VitroRequest vreq) {
        log.debug("Running org query");
        final HashMap info = new HashMap();
        String rq = "" +
                "SELECT ?name ?overview \n" +
                "WHERE { \n" +
                "   ?org a foaf:Organization ; \n" +
                "       rdfs:label ?name . \n" +
                "OPTIONAL { ?org vivo:overview ?overview } \n" +
                "}";
        ParameterizedSparqlString q2 = getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Recent query:\n" + query);
        try {
            vreq.getRDFService().sparqlSelectQuery(query, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {

                    Literal name = qs.getLiteral("name");
                    Literal overview = qs.getLiteral("overview");
                    info.put("name", name);
                    info.put("uri", orgUri);
                    info.put("url", UrlBuilder.getHomeUrl() + "/individual?uri=" + orgUri);
                    info.put("overview", overview);

                }
            });
        } catch (RDFServiceException e) {
            e.printStackTrace();
        }
        return info;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList getSummaryPubCount(final String orgUri, VitroRequest vreq) {
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
        ParameterizedSparqlString q2 = getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Summary pub count query:\n" + query);
        try {
            vreq.getRDFService().sparqlSelectQuery(query, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {

                    JSONObject ot = new JSONObject();
                    try {
                        ot.put("count", qs.getLiteral("number").getValue());
                        ot.put("year", qs.getLiteral("year").getValue());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    outArray.add(ot);

                }
            });
        } catch (RDFServiceException e) {
            e.printStackTrace();
        }
        return outArray;
    }

    @SuppressWarnings("unchecked")
    private static Integer getOrgPubCount(final String orgUri, VitroRequest vreq) {
        log.debug("Running summary pub count query");
        final int[] res = new int[1];
        String rq = "" +
                "select (SUM(?number) as ?total)\n" +
                "where {\n" +
                "   ?pc a wos:InCitesPubPerYear ;\n" +
                "       wos:number ?number ;\n" +
                "       vivo:relatedBy ?org .\n" +
                "}\n" ;
        ParameterizedSparqlString q2 = getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Summary pub count query:\n" + query);
        try {
            vreq.getRDFService().sparqlSelectQuery(query, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    Literal result = qs.getLiteral("total");
                    if (result == null) {
                        res[0] = 0;
                    } else {
                        res[0] = result.getInt();
                    }
                }
            });
        } catch (RDFServiceException e) {
            e.printStackTrace();
        }
        return res[0];
    }

    @SuppressWarnings("unchecked")
    private static Integer getTotalDTUPubs(VitroRequest vreq) {
        final int[] res = new int[1];
        String rq = "select (sum(?number) as ?total)\n" +
                "where {\n" +
                "  ?tc a wos:InCitesPubPerYear ;\n" +
                "      wos:number ?number ;\n" +
                "      vivo:relatedBy d:org-technical-university-of-denmark .\n" +
                "}";
        ParameterizedSparqlString q2 = getQuery(rq);
        q2.setCommandText(rq);
        String query = q2.toString();
        log.debug("Get total DTU pubs query:\n" + query);
        try {
            vreq.getRDFService().sparqlSelectQuery(query, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    Literal result = qs.getLiteral("total");
                    if (result == null) {
                        res[0] = 0;
                    } else {
                        res[0] = result.getInt();
                    }
                }
            });
        } catch (RDFServiceException e) {
            e.printStackTrace();
        }
        return res[0];
    }

    @SuppressWarnings("unchecked")
    private static Integer getTotalDTUCites(VitroRequest vreq) {
        final int[] res = new int[1];
        String rq = "select (sum(?number) as ?total)\n" +
                "where {\n" +
                "  ?tc a wos:InCitesCitesPerYear ;\n" +
                "      wos:number ?number ;\n" +
                "      vivo:relatedBy d:org-technical-university-of-denmark .\n" +
                "}";
        ParameterizedSparqlString q2 = getQuery(rq);
        q2.setCommandText(rq);
        String query = q2.toString();
        log.debug("Get total DTU cites query:\n" + query);
        try {
            vreq.getRDFService().sparqlSelectQuery(query, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    Literal result = qs.getLiteral("total");
                    if (result == null) {
                        res[0] = 0;
                    } else {
                        res[0] = result.getInt();
                    }
                }
            });
        } catch (RDFServiceException e) {
            e.printStackTrace();
        }
        return res[0];
    }

    private static Integer getTotalCites(String orgUri, VitroRequest vreq) {
        final int[] res = new int[1];
        String rq = "select (SUM(?number) as ?total) \n" +
                "where {\n" +
                "   {\n" +
                "    ?tc a wos:InCitesCitesPerYear ;\n" +
                "       wos:number ?number ;\n" +
                "       wos:year ?year ;\n" +
                "       vivo:relatedBy ?org .\n" +
                "  }\n" +
                "}";
        ParameterizedSparqlString q2 = getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Get total cites query:\n" + query);
        try {
            vreq.getRDFService().sparqlSelectQuery(query, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    Literal result = qs.getLiteral("total");
                    if (result == null) {
                        res[0] = 0;
                    } else {
                        res[0] = result.getInt();
                    }
                }
            });
        } catch (RDFServiceException e) {
            e.printStackTrace();
        }
        return res[0];
    }

    @SuppressWarnings("unchecked")
    private static ArrayList getTopCategories(final String orgUri, VitroRequest vreq) {
        log.debug("Running top category query");
        final ArrayList outArray = new ArrayList<String>();
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
        ParameterizedSparqlString q2 = getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Top category query:\n" + query);
        try {
            vreq.getRDFService().sparqlSelectQuery(query, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {

                    JSONObject ot = new JSONObject();
                    if (qs.get("cat") != null) {
                        try {
                            ot.put("count", qs.getLiteral("number").getValue());
                            ot.put("category", qs.getLiteral("name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        outArray.add(ot);
                    }

                }
            });
        } catch (RDFServiceException e) {
            e.printStackTrace();
        }
        return outArray;
    }



    @SuppressWarnings("unchecked")
    private static ArrayList getRelatedPubs(String orgUri, VitroRequest vreq) {
        log.debug("Running org query");
        final ArrayList members = new ArrayList<String>();
        String rq = "" +
                "SELECT DISTINCT ?pub \n" +
                "WHERE { \n" +
                "?org a foaf:Organization ; \n" +
                "       vivo:relatedBy ?address . \n" +
                "?address a wos:Address ; \n" +
                "       vivo:relatedBy ?authorship .\n" +
                "?authorship a vivo:Authorship ;\n" +
                "           vivo:relates ?pub . \n" +
                "?pub a wos:Publication . \n" +
                "}";
        ParameterizedSparqlString q2 = getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Get related pubs query:\n" + query);
        try {
            vreq.getRDFService().sparqlSelectQuery(query, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {

                    Resource pub = qs.getResource("pub");
                    members.add(pub.toString());

                }
            });
        } catch (RDFServiceException e) {
            e.printStackTrace();
        }
        return members;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList getCollaboratingOrgs(String orgUri, VitroRequest vreq) {
        log.debug("Running collaborrating org query");
        final ArrayList outArray = new ArrayList<String>();
        String rq = "select ?org ?orgName (COUNT(?pub) as ?pubs)\n" +
                "where {\n" +
                "   ?pub a wos:Publication ;\n" +
                "        vivo:relatedBy ?authorship, ?authorship2 .\n" +
                "  ?focusOrg a wos:UnifiedOrganization ;\n" +
                "       vivo:relatedBy ?address .\n" +
                "  ?address a wos:Address ;\n" +
                "          vivo:relatedBy ?authorship .\n" +
                "  ?authorship a vivo:Authorship ;\n" +
                "        vivo:relates ?pub .\n" +
                "  ?org a wos:UnifiedOrganization ;\n" +
                "       rdfs:label ?orgName ;\n" +
                "       vivo:relatedBy ?address2 .\n" +
                "  ?address2 a wos:Address ;\n" +
                "          vivo:relatedBy ?authorship2 .\n" +
                "  ?authorship2 a vivo:Authorship ;\n" +
                "          vivo:relates ?pub .\n" +
                "  FILTER (?focusOrg != ?org)\n" +
                "  FILTER (?org != d:org-technical-university-of-denmark)\n" +
                //"  FILTER (!REGEX(?orgName, \".*system\", \"i\"))\n" +
                "}\n" +
                "GROUP BY ?org ?orgName\n" +
                "ORDER BY DESC(?pubs)" ;
        if (orgUri.endsWith("org-technical-university-of-denmark")) {
            rq = rq + "\nLIMIT 100";
        }
        ParameterizedSparqlString q2 = getQuery(rq);
        q2.setIri("focusOrg", orgUri);
        String query = q2.toString();
        log.info("Collaborating orgs query:\n" + query);
        try {
            vreq.getRDFService().sparqlSelectQuery(query, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (qs.get("org") != null) {
                        String uri = qs.getResource("org").toString();
                        JSONObject ot = new JSONObject();
                        try {
                            ot.put("publications", qs.getLiteral("pubs").getValue());
                            ot.put("name", qs.getLiteral("orgName"));
                            ot.put("uri", uri);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        outArray.add(ot);
                    }

                }
            });
        } catch (RDFServiceException e) {
            e.printStackTrace();
        }
        return outArray;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList getRelatedPubCategories(String orgUri, VitroRequest vreq) {
        log.debug("Running org category query");
        final ArrayList outArray = new ArrayList<String>();
        String rq = "" +
                "SELECT ?category (SAMPLE(?label) as ?name) (COUNT(distinct ?pub) as ?count)\n" +
                "WHERE { \n" +
                "?org a foaf:Organization ; \n" +
                "       vivo:relatedBy ?address . \n" +
                "?address a wos:Address ; \n" +
                "       vivo:relatedBy ?authorship .\n" +
                "?authorship a vivo:Authorship ;\n" +
                "           vivo:relates ?pub . \n" +
                "?pub a wos:Publication ; \n" +
                "    vivo:hasPublicationVenue ?venue . \n" +
                "?venue wos:hasCategory ?category . \n" +
                "?category rdfs:label ?label . \n" +
                "}\n" +
                "GROUP BY ?category ?label\n" +
                "ORDER BY DESC(?count) ?label";
        ParameterizedSparqlString q2 = getQuery(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Releated categories query:\n" + query);
        try {
            vreq.getRDFService().sparqlSelectQuery(query, new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    if (qs.get("category") != null) {
                        String uri = qs.getResource("category").toString();
                        JSONObject ot = new JSONObject();
                        try {
                            ot.put("count", qs.getLiteral("count").getValue());
                            ot.put("category", qs.getLiteral("name"));
                            ot.put("uri", uri);
                            //ot.put("local_name", getLocalName(uri));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        outArray.add(ot);
                    }

                }
            });
        } catch (RDFServiceException e) {
            e.printStackTrace();
        }
        return outArray;
    }

    //setup query string here. only need to set prefixes once.
    private static ParameterizedSparqlString getQuery(String raw) {
        ParameterizedSparqlString q2 = new ParameterizedSparqlString();
        q2.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        q2.setNsPrefix("vivo", "http://vivoweb.org/ontology/core#");
        q2.setNsPrefix("wos", "http://webofscience.com/ontology/wos#");
        q2.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        q2.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        q2.setNsPrefix("d", namespace);
        q2.setCommandText(raw);
        return q2;
    }

    private static String getLocalName(String uri) {
        return uri.replace(namespace, "");
    }

}