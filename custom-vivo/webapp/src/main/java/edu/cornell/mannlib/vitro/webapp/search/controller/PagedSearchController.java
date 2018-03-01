/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.dtu.adm.rap.search.RAPSearchFacets;
import dk.dtu.adm.rap.search.SearchFacet;
import dk.dtu.adm.rap.search.SearchFacetCategory;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassGroupsForRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField.Count;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.LinkTemplateModel;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.searchresult.IndividualSearchResult;
import edu.ucsf.vitro.opensocial.OpenSocialManager;

/**
 * Paged search controller that uses the search engine
 */

public class PagedSearchController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(PagedSearchController.class);

    protected static final int DEFAULT_HITS_PER_PAGE = 25;
    protected static final int DEFAULT_MAX_HIT_COUNT = 1000;

    private static final String PARAM_XML_REQUEST = "xml";
    private static final String PARAM_CSV_REQUEST = "csv";
    private static final String PARAM_START_INDEX = "startIndex";
    private static final String PARAM_HITS_PER_PAGE = "hitsPerPage";
    private static final String PARAM_CLASSGROUP = "classgroup";
    private static final String PARAM_RDFTYPE = "type";
    // RAP make this field public
    public static final String PARAM_QUERY_TEXT = "querytext";
    public static final String FACET_FIELD_PREFIX = "facet_";
    public static final String PARAM_FACET_AS_TEXT = "facetAsText";
    public static final String PARAM_FACET_TEXT_VALUE = "facetTextValue";

    protected static final Map<Format,Map<Result,String>> templateTable;
    // RAP
    //protected static final Map<String, String> facetPublicNameTable;

    protected enum Format {
        HTML, XML, CSV;
    }

    protected enum Result {
        PAGED, ERROR, BAD_QUERY
    }

    static{
        templateTable = setupTemplateTable();
    }

    /**
     * Overriding doGet from FreemarkerHttpController to do a page template (as
     * opposed to body template) style output for XML requests.
     *
     * This follows the pattern in AutocompleteController.java.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        VitroRequest vreq = new VitroRequest(request);
        boolean wasXmlRequested = isRequestedFormatXml(vreq);
        boolean wasCSVRequested = isRequestedFormatCSV(vreq);
        if( !wasXmlRequested && !wasCSVRequested){
            super.doGet(vreq,response);
        }else if (wasXmlRequested){
            try {
                ResponseValues rvalues = processRequest(vreq);

                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/xml;charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename=search.xml");
                writeTemplate(rvalues.getTemplateName(), rvalues.getMap(), request, response);
            } catch (Exception e) {
                log.error(e, e);
            }
        }else if (wasCSVRequested){
        	try {
                ResponseValues rvalues = processRequest(vreq);

                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/csv;charset=UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename=search.csv");
                writeTemplate(rvalues.getTemplateName(), rvalues.getMap(), request, response);
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {    	    	
    	
        //There may be other non-html formats in the future
        Format format = getFormat(vreq);
        boolean wasXmlRequested = Format.XML == format;
        boolean wasCSVRequested = Format.CSV == format;
        log.debug("Requested format was " + (wasXmlRequested ? "xml" : "html"));
        boolean wasHtmlRequested = ! (wasXmlRequested || wasCSVRequested);

        try {

            //make sure an IndividualDao is available
            if( vreq.getWebappDaoFactory() == null
                    || vreq.getWebappDaoFactory().getIndividualDao() == null ){
                log.error("Could not get webappDaoFactory or IndividualDao");
                throw new Exception("Could not access model.");
            }
            IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
            VClassGroupDao grpDao = vreq.getWebappDaoFactory().getVClassGroupDao();
            VClassDao vclassDao = vreq.getWebappDaoFactory().getVClassDao();

            ApplicationBean appBean = vreq.getAppBean();

            log.debug("IndividualDao is " + iDao.toString() + " Public classes in the classgroup are " + grpDao.getPublicGroupsWithVClasses().toString());
            log.debug("VClassDao is "+ vclassDao.toString() );

            int startIndex = getStartIndex(vreq);
            int hitsPerPage = getHitsPerPage( vreq );

            String queryText = vreq.getParameter(PARAM_QUERY_TEXT);
            log.debug("Query text is \""+ queryText + "\"");

// RAP: allow empty searches
            if(queryText == null) {
                queryText = "";
            }
//            String badQueryMsg = badQueryText( queryText, vreq );
//            if( badQueryMsg != null ){
//                return doFailedSearch(badQueryMsg, queryText, format, vreq);
//            }

            SearchQuery query = getQuery(queryText, hitsPerPage, startIndex, vreq);
            SearchEngine search = ApplicationUtils.instance().getSearchEngine();
            SearchResponse response = null;

            try {
                response = search.query(query);
            } catch (Exception ex) {
                String msg = makeBadSearchMessage(queryText, ex.getMessage(), vreq);
                log.error("could not run search query",ex);
                return doFailedSearch(msg, queryText, format, vreq);
            }

            if (response == null) {
                log.error("Search response was null");
                return doFailedSearch(I18n.text(vreq, "error_in_search_request"), queryText, format, vreq);
            }

            SearchResultDocumentList docs = response.getResults();
            if (docs == null) {
                log.error("Document list for a search was null");
                return doFailedSearch(I18n.text(vreq, "error_in_search_request"), queryText,format, vreq);
            }

            long hitCount = docs.getNumFound();
            log.debug("Number of hits = " + hitCount);
            if ( hitCount < 1 ) {
                return doNoHits(queryText, format, vreq);
            }

            List<Individual> individuals = new ArrayList<Individual>(docs.size());
            Iterator<SearchResultDocument> docIter = docs.iterator();
            while( docIter.hasNext() ){
                try {
                    SearchResultDocument doc = docIter.next();
                    String uri = doc.getStringValue(VitroSearchTermNames.URI);
                    Individual ind = iDao.getIndividualByURI(uri);
                    if(ind != null) {
                      ind.setSearchSnippet( getSnippet(doc, response) );
                      individuals.add(ind);
                    }
                } catch(Exception e) {
                    log.error("Problem getting usable individuals from search hits. ",e);
                }
            }

//          ParamMap pagingLinkParams = new ParamMap();
            ParamMap pagingLinkParams = RAPgetQueryParamMap(vreq);
            pagingLinkParams.put(PARAM_QUERY_TEXT, queryText);
            pagingLinkParams.put(PARAM_HITS_PER_PAGE, String.valueOf(hitsPerPage));

            if( wasXmlRequested ){
                pagingLinkParams.put(PARAM_XML_REQUEST,"1");
            }

            /* Compile the data for the templates */

            Map<String, Object> body = new HashMap<String, Object>();

            String classGroupParam = vreq.getParameter(PARAM_CLASSGROUP);
            log.debug("ClassGroupParam is \""+ classGroupParam + "\"");
            boolean classGroupFilterRequested = false;
            if (!StringUtils.isEmpty(classGroupParam)) {
                VClassGroup grp = grpDao.getGroupByURI(classGroupParam);
                classGroupFilterRequested = true;
                if (grp != null && grp.getPublicName() != null) {
                    body.put("classGroupURI", grp.getURI());
                    body.put("classGroupName", grp.getPublicName());
                }
            }

            String typeParam = vreq.getParameter(PARAM_RDFTYPE);
            boolean typeFilterRequested = false;
            if (!StringUtils.isEmpty(typeParam)) {
                VClass type = vclassDao.getVClassByURI(typeParam);
                typeFilterRequested = true;
                if (type != null && type.getName() != null)
                    body.put("typeName", type.getName());
            }

            /* Add ClassGroup and type refinement links to body */
            if( wasHtmlRequested ){
                // RAP
                body.put("facets", getFacetLinks(vreq, response, queryText));
                body.put("facetsAsText", RAPSearchFacets.getSearchFacetsAsText());
                body.put(PARAM_FACET_AS_TEXT, vreq.getParameter(PARAM_FACET_AS_TEXT));
                body.put(PARAM_FACET_TEXT_VALUE, vreq.getParameter(PARAM_FACET_TEXT_VALUE));
                body.put("RAPQueryReduce", RAPQueryReduce(vreq, grpDao, vclassDao));
                if ( !classGroupFilterRequested && !typeFilterRequested ) {
                    // Search request includes no ClassGroup and no type, so add ClassGroup search refinement links.
                    body.put("classGroupLinks", getClassGroupsLinks(vreq, grpDao, docs, response, queryText));
                } else if ( classGroupFilterRequested && !typeFilterRequested ) {
                    // Search request is for a ClassGroup, so add rdf:type search refinement links
                    // but try to filter out classes that are subclasses
                    body.put("classLinks", getVClassLinks(vreq, vclassDao, docs, response, queryText));
                    pagingLinkParams.put(PARAM_CLASSGROUP, classGroupParam);
                } else {
                    //search request is for a class so there are no more refinements
                    pagingLinkParams.put(PARAM_RDFTYPE, typeParam);
                }
            }

            body.put("individuals", IndividualSearchResult
                    .getIndividualTemplateModels(individuals, vreq));

            body.put("querytext", queryText);
            body.put("title", queryText + " - " + appBean.getApplicationName()
                    + " Search Results");

            body.put("hitCount", hitCount);
            body.put("startIndex", startIndex);

            body.put("pagingLinks",
                    getPagingLinks(startIndex, hitsPerPage, hitCount,
                                   vreq.getServletPath(),
                                   pagingLinkParams, vreq));

            if (startIndex != 0) {
                body.put("prevPage", getPreviousPageLink(startIndex,
                        hitsPerPage, vreq.getServletPath(), pagingLinkParams));
            }
            if (startIndex < (hitCount - hitsPerPage)) {
                body.put("nextPage", getNextPageLink(startIndex, hitsPerPage,
                        vreq.getServletPath(), pagingLinkParams));
            }

	        // VIVO OpenSocial Extension by UCSF
	        try {
		        OpenSocialManager openSocialManager = new OpenSocialManager(vreq, "search");
		        // put list of people found onto pubsub channel
	            // only turn this on for a people only search
	            if ("http://vivoweb.org/ontology#vitroClassGrouppeople".equals(vreq.getParameter(PARAM_CLASSGROUP))) {
			        List<String> ids = OpenSocialManager.getOpenSocialId(individuals);
			        openSocialManager.setPubsubData(OpenSocialManager.JSON_PERSONID_CHANNEL,
			        		OpenSocialManager.buildJSONPersonIds(ids, "" + ids.size() + " people found"));
	            }
				// TODO put this in a better place to guarantee that it gets called at the proper time!
				openSocialManager.removePubsubGadgetsWithoutData();
		        body.put("openSocial", openSocialManager);
		        if (openSocialManager.isVisible()) {
		        	body.put("bodyOnload", "my.init();");
		        }
	        } catch (IOException e) {
	            log.error("IOException in doTemplate()", e);
	        } catch (SQLException e) {
	            log.error("SQLException in doTemplate()", e);
	        }

	        String template = templateTable.get(format).get(Result.PAGED);

            return new TemplateResponseValues(template, body);
        } catch (Throwable e) {
            return doSearchError(e,format);
        }
    }


    private int getHitsPerPage(VitroRequest vreq) {
        int hitsPerPage = DEFAULT_HITS_PER_PAGE;
        try{
            hitsPerPage = Integer.parseInt(vreq.getParameter(PARAM_HITS_PER_PAGE));
        } catch (Throwable e) {
            hitsPerPage = DEFAULT_HITS_PER_PAGE;
        }
        log.debug("hitsPerPage is " + hitsPerPage);
        return hitsPerPage;
    }

    private int getStartIndex(VitroRequest vreq) {
        int startIndex = 0;
        try{
            startIndex = Integer.parseInt(vreq.getParameter(PARAM_START_INDEX));
        }catch (Throwable e) {
            startIndex = 0;
        }
        log.debug("startIndex is " + startIndex);
        return startIndex;
    }

    private String badQueryText(String qtxt, VitroRequest vreq) {
        if( qtxt == null || "".equals( qtxt.trim() ) )
        	return I18n.text(vreq, "enter_search_term");

        if( qtxt.equals("*:*") )
        	return I18n.text(vreq, "invalid_search_term") ;

        return null;
    }

    /**
     * Get the links to the facet categories for the individuals in the documents
     */
    private static List<SearchFacet> getFacetLinks(VitroRequest request,
            SearchResponse response, String querytext) {
        List<SearchFacet> searchFacets = new ArrayList<SearchFacet>();
        for(SearchFacet sf : RAPSearchFacets.getSearchFacets()) {
            SearchFacetField ff = null;
            for (SearchFacetField sff : response.getFacetFields()) {
                if(!sff.getValues().isEmpty()
                        && sff.getName().equals(sf.getFieldName())) {
                    ff = sff;
                    break;
                }
            }
            if(ff == null) {
                continue;
            }
            for(Count value : ff.getValues()) {
                if(value.getCount() < 1) {
                    continue;
                }
                String name = value.getName();
                String label = name;
                if(name.startsWith("http://")) {
                    IndividualDao iDao = request.getWebappDaoFactory()
                            .getIndividualDao();
                    Individual ind = iDao.getIndividualByURI(name);
                    if(ind != null) {
                        label = ind.getRdfsLabel();
                    }
                }
                // need a fresh copy of the params because we're gonna modify it
                ParamMap facetParams = RAPgetQueryParamMap(request);
                facetParams.put(PagedSearchController.PARAM_QUERY_TEXT, querytext);
                String val = facetParams.get(ff.getName());
                if ((val != null) && (!StringUtils.isEmpty(val))) {
                    facetParams.put(ff.getName(), val + ";;" + name);
                } else {
                    facetParams.put(ff.getName(), name);
                }
                SearchFacetCategory category = new SearchFacetCategory(label, facetParams, value.getCount());
                sf.getCategories().add(category);
            }
            searchFacets.add(sf);
            log.debug("Added facet " + sf.getPublicName() + " to template.");
        }
        return searchFacets;
    }

    /**
     * Get the class groups represented for the individuals in the documents.
     */
    private List<VClassGroupSearchLink> getClassGroupsLinks(VitroRequest vreq, VClassGroupDao grpDao, SearchResultDocumentList docs, SearchResponse rsp, String qtxt) {
        Map<String,Long> cgURItoCount = new HashMap<String,Long>();

        List<VClassGroup> classgroups = new ArrayList<VClassGroup>( );
        List<SearchFacetField> ffs = rsp.getFacetFields();
        for(SearchFacetField ff : ffs){
            if(VitroSearchTermNames.CLASSGROUP_URI.equals(ff.getName())){
                List<Count> counts = ff.getValues();
                for( Count ct: counts){
                    VClassGroup vcg = grpDao.getGroupByURI( ct.getName() );
                    if( vcg == null ){
                        log.debug("could not get classgroup for URI " + ct.getName());
                    }else{
                        classgroups.add(vcg);
                        cgURItoCount.put(vcg.getURI(),  ct.getCount());
                    }
                }
            }
        }

        grpDao.sortGroupList(classgroups);

        VClassGroupsForRequest vcgfr = VClassGroupCache.getVClassGroups(vreq);
        List<VClassGroupSearchLink> classGroupLinks = new ArrayList<VClassGroupSearchLink>(classgroups.size());
        for (VClassGroup vcg : classgroups) {
        	String groupURI = vcg.getURI();
			VClassGroup localizedVcg = vcgfr.getGroup(groupURI);
            long count = cgURItoCount.get( groupURI );
            if (localizedVcg.getPublicName() != null && count > 0 )  {
                classGroupLinks.add(new VClassGroupSearchLink(vreq, qtxt, localizedVcg, count));
            }
        }
        return classGroupLinks;
    }

    private List<VClassSearchLink> getVClassLinks(VitroRequest vreq, VClassDao vclassDao, SearchResultDocumentList docs, SearchResponse rsp, String qtxt){
        HashSet<String> typesInHits = getVClassUrisForHits(docs);
        List<VClass> classes = new ArrayList<VClass>(typesInHits.size());
        Map<String,Long> typeURItoCount = new HashMap<String,Long>();

        List<SearchFacetField> ffs = rsp.getFacetFields();
        for(SearchFacetField ff : ffs){
            if(VitroSearchTermNames.RDFTYPE.equals(ff.getName())){
                List<Count> counts = ff.getValues();
                for( Count ct: counts){
                    String typeUri = ct.getName();
                    long count = ct.getCount();
                    try{
                        if( VitroVocabulary.OWL_THING.equals(typeUri) ||
                            count == 0 )
                            continue;
                        VClass type = vclassDao.getVClassByURI(typeUri);
                        if( type != null &&
                            ! type.isAnonymous() &&
                              type.getName() != null && !"".equals(type.getName()) &&
                              type.getGroupURI() != null ){ //don't display classes that aren't in classgroups
                            typeURItoCount.put(typeUri,count);
                            classes.add(type);
                        }
                    }catch(Exception ex){
                        if( log.isDebugEnabled() )
                            log.debug("could not add type " + typeUri, ex);
                    }
                }
            }
        }


        Collections.sort(classes, new Comparator<VClass>(){
            public int compare(VClass o1, VClass o2) {
                return o1.compareTo(o2);
            }});

        List<VClassSearchLink> vClassLinks = new ArrayList<VClassSearchLink>(classes.size());
        for (VClass vc : classes) {
            long count = typeURItoCount.get(vc.getURI());
            vClassLinks.add(new VClassSearchLink(vreq, qtxt, vc, count ));
        }

        return vClassLinks;
    }

    private HashSet<String> getVClassUrisForHits(SearchResultDocumentList docs){
        HashSet<String> typesInHits = new HashSet<String>();
        for (SearchResultDocument doc : docs) {
            try {
                Collection<Object> types = doc.getFieldValues(VitroSearchTermNames.RDFTYPE);
                if (types != null) {
                    for (Object o : types) {
                        String typeUri = o.toString();
                        typesInHits.add(typeUri);
                    }
                }
            } catch (Exception e) {
                log.error("problems getting rdf:type for search hits",e);
            }
        }
        return typesInHits;
    }

    private String getSnippet(SearchResultDocument doc, SearchResponse response) {
        String docId = doc.getStringValue(VitroSearchTermNames.DOCID);
        StringBuffer text = new StringBuffer();
        Map<String, Map<String, List<String>>> highlights = response.getHighlighting();
        if (highlights != null && highlights.get(docId) != null) {
            List<String> snippets = highlights.get(docId).get(VitroSearchTermNames.ALLTEXT);
            if (snippets != null && snippets.size() > 0) {
                text.append("... " + snippets.get(0) + " ...");
            }
        }
        return text.toString();
    }

    private SearchQuery getQuery(String queryText, int hitsPerPage, int startIndex, VitroRequest vreq) {
        // RAP: AND in search terms for specific "facet as text" field
        String facetAsText = vreq.getParameter(PARAM_FACET_AS_TEXT);
        if(facetAsText != null) {
            SearchFacet textFacet = RAPSearchFacets.getSearchFacetByFieldName(facetAsText);
            if(textFacet != null && textFacet.isFacetAsText()) {
                String textValue = vreq.getParameter(PARAM_FACET_TEXT_VALUE);
                if(textValue != null) {
                    if (!StringUtils.isEmpty(queryText)) {
                        queryText += " AND ";
                    }
                    if (facetAsText.contains("publication-year")) {
                        if (textValue.contains("-")) {
                            queryText += "(";
                            boolean i = false;
                            String years[] = textValue.split("-");
                            int yearFrom = Integer.valueOf(years[0]);
                            int yearTo = Integer.valueOf(years[1]) + 1;
                            for (int year = yearFrom; year < yearTo; year++) {
                                if (year > yearFrom) {
                                    queryText += " OR ";
                                }
                                queryText += textFacet.getFieldName() + ":" + year;
                            }
                            queryText += ")";
                        } else {
                            queryText += textFacet.getFieldName() + ":" + textValue;
                        }
                    } else {
                        queryText += textFacet.getFieldName() + ":\""
                                + textValue.replaceAll(Pattern.quote("\""), "") + "\"";
                    }
                }
            }
        }

        log.info("query text is " + queryText);

        // Lowercase the search term to support wildcard searches: The search engine applies no text
        // processing to a wildcard search term.
        SearchQuery query = ApplicationUtils.instance().getSearchEngine().createQuery(queryText);

        query.setStart( startIndex )
             .setRows(hitsPerPage);

        // ClassGroup filtering param
        String classgroupParam = vreq.getParameter(PARAM_CLASSGROUP);

        // rdf:type filtering param
        String typeParam = vreq.getParameter(PARAM_RDFTYPE);

        if ( ! StringUtils.isBlank(classgroupParam) ) {
            // ClassGroup filtering
            log.debug("Firing classgroup query ");
            log.debug("request.getParameter(classgroup) is "+ classgroupParam);
            query.addFilterQuery(VitroSearchTermNames.CLASSGROUP_URI + ":\"" + classgroupParam + "\"");

            //with ClassGroup filtering we want type facets
            query.addFacetFields(VitroSearchTermNames.RDFTYPE).setFacetLimit(-1);

        }else if (  ! StringUtils.isBlank(typeParam) ) {
            // rdf:type filtering
            log.debug("Firing type query ");
            log.debug("request.getParameter(type) is "+ typeParam);
            query.addFilterQuery(VitroSearchTermNames.RDFTYPE + ":\"" + typeParam + "\"");
            //with type filtering we don't have facets.
        }else{
            //When no filtering is set, we want ClassGroup facets
            query.addFacetFields(VitroSearchTermNames.CLASSGROUP_URI).setFacetLimit(-1);
            // RAP
        }
        addRAPFacetFields(query, vreq);
        log.debug("Query = " + query.toString());
        return query;
    }

    protected static void addRAPFacetFields(SearchQuery query, VitroRequest vreq) {
        for(SearchFacet facet : RAPSearchFacets.getSearchFacets()) {
            query.addFacetFields(facet.getFieldName()).setFacetLimit(-1);
        }
        ParamMap facetParams = getFacetParamMap(vreq);
        for(String parameterName : facetParams.keySet()) {
            String parameterValue = facetParams.get(parameterName);
            if(!parameterValue.isEmpty()) {
                if (parameterValue.contains(";;")) {
                    for (String val : parameterValue.split(";;")) {
                        query.addFilterQuery(parameterName + ":\"" + val + "\"");
                    }
                } else {
                    query.addFilterQuery(parameterName + ":\"" + parameterValue + "\"");
                }
            }
        }
	query.setFacetMinCount(1);
    }

    private static ParamMap getFacetParamMap(VitroRequest vreq) {
        ParamMap map = new ParamMap();
        Enumeration<String> parameterNames = vreq.getParameterNames();
        while(parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            if(parameterName.startsWith(FACET_FIELD_PREFIX)) {
                String parameterValue = vreq.getParameter(parameterName);
                String s = map.get(parameterName);
                if ((s != null) && (!StringUtils.isEmpty(s))) {
                    map.put(parameterName, s + ";;" + parameterValue);
                } else {
                    map.put(parameterName, parameterValue);
                }
            }
        }
        return map;
    }

    private static ParamMap RAPgetQueryParamMap(VitroRequest vreq) {
        ParamMap map = getFacetParamMap(vreq);
        String s = vreq.getParameter(PARAM_FACET_TEXT_VALUE);
        if(!StringUtils.isEmpty(s)) {
            map.put(PARAM_FACET_TEXT_VALUE, s);
            map.put(PARAM_FACET_AS_TEXT, vreq.getParameter(PARAM_FACET_AS_TEXT));
        }
        s = vreq.getParameter(PARAM_QUERY_TEXT);
        if(!StringUtils.isEmpty(s)) {
            map.put(PARAM_QUERY_TEXT, s);
        }
        s = vreq.getParameter(PARAM_RDFTYPE);
        if(!StringUtils.isEmpty(s)) {
            map.put(PARAM_RDFTYPE, s);
        } else {
            s = vreq.getParameter(PARAM_CLASSGROUP);
            if(!StringUtils.isEmpty(s)) {
                map.put(PARAM_CLASSGROUP, s);
            }
        }
        return map;
    }

    private static List RAPQueryReduce(VitroRequest vreq, VClassGroupDao grpDao, VClassDao vclassDao) {
        ParamMap map = RAPgetQueryParamMap(vreq);
        List<LinkTemplateModel> qr = new ArrayList<LinkTemplateModel>();

        String s = map.get(PARAM_QUERY_TEXT);
        if ((s != null) && (!StringUtils.isEmpty(s))) {
            map.remove(PARAM_QUERY_TEXT);
            qr.add(new LinkTemplateModel(s, "/search", map));
            map.put(PARAM_QUERY_TEXT, s);
        }
        s = map.get(PARAM_FACET_TEXT_VALUE);
        if ((s != null) && (!StringUtils.isEmpty(s))) {
            String label = map.get(PARAM_FACET_AS_TEXT);
            SearchFacet textFacet = RAPSearchFacets.getSearchFacetByFieldName(map.get(PARAM_FACET_AS_TEXT));
            if(textFacet != null && textFacet.isFacetAsText()) {
                label = textFacet.getPublicName();
            }
            map.remove(PARAM_FACET_TEXT_VALUE);
            qr.add(new LinkTemplateModel(label + ": " + s, "/search", map));
            map.put(PARAM_FACET_TEXT_VALUE, s);
        }
/*      Don't display or give the option to remove the class group for now, it adds to confusion when just viewing publication
        s = map.get(PARAM_CLASSGROUP);
        if ((s != null) && (!StringUtils.isEmpty(s))) {
            VClassGroup vcg = grpDao.getGroupByURI(s);
            String label = "";
            if( vcg == null ){
                label = s;
            } else {
                label = vcg.getPublicName();
            }
            map.remove(PARAM_CLASSGROUP);
            qr.add(new LinkTemplateModel("Type: " + label, "/search", map));
            map.put(PARAM_CLASSGROUP, s);
        }
        s = map.get(PARAM_RDFTYPE);
        if ((s != null) && (!StringUtils.isEmpty(s))) {
            VClass type = vclassDao.getVClassByURI(s);
            String label = "";
            if( type == null ){
                label = s;
            } else {
                label = type.getName();
            }
            map.remove(PARAM_RDFTYPE);
            qr.add(new LinkTemplateModel("Sub-Type: " + label, "/search", map));
            map.put(PARAM_RDFTYPE, s);
        }
*/
        for(String key : RAPSearchFacets.getFacetFields()) {
            s = map.get(key);
            if ((s != null) && (!StringUtils.isEmpty(s))) {
                String label = key;
                SearchFacet textFacet = RAPSearchFacets.getSearchFacetByFieldName(key);
                if(textFacet != null) {
                   label = textFacet.getPublicName();
                }
                if (s.contains(";;")) {
                    ArrayList<String> vals = new ArrayList(Arrays.asList(s.split(";;")));
                    for (int i = 0; i < vals.size(); i++) {
                        String val = vals.get(i);
                        String valSaved = vals.get(i);
                        if(val.startsWith("http://")) {
                            IndividualDao iDao = vreq.getWebappDaoFactory()
                                    .getIndividualDao();
                            Individual ind = iDao.getIndividualByURI(val);
                            if(ind != null) {
                                val = ind.getRdfsLabel();
                            }
                        }
                        vals.remove(i);
                        map.put(key, StringUtils.join(vals, ";;"));
                        vals.add(i, valSaved);
                        qr.add(new LinkTemplateModel(label + ": " + val, "/search", map));
                    }
                    map.put(key, s);
                } else {
                    String val = s;
                    if(val.startsWith("http://")) {
                        IndividualDao iDao = vreq.getWebappDaoFactory()
                                .getIndividualDao();
                        Individual ind = iDao.getIndividualByURI(val);
                        if(ind != null) {
                            val = ind.getRdfsLabel();
                        }
                    }
                    map.remove(key);
                    qr.add(new LinkTemplateModel(label + ": " + val, "/search", map));
                    map.put(key, s);
                }
            }
        }
        return qr;
    }

    public static class VClassGroupSearchLink extends LinkTemplateModel {
        long count = 0;
        VClassGroupSearchLink(VitroRequest vreq, String querytext, VClassGroup classgroup, long count) {
            super(classgroup.getPublicName(), "/search", VClassGroupSearchLinkMap(vreq, querytext, classgroup));
            this.count = count;
        }
        public String getCount() { return Long.toString(count); }
        private static ParamMap VClassGroupSearchLinkMap(VitroRequest vreq, String querytext, VClassGroup classgroup) {
            ParamMap map = RAPgetQueryParamMap(vreq);
            map.put(PARAM_QUERY_TEXT, querytext);
            map.put(PARAM_CLASSGROUP, classgroup.getURI());
            return map;
        }
    }

    public static class VClassSearchLink extends LinkTemplateModel {
        long count = 0;
        VClassSearchLink(VitroRequest vreq, String querytext, VClass type, long count) {
            super(type.getName(), "/search", VClassSearchLinkMap(vreq, querytext, type));
            this.count = count;
        }
        public String getCount() { return Long.toString(count); }
        private static ParamMap VClassSearchLinkMap(VitroRequest vreq, String querytext, VClass type) {
            ParamMap map = RAPgetQueryParamMap(vreq);
            map.put(PARAM_QUERY_TEXT, querytext);
            map.put(PARAM_RDFTYPE, type.getURI());
            return map;
        }
    }

    protected static List<PagingLink> getPagingLinks(int startIndex, int hitsPerPage, long hitCount, String baseUrl, ParamMap params, VitroRequest vreq) {

        List<PagingLink> pagingLinks = new ArrayList<PagingLink>();

        // No paging links if only one page of results
        if (hitCount <= hitsPerPage) {
            return pagingLinks;
        }

        int maxHitCount = DEFAULT_MAX_HIT_COUNT ;
        if( startIndex >= DEFAULT_MAX_HIT_COUNT  - hitsPerPage )
            maxHitCount = startIndex + DEFAULT_MAX_HIT_COUNT;

        for (int i = 0; i < hitCount; i += hitsPerPage) {
            params.put(PARAM_START_INDEX, String.valueOf(i));
            if ( i < maxHitCount - hitsPerPage) {
                int pageNumber = i/hitsPerPage + 1;
                boolean iIsCurrentPage = (i >= startIndex && i < (startIndex + hitsPerPage));
                if ( iIsCurrentPage ) {
                    pagingLinks.add(new PagingLink(pageNumber));
                } else {
                    pagingLinks.add(new PagingLink(pageNumber, baseUrl, params));
                }
            } else {
            	pagingLinks.add(new PagingLink(I18n.text(vreq, "paging_link_more"), baseUrl, params));
                break;
            }
        }

        return pagingLinks;
    }

    private String getPreviousPageLink(int startIndex, int hitsPerPage, String baseUrl, ParamMap params) {
        params.put(PARAM_START_INDEX, String.valueOf(startIndex-hitsPerPage));
        return UrlBuilder.getUrl(baseUrl, params);
    }

    private String getNextPageLink(int startIndex, int hitsPerPage, String baseUrl, ParamMap params) {
        params.put(PARAM_START_INDEX, String.valueOf(startIndex+hitsPerPage));
        return UrlBuilder.getUrl(baseUrl, params);
    }

    protected static class PagingLink extends LinkTemplateModel {

        PagingLink(int pageNumber, String baseUrl, ParamMap params) {
            super(String.valueOf(pageNumber), baseUrl, params);
        }

        // Constructor for current page item: not a link, so no url value.
        PagingLink(int pageNumber) {
            setText(String.valueOf(pageNumber));
        }

        // Constructor for "more..." item
        PagingLink(String text, String baseUrl, ParamMap params) {
            super(text, baseUrl, params);
        }
    }

    private ExceptionResponseValues doSearchError(Throwable e, Format f) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("message", "Search failed: " + e.getMessage());
        return new ExceptionResponseValues(getTemplate(f,Result.ERROR), body, e);
    }

    private TemplateResponseValues doFailedSearch(String message, String querytext, Format f, VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title", I18n.text(vreq, "search_for", querytext));
        if ( StringUtils.isEmpty(message) ) {
        	message = I18n.text(vreq, "search_failed");
        }
        body.put("message", message);
        return new TemplateResponseValues(getTemplate(f,Result.ERROR), body);
    }

    private TemplateResponseValues doNoHits(String querytext, Format f, VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title", I18n.text(vreq, "search_for", querytext));
        body.put("message", I18n.text(vreq, "no_matching_results"));
        body.put("querytext", querytext);
        body.put("facetsAsText", RAPSearchFacets.getSearchFacetsAsText());
        body.put(PARAM_FACET_AS_TEXT, vreq.getParameter(PARAM_FACET_AS_TEXT));
        body.put(PARAM_FACET_TEXT_VALUE, vreq.getParameter(PARAM_FACET_TEXT_VALUE));
        return new TemplateResponseValues(getTemplate(f,Result.ERROR), body);
    }

    /**
     * Makes a message to display to user for a bad search term.
     */
    private String makeBadSearchMessage(String querytext, String exceptionMsg, VitroRequest vreq){
        String rv = "";
        try{
            //try to get the column in the search term that is causing the problems
            int coli = exceptionMsg.indexOf("column");
            if( coli == -1) return "";
            int numi = exceptionMsg.indexOf(".", coli+7);
            if( numi == -1 ) return "";
            String part = exceptionMsg.substring(coli+7,numi );
            int i = Integer.parseInt(part) - 1;

            // figure out where to cut preview and post-view
            int errorWindow = 5;
            int pre = i - errorWindow;
            if (pre < 0)
                pre = 0;
            int post = i + errorWindow;
            if (post > querytext.length())
                post = querytext.length();
            // log.warn("pre: " + pre + " post: " + post + " term len:
            // " + term.length());

            // get part of the search term before the error and after
            String before = querytext.substring(pre, i);
            String after = "";
            if (post > i)
                after = querytext.substring(i + 1, post);

            rv = I18n.text(vreq, "search_term_error_near") +
            		" <span class='searchQuote'>"
                + before + "<span class='searchError'>" + querytext.charAt(i)
                + "</span>" + after + "</span>";
        } catch (Throwable ex) {
            return "";
        }
        return rv;
    }

    public static final int MAX_QUERY_LENGTH = 500;

    protected boolean isRequestedFormatXml(VitroRequest req){
        if( req != null ){
            String param = req.getParameter(PARAM_XML_REQUEST);
            if( param != null && "1".equals(param)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    protected boolean isRequestedFormatCSV(VitroRequest req){
        if( req != null ){
            String param = req.getParameter(PARAM_CSV_REQUEST);
            if( param != null && "1".equals(param)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    protected Format getFormat(VitroRequest req){
        if( req != null && req.getParameter("xml") != null && "1".equals(req.getParameter("xml")))
            return Format.XML;
        else if ( req != null && req.getParameter("csv") != null && "1".equals(req.getParameter("csv")))
        	return Format.CSV;
        else
            return Format.HTML;
    }

    protected static String getTemplate(Format format, Result result){
        if( format != null && result != null)
            return templateTable.get(format).get(result);
        else{
            log.error("getTemplate() must not have a null format or result.");
            return templateTable.get(Format.HTML).get(Result.ERROR);
        }
    }

    protected static Map<Format,Map<Result,String>> setupTemplateTable(){
        Map<Format,Map<Result,String>> table = new HashMap<>();

        HashMap<Result,String> resultsToTemplates = new HashMap<Result,String>();

        // set up HTML format
        resultsToTemplates.put(Result.PAGED, "search-pagedResults.ftl");
        resultsToTemplates.put(Result.ERROR, "search-error.ftl");
        // resultsToTemplates.put(Result.BAD_QUERY, "search-badQuery.ftl");
        table.put(Format.HTML, Collections.unmodifiableMap(resultsToTemplates));

        // set up XML format
        resultsToTemplates = new HashMap<Result,String>();
        resultsToTemplates.put(Result.PAGED, "search-xmlResults.ftl");
        resultsToTemplates.put(Result.ERROR, "search-xmlError.ftl");

        // resultsToTemplates.put(Result.BAD_QUERY, "search-xmlBadQuery.ftl");
        table.put(Format.XML, Collections.unmodifiableMap(resultsToTemplates));

        // set up CSV format
        resultsToTemplates = new HashMap<Result,String>();
        resultsToTemplates.put(Result.PAGED, "search-csvResults.ftl");
        resultsToTemplates.put(Result.ERROR, "search-csvError.ftl");

        // resultsToTemplates.put(Result.BAD_QUERY, "search-xmlBadQuery.ftl");
        table.put(Format.CSV, Collections.unmodifiableMap(resultsToTemplates));


        return Collections.unmodifiableMap(table);
    }
}
