package dk.dtu.adm.rap.utils;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

//import java.io;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Created by franck on 2018-04-10
 */
public class DataCache {
    private static final Log log = LogFactory.getLog(DataCache.class.getName());

    public void write(String path, String key, String data, long time) {
        File root = new File(path);
        if (!root.exists()) {
            if (root.mkdirs()) {
                log.info("created new cache root: " + path);
            } else {
                log.error("could not create new cache root: " + path);
                return;
            }
        }
        try {
            FileUtils.writeStringToFile(new File(path + "/" + key), data); 
        } catch (IOException e) {
            log.error("failed to write to: " + path + "/" + key);
        }
        try {
            FileUtils.writeStringToFile(new File(path + "/cache.log"), Long.toString(time) + "\t" + key + "\n", true); 
        } catch (IOException e) {
            log.error("failed to append to: " + path + "/cache.log");
        }
    }

    public String read(String path, String key) {
        File file = new File(path, key);
        if (!file.exists()) {
            log.info("cache miss for: " + key);
            return null;
        }
        try {
            return FileUtils.readFileToString(new File(path + "/" + key));
        } catch (IOException e) {
            log.error("failed to read: " + path + "/" + key);
            return null;
        }
    }
}
