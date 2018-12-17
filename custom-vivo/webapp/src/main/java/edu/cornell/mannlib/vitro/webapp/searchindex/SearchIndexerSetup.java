/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.search.controller.IndexController;
import edu.cornell.mannlib.vitro.webapp.search.controller.IndexHistory;
import edu.cornell.mannlib.vitro.webapp.startup.ComponentStartupStatusImpl;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;
import edu.cornell.mannlib.vitro.webapp.utils.developer.listeners.DeveloperDisabledChangeListener;

/**
 * Start the SearchIndexer. Create a listener on the RDFService and link it to
 * the indexer.
 * 
 * Create a history object as a listener and make it available to the
 * IndexController.
 * 
 * Create a listener that will call commit() on the SearchEngine every time it
 * hears a progress or completion event.
 */
public class SearchIndexerSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(SearchIndexerSetup.class);

	private ServletContext ctx;
	private Application app;
	private SearchIndexer searchIndexer;
	private IndexingChangeListener listener;
	private DeveloperDisabledChangeListener listenerWrapper;
	private IndexHistory history;
	
	/**
	 * RAP addition to return the SearchIndexerSetup object associated with a
	 * servlet context
	 * @param ctx
	 * @return null if contextInitialized() has not completed successfully
	 */
	public static SearchIndexerSetup getInstance(ServletContext ctx) {
	    return (SearchIndexerSetup)  ctx.getAttribute(
	            SearchIndexerSetup.class.getSimpleName());
	}
	
	   // RAP addition
    private boolean isRegistered;
    
    /**
     * Report whether the search indexer is registered to listen for data changes
     * @return true if registered, false if not
     */
    public boolean isRegistered() {
        return this.isRegistered;
    }
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {	    
		ctx = sce.getServletContext();
		app = ApplicationUtils.instance();
		
		// RAP addition
		ctx.setAttribute(SearchIndexerSetup.class.getSimpleName(), this);
		
		StartupStatus ss = StartupStatus.getBean(ctx);

		searchIndexer = app.getSearchIndexer();

		// A change listener, wrapped so it can respond to a developer flag.
		listener = new IndexingChangeListener(searchIndexer);
		listenerWrapper = new DeveloperDisabledChangeListener(listener,
		        Key.SEARCH_INDEX_SUPPRESS_MODEL_CHANGE_LISTENER);

		// RAP change
		registerChangeListener();

		this.history = new IndexHistory();
		searchIndexer.addListener(this.history);
		IndexController.setHistory(this.history);

		searchIndexer
		.startup(app, new ComponentStartupStatusImpl(this, ss));

		ss.info(this, "Setup of search indexer completed.");
		
	}
	
	/**
	 * RAP refactoring: separate method for registering change listener
	 */
	public void registerChangeListener() {
	    try {
	        RDFServiceUtils.getRDFServiceFactory(
	                ctx).registerJenaModelChangedListener(listenerWrapper);
	    } catch (RDFServiceException e) {
	        StartupStatus ss = StartupStatus.getBean(ctx);
	        ss.fatal(this, "Failed to register the model changed listener.", e);
	    }
	    this.isRegistered = true;
	}
	
	/**
     * RAP refactoring: separate method for unregistering change listener
     */
    public void unregisterChangeListener() {
        try {
            RDFServiceUtils.getRDFServiceFactory(
                    ctx).unregisterJenaModelChangedListener(listenerWrapper);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
        this.isRegistered = false;
    }

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		searchIndexer.shutdown(app);

		searchIndexer.removeListener(this.history);

		try {
			RDFServiceUtils.getRDFServiceFactory(ctx).unregisterJenaModelChangedListener(
					listenerWrapper);
		} catch (RDFServiceException e) {
			log.warn("Failed to unregister the indexing listener.");
		}
		listener.shutdown();
	}

}
