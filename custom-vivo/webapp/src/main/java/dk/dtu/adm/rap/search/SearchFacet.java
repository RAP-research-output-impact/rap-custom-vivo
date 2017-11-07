package dk.dtu.adm.rap.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a search facet with associated categories for display
 * on a search results page
 * @author Brian Lowe
 *
 */
public class SearchFacet {

    private String publicName;
    private String fieldName;
    private List<SearchFacetCategory> categories = new ArrayList<SearchFacetCategory>();
    
    public SearchFacet(String fieldName, String publicName) {
        this.fieldName = fieldName;
        this.publicName = publicName;
    }
    
    /**
     * @return the name of the field used to store the facet in the search index
     */
    public String getPublicName() {
        return this.publicName;
    }
    
    /**
     * set the name of the field used to store the facet in the search index
     */
    public void setPublicName(String publicName) {
        this.publicName = publicName;
    }
    
    /**
     * @return the name of the facet to display to the user
     */
    public String getFieldName() {
        return this.fieldName;
    }
    
    /**
     * Set the name of the facet to display to the user
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public List<SearchFacetCategory> getCategories() {
        return this.categories;
    }
    
    public boolean isFacetAsText() {
        return false;
    }
    
}

