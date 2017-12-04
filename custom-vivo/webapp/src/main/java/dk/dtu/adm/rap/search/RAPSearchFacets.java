package dk.dtu.adm.rap.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RAPSearchFacets {

    private static List<SearchFacet> searchFacets = new ArrayList<SearchFacet>();
    private static List<SearchFacetAsText> searchFacetsAsText = new ArrayList<SearchFacetAsText>();
    private static Map<String, SearchFacet> facetsByFieldName = new HashMap<String, SearchFacet>();
    
    static {
        searchFacets.add(new SearchFacet("facet_wos-category_ss", "Subject categories"));
        searchFacets.add(new SearchFacet("facet_document-type_ss", "Document types"));
        searchFacets.add(new SearchFacet("facet_research-area_ss", "Research areas"));
        searchFacets.add(new SearchFacet("facet_publication-year_ss", "Publication years"));
        searchFacets.add(new SearchFacet("facet_organization-enhanced_ss", "Organi.-Enhanced"));
        searchFacets.add(new SearchFacet("facet_journal_ss", "Journals"));
        searchFacets.add(new SearchFacet("facet_conference_ss", "Conferences"));
        searchFacets.add(new SearchFacet("facet_country_ss", "Countries"));
        searchFacets.add(new SearchFacet("facet_funding-agency_ss", "Funding Agencies"));
        for(SearchFacet facet : searchFacets) {
            facetsByFieldName.put(facet.getFieldName(), facet);
            String textFieldName = facet.getFieldName().replaceAll(
                    Pattern.quote("_ss"), "_en")
                    .replaceAll(Pattern.quote("facet_"), "facetext_");
            SearchFacetAsText fat = new SearchFacetAsText(
                    textFieldName, facet.getPublicName());
            searchFacetsAsText.add(fat);
            facetsByFieldName.put(textFieldName, fat);
        }
    }
    
    public static List<SearchFacet> getSearchFacets() {
        ArrayList<SearchFacet> facets = new ArrayList<SearchFacet>();
        for(SearchFacet sf : searchFacets) {
            facets.add(new SearchFacet(sf.getFieldName(), sf.getPublicName()));
        }
        return facets;
    }
    
    public static List<SearchFacetAsText> getSearchFacetsAsText() {
        ArrayList<SearchFacetAsText> facets = new ArrayList<SearchFacetAsText>();
        for(SearchFacetAsText sf : searchFacetsAsText) {
            facets.add(new SearchFacetAsText(sf.getFieldName(), sf.getPublicName()));
        }
        return facets;
    }
    
    public static SearchFacet getSearchFacetByFieldName(String fieldName) {
        return facetsByFieldName.get(fieldName);
    }

    public static List<String> getFacetFields() {
        ArrayList<String> facets = new ArrayList<String>();
        for(SearchFacet sf : searchFacets) {
            facets.add(sf.getFieldName());
        }
        return facets;
    }
}
