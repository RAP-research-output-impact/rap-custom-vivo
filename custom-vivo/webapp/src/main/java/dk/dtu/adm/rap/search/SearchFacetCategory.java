package dk.dtu.adm.rap.search;

import edu.cornell.mannlib.vitro.webapp.search.controller.PagedSearchController;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.LinkTemplateModel;

public class SearchFacetCategory extends LinkTemplateModel {

    private boolean selected = false;
    long count;
    
    public SearchFacetCategory(String querytext, SearchFacet facet, 
            String label, String value, long count) {
        super(label, "/search", 
                PagedSearchController.PARAM_QUERY_TEXT, querytext, 
                facet.getFieldName(), value);
        this.count = count;
    }
    
    public long getCount() {
        return this.count;
    }
    
    public boolean selected() {
        return this.selected;
    }
    
}
