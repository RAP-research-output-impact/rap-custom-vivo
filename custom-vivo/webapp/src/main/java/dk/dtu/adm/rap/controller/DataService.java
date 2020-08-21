package dk.dtu.adm.rap.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator; 
import java.text.DecimalFormat;

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
import javax.ws.rs.core.Response.Status;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.Model;

import dk.dtu.adm.rap.utils.DataCache;
import dk.dtu.adm.rap.utils.StoreUtils;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

@Path("/report/")
public class DataService {

    private static final Log log = LogFactory.getLog(DataService.class.getName());
    private static final DataCache cache = new DataCache();

    @Path("/org/{vid}")
    @GET
    @Produces("application/json")
    public Response getOrg(@PathParam("vid") String vid,
            @Context Request request, @Context HttpServletRequest httpRequest) {
        return getOrg(vid, null, null, request, httpRequest);
    }

    @Path("/org/{vid}/{startYear}")
    @GET
    @Produces("application/json")
    public Response getOrg(@PathParam("vid") String vid,
            @PathParam("startYear") String startYear,
            @Context Request request, @Context HttpServletRequest httpRequest) {
        return getOrg(vid, startYear, null, request, httpRequest);
    }

    @Path("/org/{vid}/{startYear}/{endYear}")
    @GET
    @Produces("application/json")
    public Response getOrg(@PathParam("vid") String vid,
            @PathParam("startYear") String startYear,
            @PathParam("endYear") String endYear,
            @Context Request request, @Context HttpServletRequest httpRequest) {

        VitroRequest vreq = new VitroRequest(httpRequest);

        Integer startYearInt = parseInt(startYear);
        Integer endYearInt = parseInt(endYear);

        if (!authorized(vreq)) {
            notAuthorizedResponse();
        }
        ConfigurationProperties props = ConfigurationProperties.getBean(httpRequest);
        String cacheRoot = props.getProperty("DataCache.root");
        if (startYearInt == null) {
            startYearInt = parseInt(props.getProperty("DataCache.year.start"));
        }
        if (endYearInt == null) {
            endYearInt = parseInt(props.getProperty("DataCache.year.end"));
        }
        String cachekey = "orgs/" + vid + "." + Integer.toString(startYearInt) + "." + Integer.toString(endYearInt);
        String data = cache.read(cacheRoot, cachekey);
        if (data == null) {
            Connection mysql = sql_setup(props);
            HashMap<String, Integer> orgmap = organisations(mysql);
            long start = System.currentTimeMillis();
            JSONObject jo = new JSONObject();
            try {
                StoreUtils storeUtils = getStoreUtils(httpRequest);
                String uri = storeUtils.getNamespace() + vid;
                jo.put("summary",    getSummary(mysql, orgmap, uri, startYearInt, endYearInt, storeUtils));
                jo.put("categories", getRelatedPubCategories(mysql, orgmap, storeUtils.getNamespace(), uri, startYearInt, endYearInt, storeUtils));
                jo.put("org_totals", getSummaryPubCount(mysql, orgmap, vid, startYearInt, endYearInt));
                jo.put("dtu_totals", getSummaryPubCount(mysql, orgmap, "org-technical-university-of-denmark", startYearInt, endYearInt));
                jo.put("copub_totals", getSummaryCopubCount(uri, startYearInt, endYearInt, storeUtils));
                log.debug("done - getSummaryCopubCount");
                jo.put("top_categories", getTopCategories(mysql, orgmap, storeUtils.getNamespace(), vid, startYearInt, endYearInt, storeUtils));
                jo.put("by_department", getCoPubsByDepartment(uri, startYearInt, endYearInt, storeUtils));
                jo.put("funders", getFunders(uri, startYearInt, endYearInt, storeUtils));
                jo.put("dtu_researchers", getDtuResearchers(uri, startYearInt, endYearInt, storeUtils));
                log.debug("done - getCoPubsByDepartment");
            } catch (JSONException e) {
                log.error(e, e);
            }
            data = jo.toString();
            cache.write(cacheRoot, cachekey, data, (System.currentTimeMillis() - start));
            if (mysql != null) {
                try {
                    mysql.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
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
            @Context Request request, @Context HttpServletRequest httpRequest) {
        return getCoPubByDept(vid, null, null, request, httpRequest);
    }

    @Path("/org/{vid}/by-dept/{startYear}")
    @GET
    @Produces("application/json")
    public Response getCoPubByDept(@PathParam("vid") String vid,
            @PathParam("startYear") String startYear,
            @Context Request request, @Context HttpServletRequest httpRequest) {
        return getCoPubByDept(vid, startYear, null, request, httpRequest);
    }

    @Path("/org/{vid}/by-dept/{startYear}/{endYear}")
    @GET
    @Produces("application/json")
    public Response getCoPubByDept(@PathParam("vid") String vid,
            @PathParam("startYear") String startYear,
            @PathParam("endYear") String endYear,
            @Context Request request, @Context HttpServletRequest httpRequest) {
        VitroRequest vreq = new VitroRequest(httpRequest);
        Integer startYearInt = parseInt(startYear);
        Integer endYearInt = parseInt(endYear);
        if (!authorized(vreq)) {
            notAuthorizedResponse();
        }
        ConfigurationProperties props = ConfigurationProperties.getBean(httpRequest);
        String cacheRoot = props.getProperty("DataCache.root");
        String namespace = props.getProperty("Vitro.defaultNamespace");
        String uri = namespace + vid;
        StoreUtils storeUtils = getStoreUtils(httpRequest);
        String cachekey = "dept/" + vid + "." + Integer.toString(startYearInt) + "." + Integer.toString(endYearInt);
        String data = cache.read(cacheRoot, cachekey);
        if (data == null) {
            long start = System.currentTimeMillis();
            String orgName = storeUtils.getFromStore(getQuery(
                    "SELECT ?name where { <" + uri + "> rdfs:label ?name }", storeUtils)).get(0).get(
                            "name").toString();
            Model tmpModel = deptModel(uri, startYearInt, endYearInt, storeUtils);
            String rq = readQuery("coPubByDept/vds/dtuSubOrgCount.rq");
            log.debug("Dept query:\n" + rq);
            ArrayList<HashMap> depts = storeUtils.getFromModel(getQuery(rq, storeUtils), tmpModel);
            JSONArray out = new JSONArray();
            for (HashMap dept: depts) {
                if ( dept.get("org") != null ) {
                    String deptUri = dept.get("org").toString();
                    String exOrgRq = getQuery(readQuery("coPubByDept/vds/externalSubOrgCount.rq"), storeUtils);
                    ParameterizedSparqlString q2 = storeUtils.getQuery(exOrgRq);
                    q2.setIri("org", deptUri);
                    String exOrgQuery = q2.toString();
                    log.debug("External Dept query:\n" + exOrgQuery);
                    ArrayList<HashMap> subOrgs = null;
                    try {
                        subOrgs = storeUtils.getFromModelJSON(exOrgQuery, tmpModel);
                    } catch (JSONException e) {
                        log.error(e, e);
                    }
                    dept.put("sub_orgs", new JSONArray(subOrgs));
                    out.put(dept);
                }
            }
            JSONObject jo = new JSONObject();
            try {
                jo.put("name", orgName);
                jo.put("departments", out);
            } catch (JSONException e) {
                log.error(e, e);
            }
            data = jo.toString();
            cache.write(cacheRoot, cachekey, data, (System.currentTimeMillis() - start));
        }
        ResponseBuilder builder = Response.ok(data);
        return builder.build();
    }

    private boolean authorized(HttpServletRequest request) {
        return authorized(new VitroRequest(request));
    }

    private boolean authorized(VitroRequest vreq) {
        if (LoginStatusBean.getBean(vreq).isLoggedIn()) {
            return true;
        }
        String addr = vreq.getRemoteAddr();
        if (addr.equals("127.0.0.1")) {
            return true;
        }
        if (addr.equals("::1")) {
            return true;
        }
        return false;
    }

    private Response notAuthorizedResponse() {
        return Response.status(403).type("text/plain").entity(
                "Restricted to authenticated users").build();
    }

    @Path("/worldmap")
    @GET
    @Produces("application/json")
    public Response getWorldMap(@Context Request request, @Context HttpServletRequest httpRequest) {
        VitroRequest vreq = new VitroRequest(httpRequest);
        if (!authorized(vreq)) {
            notAuthorizedResponse();
        }
        String dept = vreq.getParameter("dept");
        String dep = "";
        if ((dept == null) || (dept == "")) {
            dept = null;
            dep = "all";
        } else {
            dep = dept;
        }
        String yearStart = vreq.getParameter("startYear");
        String yearEnd = vreq.getParameter("endYear");
        ConfigurationProperties props = ConfigurationProperties.getBean(httpRequest);
        String cacheRoot = props.getProperty("DataCache.root");
        String cachekey = "worldmap/worldmap." + dep + "." + yearStart + "." + yearEnd;
        String data = cache.read(cacheRoot, cachekey);
        if (data == null) {
            long start = System.currentTimeMillis();
            JSONObject jo = new JSONObject();
            try {
                StoreUtils storeUtils = getStoreUtils(httpRequest);
                jo.put("summary", getWorldwidePubs(dept, yearStart, yearEnd, storeUtils));
            } catch (JSONException e) {
                log.error(e, e);
            }
            data = jo.toString();
            cache.write(cacheRoot, cachekey, data, (System.currentTimeMillis() - start));
        }
        ResponseBuilder builder = Response.ok(data);
        return builder.build();
    }

    @Path("/country/{cCode}")
    @GET
    @Produces("application/json")
    public Response getCountry(@PathParam("cCode") String cCode,
            @Context Request request, @Context HttpServletRequest httpRequest) {
        VitroRequest vreq = new VitroRequest(httpRequest);
        if (!authorized(vreq)) {
            notAuthorizedResponse();
        }
        String dept = vreq.getParameter("dept");
        String dep = "";
        if ((dept == null) || (dept == "")) {
            dept = null;
            dep = "all";
        } else {
            dep = dept;
        }
        String yearStart = vreq.getParameter("startYear");
        String yearEnd = vreq.getParameter("endYear");
        ConfigurationProperties props = ConfigurationProperties.getBean(httpRequest);
        String cacheRoot = props.getProperty("DataCache.root");
        String cachekey = "country/" + cCode + "/country-" + cCode + "." + dep + "." + yearStart + "." + yearEnd;
        String data = cache.read(cacheRoot, cachekey);
        if (data == null) {
            long start = System.currentTimeMillis();
            JSONObject jo = new JSONObject();
            try {
                StoreUtils storeUtils = getStoreUtils(httpRequest);
                jo.put("orgs", getCoPubsCountry(cCode.toUpperCase(), dept, yearStart, yearEnd, storeUtils));
            } catch (JSONException e) {
                log.error(e, e);
            }
            data = jo.toString();
            cache.write(cacheRoot, cachekey, data, (System.currentTimeMillis() - start));
        }
        ResponseBuilder builder = Response.ok(data);
        return builder.build();
    }

    private Response processPartnerOrFunderRequest(HttpServletRequest request,
            String requestLabel, String orgParamName,
            PartnerOrFunderJSONGenerator generator) {
        StoreUtils storeUtils = getStoreUtils(request);
        if (!authorized(request)) {
            return Response.status(403).type("text/plain").entity(
                    "Restricted to authenticated users").build();
        }
        String field = request.getParameter(orgParamName);
        if(StringUtils.isEmpty(field)) {
            field = null;
        }
        String dept = request.getParameter("dept");
        if(StringUtils.isEmpty(dept)) {
            dept = null;
        }
        String yearStart = request.getParameter("startYear");
        String yearEnd = request.getParameter("endYear");
        if(yearStart == null || yearEnd == null) {
            return Response.status(Status.BAD_REQUEST).entity(
                    "Parameters startYear and endYear must be specified").build();
        }
        ConfigurationProperties props = ConfigurationProperties.getBean(request);
        String cacheRoot = props.getProperty("DataCache.root");
        String fieldStr = (field == null) ? "all" : field.replaceAll(".*/", "");
        String deptStr = (dept == null) ? "all" : dept;
        String cacheFilename = requestLabel + "/" + fieldStr + "/" + requestLabel + "-" + fieldStr + "." + deptStr + "." + yearStart + "." + yearEnd;
        String data = cache.read(cacheRoot, cacheFilename);
        if (data == null) {
            long start = System.currentTimeMillis();
            JSONObject jo = new JSONObject();
            try {
                if (requestLabel == "partners_by_funder" || requestLabel == "partners" || requestLabel == "partners_by_subject") {
                    jo.put("orgs", generator.getData(props, field, dept, yearStart, yearEnd, storeUtils));
                } else {
                    jo.put(requestLabel, generator.getData(props, field, dept, yearStart, yearEnd, storeUtils));
                }
            } catch (JSONException e) {
                log.error(e, e);
            }
            data = jo.toString();
            cache.write(cacheRoot, cacheFilename, data, (System.currentTimeMillis() - start));
        }
        ResponseBuilder builder = Response.ok(data);
        return builder.build();
    }

    @Path("/partners")
    @GET
    @Produces("application/json")
    public Response getPartners(@Context HttpServletRequest request) {
        return processPartnerOrFunderRequest(
                request, "partners", null, new PartnerListGenerator());
    }

    @Path("/funders")
    @GET
    @Produces("application/json")
    public Response getFunders(@Context HttpServletRequest request) {
        return processPartnerOrFunderRequest(
                request, "funders", null, new FunderListGenerator());
    }

    @Path("/partners-by-funder")
    @GET
    @Produces("application/json")
    public Response getPartnersByFunder(@Context HttpServletRequest request) {
        return processPartnerOrFunderRequest(
                request, "partners_by_funder", "funder", new PartnerByFunderListGenerator());
    }

    @Path("/copub-subjects")
    @GET
    @Produces("application/json")
    public Response getCopubsBySubject(@Context HttpServletRequest request) {
        return processPartnerOrFunderRequest(
                request, "subjects", null, new SubjectListGenerator());
    }

    @Path("/partners-by-subject")
    @GET
    @Produces("application/json")
    public Response getPartnersBySubject(@Context HttpServletRequest request) {
        return processPartnerOrFunderRequest(
                request, "partners_by_subject", "subject", new PartnerBySubjectListGenerator());
    }

    private interface PartnerOrFunderJSONGenerator {
        public ArrayList getData(ConfigurationProperties props, String field, String dept, String yearStart, String yearEnd,
                StoreUtils storeUtils);
    }

    private class PartnerListGenerator implements PartnerOrFunderJSONGenerator {
        /**
         * @param props Configuration properties
         * @param dept Department RDF local name. May be null.  If null, partners
         *             list will be generated for all of DTU.
         * @param yearStart May not be null.
         * @param yearEnd May not be null.
         * @param StoreUtils
         * @return
         */
        public ArrayList getData(ConfigurationProperties props, String field, String dept, String yearStart, String yearEnd,
                StoreUtils storeUtils) {
            String deptStr = (dept == null) ? "all of DTU" : dept;
            log.debug("Querying for partner list for " + deptStr);
                String rq = "SELECT ?org (MIN(?partnerLabel) AS ?name) (COUNT(DISTINCT ?pub) as ?publications)\n" +
                        "WHERE {\n";
                if(dept != null) {
                    rq +=
                        "  ?dtuAddress vivo:relates ?dept  .\n" +
                        "  ?dtuAddress a wos:Address .\n" +
                        "  ?dtuAddress vivo:relates ?pub .\n";
                }
                rq +=
                        "  ?pub a wos:Publication .\n" +
                        "  ?partnerAddress vivo:relates ?pub .\n" +
                        "  ?partnerAddress a wos:Address .\n" +
                        "  ?partnerAddress vivo:relates ?org .\n" +
                        "  ?org a wos:UnifiedOrganization .\n" +
                        "  FILTER (?org != d:org-technical-university-of-denmark)\n" +
                        "  ?org rdfs:label ?partnerLabel .\n" +
                        "  ?pub vivo:dateTimeValue ?dtv .\n" +
                        "  ?dtv vivo:dateTime ?dateTime .\n" +
                        "  FILTER(xsd:dateTime(CONCAT(?yearStart, \"-01-01T00:00:00\")) <= ?dateTime)\n" +
                        "  FILTER(xsd:dateTime(CONCAT(?yearEnd, \"-12-31T23:59:59\")) >= ?dateTime)\n" +
                        "}\n" +
                        "GROUP by ?org\n" +
                        "ORDER BY DESC(?publications)";
            ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
            q2.setLiteral("yearStart", yearStart);
            q2.setLiteral("yearEnd", yearEnd);
            if(dept != null) {
                q2.setIri("dept", storeUtils.getNamespace() + dept);
            }
            String query = q2.toString();
            log.debug("Partners query:\n" + query);
            ArrayList<JSONObject> outRows = storeUtils.getFromStoreJSON(query);
            AddOrgImpact (props, yearStart, yearEnd, outRows);
            return outRows;
        }
    }

    private class FunderListGenerator implements PartnerOrFunderJSONGenerator {
        /**
         * @param props Configuration properties
         * @param dept Department RDF local name. May be null.  If null, partners
         *             list will be generated for all of DTU.
         * @param yearStart May not be null.
         * @param yearEnd May not be null.
         * @param StoreUtils
         * @return
         */
        public ArrayList getData(ConfigurationProperties props, String field, String dept, String yearStart, String yearEnd,
                StoreUtils storeUtils) {
            String deptStr = (dept == null) ? "all of DTU" : dept;
            log.debug("Querying for funder list for " + deptStr);
                String rq = "SELECT ?funder (MIN(?funderLabel) AS ?name) (COUNT(DISTINCT ?pub) as ?publications)\n" +
                        "WHERE {\n";
                if(dept != null) {
                    rq +=
                        "  ?dtuAddress vivo:relates ?dept  .\n" +
                        "  ?dtuAddress a wos:Address .\n" +
                        "  ?dtuAddress vivo:relates ?pub .\n";
                }
                rq +=
                        "  ?pub a wos:Publication .\n" +
                        "  ?grant vivo:relates ?pub .\n" +
                        "  ?grant a vivo:Grant .\n" +
                        "  ?grant vivo:relates ?funder .\n" +
                        "  ?funder a wos:Funder .\n" +
                        "  ?funder rdfs:label ?funderLabel . \n" +
                        "  ?pub vivo:dateTimeValue ?dtv .\n" +
                        "  ?dtv vivo:dateTime ?dateTime .\n" +
                        "  FILTER(xsd:dateTime(CONCAT(?yearStart, \"-01-01T00:00:00\")) <= ?dateTime)\n" +
                        "  FILTER(xsd:dateTime(CONCAT(?yearEnd, \"-12-31T23:59:59\")) >= ?dateTime)\n" +
                        "}\n" +
                        "GROUP by ?funder\n" +
                        "ORDER BY DESC(?publications)";
            ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
            q2.setLiteral("yearStart", yearStart);
            q2.setLiteral("yearEnd", yearEnd);
            if(dept != null) {
                q2.setIri("dept", storeUtils.getNamespace() + dept);
            }
            String query = q2.toString();
            log.debug("Funders query:\n" + query);
            return storeUtils.getFromStoreJSON(query);
        }
    }

    private class PartnerByFunderListGenerator implements PartnerOrFunderJSONGenerator {
        /**
         * @param props Configuration properties
         * @param funder Funder RDF local name. May be null.  If null, partners
         *             list will be generated for all of DTU.
         * @param yearStart May not be null.
         * @param yearEnd May not be null.
         * @param StoreUtils
         * @return
         */
        public ArrayList getData(ConfigurationProperties props, String funder, String dept, String yearStart, String yearEnd,
                StoreUtils storeUtils) {
            if(funder == null) {
                throw new RuntimeException("Parameter 'funder' is required");
            }
            log.debug("Querying for partner list for funder " + funder);
            String rq = "SELECT ?org (MIN(?partnerLabel) AS ?name) (COUNT(DISTINCT ?pub) as ?publications)\n" +
                        "WHERE {\n" +
                        "  ?grant vivo:relates ?funder .\n" +
                        "  ?grant a vivo:Grant .\n" +
                        "  ?grant vivo:relates ?pub .\n" +
                        "  ?pub a wos:Publication .\n" +
                        "  ?partnerAddress vivo:relates ?pub .\n" +
                        "  ?partnerAddress a wos:Address .\n" +
                        "  ?partnerAddress vivo:relates ?org  .\n";
            if (dept != null) {
                rq +=   "  ?dtuAddress vivo:relates ?pub .\n" +
                        "  ?dtuAddress a wos:Address .\n" +
                        "  ?dtuAddress vivo:relates ?dept  .\n";
            }
            rq +=       "  ?org a wos:UnifiedOrganization .\n" +
                        "  ?org rdfs:label ?partnerLabel .                     \n" +
                        "  FILTER (?org != d:org-technical-university-of-denmark)\n" +
                        "  ?pub vivo:dateTimeValue ?dtv .\n" +
                        "  ?dtv vivo:dateTime ?dateTime .\n" +
                        "  FILTER(xsd:dateTime(CONCAT(?yearStart, \"-01-01T00:00:00\")) <= ?dateTime)\n" +
                        "  FILTER(xsd:dateTime(CONCAT(?yearEnd, \"-12-31T23:59:59\")) >= ?dateTime)\n" +
                        "}\n" +
                        "GROUP by ?org\n" +
                        "ORDER BY DESC(?publications)";
            ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
            q2.setLiteral("yearStart", yearStart);
            q2.setLiteral("yearEnd", yearEnd);
            if(dept != null) {
                q2.setIri("dept", storeUtils.getNamespace() + dept);
            }
            String funderIri = funder;
            if(!funder.startsWith(storeUtils.getNamespace())) {
                funderIri = storeUtils.getNamespace() + funder;
            }
            q2.setIri("funder", funderIri);
            String query = q2.toString();
            log.debug("Partners by funder query:\n" + query);
            return storeUtils.getFromStoreJSON(query);
        }
    }

    private class SubjectListGenerator implements PartnerOrFunderJSONGenerator {
        /**
         * @param props Configuration properties
         * @param dept Department RDF local name. May be null.  If null, partners
         *             list will be generated for all of DTU.
         * @param yearStart May not be null.
         * @param yearEnd May not be null.
         * @param deptNamespace The namespace to which the local name in 'dept' will
         *                      be appended. May not be null.
         * @param StoreUtils
         * @return
         */
        public ArrayList getData(ConfigurationProperties props, String field, String dept, String yearStart, String yearEnd,
                StoreUtils storeUtils) {
            String deptStr = (dept == null) ? "all of DTU" : dept;
            log.debug("Querying for subject list for " + deptStr);
                String rq = "SELECT ?subject (MIN(?subjectLabel) AS ?name) (COUNT(DISTINCT ?pub) as ?publications)\n" +
                        "WHERE {\n";
                if(dept != null) {
                    rq +=
                        "  ?dtuAddress vivo:relates ?dept  .\n" +
                        "  ?dtuAddress a wos:Address .\n" +
                        "  ?dtuAddress vivo:relates ?pub .\n";
                }
                rq +=
                        "  ?pub a wos:Publication .\n" +
                        "  ?pub wos:hasCategory ?subject . \n" +
                        "  ?subject rdfs:label ?subjectLabel . \n" +
                        "  ?pub vivo:dateTimeValue ?dtv .\n" +
                        "  ?dtv vivo:dateTime ?dateTime .\n" +
                        "  FILTER(xsd:dateTime(CONCAT(?yearStart, \"-01-01T00:00:00\")) <= ?dateTime)\n" +
                        "  FILTER(xsd:dateTime(CONCAT(?yearEnd, \"-12-31T23:59:59\")) >= ?dateTime)\n" +
                        "}\n" +
                        "GROUP by ?subject\n" +
                        "ORDER BY DESC(?publications)";
            ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
            q2.setLiteral("yearStart", yearStart);
            q2.setLiteral("yearEnd", yearEnd);
            if(dept != null) {
                q2.setIri("dept", storeUtils.getNamespace() + dept);
            }
            String query = q2.toString();
            log.debug("Subjects query:\n" + query);
            return storeUtils.getFromStoreJSON(query);
        }
    }

    private class PartnerBySubjectListGenerator implements PartnerOrFunderJSONGenerator {
        /**
         * @param props Configuration properties
         * @param subject Funder RDF local name. May be null.  If null, partners
         *                list will be generated for all of DTU.
         * @param yearStart May not be null.
         * @param yearEnd May not be null.
         * @param StoreUtils
         * @return
         */
        public ArrayList getData(ConfigurationProperties props, String subject, String dept, String yearStart, String yearEnd,
                StoreUtils storeUtils) {
            if(subject == null) {
                throw new RuntimeException("Parameter 'subject' is required");
            }
            log.debug("Querying for partner list for subject " + subject);
            String rq = "SELECT ?org (MIN(?partnerLabel) AS ?name) (COUNT(DISTINCT ?pub) as ?publications)\n" +
                        "WHERE {\n" +
                        "  ?pub a wos:Publication .\n" +
                        "  ?pub wos:hasCategory ?subject . \n" +
                        "  ?partnerAddress vivo:relates ?pub .\n" +
                        "  ?partnerAddress a wos:Address .\n" +
                        "  ?partnerAddress vivo:relates ?org  .\n";
            if (dept != null) {
                rq +=   "  ?dtuAddress vivo:relates ?pub .\n" +
                        "  ?dtuAddress a wos:Address .\n" +
                        "  ?dtuAddress vivo:relates ?dept  .\n";
            }
            rq +=       "  ?org a wos:UnifiedOrganization .\n" +
                        "  ?org rdfs:label ?partnerLabel .                     \n" +
                        "  FILTER (?org != d:org-technical-university-of-denmark)\n" +
                        "  ?pub vivo:dateTimeValue ?dtv .\n" +
                        "  ?dtv vivo:dateTime ?dateTime .\n" +
                        "  FILTER(xsd:dateTime(CONCAT(?yearStart, \"-01-01T00:00:00\")) <= ?dateTime)\n" +
                        "  FILTER(xsd:dateTime(CONCAT(?yearEnd, \"-12-31T23:59:59\")) >= ?dateTime)\n" +
                        "}\n" +
                        "GROUP by ?org\n" +
                        "ORDER BY DESC(?publications)";
            ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
            q2.setLiteral("yearStart", yearStart);
            q2.setLiteral("yearEnd", yearEnd);
            String subjectIri = subject;
            if(!subject.startsWith(storeUtils.getNamespace())) {
                subjectIri = storeUtils.getNamespace() + subject;
            }
            q2.setIri("subject", subjectIri);
            String query = q2.toString();
            log.debug("Partners by subject query:\n" + query);
            return storeUtils.getFromStoreJSON(query);
        }
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

    private Object getSummary(Connection mysql, HashMap<String, Integer> orgmap,
            String orgUri, Integer startYear, Integer endYear,
            StoreUtils storeUtils) {
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
        ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("getSummary query:\n" + query);
        ArrayList summaryArray = storeUtils.getFromStoreJSON(query);
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
            resultSet = statement.executeQuery("select sum(total) as tot,sum(cites) as cit,sum(total*collind) as cind,sum(total*collint) as cint," +
                                               "sum(total*impact) as imp,sum(total*top1) as t1,sum(total*top10) as t10 from inds " +
                                               "where org=" + Integer.toString (id) + " and year >= " + Integer.toString(startYear) +
                                               " and year <= " + Integer.toString(endYear));
            while (resultSet.next()) {
                if (resultSet.getString("tot") == null) {
                    summary.put("orgTotal", "");
                    summary.put("orgCitesTotal", "");
                    summary.put("orgImpact", "");
                    summary.put("orgimp",  "");
                    summary.put("orgt1",   "");
                    summary.put("orgt10",  "");
                    summary.put("orgcind", "");
                    summary.put("orgcint", "");
                } else {
                    int total = Integer.parseInt(resultSet.getString("tot"));
                    int cites = Integer.parseInt(resultSet.getString("cit"));
                    summary.put("orgTotal", total);
                    summary.put("orgCitesTotal", cites);
                    summary.put("orgImpact", (Float)((float)cites / total));
                    summary.put("orgimp",  Float.parseFloat(resultSet.getString("imp")) / total);
                    summary.put("orgt1",   Float.parseFloat(resultSet.getString("t1")) / total);
                    summary.put("orgt10",  Float.parseFloat(resultSet.getString("t10")) / total);
                    summary.put("orgcind", Float.parseFloat(resultSet.getString("cind")) / total);
                    summary.put("orgcint", Float.parseFloat(resultSet.getString("cint")) / total);
                }
            }
        } catch (SQLException e) {
            logSQLException(e);
        } catch (JSONException e) {
            log.error(e, e);
        } finally {
            sql_close(statement, resultSet);
        }
        id = orgmap.get("org-technical-university-of-denmark");
        if (id == null) {
            log.error("Could not get mapping for org: 'org-technical-university-of-denmark'");
            id = 0;
        }
        try {
            statement = mysql.createStatement();
            resultSet = statement.executeQuery("select sum(total) as tot,sum(cites) as cit,sum(total*collind) as cind,sum(total*collint) as cint," +
                                               "sum(total*impact) as imp,sum(total*top1) as t1,sum(total*top10) as t10 from inds " +
                                               "where org=" + Integer.toString (id) + " and year >= " + Integer.toString(startYear) +
                                               " and year <= " + Integer.toString(endYear));
            while (resultSet.next()) {
                int total = Integer.parseInt(resultSet.getString("tot"));
                int cites = Integer.parseInt(resultSet.getString("cit"));
                summary.put("dtuTotal", total);
                summary.put("dtuCitesTotal", cites);
                summary.put("dtuImpact", (Float)((float)cites / total));
                summary.put("dtuimp",  Float.parseFloat(resultSet.getString("imp")) / total);
                summary.put("dtut1",   Float.parseFloat(resultSet.getString("t1")) / total);
                summary.put("dtut10",  Float.parseFloat(resultSet.getString("t10")) / total);
                summary.put("dtucind", Float.parseFloat(resultSet.getString("cind")) / total);
                summary.put("dtucint", Float.parseFloat(resultSet.getString("cint")) / total);
            }
        } catch (SQLException e) {
            logSQLException(e);
        } catch (JSONException e) {
            log.error(e, e);
        } finally {
            sql_close(statement, resultSet);
        }
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
            log.info("select year,total from inds where org=" + Integer.toString (id) + " and year >= " + Integer.toString(startYear) +
                     " and year <= " + Integer.toString(endYear));
            resultSet = statement.executeQuery("select year,total from inds where org=" + Integer.toString (id) + " and year >= " + Integer.toString(startYear) +
                                               " and year <= " + Integer.toString(endYear));
            while (resultSet.next()) {
                log.info("adding " + resultSet.getString("year") + " : " + resultSet.getString("total"));
                JSONObject thisItem = new JSONObject();
                thisItem.put("year", resultSet.getString("year"));
                thisItem.put("number", resultSet.getString("total"));
                outRows.add(thisItem);
            }
        } catch (SQLException e) {
            logSQLException(e);
        } catch (JSONException e) {
            log.error(e, e);
        } finally {
            sql_close(statement, resultSet);
        }
        log.debug("done - getSummaryPubCount");
        return outRows;
    }

    private ArrayList getSummaryCopubCount(final String orgUri,
            Integer startYear, Integer endYear, StoreUtils storeUtils) {
        log.debug("getSummaryCopubCount - " + orgUri);
        String rq = readQuery("summaryCopubCount/getSummaryCopubCount.rq");
        ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
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
        return storeUtils.getFromStoreJSON(query);
    }

    private ArrayList getTopCategories(Connection mysql,
            HashMap<String, Integer> orgmap, String namespace,
            final String orgID, Integer startYear, Integer endYear,
            StoreUtils storeUtils) {
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
                thisItem.put("copub", getCategoryCopub(
                        catUri, namespace + orgID, startYear, endYear, storeUtils));
                outRows.add(thisItem);
            }
        } catch (SQLException e) {
            logSQLException(e);
        } catch (JSONException e) {
            log.error(e, e);
        } finally {
            sql_close(statement, resultSet);
        }
        AddCategoriesRanks(mysql, orgmap, namespace, null, startYear, endYear, outRows);
        log.debug("done - getTopCategories");
        return outRows;
    }

    private Integer getCategoryCopub(final String catUri, final String orgUri,
            Integer startYear, Integer endYear, StoreUtils storeUtils) {
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
        ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
        q2.setCommandText(rq);
        q2.setIri("org", orgUri);
        q2.setIri("cat", catUri);
        String query = q2.toString();
        log.debug("Subject copub query:\n" + query);
        ArrayList subArray = storeUtils.getFromStoreJSON(query);
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
            logSQLException(e);
        } finally {
            sql_close(statement, resultSet);
        }
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
                logSQLException(e);
            } finally {
                sql_close(statement, resultSet);
            }
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

    private ArrayList AddOrgImpact(ConfigurationProperties props, String startYear, String endYear, ArrayList<JSONObject> outRows) {
        log.debug("AddOrgImpact");
        HashMap<String, String> impact = new HashMap<String, String>();

        if (startYear == null) {
            startYear = "2000";
        }
        if (endYear == null) {
            endYear = "2030";
        }
        Statement statement = null;
        ResultSet resultSet = null;
        Connection mysql = sql_setup(props);
        try {
            statement = mysql.createStatement();
            resultSet = statement.executeQuery("select name,sum(total) as tot,sum(total*impact) as impa from inds,orgs where org=id and year >= " + startYear +
                                               " and year <= " + endYear + " group by org");
            int rank = 1;
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            while (resultSet.next()) {
                String name  = resultSet.getString("name");
                int    total = Integer.parseInt(resultSet.getString("tot"));
                String impa  = decimalFormat.format(Float.parseFloat(resultSet.getString("impa")) / total);
                impact.put(name, impa);
            }
        } catch (SQLException e) {
            logSQLException(e);
        } finally {
            sql_close(statement, resultSet);
        }
        Iterator itr = outRows.iterator();
        while (itr.hasNext()) {
            JSONObject r = (JSONObject)itr.next();
            try {
                Integer pubs = (Integer)r.get("publications");
                if (pubs > 2) {
                    String org = (String)r.get("org");
                    String impa = impact.get(org.replaceAll(".*/", ""));
                    if (impa != null) {
                        r.put("impact", impa);
                    }
                } else {
                    itr.remove();
                }
            } catch (Throwable var10) {
                log.error(var10, var10);
            }
        }
        log.debug("done - AddOrgImpact");
        return outRows;
    }

    private HashMap<String, Integer> organisations(Connection mysql) {
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
            logSQLException(e);
        } finally {
            sql_close(statement, resultSet);
        }
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
            logSQLException(e);
        } finally {
            sql_close(statement, resultSet);
        }
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
            logSQLException(e);
        } catch (ClassNotFoundException e) {
            log.error(e, e);
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
            logSQLException(e);
        }
    }

    private ArrayList getRelatedPubCategories(Connection mysql,
            HashMap<String, Integer> orgmap, String namespace,
            final String orgUri, Integer startYear, Integer endYear,
            StoreUtils storeUtils) {
        log.debug("getRelatedPubCategories - " + orgUri);
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
        ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("getRelatedPubCategories query:\n" + query);
        ArrayList<JSONObject> outRows = storeUtils.getFromStoreJSON(query);
        AddCategoriesRanks(mysql, orgmap, namespace, orgUri.replaceAll(".*/", ""), startYear, endYear, outRows);
        log.debug("done - getRelatedPubCategories");
        return outRows;
    }

    private ArrayList getCoPubsByDepartment(String orgUri, Integer startYear,
            Integer endYear, StoreUtils storeUtils) {
        log.debug("getCoPubsByDepartment - " + orgUri);
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
        ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Related categories query:\n" + query);
        return storeUtils.getFromStoreJSON(query);
    }

    private ArrayList getFunders(String orgUri, Integer startYear,
            Integer endYear, StoreUtils storeUtils) {
        log.debug("getFunders - " + orgUri);
        String rq = "" +
                "SELECT DISTINCT ?funder (MIN(?funderLabel) AS ?name) (COUNT(DISTINCT ?pub) as ?number)\r\n" +
                "WHERE {\r\n" +
                "    ?org vivo:relatedBy ?address .\r\n" +
                "    ?address a wos:Address .\r\n" +
                "    ?pub vivo:relatedBy ?address .\r\n" +
                "    ?pub a wos:Publication ;\r\n" +
                "    vivo:relatedBy ?dtuAddress .\r\n" +
                "    ?dtuAddress a wos:Address ;\r\n" +
                "    vivo:relates <http://rap.adm.dtu.dk/individual/org-technical-university-of-denmark> .\r\n" +
                "    ?grant vivo:relates ?pub .\r\n" +
                "    ?grant a vivo:Grant . \r\n" +
                "    ?grant vivo:relates ?funder . \r\n" +
                "    ?funder a wos:Funder .\r\n" +
                "    ?funder rdfs:label ?funderLabel . \r\n" +
                getYearDtv(startYear, endYear) +
                getDtvFilter(startYear, endYear) +
                "}\r\n" +
                "GROUP BY ?funder \r\n" +
                "ORDER BY DESC(?number)\r\n" +
                "LIMIT 20";
        ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("Funders query:\n" + query);
        return storeUtils.getFromStoreJSON(query);
    }

    private ArrayList<JSONObject> getDtuResearchers(String orgUri, Integer startYear,
            Integer endYear, StoreUtils storeUtils) {
        log.debug("getDtuResearchers - " + orgUri);
	// Wrap GROUP BY query in outer query to prevent row with null values if there are no results
        String rq = 
                "SELECT DISTINCT ?dtuResearcher ?name ?number\r\n" +
                "WHERE {\r\n" +
		"FILTER(BOUND(?dtuResearcher))\r\n" +
		"FILTER(BOUND(?name))\r\n" +
		"FILTER(BOUND(?number))\r\n" + 
                "{ # begin subquery \r\n" +
		"SELECT DISTINCT ?dtuResearcher (MIN(?fullName) AS ?name) (COUNT(DISTINCT ?pub) as ?number)\r\n" +
                "WHERE {\r\n" +
                "    ?org vivo:relatedBy ?address .\r\n" +
                "    ?address a wos:Address .\r\n" +
                "    ?pub vivo:relatedBy ?address .\r\n" +
                "    ?pub a wos:Publication ;\r\n" +
                "        vivo:relatedBy ?dtuAddress .\r\n" +
                "    ?dtuAddress a wos:Address ;\r\n" +
                "        vivo:relates <http://rap.adm.dtu.dk/individual/org-technical-university-of-denmark> .\r\n" +
                "    ?dtuAddress vivo:relatedBy ?authorship .\r\n" +
                "    ?pub vivo:relatedBy ?authorship .\r\n" +
                "    ?authorship a vivo:Authorship .\r\n" +
                "    # Better to use label rather than fullName in order \r\n" +
                "    # to distinguish people who differ only by middle initial. \r\n" +
                "    #?authorship wos:fullName ?fullName .\r\n" +
                "    ?authorship vivo:relates ?dtuResearcher .  \r\n" +
                "    ?dtuResearcher a foaf:Person ." +
                "    ?dtuResearcher rdfs:label ?fullName ." +
                getYearDtv(startYear, endYear) +
                getDtvFilter(startYear, endYear) +
                "}\r\n" +
                "GROUP BY ?dtuResearcher \r\n" +
                "ORDER BY DESC(?number)\r\n" +
                "LIMIT 40\r\n" + 
		"} # end subquery \r\n" +
		"} ORDER BY DESC(?number)\r\n";
        ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
        q2.setIri("org", orgUri);
        String query = q2.toString();
        log.debug("DTU researchers query:\n" + query);
        ArrayList<JSONObject> json = storeUtils.getFromStoreJSON(query);
        Set<String> done = new HashSet<String>();
        int researcherCount = 0;
        ArrayList<JSONObject> deadRows = new ArrayList<JSONObject>();
        for(JSONObject row : json) {
            try {
                String name = row.getString("name");
                if (done.contains(name)) {
                    deadRows.add(row);
                    continue;
                }
                done.add(name);
                String dtuResearcher = row.getString("dtuResearcher");
                String partnerResearcherQueryStr = getPartnerResearcherQueryStr(
                        dtuResearcher, orgUri, startYear, endYear, storeUtils);
                log.debug("Partner researcher query:\n" + partnerResearcherQueryStr);
                ArrayList<JSONObject> partnerResearchers = storeUtils.getFromStoreJSON(
                        partnerResearcherQueryStr);
                log.debug(partnerResearchers.size() + " partner researchers");
                JSONArray partnerResearchersJson = new JSONArray(partnerResearchers);
                if(partnerResearchersJson.length() == 1
                        && !partnerResearchersJson.getJSONObject(0).has("name")) {
                    // Because the SPARQL has a GROUP BY, a lack of any results
                    // will return a single row with a 0 count but no name value.
                    // We don't want this, so we will return an empty array instead.
                    row.put("partner_researchers", new JSONArray());
                } else {
                    row.put("partner_researchers", partnerResearchersJson);
                }
                researcherCount++;
                if (researcherCount == 20) {
                    break;
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        for(JSONObject row : deadRows) {
            json.remove(row);
        }
        return json;
    }

    private String getPartnerResearcherQueryStr(String dtuResearcher,
            String org, Integer startYear, Integer endYear, StoreUtils storeUtils) {
        ParameterizedSparqlString queryStr = storeUtils.getQuery(
                "SELECT DISTINCT "
                + "?partnerResearcher (MIN(?partnerResearcherFullName) AS ?name)"
                + " (COUNT(DISTINCT ?pub) as ?number) "
                + "WHERE { \n"
                + "  ?org vivo:relatedBy ?address . \n"
                + "  ?address a wos:Address . \n"
                + "  ?pub vivo:relatedBy ?address . \n"
                + "  ?pub a wos:Publication ; \n"
                + "      vivo:relatedBy ?dtuAddress . \n"
                + "  ?dtuAddress a wos:Address ; \n"
                + "      vivo:relates <http://rap.adm.dtu.dk/individual/org-technical-university-of-denmark> . \n"
                + "  ?dtuAddress vivo:relatedBy ?authorship . \n"
                + "  ?pub vivo:relatedBy ?authorship . \n"
                + "  ?authorship a vivo:Authorship . \n"
                + "  # Better to use label rather than fullName in order \n"
                + "  # to distinguish people who differ only by middle initial. \n"
                + "  #?authorship wos:fullName ?fullName .\n"
                + "  ?authorship vivo:relates ?dtuResearcher . \n"
                + "  ?address vivo:relatedBy ?partnerAuthorship . \n"
                + "  ?pub vivo:relatedBy ?partnerAuthorship . \n"
                + "  ?partnerAuthorship a vivo:Authorship . \n"
                + "  ?partnerAuthorship vivo:relates ?partnerResearcher . \n"
                + "  ?partnerResearcher a foaf:Person . \n"
                + "  ?partnerResearcher rdfs:label ?partnerResearcherFullName . \n"
                + "  FILTER(?partnerResearcher != ?dtuResearcher) \n"
                + getYearDtv(startYear, endYear)
                + getDtvFilter(startYear, endYear)
                + "} \n"
                + "GROUP BY ?partnerResearcher \n"
                + "ORDER BY DESC(?number) \n"
                + "LIMIT 20 \n");
        queryStr.setIri("dtuResearcher", dtuResearcher);
        queryStr.setIri("org", org);
        return queryStr.toString();
    }

    private ArrayList getWorldwidePubs(String dept, String yearStart,
            String yearEnd, StoreUtils storeUtils) {
        log.debug("Querying for country codes for copublication");
        String rq;
        if (dept == null) {
            rq = "SELECT ?code (COUNT(DISTINCT ?pub) as ?publications)\n" +
                 "WHERE {\n" +
                 "    ?pub a wos:Publication .\n" +
                 "    ?orgAddress vivo:relates ?pub .\n" +
                 "    ?orgAddress geo:codeISO3 ?code .\n" +
                 "    ?pub vivo:dateTimeValue ?dtv .\n" +
                 "    ?dtv vivo:dateTime ?dateTime .\n" +
                 "    FILTER(xsd:dateTime(\"" + yearStart + "-01-01T00:00:00\") <= ?dateTime)\n" +
                 "    FILTER(xsd:dateTime(\"" + yearEnd + "-12-31T23:59:59\") >= ?dateTime)\n" +
                 "}\n" +
                 "GROUP by ?code\n" +
                 "ORDER BY DESC(?publications)\n";
        } else {
            rq = "SELECT ?code (COUNT(DISTINCT ?pub) as ?publications)\n" +
                 "WHERE {\n" +
                 "    ?dtuAddress vivo:relates d:" + dept + " .\n" +
                 "    ?dtuAddress a wos:Address .\n" +
                 "    ?dtuAddress vivo:relates ?pub .\n" +
                 "    ?pub a wos:Publication .\n" +
                 "    ?orgAddress vivo:relates ?pub .\n" +
                 "    ?orgAddress geo:codeISO3 ?code .\n" +
                 "    ?pub vivo:dateTimeValue ?dtv .\n" +
                 "    ?dtv vivo:dateTime ?dateTime .\n" +
                 "    FILTER(xsd:dateTime(\"" + yearStart + "-01-01T00:00:00\") <= ?dateTime)\n" +
                 "    FILTER(xsd:dateTime(\"" + yearEnd + "-12-31T23:59:59\") >= ?dateTime)\n" +
                 "}\n" +
                 "GROUP by ?code\n" +
                 "ORDER BY DESC(?publications)\n";
        }
        ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
        String query = q2.toString();
        log.debug("Country pubs query:\n" + query);
        return storeUtils.getFromStoreJSON(query);
    }

    private ArrayList getCoPubsCountry(String countryCode, String dept,
            String yearStart, String yearEnd, StoreUtils storeUtils) {
        log.debug("Running query to find copubs by country and org");
        String rq;
        if (dept == null) {
            rq = "select ?org ?name ?publications\n" +
                 "where {\n" +
                 "    ?org obo:RO_0001025 ?country .\n" +
                 "    ?country geo:codeISO3 \"" + countryCode + "\"^^<http://www.w3.org/2001/XMLSchema#string> .\n" +
                 "    {\n" +
                 "        select ?org ?name (COUNT(DISTINCT ?pub) as ?publications)\n" +
                 "        where {\n" +
                 "            ?pub a wos:Publication .\n" +
                 "            ?orgAddress vivo:relates ?pub .\n" +
                 "            ?orgAddress geo:codeISO3 \"" + countryCode + "\"^^<http://www.w3.org/2001/XMLSchema#string> .\n" +
                 "            ?org a wos:UnifiedOrganization ;\n" +
                 "                rdfs:label ?name ;\n" +
                 "                vivo:relatedBy ?orgAddress .\n" +
                 "            ?pub vivo:dateTimeValue ?dtv .\n" +
                 "            ?dtv vivo:dateTime ?dateTime .\n" +
                 "            FILTER(xsd:dateTime(\"" + yearStart + "-01-01T00:00:00\") <= ?dateTime)\n" +
                 "            FILTER(xsd:dateTime(\"" + yearEnd + "-12-31T23:59:59\") >= ?dateTime)\n" +
                 "            FILTER (?org != d:org-technical-university-of-denmark)\n" +
                 "        }\n" +
                 "        GROUP BY ?org ?name\n" +
                 "        ORDER BY DESC(?publications)\n" +
                 "    }\n" +
                 "}\n" +
                 "ORDER BY DESC(?publications)\n";
        } else {
            rq = "select ?org ?name ?publications\n" +
                 "where {\n" +
                 "    ?org obo:RO_0001025 ?country .\n" +
                 "    ?country geo:codeISO3 \"" + countryCode + "\"^^<http://www.w3.org/2001/XMLSchema#string> .\n" +
                 "    {\n" +
                 "        select ?org ?name (COUNT(DISTINCT ?pub) as ?publications)\n" +
                 "        where {\n" +
                 "            ?dtuAddress vivo:relates d:" + dept + " .\n" +
                 "            ?dtuAddress a wos:Address .\n" +
                 "            ?dtuAddress vivo:relates ?pub .\n" +
                 "            ?pub a wos:Publication .\n" +
                 "            ?orgAddress vivo:relates ?pub .\n" +
                 "            ?orgAddress geo:codeISO3 \"" + countryCode + "\"^^<http://www.w3.org/2001/XMLSchema#string> .\n" +
                 "            ?org a wos:UnifiedOrganization ;\n" +
                 "                rdfs:label ?name ;\n" +
                 "                vivo:relatedBy ?orgAddress .\n" +
                 "            ?pub vivo:dateTimeValue ?dtv .\n" +
                 "            ?dtv vivo:dateTime ?dateTime .\n" +
                 "            FILTER(xsd:dateTime(\"" + yearStart + "-01-01T00:00:00\") <= ?dateTime)\n" +
                 "            FILTER(xsd:dateTime(\"" + yearEnd + "-12-31T23:59:59\") >= ?dateTime)\n" +
                 "            FILTER (?org != d:org-technical-university-of-denmark)\n" +
                 "        }\n" +
                 "        GROUP BY ?org ?name\n" +
                 "        ORDER BY DESC(?publications)\n" +
                 "    }\n" +
                 "}\n" +
                 "ORDER BY DESC(?publications)\n";
        }
        ParameterizedSparqlString q2 = storeUtils.getQuery(rq);
        String query = q2.toString();
        log.debug("Related categories query:\n" + query);
        return storeUtils.getFromStoreJSON(query);
    }

    private Model deptModel(String externalOrgUri, Integer startYear,
            Integer endYear, StoreUtils storeUtils) {
        String rq = readQuery("coPubByDept/vds/summaryModel.rq");
        ParameterizedSparqlString ps = storeUtils.getQuery(rq);
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
        return storeUtils.getModelFromStore(processedRq);
    }

    private String getQuery(String raw, StoreUtils storeUtils) {
        ParameterizedSparqlString ps = storeUtils.getQuery(raw);
        return ps.toString();
    }

    public static String readQuery( String name ) {
        URL qurl = Resources.getResource(name);
        try {
            return Resources.toString(qurl, Charsets.UTF_8);
        } catch (IOException e) {
            log.error(e, e);
            return "";
        }
    }

    /**
     * Construct a StoreUtils for executing queries in the current request
     * @param httpRequest the current request object
     */
    private StoreUtils getStoreUtils(HttpServletRequest httpRequest) {
        VitroRequest vreq = new VitroRequest(httpRequest);
        StoreUtils storeUtils = new StoreUtils();
        ConfigurationProperties props = ConfigurationProperties.getBean(
                httpRequest);
        String namespace = props.getProperty("Vitro.defaultNamespace");
        storeUtils.setRdfService(namespace, vreq.getRDFService());
        return storeUtils;
    }

    private void logSQLException(SQLException e) {
        log.error("SQL error");
        log.error("SQLException: " + e.getMessage());
        log.error("SQLState:     " + e.getSQLState());
        log.error("VendorError:  " + e.getErrorCode());
        log.error(e, e);
    }


}
