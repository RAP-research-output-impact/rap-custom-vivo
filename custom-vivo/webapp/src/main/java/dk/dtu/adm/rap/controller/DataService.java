package dk.dtu.adm.rap.controller;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Model;
import dk.dtu.adm.rap.utils.StoreUtils;
import dk.dtu.adm.rap.utils.DataCache;
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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Path("/report/")
public class DataService {

    @Context
    private HttpServletRequest httpRequest;

    private static final Log log = LogFactory.getLog(DataService.class.getName());
    private static String namespace;
    private StoreUtils storeUtils;
    private static final DataCache cache = new DataCache();

    @Path("/org/{vid}")
    @GET
    @Produces("application/json")
    public Response getOrg(@PathParam("vid") String vid, @Context Request request) {
        return getOrg(vid, null, null, request);
    }

    @Path("/org/{vid}/{startYear}")
    @GET
    @Produces("application/json")
    public Response getOrg(@PathParam("vid") String vid,
            @PathParam("startYear") String startYear,
            @Context Request request) {
        return getOrg(vid, startYear, null, request);
    }

    @Path("/org/{vid}/{startYear}/{endYear}")
    @GET
    @Produces("application/json")
    public Response getOrg(@PathParam("vid") String vid,
            @PathParam("startYear") String startYear,
            @PathParam("endYear") String endYear,
            @Context Request request) {

        VitroRequest vreq = new VitroRequest(httpRequest);

        Integer startYearInt = parseInt(startYear);
        Integer endYearInt = parseInt(endYear);

        if (!authorized(vreq)) {
            return Response.status(403).type("text/plain").entity("Restricted to authenticated users").build();
        }
        ConfigurationProperties props = ConfigurationProperties.getBean(httpRequest);
        String cacheRoot = props.getProperty("DataCache.root");
        if (startYearInt == null) {
            startYearInt = parseInt(props.getProperty("DataCache.year.start"));
        }
        if (endYearInt == null) {
            endYearInt = parseInt(props.getProperty("DataCache.year.end"));
        }
        String cachekey = "org-" + vid + "-" + Integer.toString(startYearInt) + "-" + Integer.toString(endYearInt);
        String data = cache.read(cacheRoot, cachekey);
        if (data == null) {
            Connection mysql = sql_setup(props);
            HashMap<String, Integer> orgmap = organisations(mysql);
            long start = System.currentTimeMillis();
            JSONObject jo = new JSONObject();
            try {
                namespace = props.getProperty("Vitro.defaultNamespace");
                String uri = namespace + vid;
                this.storeUtils = new StoreUtils();
                this.storeUtils.setRdfService(namespace, vreq.getRDFService());
                jo.put("summary",    getSummary(mysql, orgmap, uri, startYearInt, endYearInt));
                jo.put("categories", getRelatedPubCategories(mysql, orgmap, namespace, uri, startYearInt, endYearInt));
                jo.put("org_totals", getSummaryPubCount(mysql, orgmap, vid, startYearInt, endYearInt));
                jo.put("dtu_totals", getSummaryPubCount(mysql, orgmap, "org-technical-university-of-denmark", startYearInt, endYearInt));
                jo.put("copub_totals", getSummaryCopubCount(uri, startYearInt, endYearInt));
                jo.put("top_categories", getTopCategories(mysql, orgmap, namespace, vid, startYearInt, endYearInt));
                jo.put("by_department", getCoPubsByDepartment(uri, startYearInt, endYearInt));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            data = jo.toString();
            cache.write(cacheRoot, cachekey, data, (System.currentTimeMillis() - start));
        }
        ResponseBuilder builder = Response.ok(data);
        return builder.build();
    }

    private Integer parseInt(String value) {
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

    @Path("/org/{vid}/by-dept/")
    @GET
    @Produces("application/json")
    public Response getCoPubByDept(@PathParam("vid") String vid,
            @Context Request request) {
        return getCoPubByDept(vid, null, null, request);
    }

    @Path("/org/{vid}/by-dept/{startYear}")
    @GET
    @Produces("application/json")
    public Response getCoPubByDept(@PathParam("vid") String vid,
            @PathParam("startYear") String startYear,
            @Context Request request) {
        return getCoPubByDept(vid, startYear, null, request);
    }

    @Path("/org/{vid}/by-dept/{startYear}/{endYear}")
    @GET
    @Produces("application/json")
    public Response getCoPubByDept(@PathParam("vid") String vid,
            @PathParam("startYear") String startYear,
            @PathParam("endYear") String endYear,
            @Context Request request) {
        VitroRequest vreq = new VitroRequest(httpRequest);

        Integer startYearInt = parseInt(startYear);
        Integer endYearInt = parseInt(endYear);

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
        String orgName = this.storeUtils.getFromStore(getQuery("SELECT ?name where { <" + uri + "> rdfs:label ?name }")).get(0).get("name").toString();

        // cached resource did change -> serve updated content
        if (builder == null) {
            Model tmpModel = deptModel(uri, startYearInt, endYearInt);
            String rq = readQuery("coPubByDept/vds/dtuSubOrgCount.rq");
            log.debug("Dept query:\n" + rq);
            ArrayList<HashMap> depts = this.storeUtils.getFromModel(getQuery(rq), tmpModel);
            JSONArray out = new JSONArray();
            for (HashMap dept: depts) {
                if ( dept.get("org") != null ) {
                    String deptUri = dept.get("org").toString();
                    String exOrgRq = getQuery(readQuery("coPubByDept/vds/externalSubOrgCount.rq"));
                    ParameterizedSparqlString q2 = this.storeUtils.getQuery(exOrgRq);
                    q2.setIri("org", deptUri);
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
            }
            JSONObject jo = new JSONObject();
            try {
                //jo.put("summary", getSummary(uri));
                jo.put("name", orgName);
                jo.put("departments", out);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String outJson = jo.toString();
            builder = Response.ok(outJson);
        }

        builder.tag(etag);
        return builder.build();
    }

    private boolean authorized(VitroRequest vreq) {
        if (LoginStatusBean.getBean(vreq).isLoggedIn()) {
            return true;
        }
        String addr = httpRequest.getRemoteAddr();
        if (addr.equals("127.0.0.1")) {
            return true;
        }
        if (addr.equals("::1")) {
            return true;
        }
        return false;
    }

    @Path("/worldmap")
    @GET
    @Produces("application/json")
    public Response getWorldMap(@Context Request request) {
        VitroRequest vreq = new VitroRequest(httpRequest);
        if (!authorized(vreq)) {
            return Response.status(403).type("text/plain").entity("Restricted to authenticated users").build();
        }
        ConfigurationProperties props = ConfigurationProperties.getBean(httpRequest);
        String cacheRoot = props.getProperty("DataCache.root");
        String data = cache.read(cacheRoot, "worldmap");
        if (data == null) {
            long start = System.currentTimeMillis();
            JSONObject jo = new JSONObject();
            try {
                namespace = props.getProperty("Vitro.defaultNamespace");
                this.storeUtils = new StoreUtils();
                this.storeUtils.setRdfService(namespace, vreq.getRDFService());
                jo.put("summary", getWorldwidePubs());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            data = jo.toString();
            cache.write(cacheRoot, "worldmap", data, (System.currentTimeMillis() - start));
        }
        ResponseBuilder builder = Response.ok(data);
        return builder.build();
    }

    @Path("/country/{cCode}")
    @GET
    @Produces("application/json")
    public Response getCountry(@PathParam("cCode") String cCode, @Context Request request) {
        VitroRequest vreq = new VitroRequest(httpRequest);
        if (!authorized(vreq)) {
            return Response.status(403).type("text/plain").entity("Restricted to authenticated users").build();
        }
        ConfigurationProperties props = ConfigurationProperties.getBean(httpRequest);
        String cacheRoot = props.getProperty("DataCache.root");
        String data = cache.read(cacheRoot, "country-" + cCode);
        if (data == null) {
            long start = System.currentTimeMillis();
            JSONObject jo = new JSONObject();
            try {
                namespace = props.getProperty("Vitro.defaultNamespace");
                this.storeUtils = new StoreUtils();
                this.storeUtils.setRdfService(namespace, vreq.getRDFService());
                jo.put("orgs", getCoPubsCountry(cCode.toUpperCase()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            data = jo.toString();
            cache.write(cacheRoot, "country-" + cCode, data, (System.currentTimeMillis() - start));
        }
        ResponseBuilder builder = Response.ok(data);
        return builder.build();
    }

    private String getYearFilter(Integer startYear, Integer endYear) {
        String yearFilter = "";
        if(startYear != null) {
            yearFilter += "   FILTER(" + startYear + " <= ?year) \n";
        }
        if(endYear != null) {
            yearFilter += "   FILTER(" + endYear + " >= ?year) \n";
        }
        return yearFilter;
    }

    private String getDtvFilter(Integer startYear, Integer endYear) {
        String yearFilter = "";
        if(startYear != null) {
            yearFilter += "   FILTER(\"" + startYear
                    + "-01-01T00:00:00\"^^xsd:dateTime <= ?dateTime) \n";
        }
        if(endYear != null) {
            yearFilter += "   FILTER(\"" + endYear
                    + "-12-31T23:59:59\"^^xsd:dateTime >= ?dateTime) \n";
        }
        return yearFilter;
    }

    private String getYearDtv(Integer startYear, Integer endYear) {
        String yearDtv = "";
        if(startYear != null || endYear != null) {
            yearDtv = "   ?pub vivo:dateTimeValue ?dtv . \n" +
                      "   ?dtv vivo:dateTime ?dateTime .\n ";
        }
        return yearDtv;
    }

    private Object getSummary(Connection mysql, HashMap<String, Integer> orgmap, String orgUri, Integer startYear, Integer endYear) {
        log.debug("getSummary: " + orgUri);
        String yearDtv = getYearDtv(startYear, endYear);
        String dtvFilter = getDtvFilter(startYear, endYear);
        String rq = "SELECT \n" +
                    "    ?name\n" +
                    "    ?country\n" +
                    "    ?coPubTotal\n" +
                    "    ?categories\n" +
                    "WHERE {\n" +
                    "    ?org rdfs:label ?name .\n" +
                    "    OPTIONAL { ?org obo:RO_0001025 ?countryUri .\n" +
                    "               ?countryUri rdfs:label ?country " +
                    "    }\n" +
                    "    {\n" +
                    "        SELECT (COUNT(DISTINCT ?pub) as ?coPubTotal) " +
                    "        WHERE {\n" +
                    "            ?org a foaf:Organization ; \n" +
                    "            vivo:relatedBy ?address .\n" +
                    "            ?address a wos:Address ;\n" +
                    "            vivo:relates ?pub .\n" +
                    "            ?pub a wos:Publication .\n" +
                                 yearDtv +
                                 dtvFilter +
                    "        }\n" +
                    "    }\n" +
                    "    {\n" +
                    "        SELECT (COUNT( DISTINCT ?cat) as ?categories) " +
                    "        WHERE {\n" +
                    "            ?org a foaf:Organization ;\n" +
                    "            vivo:relatedBy ?address .\n" +
                    "            ?address a wos:Address ;\n" +
                    "            vivo:relates ?pub .\n" +
                    "            ?pub a wos:Publication ;\n" +
                    "            wos:hasCategory ?cat .\n" +
                                 yearDtv +
                                 dtvFilter +
                    "        }\n" +
                    "    }\n" +
                    "}";
        ParameterizedSparqlString q2 = this.storeUtils.getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Summary pub count query:\n" + query);
        ArrayList summaryArray = this.storeUtils.getFromStoreJSON(query);
        log.debug("done - getSummary sparql");
        if (summaryArray.isEmpty()) {
            log.debug("done - getSummary empty");
            return null;
        }
        JSONObject summary = (JSONObject)summaryArray.get(0);

        orgUri.replaceAll(".*/", "");

        Integer id = orgmap.get(orgUri.replaceAll(".*/", ""));
        if (id == null) {
            log.error("Could not get mapping for org: '" + orgUri + "'");
            id = 0;
        }
        if (startYear == null) {
            startYear = 2000;
        }
        if (endYear == null) {
            endYear = 2030;
        }
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = mysql.createStatement();
            resultSet = statement.executeQuery("select sum(total) as tot,sum(cites) as cit,sum(collind) as cind,sum(collint) as cint," +
                                               "sum(impact) as imp,sum(top1) as t1,sum(top10) as t10,count(*) as n from inds " +
                                               "where org=" + Integer.toString (id) + " and year >= " + Integer.toString(startYear) +
                                               " and year <= " + Integer.toString(endYear));
            while (resultSet.next()) {
                int total = Integer.parseInt(resultSet.getString("tot"));
                int cites = Integer.parseInt(resultSet.getString("cit"));
                int n     = Integer.parseInt(resultSet.getString("n"));
                summary.put("orgTotal", total);
                summary.put("orgCitesTotal", cites);
                summary.put("orgImpact", (Float)((float)cites / total));
                summary.put("orgimp",  Float.parseFloat(resultSet.getString("imp")) / n);
                summary.put("orgt1",   Float.parseFloat(resultSet.getString("t1")) / n);
                summary.put("orgt10",  Float.parseFloat(resultSet.getString("t10")) / n);
                summary.put("orgcind", Float.parseFloat(resultSet.getString("cind")) / n);
                summary.put("orgcint", Float.parseFloat(resultSet.getString("cint")) / n);
            }
        } catch (SQLException e) {
            log.error("SQL error");
            log.error("SQLException: " + e.getMessage());
            log.error("SQLState:     " + e.getSQLState());
            log.error("VendorError:  " + e.getErrorCode());
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sql_close(statement, resultSet);
        id = orgmap.get("org-technical-university-of-denmark");
        if (id == null) {
            log.error("Could not get mapping for org: 'org-technical-university-of-denmark'");
            id = 0;
        }
        try {
            statement = mysql.createStatement();
            resultSet = statement.executeQuery("select sum(total) as tot,sum(cites) as cit,sum(collind) as cind,sum(collint) as cint," +
                                               "sum(impact) as imp,sum(top1) as t1,sum(top10) as t10,count(*) as n from inds " +
                                               "where org=" + Integer.toString (id) + " and year >= " + Integer.toString(startYear) +
                                               " and year <= " + Integer.toString(endYear));
            while (resultSet.next()) {
                int total = Integer.parseInt(resultSet.getString("tot"));
                int cites = Integer.parseInt(resultSet.getString("cit"));
                int n     = Integer.parseInt(resultSet.getString("n"));
                summary.put("dtuTotal", total);
                summary.put("dtuCitesTotal", cites);
                summary.put("dtuImpact", (Float)((float)cites / total));
                summary.put("dtuimp",  Float.parseFloat(resultSet.getString("imp")) / n);
                summary.put("dtut1",   Float.parseFloat(resultSet.getString("t1")) / n);
                summary.put("dtut10",  Float.parseFloat(resultSet.getString("t10")) / n);
                summary.put("dtucind", Float.parseFloat(resultSet.getString("cind")) / n);
                summary.put("dtucint", Float.parseFloat(resultSet.getString("cint")) / n);
            }
        } catch (SQLException e) {
            log.error("SQL error");
            log.error("SQLException: " + e.getMessage());
            log.error("SQLState:     " + e.getSQLState());
            log.error("VendorError:  " + e.getErrorCode());
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sql_close(statement, resultSet);
        log.debug("done - getSummary");
        return summary;
    }

    private ArrayList getSummaryPubCount(Connection mysql, HashMap<String, Integer> orgmap, final String orgID, Integer startYear, Integer endYear) {
        log.debug("getSummaryPubCount: " + orgID);
        ArrayList<JSONObject> outRows = new ArrayList<JSONObject>();

        Integer id = orgmap.get(orgID);
        if (id == null) {
            log.error("Could not get mapping for org: '" + orgID + "'");
            id = 0;
        }
        if (startYear == null) {
            startYear = 2000;
        }
        if (endYear == null) {
            endYear = 2030;
        }
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = mysql.createStatement();
            resultSet = statement.executeQuery("select year,total from inds where org=" + Integer.toString (id) + " and year >= " + Integer.toString(startYear) +
                                               " and year <= " + Integer.toString(endYear));
            while (resultSet.next()) {
                JSONObject thisItem = new JSONObject();
                thisItem.put("year", resultSet.getString("year"));
                thisItem.put("number", resultSet.getString("total"));
                outRows.add(thisItem);
            }
        } catch (SQLException e) {
            log.error("SQL error");
            log.error("SQLException: " + e.getMessage());
            log.error("SQLState:     " + e.getSQLState());
            log.error("VendorError:  " + e.getErrorCode());
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sql_close(statement, resultSet);
        log.debug("done - getSummaryPubCount");
        return outRows;
    }
    
    private ArrayList getSummaryCopubCount(final String orgUri, 
            Integer startYear, Integer endYear) {
        log.debug("Hello. running summary copub count query");
        String rq = readQuery("summaryCopubCount/getSummaryCopubCount.rq");
        ParameterizedSparqlString q2 = this.storeUtils.getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("collab", orgUri);
        if(startYear == null) {
            startYear = 1;
        }
        if(endYear == null) {
            endYear = 9999;
        }
        q2.setLiteral("startYear", String.format("%04d", startYear) + "-01-01T00:00:00", 
                XSDDatatype.XSDdateTime);
        q2.setLiteral("endYear", String.format("%04d", endYear) + "-12-31T23:59:59", 
                XSDDatatype.XSDdateTime);
        String query = q2.toString();
        log.debug("Summary copub count query:\n" + query);
        return this.storeUtils.getFromStoreJSON(query);
    }

    private ArrayList getTopCategories(Connection mysql, HashMap<String, Integer> orgmap, String namespace, final String orgID, Integer startYear, Integer endYear) {
        log.debug("getTopCategories: " + orgID);
        ArrayList<JSONObject> outRows = new ArrayList<JSONObject>();
        HashMap<Integer, HashMap> subs = subjects(mysql);
        Integer id;

        id = orgmap.get(orgID);
        if (id == null) {
            log.error("Could not get mapping for org: '" + orgID + "'");
            id = 0;
        }
        if (startYear == null) {
            startYear = 2000;
        }
        if (endYear == null) {
            endYear = 2030;
        }
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = mysql.createStatement();
            resultSet = statement.executeQuery("select sub,sum(count) as c from subcount where org=" + Integer.toString (id) + " and year >= " +
                                               Integer.toString(startYear) + " and year <= " + Integer.toString(endYear) + " group by sub order by c desc limit 20");
            int rank = 1;
            while (resultSet.next()) {
                JSONObject thisItem = new JSONObject();
                String sub = resultSet.getString("sub");
                HashMap<String, String> s = subs.get(Integer.parseInt(sub));
                thisItem.put("name", s.get("label"));
                String catUri = namespace + s.get("name");
                thisItem.put("category", catUri);
                thisItem.put("number", resultSet.getString("c"));
                thisItem.put("rank", rank++);
                thisItem.put("copub", getCategoryCopub (catUri, namespace + orgID, startYear, endYear));
                outRows.add(thisItem);
            }
        } catch (SQLException e) {
            log.error("SQL error");
            log.error("SQLException: " + e.getMessage());
            log.error("SQLState:     " + e.getSQLState());
            log.error("VendorError:  " + e.getErrorCode());
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sql_close(statement, resultSet);
        log.debug("done - getTopCategories");
        AddCategoriesRanks(mysql, orgmap, namespace, null, startYear, endYear, outRows);
        return outRows;
    }

    private Integer getCategoryCopub(final String catUri, final String orgUri, Integer startYear, Integer endYear) {
        String yearDtv = getYearDtv(startYear, endYear);
        String dtvFilter = getDtvFilter(startYear, endYear);
        String rq = "SELECT (COUNT(DISTINCT ?pub) as ?coPubSub)\n" +
                    "WHERE {\n" +
                    "    ?org a foaf:Organization ;\n" +
                    "        vivo:relatedBy ?address .\n" +
                    "    ?address a wos:Address ;\n" +
                    "        vivo:relates ?pub .\n" +
                    "    ?pub a wos:Publication ;\n" +
                    "        wos:hasCategory ?cat .\n" +
                         yearDtv +
                         dtvFilter +
                    "}\n";
        ParameterizedSparqlString q2 = this.storeUtils.getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        q2.setIri("cat", catUri);
        String query = q2.toString();
        log.debug("Subject copub query:\n" + query);
        ArrayList subArray = this.storeUtils.getFromStoreJSON(query);
        log.debug("done - Subject copub sparql");
        if (subArray.isEmpty()) {
            log.debug("done - Subject copub empty");
            return 0;
        }
        JSONObject sub = (JSONObject)subArray.get(0);
        try {
            Integer n = (Integer)sub.get("coPubSub");
            return n;
        } catch (JSONException e) {
            log.debug("sub empty");
            return 0;
        }
    }

    private ArrayList AddCategoriesRanks(Connection mysql, HashMap<String, Integer> orgmap, String namespace, final String orgID, Integer startYear, Integer endYear, ArrayList<JSONObject> subjects) {
        log.debug("AddCategoriesRanks: " + orgID);
        ArrayList<JSONObject> outRows = new ArrayList<JSONObject>();
        HashMap<Integer, HashMap> subs = subjects(mysql);
        HashMap<String, Integer> DTUranks = new HashMap<String, Integer>();
        HashMap<String, Integer> ORGranks = new HashMap<String, Integer>();
        HashMap<String, Integer> DTUnums = new HashMap<String, Integer>();
        Integer id;

        id = orgmap.get("org-technical-university-of-denmark");
        if (id == null) {
            log.error("Could not get mapping for org: '" + orgID + "'");
            id = 0;
        }
        if (startYear == null) {
            startYear = 2000;
        }
        if (endYear == null) {
            endYear = 2030;
        }
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = mysql.createStatement();
            resultSet = statement.executeQuery("select sub,sum(count) as c from subcount where org=" + Integer.toString (id) + " and year >= " +
                                               Integer.toString(startYear) + " and year <= " + Integer.toString(endYear) + " group by sub order by c desc");
            int rank = 1;
            while (resultSet.next()) {
                String sub = resultSet.getString("sub");
                HashMap<String, String> s = subs.get(Integer.parseInt(sub));
                String key = namespace + s.get("name");
                DTUranks.put(key, rank++);
                DTUnums.put(key, Integer.parseInt(resultSet.getString("c")));
            }
        } catch (SQLException e) {
            log.error("SQL error");
            log.error("SQLException: " + e.getMessage());
            log.error("SQLState:     " + e.getSQLState());
            log.error("VendorError:  " + e.getErrorCode());
            e.printStackTrace();
        }
        sql_close(statement, resultSet);
        if (orgID != null) {
            id = orgmap.get(orgID);
            if (id == null) {
                log.error("Could not get mapping for org: '" + orgID + "'");
                id = 0;
            }
            statement = null;
            resultSet = null;
            try {
                statement = mysql.createStatement();
                resultSet = statement.executeQuery("select sub,sum(count) as c from subcount where org=" + Integer.toString (id) + " and year >= " +
                                                   Integer.toString(startYear) + " and year <= " + Integer.toString(endYear) + " group by sub order by c desc");
                int rank = 1;
                while (resultSet.next()) {
                    String sub = resultSet.getString("sub");
                    HashMap<String, String> s = subs.get(Integer.parseInt(sub));
                    String key = namespace + s.get("name");
                    ORGranks.put(key, rank++);
                }
            } catch (SQLException e) {
                log.error("SQL error");
                log.error("SQLException: " + e.getMessage());
                log.error("SQLState:     " + e.getSQLState());
                log.error("VendorError:  " + e.getErrorCode());
                e.printStackTrace();
            }
            sql_close(statement, resultSet);
        }
        for(JSONObject s:subjects) {
            try {
                String uri = (String)s.get("category");
                Integer rank = DTUranks.get(uri);
                if (rank != null) {
                    s.put("DTUrank", rank);
                }
                if (orgID != null) {
                    rank = ORGranks.get(uri);
                    if (rank != null) {
                        s.put("rank", rank);
                    }
                } else {
                    Integer num = DTUnums.get(uri);
                    if (num != null) {
                        s.put("DTUnumber", num);
                    }
                }
            } catch (Throwable var10) {
                log.error(var10, var10);
            }
        }
        log.debug("done - AddCategoriesRanks");
        return outRows;
    }

    private HashMap organisations(Connection mysql) {
        HashMap<String, Integer> orgmap = new HashMap<String, Integer>();

        log.debug("loading orgs");
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = mysql.createStatement();
            resultSet = statement.executeQuery("select id,name from orgs");
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                orgmap.put(name, Integer.parseInt(resultSet.getString("id")));
            }
        } catch (SQLException e) {
            log.error("SQL error");
            log.error("SQLException: " + e.getMessage());
            log.error("SQLState:     " + e.getSQLState());
            log.error("VendorError:  " + e.getErrorCode());
            e.printStackTrace();
        }
        sql_close(statement, resultSet);
        log.debug("done");
        return orgmap;
    }

    private HashMap subjects(Connection mysql) {
        HashMap<Integer, HashMap> subjects = new HashMap<Integer, HashMap>();

        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = mysql.createStatement();
            resultSet = statement.executeQuery("select id,name,label from subs");
            while (resultSet.next()) {
                HashMap<String, String> sub = new HashMap<String, String>();
                int id = Integer.parseInt(resultSet.getString("id"));
                sub.put("name", resultSet.getString("name"));
                sub.put("label", resultSet.getString("label"));
                subjects.put(id, sub);
            }
        } catch (SQLException e) {
            log.error("SQL error");
            log.error("SQLException: " + e.getMessage());
            log.error("SQLState:     " + e.getSQLState());
            log.error("VendorError:  " + e.getErrorCode());
            e.printStackTrace();
        }
        sql_close(statement, resultSet);
        return subjects;
    }

    private Connection sql_setup(ConfigurationProperties props) {
        String url  = props.getProperty("VitroConnection.DataSource.url");
        String user = props.getProperty("VitroConnection.DataSource.username");
        String pass = props.getProperty("VitroConnection.DataSource.password");
        Connection connect = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager.getConnection(url + "?user=" + user + "&password=" + pass);
        } catch (SQLException e) {
            log.error("SQL error");
            log.error("SQLException: " + e.getMessage());
            log.error("SQLState:     " + e.getSQLState());
            log.error("VendorError:  " + e.getErrorCode());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connect;
    }

    private void sql_close(Statement statement, ResultSet resultSet) {
        try {
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            log.error("SQL error");
            log.error("SQLException: " + e.getMessage());
            log.error("SQLState:     " + e.getSQLState());
            log.error("VendorError:  " + e.getErrorCode());
            e.printStackTrace();
        }
    }

    private ArrayList getRelatedPubCategories(Connection mysql, HashMap<String, Integer> orgmap, String namespace, final String orgUri, Integer startYear, Integer endYear) {
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
                getYearDtv(startYear, endYear) +
                getDtvFilter(startYear, endYear) +
                "?category rdfs:label ?label . \n" +
                "}\n" +
                "GROUP BY ?category ?label\n" +
                "ORDER BY DESC(?number) ?label";
        ParameterizedSparqlString q2 = this.storeUtils.getQuery(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Related categories query:\n" + query);
        ArrayList<JSONObject> outRows = this.storeUtils.getFromStoreJSON(query);
        AddCategoriesRanks(mysql, orgmap, namespace, orgUri.replaceAll(".*/", ""), startYear, endYear, outRows);
        return outRows;
    }

    private ArrayList getCoPubsByDepartment(String orgUri, Integer startYear, Integer endYear) {
        log.debug("Running copub by department query");
        String rq = "" +
                "SELECT DISTINCT ?dtuSubOrg ?dtuSubOrgName ?otherOrgs (COUNT(DISTINCT ?pub) as ?number)\n" +
                "WHERE {\n" +
                "?org vivo:relatedBy ?address .\n" +
                "?address a wos:Address .\n" +
                "?pub vivo:relatedBy ?address .\n" +
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
                getYearDtv(startYear, endYear) +
                getDtvFilter(startYear, endYear) +
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


    private Model deptModel(String externalOrgUri, Integer startYear, Integer endYear) {
        String rq = readQuery("coPubByDept/vds/summaryModel.rq");
        ParameterizedSparqlString ps = this.storeUtils.getQuery(rq);
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
        ps.setIri("externalOrg", externalOrgUri);
        String processedRq =  ps.toString();
        log.debug("Dept model query:\n " + processedRq);
        return this.storeUtils.getModelFromStore(processedRq);
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
