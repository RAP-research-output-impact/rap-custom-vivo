package dk.dtu.adm.rap.controller;

import static edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevel.WORKING;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.api.VitroApiServlet;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.State;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerSetup;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

/**
 * A service to speed data loading by permitting the search index and 
 * reasoner to be unregistered and reregistered for listening to data changes,
 * and which allows reindexing and inference recomputation to be requested.
 * @author Brian Lowe
 *
 */
@Path("/indexingInference/")
public class IndexingInferenceService extends VitroApiServlet {
    
    private static final long serialVersionUID = 1L;
    @Context ServletContext context;
    private static final AuthorizationRequest REQUIRED_ACTIONS = SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION;

    private static final Log log = LogFactory.getLog(IndexingInferenceService.class);
    
    @Path("/status")
    @GET
    @Produces("application/json")
    public Response status(@Context HttpServletRequest request) {     
        if (!authorized(request)) {
            return unauthorizedResponse();
        }        
        return statusResponse();        
    }   
    
    @Path("/reasoner/register")
    @POST
    @Produces("application/json")
    public Response registerReasoner(@Context HttpServletRequest request) {
        if (!authorized(request)) {
            return unauthorizedResponse();
        }
        getSimpleReasoner().registerChangeListener();
        return statusResponse();
    }
    
    @Path("/reasoner/unregister")
    @POST
    @Produces("application/json")
    public Response unregisterReasoner(@Context HttpServletRequest request) {
        if (!authorized(request)) {
            return unauthorizedResponse();
        }
        getSimpleReasoner().unregisterChangeListener();
        return statusResponse();
    }
    
    @Path("/searchIndexer/register")
    @POST
    @Produces("application/json")
    public Response registerSearchIndexer(@Context HttpServletRequest request) {
        if (!authorized(request)) {
            return unauthorizedResponse();
        }
        SearchIndexerSetup.getInstance(context).registerChangeListener();
        return statusResponse();
    }
    
    @Path("/searchIndexer/unregister")
    @POST
    @Produces("application/json")
    public Response unregisterSearchIndexer(@Context HttpServletRequest request) {
        if (!authorized(request)) {
            return unauthorizedResponse();
        }
        SearchIndexerSetup.getInstance(context).unregisterChangeListener();
        return statusResponse();
    }
    
    @Path("reasoner/recompute")
    @POST
    @Produces("application/json")
    public Response recompute(@Context HttpServletRequest request) {
        if (!authorized(request)) {
            return unauthorizedResponse();
        }
        SimpleReasoner reasoner = getSimpleReasoner();
        if (!reasoner.isRecomputing()) {            
            VitroBackgroundThread thread = new VitroBackgroundThread(
                    new Recomputer((reasoner)),
                    "SimpleReasonerRecomputController.Recomputer");
            thread.setWorkLevel(WORKING);
            thread.start();
        }
        int maxTries = 20;
        while( (!reasoner.isRecomputing()) && (maxTries > 0) ) {
            maxTries -- ;
            try {
              Thread.sleep(100); // ms
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return statusResponse();
    }
    
    @Path("searchIndexer/index")
    @POST
    @Produces("application/json")
    public Response index(@Context HttpServletRequest request) {
        if (!authorized(request)) {
            return unauthorizedResponse();
        }
        SearchIndexer searchIndexer = ApplicationUtils.instance().getSearchIndexer();
        searchIndexer.rebuildIndex();
        int maxTries = 20;
        while( (State.IDLE.equals(searchIndexer.getStatus().getState()))
                && (maxTries > 0) ) {
            maxTries -- ;
            try {
              Thread.sleep(100); // ms
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return statusResponse();
    }
    
    private Response statusResponse() {
        ResponseBuilder builder = Response.ok(getStatus().toString());
        return builder.build();
    }
    
    private Response unauthorizedResponse() {
        return Response.status(403).type("text/plain").entity(
                "Restricted to authenticated users").build();
    }
    
    private JSONObject getStatus() {
        SimpleReasoner reasoner = getSimpleReasoner();
        SearchIndexerSetup search = SearchIndexerSetup.getInstance(context);
        JSONObject jo = new JSONObject();
        try {
            jo.put("reasonerRegisteredForChanges", reasoner.isRegistered());
            jo.put("searchIndexerRegisteredForChanges", search.isRegistered());
            jo.put("reasonerIsRecomputing", reasoner.isRecomputing());
            jo.put("searchIndexerIsIndexing", !(State.IDLE.equals(
                    ApplicationUtils.instance().getSearchIndexer().getStatus().getState())));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jo;
    }
    
    private SimpleReasoner getSimpleReasoner() {
        Object sr = context.getAttribute(SimpleReasoner.class.getName());
        if (!(sr instanceof SimpleReasoner)) {
            throw new RuntimeException("No SimpleReasoner has been set up.");            
        } else {
            return (SimpleReasoner) sr;
        }
    }
    
    private boolean authorized(HttpServletRequest request) {
        VitroRequest vreq = new VitroRequest(request);
        try {
            confirmAuthorization(request, REQUIRED_ACTIONS);
            return true;
        } catch (AuthException e) {
            // Not authorized by email/password params.  Try remaining options.
        }
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
    
    // private inner class copied from SimpleReasoner
    private class Recomputer implements Runnable {

        private SimpleReasoner simpleReasoner;

        public Recomputer(SimpleReasoner simpleReasoner) {
            this.simpleReasoner = simpleReasoner;
        }

        public void run() {
            simpleReasoner.recompute();
        }

    }
    
}
