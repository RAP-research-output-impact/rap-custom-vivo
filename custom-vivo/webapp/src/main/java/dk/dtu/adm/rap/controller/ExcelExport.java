package dk.dtu.adm.rap.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class ExcelExport extends VitroHttpServlet {

    private static final long serialVersionUID = 1L;
    private final static Log log = LogFactory.getLog(ExcelExport.class);
    private final static String ORG_PARAM = "orgLocalName";
    private final static String DATA_SERVICE = "/vds/report/org/";
    private final static String THIS_SERVLET = "/excelExport";
    private static final String CONTENT_TYPE = 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EXTENSION = "xslx";
    private static final String DTU = "Technical University of Denmark";
    private static final String YEAR = "Year";
    private static final int WIDTH = 3;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        VitroRequest vreq = new VitroRequest(request);
        String orgLocalName = vreq.getParameter(ORG_PARAM);
        if(orgLocalName == null) {
            throw new ServletException("Parameter " + ORG_PARAM + " most be supplied");
        }
        JSONObject json;
        try {
            json = getJson(getBaseURI(vreq), orgLocalName, vreq);
            XSSFWorkbook wb = generateWorkbook(json);        
            response.setContentType(CONTENT_TYPE);
            OutputStream out = response.getOutputStream();        
            wb.write(out);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }       
    }
    
    private String getBaseURI(VitroRequest vreq) {
        return vreq.getRequestURL().toString().split("\\?")[0]
                .replaceAll(THIS_SERVLET, "");
    }
    
    private List<Integer> getYears(JSONObject json) throws JSONException {
        List<Integer> years = new ArrayList<Integer>();
        org.json.JSONArray orgTotals = json.getJSONArray("org_totals");
        for(int i = 0; i < orgTotals.length(); i++) {
            JSONObject total = orgTotals.getJSONObject(i);
            years.add(total.getInt("year"));
        }
        Collections.sort(years);
        return years;
    }

    private XSSFWorkbook generateWorkbook(
            JSONObject json) throws JSONException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Report");
        PropertyTemplate pt = new PropertyTemplate();
        RowCreator rowCreator = new RowCreator(sheet);
        List<Integer> years = getYears(json);
        rowCreator.createRow();
        addTotals(years, json, wb, sheet, rowCreator, pt);
        pt.applyBorders(sheet);
//        for (int i = 0; i < WIDTH; i++) {
//            try {
//                if(i != 4) {
//                    sheet.autoSizeColumn(i);
//                }
//            } catch (Exception e) {
//                log.error("Unable to set width of column " + i);
//            }
//        }        
        sheet.setColumnWidth(0, 7500);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 6000);
        return wb;
    }
    
    private String getOrgName(JSONObject data) throws JSONException {
        JSONObject summary = data.getJSONObject("summary");
        return summary.getString("name");
    }
    
    private void addTotals(List<Integer> years, JSONObject data, XSSFWorkbook wb, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        int startingIndex = rowCreator.getRowIndex();
        XSSFRow header = rowCreator.createRow();
        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setWrapText(true);
        CellStyle dataStyle = wb.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        dataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFCell yearHeader = addBoldText(wb, header, 0, YEAR);
        yearHeader.setCellStyle(headerStyle);
        XSSFCell orgHeader = addBoldText(wb, header, 1, getOrgName(data));
        orgHeader.setCellStyle(headerStyle);
        XSSFCell dtuHeader = addBoldText(wb, header, 2, DTU);
        dtuHeader.setCellStyle(headerStyle);
        for(Integer year : years) {
            XSSFRow row = rowCreator.createRow();
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(year);
            cell.setCellStyle(dataStyle);
            Integer orgTotal = getTotal(data, "org_totals", year);
            if(orgTotal != null) {
                cell = row.createCell(1);
                cell.setCellValue(orgTotal);
                cell.setCellStyle(dataStyle);
            }
            Integer dtuTotal = getTotal(data, "dtu_totals", year);
            if(dtuTotal != null) {        
                cell = row.createCell(2);
                cell.setCellValue(dtuTotal);
                cell.setCellValue(dtuTotal);
            }
        }
        pt.drawBorders(new CellRangeAddress(
                startingIndex, startingIndex, 0, WIDTH - 1),
                BorderStyle.MEDIUM, IndexedColors.BLACK.getIndex(), BorderExtent.HORIZONTAL);
        pt.drawBorders(new CellRangeAddress(
                startingIndex, rowCreator.getRowIndex(), 0, 0),
                BorderStyle.MEDIUM, IndexedColors.BLACK.getIndex(), BorderExtent.LEFT);
        pt.drawBorders(new CellRangeAddress(
                startingIndex, rowCreator.getRowIndex(), WIDTH - 1, WIDTH - 1),
                BorderStyle.MEDIUM, IndexedColors.BLACK.getIndex(), BorderExtent.RIGHT);
        pt.drawBorders(new CellRangeAddress(
                rowCreator.getRowIndex(), rowCreator.getRowIndex(), 0, WIDTH - 1),
                BorderStyle.MEDIUM, IndexedColors.BLACK.getIndex(), BorderExtent.BOTTOM);
    }
    
    private Integer getTotal(JSONObject data, String arrayName, int year) 
            throws JSONException {
        JSONArray array = data.getJSONArray(arrayName);
        // This should be faster than making a hashmap unless we have an
        // insane number of years
        for(int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            int value = object.getInt("year");
            if(year == value) {
                return object.getInt("number");
            }
        }
        return null;
    }

    private JSONObject getJson(String baseURI, String orgLocalName, VitroRequest vreq) 
            throws ClientProtocolException, IOException, JSONException {
        HttpClient httpClient = getHttpClient(vreq.getCookies());       
        String request = baseURI + DATA_SERVICE + orgLocalName;
        log.info("Requesting JSON from " + request);
        HttpGet get = new HttpGet(request);
        log.info("Setting headers on GET");
        Enumeration<String> headerNames = vreq.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if("accept".equalsIgnoreCase(headerName)) {
                // we don't want to ask for HTML or whatever the browser wanted
                continue;
            }
            Enumeration<String> headerValues = vreq.getHeaders(headerName);
            while(headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                get.setHeader(headerName, headerValue);
                log.info("Setting header " + headerName + " with value " + headerValue);
            }
        }
        HttpResponse response = null;
        try {
            response = httpClient.execute(get);
            String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");                
            JSONObject json = new JSONObject(jsonStr);
            return json;
        } finally {
            if(response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }
    
    private HttpClient getHttpClient(Cookie[] cookies) {
        //HttpClient httpClient = HttpClientFactory.getHttpClient();        
        BasicCookieStore cookieStore = new BasicCookieStore();
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.setCookieStore(cookieStore);
        // forward the caller's cookies to the web service for authentication
        log.info("Setting cookies");
        for(int i = 0; i < cookies.length; i++) {
            Cookie c = cookies[i];            
            BasicClientCookie cookie = new BasicClientCookie(c.getName(), c.getValue());
            cookie.setDomain(c.getDomain());
            cookie.setPath(c.getPath());
            cookie.setSecure(c.getSecure());
            cookie.setVersion(c.getVersion());
            // Do we need to convert c.getMaxAge() to cookie.setExpiryDate() ? 
            cookieStore.addCookie(cookie);      
            log.info("Setting cookie " + c.getName() + " with value " + c.getValue());
        }
        return httpClient;
    }
    
    private XSSFCell addBoldText(XSSFWorkbook wb, XSSFRow row, int column, 
            String text) {        
        XSSFFont boldFont = wb.createFont();
        boldFont.setBold(true);
        XSSFRichTextString rtf = new XSSFRichTextString(text);
        rtf.applyFont(boldFont);
        XSSFCell cell = row.createCell(column);
        cell.setCellValue(rtf);
        return cell;
    }
    
    private class RowCreator {

        private int rowIndex = -1;
        private XSSFSheet sheet;

        public RowCreator(XSSFSheet sheet) {
            this.sheet = sheet;
        }

        public XSSFRow createRow() {
            rowIndex = rowIndex + 1;
            return sheet.createRow(rowIndex);
        }

        public int getRowIndex() {
            return this.rowIndex;
        }

    }

}
