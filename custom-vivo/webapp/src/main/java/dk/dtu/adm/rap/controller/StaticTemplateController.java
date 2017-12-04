/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package dk.dtu.adm.rap.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/*
 * Servlet that only specifies a template, without putting any data
 * into the template model. Page content is fully specified in the template.
 */
public class StaticTemplateController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(StaticTemplateController.class);
    
    protected ResponseValues processRequest(VitroRequest vreq) {
        String requestedUrl = vreq.getServletPath();
        String templateName = requestedUrl.substring (1) + ".ftl";
        
	log.debug("requestedUrl='" + requestedUrl + "', templateName='" + templateName + "'");
		
        return new TemplateResponseValues(templateName);
    }    
}
