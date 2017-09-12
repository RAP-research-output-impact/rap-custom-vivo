package dk.dtu.adm.rap.search;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.LinkTemplateModel;

public class SearchFacetCategory extends LinkTemplateModel {

    private boolean selected = false;
    long count;
    
    public SearchFacetCategory(String label, ParamMap facetParams, long count) {
        super(label, "/search", facetParams);
        this.count = count;
    }
    
    public long getCount() {
        return this.count;
    }
    
    public boolean selected() {
        return this.selected;
    }
    
}
