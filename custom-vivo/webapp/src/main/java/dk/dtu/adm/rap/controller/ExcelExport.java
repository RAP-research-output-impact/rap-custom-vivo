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
    //private static final int WIDTH = 3;
    
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
            // TODO pass the year parameters to the service
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
        rowCreator.createRow();
        addCategories(json, wb, sheet, rowCreator, pt);
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
        CellStyle headerStyleFirstColumn = getHeaderStyleFirstColumn(wb);
        CellStyle headerStyleRemainingColumns = getHeaderStyleRemainingColumns(wb);
        XSSFCell yearHeader = addBoldText(wb, header, 0, YEAR);
        yearHeader.setCellStyle(headerStyleFirstColumn);
        XSSFCell orgHeader = addBoldText(wb, header, 1, getOrgName(data));
        orgHeader.setCellStyle(headerStyleRemainingColumns);
        XSSFCell dtuHeader = addBoldText(wb, header, 2, DTU);
        dtuHeader.setCellStyle(headerStyleRemainingColumns);
        for(Integer year : years) {
            XSSFRow row = rowCreator.createRow();
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(year);
            cell.setCellStyle(getDataStyleText(wb));
            cell.setCellStyle(getDataStyle(wb));
            Integer orgTotal = getTotal(data, "org_totals", year);
            cell = row.createCell(1);
            if(orgTotal != null) {
                cell.setCellValue(orgTotal);

            }
            cell = row.createCell(2);
            cell.setCellStyle(getDataStyle(wb));
            Integer dtuTotal = getTotal(data, "dtu_totals", year);
            if(dtuTotal != null) {        
                cell.setCellValue(dtuTotal);
            }
        }
        drawBorders(3, pt, startingIndex, rowCreator);
    }
    
    private void addCategories(JSONObject data, XSSFWorkbook wb, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        int startingIndex = rowCreator.getRowIndex();
        XSSFRow header = rowCreator.createRow();
        CellStyle headerStyleFirstColumn = getHeaderStyleFirstColumn(wb);
        CellStyle headerStyleRemainingColumns = getHeaderStyleRemainingColumns(wb);
        XSSFCell header0 = addBoldText(wb, header, 0, "Partner's top research subjects");
        header0.setCellStyle(headerStyleFirstColumn);
        XSSFCell header1 = header.createCell(1);
        header1.setCellStyle(headerStyleRemainingColumns);
        XSSFCell header2 = addBoldText(wb, header, 2, "Publications");
        header2.setCellStyle(headerStyleRemainingColumns);
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
        JSONArray array = data.getJSONArray("categories");
        for(int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            int value = object.getInt("number");
            String name = object.getString("name");
            XSSFRow row = rowCreator.createRow();
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(name);
            cell.setCellStyle(getDataStyleText(wb));
            cell = row.createCell(1);
            cell.setCellStyle(getDataStyle(wb));
            cell = row.createCell(2);
            cell.setCellValue(value);
            cell.setCellStyle(getDataStyle(wb));
            sheet.addMergedRegion(new CellRangeAddress(
                    rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
        }
        drawBorders(3, pt, startingIndex, rowCreator);        
    }
    
    private void drawBorders(int width, PropertyTemplate pt, int startingIndex, RowCreator rowCreator) {
        pt.drawBorders(new CellRangeAddress(
                startingIndex, startingIndex, 0, width - 1),
                BorderStyle.MEDIUM, IndexedColors.BLACK.getIndex(), BorderExtent.HORIZONTAL);
        pt.drawBorders(new CellRangeAddress(
                startingIndex, rowCreator.getRowIndex(), 0, 0),
                BorderStyle.MEDIUM, IndexedColors.BLACK.getIndex(), BorderExtent.LEFT);
        pt.drawBorders(new CellRangeAddress(
                startingIndex, rowCreator.getRowIndex(), width - 1, width - 1),
                BorderStyle.MEDIUM, IndexedColors.BLACK.getIndex(), BorderExtent.RIGHT);
        pt.drawBorders(new CellRangeAddress(
                rowCreator.getRowIndex(), rowCreator.getRowIndex(), 0, width - 1),
                BorderStyle.MEDIUM, IndexedColors.BLACK.getIndex(), BorderExtent.BOTTOM);
    }
    
    private CellStyle getDataStyleText(XSSFWorkbook wb) {
        CellStyle dataStyle = getDataStyle(wb);
        dataStyle.setAlignment(HorizontalAlignment.LEFT);
        return dataStyle;
    }
    
    private CellStyle getDataStyle(XSSFWorkbook wb) {
        CellStyle dataStyle = wb.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        dataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        return dataStyle;
    }
    
    private CellStyle getHeaderStyleFirstColumn(XSSFWorkbook wb) {
        CellStyle headerStyle = getHeaderStyle(wb);
        headerStyle.setAlignment(HorizontalAlignment.LEFT);
        return headerStyle;
    }
    
    private CellStyle getHeaderStyleRemainingColumns(XSSFWorkbook wb) {
        CellStyle headerStyle = getHeaderStyle(wb);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        return headerStyle;
    }
    
    private CellStyle getHeaderStyle(XSSFWorkbook wb) {
        CellStyle headerStyle = wb.createCellStyle();        
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderTop(BorderStyle.THIN);       
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setWrapText(true);
        return headerStyle;
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