package dk.dtu.adm.rap.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
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
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
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

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class ExcelExport extends VitroHttpServlet {
    
    private static final long serialVersionUID = 1L;
    private final static Log log = LogFactory.getLog(ExcelExport.class);
    private final static String ORG_PARAM = "orgLocalName";
    private final static String STARTYEAR_PARAM = "startYear";
    private final static String ENDYEAR_PARAM = "endYear";
    private final static String DATA_SERVICE = "/vds/report/org/";
    private final static String THIS_SERVLET = "/excelExport";
    private static final String CONTENT_TYPE = 
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String DTU = "Technical University of Denmark";
    private static final String YEAR = "Year";
    private static final String IMPACT_FOOTNOTE = "* Citations per publication" 
                                                + " for the timespan selected.";
    private static final String LATENCY_FOOTNOTE = "* The number of"  
            + " publications for a year will not be complete until the middle"  
            + " of the following year due to latency of indexing publications" 
            + " in Web of Science.";
    private static final int LATENCY_FOOTNOTE_START_YEAR = 2018;
    private static final int SUBJECTS_CUTOFF = 20;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        VitroRequest vreq = new VitroRequest(request);
        if (!LoginStatusBean.getBean(vreq).isLoggedIn()) {
            response.setStatus(403);
            response.setContentType("text/plain");
            response.getWriter().write("Restricted to authenticated users.");
            return;
        }
        String orgLocalName = vreq.getParameter(ORG_PARAM);
        if(orgLocalName == null) {
            throw new ServletException("Parameter " + ORG_PARAM + " most be supplied");
        }
        String startYear = vreq.getParameter(STARTYEAR_PARAM);
        String endYear = vreq.getParameter(ENDYEAR_PARAM);
        try {
            JSONObject json = getJson(getBaseURI(vreq), orgLocalName, startYear, endYear, vreq);
            JSONObject byDeptJson = getByDeptJson(getBaseURI(vreq), orgLocalName, startYear, endYear, vreq);            
            XSSFWorkbook wb = generateWorkbook(json, byDeptJson);        
            response.setContentType(CONTENT_TYPE);
            OutputStream out = response.getOutputStream();        
            wb.write(out);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }       
    }
    
    private String getBaseURI(VitroRequest vreq) {
        return vreq.getRequestURL().toString().split(Pattern.quote(THIS_SERVLET))[0];
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
            JSONObject json, JSONObject byDeptJson) throws JSONException {
        XSSFWorkbook wb = new XSSFWorkbook();
        generateWorksheet("Report", wb, json, byDeptJson, !DETAILS);
        generateWorksheet("Details", wb, json, byDeptJson, DETAILS);
        return wb;
    }
    
    private XSSFWorkbook generateWorksheet(String sheetName, XSSFWorkbook wb, 
            JSONObject json, JSONObject byDeptJson, boolean details) 
                    throws JSONException {
        XSSFSheet sheet = wb.createSheet(sheetName);
        PropertyTemplate pt = new PropertyTemplate();
        RowCreator rowCreator = new RowCreator(sheet);
        List<Integer> years = getYears(json);
        try {
            addTitle(years, json, wb, sheet, rowCreator, pt);
        } catch (JSONException e) {
            log.error(e, e);
        }
        rowCreator.createRow();
        try {
            addSummary(years, json, wb, sheet, rowCreator, pt);
        } catch (JSONException e) {
            log.error(e, e);
        }
        rowCreator.createRow();
        rowCreator.createRow();
        try {
            addTotals(years, json, wb, sheet, rowCreator, pt);
        } catch (JSONException e) {
            log.error(e, e);
        }
        rowCreator.createRow();
        rowCreator.createRow();
        try {
            addTopCategories(json, wb, sheet, rowCreator, pt);
        } catch (JSONException e) {
            log.error(e, e);
        }
        rowCreator.createRow();
        rowCreator.createRow();
        try {
            addCategories(json, wb, sheet, rowCreator, pt);
        } catch (JSONException e) {
            log.error(e, e);
        }
        rowCreator.createRow();
        rowCreator.createRow();
        try {
            addByDepartment(byDeptJson, wb, sheet, rowCreator, pt, details);
        } catch (JSONException e) {
            log.error(e, e);
        }
        pt.applyBorders(sheet);     
        sheet.setColumnWidth(0, 7500);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 6000);
        sheet.setColumnWidth(3, 6000);
        addPicture(sheet, wb);
        return wb;
    }
    
    private void addPicture(XSSFSheet sheet, XSSFWorkbook workbook) {
        try {
            FileInputStream fis = new FileInputStream(new File("C:/Users/Brian Lowe/Desktop/marble24.png"));
            
            CreationHelper helper = workbook.getCreationHelper();
            final Drawing drawing = sheet.createDrawingPatriarch();

            final ClientAnchor anchor = helper.createClientAnchor();
            anchor.setAnchorType( ClientAnchor.AnchorType.MOVE_AND_RESIZE );


            String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" id=\"number-of-co-publications-per-year\" preserveAspectRatio=\"xMinYMin meet\" viewBox=\"0 0 800 300\" class=\"svg-content\" style=\"max-height: 500px; color: rgb(94, 99, 99); fill: rgb(94, 99, 99);\"><g fill=\"none\" font-size=\"10\" font-family=\"sans-serif\" text-anchor=\"end\" class=\"oy\" transform=\"translate(40,70)\" style=\"font-size: 14px;\"><path class=\"domain\" stroke=\"currentColor\" d=\"M-6,200.5H0.5V0.5H-6\"></path><g class=\"tick\" opacity=\"1\" transform=\"translate(0,200.5)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">0</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,186.21428571428572)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">1</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,171.92857142857144)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">2</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,157.64285714285714)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">3</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,143.35714285714286)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">4</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,129.07142857142856)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">5</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,114.78571428571429)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">6</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,100.5)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">7</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,86.21428571428572)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">8</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,71.92857142857142)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">9</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,57.64285714285714)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">10</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,43.35714285714286)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">11</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,29.071428571428584)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">12</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,14.785714285714278)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">13</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(0,0.5)\"><line stroke=\"currentColor\" x2=\"-6\"></line><text fill=\"currentColor\" x=\"-9\" dy=\"0.32em\" style=\"color: rgb(94, 99, 99);\">14</text></g></g><g fill=\"none\" font-size=\"10\" font-family=\"sans-serif\" text-anchor=\"middle\" class=\"ox\" transform=\"translate(40,270)\" style=\"font-size: 14px;\"><path class=\"domain\" stroke=\"currentColor\" d=\"M0.5,6V0.5H720.5V6\"></path><g class=\"tick\" opacity=\"1\" transform=\"translate(0.5,0)\"><line stroke=\"currentColor\" y2=\"6\"></line><text fill=\"currentColor\" y=\"9\" dy=\"0.71em\" style=\"color: rgb(94, 99, 99);\">2008</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(72.63796879277307,0)\"><line stroke=\"currentColor\" y2=\"6\"></line><text fill=\"currentColor\" y=\"9\" dy=\"0.71em\" style=\"color: rgb(94, 99, 99);\">2009</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(144.57883931015604,0)\"><line stroke=\"currentColor\" y2=\"6\"></line><text fill=\"currentColor\" y=\"9\" dy=\"0.71em\" style=\"color: rgb(94, 99, 99);\">2010</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(216.51970982753903,0)\"><line stroke=\"currentColor\" y2=\"6\"></line><text fill=\"currentColor\" y=\"9\" dy=\"0.71em\" style=\"color: rgb(94, 99, 99);\">2011</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(288.460580344922,0)\"><line stroke=\"currentColor\" y2=\"6\"></line><text fill=\"currentColor\" y=\"9\" dy=\"0.71em\" style=\"color: rgb(94, 99, 99);\">2012</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(360.5985491376951,0)\"><line stroke=\"currentColor\" y2=\"6\"></line><text fill=\"currentColor\" y=\"9\" dy=\"0.71em\" style=\"color: rgb(94, 99, 99);\">2013</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(432.53941965507806,0)\"><line stroke=\"currentColor\" y2=\"6\"></line><text fill=\"currentColor\" y=\"9\" dy=\"0.71em\" style=\"color: rgb(94, 99, 99);\">2014</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(504.480290172461,0)\"><line stroke=\"currentColor\" y2=\"6\"></line><text fill=\"currentColor\" y=\"9\" dy=\"0.71em\" style=\"color: rgb(94, 99, 99);\">2015</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(576.421160689844,0)\"><line stroke=\"currentColor\" y2=\"6\"></line><text fill=\"currentColor\" y=\"9\" dy=\"0.71em\" style=\"color: rgb(94, 99, 99);\">2016</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(648.559129482617,0)\"><line stroke=\"currentColor\" y2=\"6\"></line><text fill=\"currentColor\" y=\"9\" dy=\"0.71em\" style=\"color: rgb(94, 99, 99);\">2017</text></g><g class=\"tick\" opacity=\"1\" transform=\"translate(720.5,0)\"><line stroke=\"currentColor\" y2=\"6\"></line><text fill=\"currentColor\" y=\"9\" dy=\"0.71em\" style=\"color: rgb(94, 99, 99);\">2018</text></g></g><g class=\"serie\"><path d=\"M0,171.42857142857144L72.13796879277307,171.42857142857144L144.07883931015604,157.14285714285714L216.01970982753903,142.85714285714286L287.960580344922,0L360.0985491376951,42.85714285714286L432.03941965507806,57.14285714285714L503.980290172461,114.28571428571429L575.921160689844,100L648.059129482617,71.42857142857142L720,185.71428571428572\" transform=\"translate(40,70)\" class=\"chart-line\" stroke=\"#030F4F\" stroke-width=\"2\" fill=\"none\"></path><g class=\"labels\" font-size=\"14\" transform=\"translate(40,70)\"><g class=\"label\" transform=\"translate(0,171.42857142857144)\"><circle r=\"4\" fill=\"#030F4F\"></circle></g><g class=\"label\" transform=\"translate(72.13796879277307,171.42857142857144)\"><circle r=\"4\" fill=\"#030F4F\"></circle></g><g class=\"label\" transform=\"translate(144.07883931015604,157.14285714285714)\"><circle r=\"4\" fill=\"#030F4F\"></circle></g><g class=\"label\" transform=\"translate(216.01970982753903,142.85714285714286)\"><circle r=\"4\" fill=\"#030F4F\"></circle></g><g class=\"label\" transform=\"translate(287.960580344922,0)\"><circle r=\"4\" fill=\"#030F4F\"></circle></g><g class=\"label\" transform=\"translate(360.0985491376951,42.85714285714286)\"><circle r=\"4\" fill=\"#030F4F\"></circle></g><g class=\"label\" transform=\"translate(432.03941965507806,57.14285714285714)\"><circle r=\"4\" fill=\"#030F4F\"></circle></g><g class=\"label\" transform=\"translate(503.980290172461,114.28571428571429)\"><circle r=\"4\" fill=\"#030F4F\"></circle></g><g class=\"label\" transform=\"translate(575.921160689844,100)\"><circle r=\"4\" fill=\"#030F4F\"></circle></g><g class=\"label\" transform=\"translate(648.059129482617,71.42857142857142)\"><circle r=\"4\" fill=\"#030F4F\"></circle></g><g class=\"label\" transform=\"translate(720,185.71428571428572)\"><circle r=\"4\" fill=\"#030F4F\"></circle></g></g></g><text class=\"svg-title\" x=\"400\" y=\"16\" text-anchor=\"middle\" font-size=\"16\" style=\"color: rgb(94, 99, 99); font-weight: bold; font-size: 16px;\">Number of co-publications per year</text></svg>";
            
            //final int pictureIndex =
            //        workbook.addPicture(IOUtils.toByteArray(fis), Workbook.PICTURE_TYPE_PNG);

            ByteArrayOutputStream png = new ByteArrayOutputStream();
            
            PNGTranscoder transcoder = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(new StringReader(svg));
            TranscoderOutput output = new TranscoderOutput(png);
            
            try {
                transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, new Float(800));
                transcoder.transcode(input, output);
            } catch (TranscoderException e) {
                throw new RuntimeException(e);
            }
            
            final int pictureIndex =
                    workbook.addPicture(png.toByteArray(), Workbook.PICTURE_TYPE_PNG);
            
            anchor.setCol1( 0 );
            anchor.setRow1( 89 ); // same row is okay
            anchor.setRow2( 105 );
            anchor.setCol2( 3 );
            final Picture pict = drawing.createPicture( anchor, pictureIndex );
            //pict.resize();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static final boolean DETAILS = true;
    
    private String getOrgName(JSONObject data) throws JSONException {
        JSONObject summary = data.getJSONObject("summary");
        return summary.getString("name");
    }
    
    private int getTotalPubs(JSONObject data) throws JSONException {
        JSONObject summary = data.getJSONObject("summary");
        return summary.getInt("coPubTotal");
    }
    
    private int getTotalCategories(JSONObject data) throws JSONException {
        JSONObject summary = data.getJSONObject("summary");
        return summary.getInt("categories");
    }
    
    private void addTitle(List<Integer> years, JSONObject data, XSSFWorkbook wb, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        XSSFRow titleRow = rowCreator.createRow();
        titleRow.setHeightInPoints(25);
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 2));
        CellStyle titleStyle = wb.createCellStyle();
        XSSFFont titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 15);
        titleStyle.setFont(titleFont);
        XSSFCell titleCell = titleRow.createCell(0);        
        int startYear = years.get(0);
        int endYear = years.get(years.size() - 1);
        String yearsStr = Integer.toString(startYear);
        if(endYear != startYear) {
            yearsStr += "-" + Integer.toString(endYear);
        }
        titleCell.setCellValue("DTU collaboration with " + getOrgName(data) + ", " + yearsStr);
        titleCell.setCellStyle(titleStyle);
        rowCreator.createRow();
        XSSFRow subtitleRow = rowCreator.createRow();
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 2));
        String coPubTotal = Integer.toString(getTotalPubs(data));
        String categories = Integer.toString(getTotalCategories(data));
        XSSFCell subtitle = subtitleRow.createCell(0);
        CellStyle subtitleStyle = wb.createCellStyle();
        XSSFFont subtitleFont = wb.createFont();
        subtitleFont.setBold(true);
        subtitleFont.setFontHeightInPoints((short) 13);
        subtitleStyle.setFont(subtitleFont);
        subtitle.setCellStyle(subtitleStyle);
        subtitle.setCellValue(coPubTotal + " co-publications in " 
                + categories + " subject categories");
    }
    
    private void addSummary(List<Integer> years, JSONObject data, XSSFWorkbook wb, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        JSONObject summary = data.getJSONObject("summary");
        XSSFRow header = rowCreator.createRow();
        header.setHeightInPoints(30);
        int startingIndex = rowCreator.getRowIndex();
        CellStyle headerStyleFirstColumn = getHeaderStyleFirstColumn(wb);
        CellStyle headerStyleRemainingColumns = getHeaderStyleRemainingColumns(wb);
        XSSFCell blankHeader = addBoldText(wb, header, 0, " ");
        blankHeader.setCellStyle(headerStyleFirstColumn);
        XSSFCell orgHeader = addBoldText(wb, header, 1, getOrgName(data));
        orgHeader.setCellStyle(headerStyleRemainingColumns);
        XSSFCell dtuHeader = addBoldText(wb, header, 2, DTU);
        dtuHeader.setCellStyle(headerStyleRemainingColumns);
        XSSFRow row = rowCreator.createRow();
        XSSFCell cell = row.createCell(0);
        cell.setCellValue("Publications");
        cell.setCellStyle(getDataStyleText(wb));            
        cell = row.createCell(1);
        cell.setCellValue(summary.getInt("orgTotal"));
        cell.setCellStyle(getDataStyle(wb));
        cell = row.createCell(2);
        cell.setCellStyle(getDataStyle(wb));
        cell.setCellValue(summary.getInt("dtuTotal"));
        row = rowCreator.createRow();
        cell = row.createCell(0);
        cell.setCellValue("Citations");
        cell.setCellStyle(getDataStyleText(wb));            
        cell = row.createCell(1);
        cell.setCellValue(summary.getInt("orgCitesTotal"));
        cell.setCellStyle(getDataStyle(wb));
        cell = row.createCell(2);
        cell.setCellStyle(getDataStyle(wb));
        cell.setCellValue(summary.getInt("dtuCitesTotal"));
        row = rowCreator.createRow();
        cell = row.createCell(0);
        cell.setCellValue("Impact *");
        cell.setCellStyle(getDataStyleText(wb));            
        cell = row.createCell(1);
        cell.setCellValue(roundImpact(summary.getDouble("orgImpact")));
        cell.setCellStyle(getImpactStyle(wb));
        cell = row.createCell(2);
        cell.setCellStyle(getImpactStyle(wb));
        cell.setCellValue(roundImpact(summary.getDouble("dtuImpact")));
        drawBorders(3, pt, startingIndex, rowCreator);
        row = rowCreator.createRow();
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 2));
        addItalicText(wb, row, 0, IMPACT_FOOTNOTE);
    }
    
    private void addTotals(List<Integer> years, JSONObject data, XSSFWorkbook wb, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        boolean dtuDataAvailable = true;
        try {
            if(getTotal(data, "dtu_totals", years.get(0)) == null) {
                dtuDataAvailable = false;
            }
        } catch (JSONException e) {
            dtuDataAvailable = false;
        }
        XSSFRow header = rowCreator.createRow();
        header.setHeightInPoints(30);
        int startingIndex = rowCreator.getRowIndex();
        CellStyle headerStyleFirstColumn = getHeaderStyleFirstColumn(wb);
        CellStyle headerStyleRemainingColumns = getHeaderStyleRemainingColumns(wb);
        XSSFCell yearHeader = addBoldText(wb, header, 0, YEAR);
        yearHeader.setCellStyle(headerStyleFirstColumn);
        XSSFCell orgHeader = addBoldText(wb, header, 1, getOrgName(data));
        orgHeader.setCellStyle(headerStyleRemainingColumns);
        if(dtuDataAvailable) {
            XSSFCell dtuHeader = addBoldText(wb, header, 2, DTU);
            dtuHeader.setCellStyle(headerStyleRemainingColumns);
        }
        for(Integer year : years) {
            XSSFRow row = rowCreator.createRow();
            XSSFCell cell = row.createCell(0);
            if(year >= LATENCY_FOOTNOTE_START_YEAR) {
                cell.setCellValue(year + " *");
            } else {
                cell.setCellValue(year);
            }
            cell.setCellStyle(getDataStyleText(wb));
            Integer orgTotal = getTotal(data, "org_totals", year);
            cell = row.createCell(1);
            if(orgTotal != null) {
                cell.setCellValue(orgTotal);
            }
            cell.setCellStyle(getDataStyle(wb));
            if(dtuDataAvailable) {
                cell = row.createCell(2);
                cell.setCellStyle(getDataStyle(wb));
                Integer dtuTotal = getTotal(data, "dtu_totals", year);
                if(dtuTotal != null) {        
                    cell.setCellValue(dtuTotal);
                }
            }
        }
        drawBorders(dtuDataAvailable? 3 : 2, pt, startingIndex, rowCreator);
        XSSFRow row = rowCreator.createRow();
        row.setHeightInPoints(30);
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 2));
        addItalicText(wb, row, 0, LATENCY_FOOTNOTE);
        CellStyle style = wb.createCellStyle();
        style.setWrapText(true);
        XSSFCell cell = row.getCell(0);
        cell.setCellStyle(style);
    }
    
    private void addCategories(JSONObject data, XSSFWorkbook wb, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        XSSFRow header = rowCreator.createRow();
        header.setHeight((short) (header.getHeight() * 2));
        int startingIndex = rowCreator.getRowIndex();
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
        CellStyle headerStyleFirstColumn = getHeaderStyleFirstColumn(wb);
        CellStyle headerStyleRemainingColumns = getHeaderStyleRemainingColumns(wb);
        XSSFCell header0 = addBoldText(wb, header, 0, "Collaboration top research subjects");
        header0.setCellStyle(headerStyleFirstColumn);
        XSSFCell header2 = addBoldText(wb, header, 2, "Publications");
        header2.setCellStyle(headerStyleRemainingColumns);
        JSONArray array = data.getJSONArray("categories");
        addNameNumberArray(array, rowCreator, wb, sheet);
        drawBorders(3, pt, startingIndex, rowCreator);        
    }
    
    private void addTopCategories(JSONObject data, XSSFWorkbook wb, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        XSSFRow header = rowCreator.createRow();
        header.setHeight((short) (header.getHeight() * 2));
        int startingIndex = rowCreator.getRowIndex();
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
        CellStyle headerStyleFirstColumn = getHeaderStyleFirstColumn(wb);
        CellStyle headerStyleRemainingColumns = getHeaderStyleRemainingColumns(wb);
        XSSFCell header0 = addBoldText(wb, header, 0, "Partner's top research subjects");
        header0.setCellStyle(headerStyleFirstColumn);
        XSSFCell header2 = addBoldText(wb, header, 2, "Publications");
        header2.setCellStyle(headerStyleRemainingColumns);
        JSONArray array = data.getJSONArray("top_categories");
        addNameNumberArray(array, rowCreator, wb, sheet);
        drawBorders(3, pt, startingIndex, rowCreator);        
    }
    
    private void addNameNumberArray(JSONArray array, RowCreator rowCreator,
            XSSFWorkbook wb, XSSFSheet sheet) throws JSONException {
        for(int i = 0; i < array.length(); i++) {
            if(i == SUBJECTS_CUTOFF) {
                break;
            }
            JSONObject object = array.getJSONObject(i);
            int value = object.getInt("number");
            String name = object.getString("name");
            XSSFRow row = rowCreator.createRow();
            sheet.addMergedRegion(new CellRangeAddress(
                    rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(name);
            cell.setCellStyle(getDataStyleText(wb));
            cell = row.createCell(1);
            cell.setCellStyle(getDataStyleText(wb));
            cell = row.createCell(2);
            cell.setCellValue(value);
            cell.setCellStyle(getDataStyle(wb));
        }
    }
    
    private void addByDepartment(JSONObject data, XSSFWorkbook wb, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt, boolean details) 
                    throws JSONException {
        XSSFRow header = rowCreator.createRow();
        header.setHeight((short) (header.getHeight() * 2));
        int startingIndex = rowCreator.getRowIndex();
        CellStyle headerStyleFirstColumn = getHeaderStyleFirstColumn(wb);
        CellStyle headerStyleRemainingColumns = getHeaderStyleRemainingColumns(wb);
        XSSFCell header0 = addBoldText(wb, header, 0, "DTU department");
        header0.setCellStyle(headerStyleFirstColumn);
        XSSFCell header1 = addBoldText(wb, header, 1, "Publications");
        header1.setCellStyle(headerStyleRemainingColumns);
        if(details) {
            sheet.addMergedRegion(new CellRangeAddress(
                    rowCreator.rowIndex, rowCreator.rowIndex, 2, 3));
            XSSFCell header2 = addBoldText(wb, header, 2, data.getString("name") + " department");
            header2.setCellStyle(headerStyleRemainingColumns);            
        }
        JSONArray array = data.getJSONArray("departments");
        for(int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            int value = object.getInt("num");
            String name = object.getString("name");
            XSSFRow row = rowCreator.createRow();
            XSSFCell cell = addBoldText(wb, row, 0, name);
            cell.setCellStyle(getDataStyleText(wb));
            cell = row.createCell(1);
            cell.setCellValue(value);
            cell.setCellStyle(getDataStyle(wb));
            if(details) {
                sheet.addMergedRegion(new CellRangeAddress(
                        rowCreator.rowIndex, rowCreator.rowIndex, 2, 3));
                cell = row.createCell(2);
                cell.setCellValue(" ");
                cell.setCellStyle(getDataStyle(wb));
                cell = row.createCell(3);
                cell.setCellStyle(getDataStyle(wb));
                JSONArray depts = object.getJSONArray("sub_orgs");
                for(int j = 0; j < depts.length(); j++) {
                    JSONObject dept = depts.getJSONObject(j);
                    int total = dept.getInt("total");
                    String deptName = dept.getString("name");
                    row = rowCreator.createRow();
                    cell = row.createCell(0);
                    cell.setCellValue(" ");
                    cell.setCellStyle(getDataStyleText(wb));
                    cell = row.createCell(1);
                    cell.setCellValue(total);
                    cell.setCellStyle(getDataStyle(wb));
                    sheet.addMergedRegion(new CellRangeAddress(
                            rowCreator.rowIndex, rowCreator.rowIndex, 2, 3));
                    cell = row.createCell(2);
                    cell.setCellValue(deptName);
                    cell.setCellStyle(getDataStyleText(wb));
                    cell = row.createCell(3);
                    cell.setCellStyle(getDataStyleText(wb));
                }
                pt.drawBorders(new CellRangeAddress(
                        rowCreator.getRowIndex(), rowCreator.getRowIndex(), 0, 3),
                        BorderStyle.MEDIUM, IndexedColors.BLACK.getIndex(), BorderExtent.BOTTOM);
            }
        }
        if(!details) {
            drawBorders(2, pt, startingIndex, rowCreator);
        } else {
            drawBorders(4, pt, startingIndex, rowCreator);
        }
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
        CellStyle dataStyle = getBaseDataStyle(wb);
        dataStyle.setAlignment(HorizontalAlignment.LEFT);
        return dataStyle;
    }
    
    private CellStyle getImpactStyle(XSSFWorkbook wb) {
        CellStyle dataStyle = getBaseDataStyle(wb);
        dataStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat("0.00"));
        return dataStyle;        
    }
   
    private CellStyle getDataStyle(XSSFWorkbook wb) {
        CellStyle dataStyle = getBaseDataStyle(wb);
        dataStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat("#,##0"));
        return dataStyle;
    }
    
    private CellStyle getBaseDataStyle(XSSFWorkbook wb) {
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
        try {
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
        } catch (JSONException e) {
            log.debug(e, e);
            return null;
        }
    }

    private JSONObject getByDeptJson(String baseURI, String orgLocalName, 
            String startYear, String endYear, VitroRequest vreq) 
            throws ClientProtocolException, IOException, JSONException {
        return getJson(baseURI, orgLocalName, startYear, endYear, true, vreq);
    }
    
    private JSONObject getJson(String baseURI, String orgLocalName, 
            String startYear, String endYear, VitroRequest vreq) 
            throws ClientProtocolException, IOException, JSONException {
        return getJson(baseURI, orgLocalName, startYear, endYear, false, vreq);
    }
    
    private JSONObject getJson(String baseURI, String orgLocalName, 
            String startYear, String endYear, boolean byDepartment, VitroRequest vreq) 
            throws ClientProtocolException, IOException, JSONException {
        HttpClient httpClient = getHttpClient(vreq.getCookies());       
        String request = baseURI + DATA_SERVICE + orgLocalName;
        if(byDepartment) {
            request += "/by-dept";
        }
        if(startYear != null) {
            request += "/" + startYear;
            if(endYear != null) {
                request += "/" + endYear;
            }
        }
        log.debug("Requesting JSON from " + request);
        HttpGet get = new HttpGet(request);
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
                log.debug("Setting header " + headerName + " with value " + headerValue);
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
        BasicCookieStore cookieStore = new BasicCookieStore();
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.setCookieStore(cookieStore);
        // forward the caller's cookies to the web service for authentication
        for(int i = 0; i < cookies.length; i++) {
            Cookie c = cookies[i];            
            BasicClientCookie cookie = new BasicClientCookie(c.getName(), c.getValue());
            cookie.setDomain(c.getDomain());
            cookie.setPath(c.getPath());
            cookie.setSecure(c.getSecure());
            cookie.setVersion(c.getVersion());
            // Do we need to convert c.getMaxAge() to cookie.setExpiryDate() ? 
            cookieStore.addCookie(cookie);      
            log.debug("Setting cookie " + c.getName() + " with value " + c.getValue());
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
    
    private XSSFCell addItalicText(XSSFWorkbook wb, XSSFRow row, int column, 
            String text) {        
        XSSFFont boldFont = wb.createFont();
        boldFont.setItalic(true);
        XSSFRichTextString rtf = new XSSFRichTextString(text);
        rtf.applyFont(boldFont);
        XSSFCell cell = row.createCell(column);
        cell.setCellValue(rtf);
        return cell;
    }
     
    /* POI doesn't have a built-in format for a single decimal place,
    nor does it seem that there is any alternative for constructing
    custom formats.  So we will round to two decimal places and format 
    likewise. */
    private double roundImpact(double impactValue) {
        int scale = (int) Math.pow(10, 2);
        return (double) Math.round(impactValue * scale) / scale;  
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
