package dk.dtu.adm.rap.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.utils.StringUtils;
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
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
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
    private final static String SVG_1_PARAM = "svgStr1";
    private final static String SVG_2_PARAM = "svgStr2";
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
    private static final boolean THICK_BOTTOM_BORDER = true;
    private static final int HEADER_WIDTH = 7;
    
    private static final String NORMALIZED_CITATION_IMPACT_COMMENT = 
            "Normalized citation impact: Citations per publication normalised "
            + "for subject, year, and "
            + "publication type. The world average is equal to 1. Example: "
            + "A normalised citation impact of 1.23 means that the impact is "
            + "23% above the world average.";
    private static final String TOP_10_PERCENT_COMMENT = 
            "% of publications in top 10% most cited: \r\n" +  
            "Proportion of the publications belonging to the top 10% most cited "
            + "in a given subject category, year, and publication type.";
    private static final String TOP_1_PERCENT_COMMENT = 
            "% of publications in top 1% most cited\r\n" +  
            "Proportion of the publications belonging to the top 1% most cited "
            + "in a given subject category, year, and publication type.";
    private static final List<String> tocItems = Arrays.asList(
            "Collaboration overview",
            "Compare key output and impact indicators",
            "Compare annual publication and co-publication output",
            "Compare partner's top subjects with DTU and co-publications",
            "Compare top collaboration subjects with partner and DTU subjects",
            "Collaboration by DTU department",
            "Collaboration bu DTU researcher (top 20)",
            "Collaboration by funder (top 20)",
            "Notes and hints");
    
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
        if(StringUtils.isEmpty(startYear)) {
            startYear = null;
        }
        if(StringUtils.isEmpty(endYear)) {
            endYear = null;
        }
        String svgStr1 = vreq.getParameter(SVG_1_PARAM);
        String svgStr2 = vreq.getParameter(SVG_2_PARAM);
        try {
            JSONObject json = getJson(getBaseURI(vreq), orgLocalName, startYear, endYear, vreq);
            JSONObject byDeptJson = getByDeptJson(getBaseURI(vreq), orgLocalName, startYear, endYear, vreq);            
            XSSFWorkbook wb = generateWorkbook(json, byDeptJson, svgStr1, svgStr2);        
            response.setContentType(CONTENT_TYPE);
            OutputStream out = response.getOutputStream();        
            wb.write(out);
        } catch (JSONException e) {
            log.error(e, e);
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
            JSONObject json, JSONObject byDeptJson, String svgStr1, String svgStr2) throws JSONException {
        XSSFWorkbook wb = new XSSFWorkbook();
        generateWorksheet("Report", wb, json, byDeptJson, svgStr1, svgStr2, !DETAILS);
        generateWorksheet("Details", wb, json, byDeptJson, svgStr1, svgStr2, DETAILS);
        return wb;
    }
    
    private XSSFWorkbook generateWorksheet(String sheetName, XSSFWorkbook wb, 
            JSONObject json, JSONObject byDeptJson, String svgStr1, String svgStr2, boolean details) 
                    throws JSONException {
        long start = System.currentTimeMillis();
        XSSFSheet sheet = wb.createSheet(sheetName);
        WorkbookStyles wbs = new WorkbookStyles(wb);
        PropertyTemplate pt = new PropertyTemplate();
        RowCreator rowCreator = new RowCreator(sheet);
        List<Integer> years = getYears(json);
        try {
            addTitle(years, json, wbs, sheet, rowCreator, pt);
        } catch (Exception e) {
            log.error(e, e);
        }
        log.info((System.currentTimeMillis() - start) + " ms to add title");
        start = System.currentTimeMillis();
        int firstRowOfToc = rowCreator.getRowIndex();
        for(int i = 0; i < tocItems.size(); i++) {
            rowCreator.createRow();
        }
        addHorizontalRule(wbs, sheet, rowCreator);
        addHeaderRow("1. Collaboration overview", wbs.getTitleStyleThin(), wb, sheet, rowCreator);
        addTocRow(firstRowOfToc, 1, wbs, sheet, rowCreator);
        rowCreator.createRow();
        addContentRow("In total: " + getTotalPubs(json) + " co-publications in " 
                + getTotalCategories(json) + " subject categories", wb, sheet, rowCreator);
        rowCreator.createRow();
        addHeaderRow("Number of co-publications per year", wbs.getSubtitleStyle(), wb, sheet, rowCreator);
        try {
            addSvg(svgStr2, sheet, wb, rowCreator.getRowIndex() - 1, rowCreator.getRowIndex() - 1 + 18, 0, 7);
            for(int i = 0; i < 15; i++) {
                rowCreator.createRow();
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        log.info((System.currentTimeMillis() - start) + " to add first svg");
        start = System.currentTimeMillis();
        rowCreator.createRow();
        rowCreator.createRow();
        sheet.setRowBreak(rowCreator.getRowIndex());
        addHeaderRow("Number of co-publications by top research subjects", wbs.getSubtitleStyle(), wb, sheet, rowCreator);
        try {
            addSvg(svgStr1, sheet, wb, rowCreator.getRowIndex(), rowCreator.getRowIndex() + 30, 0, 7);
            for(int i = 0; i < 28; i++) {
                rowCreator.createRow();
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        log.info((System.currentTimeMillis() - start) + " ms to second svg");
        start = System.currentTimeMillis();
        rowCreator.createRow();
        addHorizontalRule(wbs, sheet, rowCreator);
        sheet.setRowBreak(rowCreator.getRowIndex());
        addHeaderRow("2. Compare key output and impact indicators", wbs.getTitleStyleThin(), wb, sheet, rowCreator);
        addTocRow(firstRowOfToc, 2, wbs, sheet, rowCreator);
        rowCreator.createRow();
        try {
            addSummary(years, json, wb, wbs, sheet, rowCreator, pt);
        } catch (Exception e) {
            log.error(e, e);
        }
        log.info((System.currentTimeMillis() - start) + " ms to add summary");
        start = System.currentTimeMillis();
        addHorizontalRule(wbs, sheet, rowCreator);
        addHeaderRow("3. Compare the annual publication and co-publication output", wbs.getTitleStyleThin(), wb, sheet, rowCreator);        
        addTocRow(firstRowOfToc, 3, wbs, sheet, rowCreator);
        rowCreator.createRow();
        try {
            addTotals(years, json, wb, wbs, sheet, rowCreator, pt);
        } catch (Exception e) {
            log.error(e, e);
        }
        log.info((System.currentTimeMillis() - start) + " ms to add totals");
        start = System.currentTimeMillis();
        addHorizontalRule(wbs, sheet, rowCreator);
        sheet.setRowBreak(rowCreator.getRowIndex());
        addHeaderRow("4. Compare partner's top subjects with DTU and co-publications", wbs.getTitleStyleThin(), wb, sheet, rowCreator);
        addTocRow(firstRowOfToc, 4, wbs, sheet, rowCreator);
        rowCreator.createRow();
        try {
            addTopCategories(json, wb, wbs, sheet, rowCreator, pt);
        } catch (Exception e) {
            log.error(e, e);
        }
        log.info((System.currentTimeMillis() - start) + " ms to add top subjects");
        start = System.currentTimeMillis();
        addHorizontalRule(wbs, sheet, rowCreator);
        sheet.setRowBreak(rowCreator.getRowIndex());
        addHeaderRow("5. Compare top collaboration subjects with partner and DTU subjects", wbs.getTitleStyleThin(), wb, sheet, rowCreator);
        addTocRow(firstRowOfToc, 5, wbs, sheet, rowCreator);
        rowCreator.createRow();
        try {
            addCategories(json, wb, wbs, sheet, rowCreator, pt);
        } catch (Exception e) {
            log.error(e, e);
        }
        log.info((System.currentTimeMillis() - start) + " ms to add top collaboration subjects");
        start = System.currentTimeMillis();
        addHorizontalRule(wbs, sheet, rowCreator);
        sheet.setRowBreak(rowCreator.getRowIndex());
        addHeaderRow("6. Collaboration by DTU department", wbs.getTitleStyleThin(), wb, sheet, rowCreator);
        addTocRow(firstRowOfToc, 6, wbs, sheet, rowCreator);
        rowCreator.createRow();
        try {
            addByDepartment(byDeptJson, wb, wbs, sheet, rowCreator, pt, details);
        } catch (Exception e) {
            log.error(e, e);
        }
        log.info((System.currentTimeMillis() - start) + " ms to add by dept");
        start = System.currentTimeMillis();
        addHorizontalRule(wbs, sheet, rowCreator);
        sheet.setRowBreak(rowCreator.getRowIndex());
        addHeaderRow("7. Collaboration by DTU researcher (top 20)", wbs.getTitleStyleThin(), wb, sheet, rowCreator);
        addTocRow(firstRowOfToc, 7, wbs, sheet, rowCreator);
        rowCreator.createRow();
        try {
            addDtuResearchers(json, wb, wbs, sheet, rowCreator, pt, details);
        } catch (Exception e) {
            log.error(e, e);
        }
        log.info((System.currentTimeMillis() - start) + " ms to add DTU researchers");
        start = System.currentTimeMillis();
        addHorizontalRule(wbs, sheet, rowCreator);
        sheet.setRowBreak(rowCreator.getRowIndex());
        addHeaderRow("8. Collaboration by funder (top 20)", wbs.getTitleStyleThin(), wb, sheet, rowCreator);
        addTocRow(firstRowOfToc, 8, wbs, sheet, rowCreator);
        rowCreator.createRow();
        try {
            addFunders(json, wb, wbs, sheet, rowCreator, pt);
        } catch (Exception e) {
            log.error(e, e);
        }
        log.info((System.currentTimeMillis() - start) + " ms to add funders");
        start = System.currentTimeMillis();
        addHorizontalRule(wbs, sheet, rowCreator);
        sheet.setRowBreak(rowCreator.getRowIndex());
        addHeaderRow("9. Notes and hints", wbs.getTitleStyleThin(), wb, sheet, rowCreator);
        addTocRow(firstRowOfToc, 9, wbs, sheet, rowCreator);
        addNotesAndHints(wb, wbs, sheet, rowCreator, pt);
        pt.applyBorders(sheet);     
        sheet.setColumnWidth(0, 7500);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 4000);
        sheet.setColumnWidth(5, 4000);
        sheet.setColumnWidth(6, 4000);
        sheet.setFitToPage(true);
        //sheet.setAutobreaks(true);
        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 0);
        return wb;
    }
    
    private void addHeaderRow(String content, CellStyle cellStyle, 
            XSSFWorkbook wb, XSSFSheet sheet, RowCreator rowCreator) {
        XSSFRow headerRow = rowCreator.createRow();
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, HEADER_WIDTH - 1));
        XSSFCell header = headerRow.createCell(0);
        header.setCellValue(content);
        header.setCellStyle(cellStyle);
    }
    
    private void addHorizontalRule(WorkbookStyles wbs, XSSFSheet sheet, 
            RowCreator rowCreator) {
        XSSFRow hrRow = rowCreator.createRow();
        for(int i = 0; i < HEADER_WIDTH; i ++) {
            XSSFCell cell = hrRow.createCell(i);
            cell.setCellValue(" ");
            cell.setCellStyle(wbs.getBottomBorderStyle());
        }
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, HEADER_WIDTH - 1));
    }
    
    private void addContentRow(String content, 
            XSSFWorkbook wb, XSSFSheet sheet, RowCreator rowCreator) {
        addContentRow(content, null, null, wb, sheet, rowCreator);
    }
    
    private void addContentRow(String content, Integer boldStart, Integer boldEnd, 
            XSSFWorkbook wb, XSSFSheet sheet, RowCreator rowCreator) {
        XSSFRow headerRow = rowCreator.createRow();
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, HEADER_WIDTH - 1));
        XSSFCell header = headerRow.createCell(0);        
        if(boldStart != null && boldEnd != null) {
            XSSFFont boldFont = wb.createFont();
            boldFont.setBold(true);
            XSSFRichTextString rtf = new XSSFRichTextString(content);
            rtf.applyFont(boldStart, boldEnd, boldFont);
            header.setCellValue(rtf);
        } else {
            header.setCellValue(content);
        }
    }
    
    private void addSvg(String svgStr, XSSFSheet sheet, XSSFWorkbook workbook, 
            int row1, int row2, int col1, int col2) {
        if(svgStr == null || sheet == null || workbook == null) {
            return;
        }            
        CreationHelper helper = workbook.getCreationHelper();
        final XSSFDrawing drawing = sheet.createDrawingPatriarch();
        final ClientAnchor anchor = helper.createClientAnchor();
        anchor.setAnchorType( ClientAnchor.AnchorType.MOVE_DONT_RESIZE );
        // add namespace because without it, Batik will balk
        if(!svgStr.contains("xmlns")) {
            svgStr = svgStr.replace("<svg", "<svg xmlns=\"http://www.w3.org/2000/svg\"");
        }        
        ByteArrayOutputStream png = new ByteArrayOutputStream();        
        PNGTranscoder transcoder = new PNGTranscoder();
        TranscoderInput input = new TranscoderInput(new StringReader(svgStr));
        TranscoderOutput output = new TranscoderOutput(png);        
        try {
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, new Float(500));
            transcoder.transcode(input, output);
        } catch (TranscoderException e) {
            throw new RuntimeException(e);
        }        
        int pictureIndex = workbook.addPicture(
                png.toByteArray(), Workbook.PICTURE_TYPE_PNG);              
        anchor.setRow1( row1 );
        anchor.setRow2( row2 );
        anchor.setCol1( col1 );
        anchor.setCol2( col2 );
        Picture pict = drawing.createPicture( anchor, pictureIndex );
        //pict.resize();        
    }
    
    private static final boolean DETAILS = true;
    
    private String getOrgName(JSONObject data) throws JSONException {
        JSONObject summary = data.getJSONObject("summary");
        return summary.getString("name");
    }
    
    private String getCountry(JSONObject data) throws JSONException {
        return data.getJSONObject("summary").getString("country");
    }
    
    private int getTotalPubs(JSONObject data) throws JSONException {
        JSONObject summary = data.getJSONObject("summary");
        return summary.getInt("coPubTotal");
    }
    
    private int getTotalCategories(JSONObject data) throws JSONException {
        JSONObject summary = data.getJSONObject("summary");
        return summary.getInt("categories");
    }
    
    private void addTitle(List<Integer> years, JSONObject data, WorkbookStyles wbs, 
            XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        XSSFRow titleRow = rowCreator.createRow();
        titleRow.setHeightInPoints(25);
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 4));
        
        XSSFCell titleCell = titleRow.createCell(0);        
        int startYear = years.get(0);
        int endYear = years.get(years.size() - 1);
        String yearsStr = Integer.toString(startYear);
        if(endYear != startYear) {
            yearsStr += "-" + Integer.toString(endYear);
        }
        titleCell.setCellValue(
                "DTU collaboration report for the timespan " + yearsStr);
        titleCell.setCellStyle(wbs.getTitleStyleBold());
        rowCreator.createRow();
        XSSFRow subtitleRow = rowCreator.createRow();
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 2));
        XSSFCell subtitle = subtitleRow.createCell(0);
        subtitle.setCellStyle(wbs.getSubtitleStyle());
        subtitle.setCellValue(getOrgName(data) + ", " + getCountry(data));
        rowCreator.createRow();
        XSSFRow contentsHeaderRow = rowCreator.createRow();
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 2));
        XSSFCell contentsHeader = contentsHeaderRow.createCell(0);
        contentsHeader.setCellValue("Contents:");
        contentsHeader.setCellStyle(wbs.getTitleStyleThin());
        rowCreator.createRow();      
    }
    
    private void addTocRow(int startOfToc, int index, WorkbookStyles wbs, 
        XSSFSheet sheet, RowCreator rowCreator) {
        int rowNum = startOfToc + index;
        XSSFRow tocRow = sheet.getRow(rowNum);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, HEADER_WIDTH));
        XSSFCell tocCell = tocRow.createCell(0);
        tocCell.setCellValue(index + ". " + tocItems.get(index - 1));
        tocCell.setCellStyle(wbs.getHyperlinkStyle());
        int target = rowCreator.getRowIndex() + 1;
        tocCell.setCellFormula
                ("HYPERLINK(\"#" + sheet.getSheetName() + "!A" 
                        + target + "\", \"" + index + ". " 
                        + tocItems.get(index - 1) + "\")");        
    }
    
    private void addSummary(List<Integer> years, JSONObject data, XSSFWorkbook wb, WorkbookStyles wbs, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        JSONObject summary = data.getJSONObject("summary");
        XSSFRow header = rowCreator.createRow();
        header.setHeightInPoints(45);
        int startingIndex = rowCreator.getRowIndex();
        CellStyle headerStyleFirstColumn = wbs.getHeaderStyleFirstColumn();
        CellStyle headerStyleRemainingColumns = wbs.getHeaderStyleRemainingColumns();
        XSSFCell blank0 = header.createCell(0);
        blank0.setCellStyle(headerStyleFirstColumn);
        XSSFCell blank1 = header.createCell(1);
        blank1.setCellStyle(headerStyleFirstColumn);
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
        XSSFCell orgHeader = addBoldText(wb, header, 2, "Partner");
        orgHeader.setCellStyle(headerStyleRemainingColumns);
        XSSFCell dtuHeader = addBoldText(wb, header, 3, "DTU");
        dtuHeader.setCellStyle(headerStyleRemainingColumns);
        addSummaryRow("Number of publications", 
                summary.getInt("orgTotal"), null, 
                summary.getInt("dtuTotal"), null, 
                Arrays.asList(wbs.getDataStyleText(), wbs.getDataStyle(), wbs.getDataStyle()), 
                null, wb, sheet, rowCreator);
        addSummaryRow("Number of citations", 
                summary.getInt("orgCitesTotal"), null, 
                summary.getInt("dtuCitesTotal"), null, 
                Arrays.asList(wbs.getDataStyleText(), wbs.getDataStyle(), wbs.getDataStyle()), 
                null, wb, sheet, rowCreator);
        addSummaryRow("Simple citation impact (citations / publication)", 
                null, roundImpact(summary.getDouble("orgImpact")), 
                null, roundImpact(summary.getDouble("dtuImpact")), 
                Arrays.asList(wbs.getDataStyleText(), wbs.getImpactStyle(), wbs.getImpactStyle()), 
                null, wb, sheet, rowCreator);
        addSummaryRow("Normalized citation impact (global average 1.0)", 
                null, roundImpact(summary.getDouble("orgimp")), 
                null, roundImpact(summary.getDouble("dtuimp")), 
                Arrays.asList(wbs.getDataStyleText(), wbs.getImpactStyle(), wbs.getImpactStyle()), 
                NORMALIZED_CITATION_IMPACT_COMMENT, wb, sheet, rowCreator);
        addSummaryRow("% of publications in top 10% most cited", 
                null, roundImpact(summary.getDouble("orgt10")), 
                null, roundImpact(summary.getDouble("dtut10")), 
                Arrays.asList(wbs.getDataStyleText(), wbs.getImpactStyle(), wbs.getImpactStyle()), 
                TOP_10_PERCENT_COMMENT, wb, sheet, rowCreator);
        addSummaryRow("% of publications in top 1% most cited", 
                null, roundImpact(summary.getDouble("orgt1")), 
                null, roundImpact(summary.getDouble("dtut1")), 
                Arrays.asList(wbs.getDataStyleText(), wbs.getImpactStyle(), wbs.getImpactStyle()), 
                TOP_1_PERCENT_COMMENT, wb, sheet, rowCreator);
        addSummaryRow("% of publications with industry collaboration", 
                null, roundImpact(summary.getDouble("orgcind")), 
                null, roundImpact(summary.getDouble("dtucind")), 
                Arrays.asList(wbs.getDataStyleText(), wbs.getImpactStyle(), wbs.getImpactStyle()), 
                null, wb, sheet, rowCreator);
        addSummaryRow("% of publications with international collaboration", 
                null, roundImpact(summary.getDouble("orgcint")), 
                null, roundImpact(summary.getDouble("dtucint")), 
                Arrays.asList(wbs.getDataStyleText(), wbs.getImpactStyle(), wbs.getImpactStyle()), 
                null, wb, sheet, rowCreator);
        drawBorders(4, pt, startingIndex, rowCreator);
        //row = rowCreator.createRow();
        //sheet.addMergedRegion(new CellRangeAddress(
        //        rowCreator.rowIndex, rowCreator.rowIndex, 0, 2));
        //addItalicText(wb, row, 0, IMPACT_FOOTNOTE);
    }
    
    private void addSummaryRow(String textValue, Integer orgInt, 
            Double orgDouble, Integer dtuInt, Double dtuDouble, 
            List<CellStyle> styles, String comment,
            XSSFWorkbook wb, XSSFSheet sheet, RowCreator rowCreator) {
        if(styles.size() != 3) {
            throw new RuntimeException("Expected 3 styles in summary row; found " + styles.size());
        }
        XSSFRow row = rowCreator.createRow();        
        XSSFCell cell = row.createCell(0);
        cell.setCellValue(textValue);
        cell.setCellStyle(styles.get(0));   
        if(comment != null) {
            addCellComment(comment, row, cell, wb, sheet);
        }
        cell = row.createCell(1);
        cell.setCellStyle(styles.get(0));
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
        cell = row.createCell(2);
        if(orgInt != null) {
            cell.setCellValue(orgInt.intValue());
        } else {
            cell.setCellValue(orgDouble.doubleValue());
        }
        cell.setCellStyle(styles.get(1));
        cell = row.createCell(3);
        if(dtuInt != null) {
            cell.setCellValue(dtuInt.intValue());
        } else {
            cell.setCellValue(dtuDouble.doubleValue());
        }
        cell.setCellStyle(styles.get(2));        
    }
    
    private void addCellComment(String commentStr, XSSFRow row, XSSFCell cell, 
            XSSFWorkbook wb, XSSFSheet sheet) {
        Drawing drawing = sheet.createDrawingPatriarch();
        CreationHelper helper = wb.getCreationHelper();        
        // Put comment in an area to the right of the table
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 2);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2(row.getRowNum() + 5);
        Comment comment = drawing.createCellComment(anchor);
        RichTextString rtf = helper.createRichTextString(commentStr);
        comment.setString(rtf);
        cell.setCellComment(comment);        
    }
    
    private void addTotals(List<Integer> years, JSONObject data, XSSFWorkbook wb, WorkbookStyles wbs, XSSFSheet sheet, 
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
        header.setHeightInPoints(45);
        int startingIndex = rowCreator.getRowIndex();
        CellStyle headerStyleFirstColumn = wbs.getHeaderStyleFirstColumn();
        CellStyle headerStyleRemainingColumns = wbs.getHeaderStyleRemainingColumns();
        XSSFCell yearHeader = addBoldText(wb, header, 0, YEAR);
        yearHeader.setCellStyle(headerStyleFirstColumn);
        XSSFCell orgHeader = addBoldText(wb, header, 1, "Partner pubs");
        orgHeader.setCellStyle(headerStyleRemainingColumns);
        if(dtuDataAvailable) {
            XSSFCell dtuHeader = addBoldText(wb, header, 2, "DTU pubs");
            dtuHeader.setCellStyle(headerStyleRemainingColumns);
        }
        XSSFCell copubHeader = addBoldText(wb, header, 3, "Co-pubs");
        copubHeader.setCellStyle(headerStyleRemainingColumns);
        for(Integer year : years) {
            XSSFRow row = rowCreator.createRow();
            XSSFCell cell = row.createCell(0);
            if(year >= LATENCY_FOOTNOTE_START_YEAR) {
                cell.setCellValue(year + " *");
            } else {
                cell.setCellValue(year);
            }
            cell.setCellStyle(wbs.getDataStyleText());
            Integer orgTotal = getTotal(data, "org_totals", year);
            cell = row.createCell(1);
            if(orgTotal != null) {
                cell.setCellValue(orgTotal);
            }
            cell.setCellStyle(wbs.getDataStyle());
            if(dtuDataAvailable) {
                cell = row.createCell(2);
                cell.setCellStyle(wbs.getDataStyle());
                Integer dtuTotal = getTotal(data, "dtu_totals", year);
                if(dtuTotal != null) {        
                    cell.setCellValue(dtuTotal);
                }
            }
            cell = row.createCell(3);
            cell.setCellStyle(wbs.getDataStyle());
            Integer copubTotal = getTotal(data, "copub_totals", year);
            if(copubTotal != null) {
                cell.setCellValue(copubTotal);
            }
        }
        drawBorders(4, pt, startingIndex, rowCreator);
        XSSFRow row = rowCreator.createRow();
        row.setHeightInPoints(30);
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 3));
        addItalicText(wb, row, 0, LATENCY_FOOTNOTE);
        CellStyle style = wb.createCellStyle();
        style.setWrapText(true);
        XSSFCell cell = row.getCell(0);
        cell.setCellStyle(style);
    }
    
    private void addTopCategories(JSONObject data, XSSFWorkbook wb, WorkbookStyles wbs, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        CellStyle headerStyleFirstColumn = wbs.getHeaderStyleFirstColumn(
                !THICK_BOTTOM_BORDER);
        CellStyle headerStyleRemainingColumns = wbs.getHeaderStyleRemainingColumns(
                !THICK_BOTTOM_BORDER);        
        XSSFRow preheader = rowCreator.createRow();
        int startingIndex = rowCreator.getRowIndex();
        preheader.setHeight((short) (preheader.getHeight() * 2));        
        XSSFCell preheader0 = addBoldText(wb, preheader, 0, "Research publication subjects");
        preheader0.setCellStyle(headerStyleFirstColumn);
        XSSFCell preheader1 = preheader.createCell(1);
        preheader1.setCellStyle(headerStyleFirstColumn);
        XSSFCell preheader2 = addBoldText(wb, preheader, 2, "Partner");
        preheader2.setCellStyle(headerStyleRemainingColumns);
        XSSFCell preheader3 = preheader.createCell(3);
        preheader3.setCellStyle(headerStyleFirstColumn);
        XSSFCell preheader4 = addBoldText(wb, preheader, 4, "DTU");
        preheader4.setCellStyle(headerStyleRemainingColumns);
        XSSFCell preheader6 = addBoldText(wb, preheader, 6, "Co-pubs");
        XSSFCell preheader5 = preheader.createCell(5);
        preheader5.setCellStyle(headerStyleFirstColumn);
        preheader6.setCellStyle(headerStyleRemainingColumns);
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 2, 3));
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 4, 5));
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex + 1, 6, 6));
        XSSFRow header = rowCreator.createRow();
        header.setHeight((short) (header.getHeight() * 2));                
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
        XSSFCell header0 = addBoldText(wb, header, 0, "Compare partner and DTU");
        header0.setCellStyle(headerStyleFirstColumn);
        XSSFCell header1 = preheader.createCell(1);
        header1.setCellStyle(headerStyleFirstColumn);
        XSSFCell header2 = addBoldText(wb, header, 2, "Pubs");
        header2.setCellStyle(headerStyleRemainingColumns);
        XSSFCell header3 = addBoldText(wb, header, 3, "Rank");
        header3.setCellStyle(headerStyleRemainingColumns);
        XSSFCell header4 = addBoldText(wb, header, 4, "Pubs");
        header4.setCellStyle(headerStyleRemainingColumns);
        XSSFCell header5 = addBoldText(wb, header, 5, "Rank");
        header5.setCellStyle(headerStyleRemainingColumns);
        int width = 7;        
        pt.drawBorders(new CellRangeAddress(
                rowCreator.getRowIndex(), rowCreator.getRowIndex(), 0,(width - 1)),
                BorderStyle.THIN, IndexedColors.BLACK.getIndex(), BorderExtent.BOTTOM);
        JSONArray array = data.getJSONArray("top_categories");
        addNameNumberArray(array, Arrays.asList(
                "number", "rank", "DTUnumber", "DTUrank", "copub"),
                rowCreator, wbs, sheet);
        drawBorders(width, pt, startingIndex, rowCreator);        
    }
    
    private void addCategories(JSONObject data, XSSFWorkbook wb, WorkbookStyles wbs, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        XSSFRow header = rowCreator.createRow();
        header.setHeight((short) (header.getHeight() * 2));
        int startingIndex = rowCreator.getRowIndex();
        CellStyle headerStyleFirstColumn = wbs.getHeaderStyleFirstColumn();
        CellStyle headerStyleRemainingColumns = wbs.getHeaderStyleRemainingColumns();
        XSSFCell header0 = addBoldText(wb, header, 0, "Collaboration publication subjects");
        header0.setCellStyle(headerStyleFirstColumn);
        XSSFCell header1 = header.createCell(1);
        header1.setCellStyle(headerStyleFirstColumn);
        XSSFCell header2 = addBoldText(wb, header, 2, "Co-pubs");
        header2.setCellStyle(headerStyleRemainingColumns);
        XSSFCell header3 = addBoldText(wb, header, 3, "Partner rank");
        header3.setCellStyle(headerStyleRemainingColumns);
        XSSFCell header4 = addBoldText(wb, header, 4, "DTU rank");
        header4.setCellStyle(headerStyleRemainingColumns);
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
        JSONArray array = data.getJSONArray("categories");
        addNameNumberArray(array, Arrays.asList("number", "rank", "DTUrank"), 
                rowCreator, wbs, sheet);
        drawBorders(5, pt, startingIndex, rowCreator);        
    }
    
    private void addFunders(JSONObject data, XSSFWorkbook wb, WorkbookStyles wbs, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) throws JSONException {
        XSSFRow header = rowCreator.createRow();
        header.setHeight((short) (header.getHeight() * 2));
        int startingIndex = rowCreator.getRowIndex();
        CellStyle headerStyleFirstColumn = wbs.getHeaderStyleFirstColumn();
        CellStyle headerStyleRemainingColumns = wbs.getHeaderStyleRemainingColumns();
        XSSFCell header0 = addBoldText(wb, header, 0, "Funder");
        header0.setCellStyle(headerStyleFirstColumn);
        XSSFCell header1 = header.createCell(1);
        header1.setCellStyle(headerStyleFirstColumn);
        XSSFCell header2 = addBoldText(wb, header, 2, "Co-pubs");
        header2.setCellStyle(headerStyleRemainingColumns);
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
        JSONArray array = data.getJSONArray("funders");
        addNameNumberArray(array, Arrays.asList("number"), 
                rowCreator, wbs, sheet);
        drawBorders(3, pt, startingIndex, rowCreator);        
    }
    
    private void addDtuResearchers(JSONObject data, XSSFWorkbook wb, WorkbookStyles wbs, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt, boolean details) throws JSONException {
        XSSFRow header = rowCreator.createRow();
        header.setHeight((short) (header.getHeight() * 2));
        int startingIndex = rowCreator.getRowIndex();
        CellStyle headerStyleFirstColumn = wbs.getHeaderStyleFirstColumn();
        CellStyle headerStyleRemainingColumns = wbs.getHeaderStyleRemainingColumns();
        XSSFCell header0 = addBoldText(wb, header, 0, "DTU Researcher");
        header0.setCellStyle(headerStyleFirstColumn);
        XSSFCell header1 = header.createCell(1);
        header1.setCellStyle(headerStyleFirstColumn);
        XSSFCell header2 = addBoldText(wb, header, 2, "Co-pubs");
        header2.setCellStyle(headerStyleRemainingColumns);
        sheet.addMergedRegion(new CellRangeAddress(
                rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
        if(details) {
            sheet.addMergedRegion(new CellRangeAddress(
                    rowCreator.rowIndex, rowCreator.rowIndex, 3, 4));
            XSSFCell header3 = addBoldText(wb, header, 3, "Partner researcher");
            header3.setCellStyle(headerStyleRemainingColumns);            
        }
        JSONArray array = data.getJSONArray("dtu_researchers");
        //addNameNumberArray(array, Arrays.asList("number"), 
        //        rowCreator, wb, sheet);
        for(int i = 0; i < array.length(); i++) {
            if(i == SUBJECTS_CUTOFF) {
                break;
            }
            JSONObject object = array.getJSONObject(i);
            String name = object.getString("name");
            XSSFRow row = rowCreator.createRow();
            sheet.addMergedRegion(new CellRangeAddress(
                    rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(name);
            cell.setCellStyle(wbs.getDataStyleText());
            cell = row.createCell(1);
            cell.setCellStyle(wbs.getDataStyleText());
            int columnPos = 1;
            columnPos++;
            cell = row.createCell(columnPos);
            cell.setCellValue(object.getInt("number"));
            cell.setCellStyle(wbs.getDataStyle());
            if(details) {
                sheet.addMergedRegion(new CellRangeAddress(
                        rowCreator.rowIndex, rowCreator.rowIndex, 3, 4));
                cell = row.createCell(3);
                cell.setCellValue(" ");
                cell.setCellStyle(wbs.getDataStyle());
                cell = row.createCell(4);
                cell.setCellStyle(wbs.getDataStyle());
                JSONArray partnerResrs = object.getJSONArray("partner_researchers");
                for(int j = 0; j < partnerResrs.length(); j++) {
                    JSONObject partnerRes = partnerResrs.getJSONObject(j);
                    int total = partnerRes.getInt("number");
                    String researcherName = partnerRes.getString("name");
                    row = rowCreator.createRow();
                    sheet.addMergedRegion(new CellRangeAddress(
                            rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
                    cell = row.createCell(0);
                    cell.setCellValue(" ");
                    cell.setCellStyle(wbs.getDataStyleText());
                    cell = row.createCell(1);
                    cell.setCellValue(" ");
                    cell.setCellStyle(wbs.getDataStyleText());
                    cell = row.createCell(2);
                    cell.setCellValue(total);
                    cell.setCellStyle(wbs.getDataStyle());
                    sheet.addMergedRegion(new CellRangeAddress(
                            rowCreator.rowIndex, rowCreator.rowIndex, 3, 4));
                    cell = row.createCell(3);
                    cell.setCellValue(researcherName);
                    cell.setCellStyle(wbs.getDataStyleText());
                    cell = row.createCell(4);
                    cell.setCellStyle(wbs.getDataStyleText());
                }
            }
        }
        if(!details) {
            drawBorders(3, pt, startingIndex, rowCreator);
        } else {
            drawBorders(5, pt, startingIndex, rowCreator);
        }
    }
    
    private void addNotesAndHints(XSSFWorkbook wb, WorkbookStyles wbs, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt) {
        rowCreator.createRow();
        addContentRow("Source: All data is retrieved from Web of Science and InCites of Clarivate Analytics.", 0, 6, wb, sheet, rowCreator);
        addContentRow("Hints: How to use the eight sections of the collaboration report.", 0, 5, wb, sheet, rowCreator);
        rowCreator.createRow();
        addHeaderRow("1. Collaboration overview", wbs.getRedStyle(), wb, sheet, rowCreator);
        rowCreator.createRow();
        addContentRow("    Quick overview of the collaboration", wb, sheet, rowCreator);
        addContentRow("      - How many co-publications (in the selected timespan)?", wb, sheet, rowCreator); 
        addContentRow("      - How many subject categories (out of 250 in total)?", wb, sheet, rowCreator); 
        addContentRow("      - What are the most popular subject categories?", wb, sheet, rowCreator);
        addContentRow("    Remember that you may change the timespan and generate a new report.", 4, 12, wb, sheet, rowCreator);
        rowCreator.createRow();
        addHeaderRow("2. Compare key output and impact indicators", wbs.getRedStyle(), wb, sheet, rowCreator);
        rowCreator.createRow();
        addContentRow("    Compare DTU and the chosen partner in the chosen timespan:", wb, sheet, rowCreator);
        addContentRow("      - How many publications and citations?", wb, sheet, rowCreator);
        addContentRow("      - How are they doing wrt. citation impact -- simple and normalized?", wb, sheet, rowCreator);
        addContentRow("      - How are they doing wrt. excellence -- proportion of publications in top 10% and top 1% most cited?", wb, sheet, rowCreator);
        addContentRow("      - How much are they collaborating -- internationally and with industry?", wb, sheet, rowCreator);
        rowCreator.createRow();
        addHeaderRow("3. Compare annual publication and co-publication output", wbs.getRedStyle(), wb, sheet, rowCreator);
        rowCreator.createRow();
        addContentRow("    Year by year: How many publications from the two universities and how many co-publications?", wb, sheet, rowCreator);
        rowCreator.createRow();
        addHeaderRow("4. Compare partner's top subjects with DTU and co-publications", wbs.getRedStyle(), wb, sheet, rowCreator);
        rowCreator.createRow();
        addContentRow("    Top subjects of the partner, of DTU and of the resulting co-publications:", wb, sheet, rowCreator);
        addContentRow("      - Sort by partner to see the partner's top 20 subjects.", wb, sheet, rowCreator);
        addContentRow("      - And how they rank on the DTU side?", wb, sheet, rowCreator);
        addContentRow("      - Are we collaborating in the partner's top 20 subjects, or outside?", wb, sheet, rowCreator);
        rowCreator.createRow();
        addHeaderRow("5. Compare top collaboration subjects with partner and DTU subjects", wbs.getRedStyle(), wb, sheet, rowCreator);
        rowCreator.createRow();
        addContentRow("    Looking at the top 20 subjects of the co-publications:", wb, sheet, rowCreator);
        addContentRow("      - How do they match the top 20 of the partner?", wb, sheet, rowCreator);
        addContentRow("      - How do they match the top 20 of DTU?", wb, sheet, rowCreator);
        rowCreator.createRow();
        addHeaderRow("6. Collaboration by DTU department", wbs.getRedStyle(), wb, sheet, rowCreator);
        rowCreator.createRow();
        addContentRow("    Listing all the DTU departments involved in the collaboration:", wb, sheet, rowCreator);
        addContentRow("      - How many co-publications for each department?", wb, sheet, rowCreator);
        addContentRow("      - Follow link to see a list of a particular department's co-publications:", wb, sheet, rowCreator);
        addContentRow("          - Title of publications, involved researchers on DTU side as well as partner side.", wb, sheet, rowCreator);
        addContentRow("          - Link to all details about a single publication and its citations.", wb, sheet, rowCreator);
        addContentRow("          - Expand to see the departments involved on the partner side.", wb, sheet, rowCreator);
        rowCreator.createRow();
        addHeaderRow("7. Collaboration by DTU researcher (top 20)", wbs.getRedStyle(), wb, sheet, rowCreator);
        rowCreator.createRow();
        addContentRow("    Listing the 20 most active DTU researchers in this collaboration in this timespan:", wb, sheet, rowCreator);
        addContentRow("      - Follow link to all the co-publications of a particular researcher.", wb, sheet, rowCreator);
        rowCreator.createRow();
        addHeaderRow("8. Collaboration by funder (top 20)", wbs.getRedStyle(), wb, sheet, rowCreator);
        rowCreator.createRow();
        addContentRow("    Listing the 20 most used funders in this collaboration in this timespan. NB:", wb, sheet, rowCreator);
        addContentRow("      - Not all publications provide funding details.", wb, sheet, rowCreator);
        addContentRow("      - Funder names are not (yet) normalized, but Clarivate is working to achieve this soon.", wb, sheet, rowCreator);
    }
    
    private void addNameNumberArray(JSONArray array, List<String> dataColumns, 
            RowCreator rowCreator, WorkbookStyles wbs, XSSFSheet sheet) 
                    throws JSONException {
        for(int i = 0; i < array.length(); i++) {
            if(i == SUBJECTS_CUTOFF) {
                break;
            }
            JSONObject object = array.getJSONObject(i);
            String name = object.getString("name");
            XSSFRow row = rowCreator.createRow();
            sheet.addMergedRegion(new CellRangeAddress(
                    rowCreator.rowIndex, rowCreator.rowIndex, 0, 1));
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(name);
            cell.setCellStyle(wbs.getDataStyleText());
            cell = row.createCell(1);
            cell.setCellStyle(wbs.getDataStyleText());
            int columnPos = 1;
            for(String dataColumn : dataColumns) {
                columnPos++;
                cell = row.createCell(columnPos);
                cell.setCellValue(object.getInt(dataColumn));
                cell.setCellStyle(wbs.getDataStyle());
            }
            
        }
    }
    
    private void addByDepartment(JSONObject data, XSSFWorkbook wb, WorkbookStyles wbs, XSSFSheet sheet, 
            RowCreator rowCreator, PropertyTemplate pt, boolean details) 
                    throws JSONException {
        XSSFRow header = rowCreator.createRow();
        header.setHeight((short) (header.getHeight() * 2));
        int startingIndex = rowCreator.getRowIndex();
        CellStyle headerStyleFirstColumn = wbs.getHeaderStyleFirstColumn();
        CellStyle headerStyleRemainingColumns = wbs.getHeaderStyleRemainingColumns();
        XSSFCell header0 = addBoldText(wb, header, 0, "DTU department");
        header0.setCellStyle(headerStyleFirstColumn);
        XSSFCell header1 = addBoldText(wb, header, 1, "Co-pubs");
        header1.setCellStyle(headerStyleRemainingColumns);
        if(details) {
            sheet.addMergedRegion(new CellRangeAddress(
                    rowCreator.rowIndex, rowCreator.rowIndex, 2, 3));
            XSSFCell header2 = addBoldText(wb, header, 2, "Partner departments");
            header2.setCellStyle(headerStyleRemainingColumns);            
        }
        JSONArray array = data.getJSONArray("departments");
        for(int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            int value = object.getInt("num");
            String name = object.getString("name");
            XSSFRow row = rowCreator.createRow();
            XSSFCell cell = addBoldText(wb, row, 0, name);
            cell.setCellStyle(wbs.getDataStyleText());
            cell = row.createCell(1);
            cell.setCellValue(value);
            cell.setCellStyle(wbs.getDataStyle());
            if(details) {
                sheet.addMergedRegion(new CellRangeAddress(
                        rowCreator.rowIndex, rowCreator.rowIndex, 2, 3));
                cell = row.createCell(2);
                cell.setCellValue(" ");
                cell.setCellStyle(wbs.getDataStyle());
                cell = row.createCell(3);
                cell.setCellStyle(wbs.getDataStyle());
                JSONArray depts = object.getJSONArray("sub_orgs");
                for(int j = 0; j < depts.length(); j++) {
                    JSONObject dept = depts.getJSONObject(j);
                    int total = dept.getInt("total");
                    String deptName = dept.getString("name");
                    row = rowCreator.createRow();
                    cell = row.createCell(0);
                    cell.setCellValue(" ");
                    cell.setCellStyle(wbs.getDataStyleText());
                    cell = row.createCell(1);
                    cell.setCellValue(total);
                    cell.setCellStyle(wbs.getDataStyle());
                    sheet.addMergedRegion(new CellRangeAddress(
                            rowCreator.rowIndex, rowCreator.rowIndex, 2, 3));
                    cell = row.createCell(2);
                    cell.setCellValue(deptName);
                    cell.setCellStyle(wbs.getDataStyleText());
                    cell = row.createCell(3);
                    cell.setCellStyle(wbs.getDataStyleText());
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
                BorderStyle.MEDIUM, IndexedColors.BLACK.getIndex(), BorderExtent.TOP);
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
            if(headerName.toLowerCase().startsWith("content")) {
                // don't pass on things like form-url-encoded, content-length, etc.
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
        String jsonStr = null;
        try {
            response = httpClient.execute(get);
            jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");                
            JSONObject json = new JSONObject(jsonStr);
            return json;
        } catch (JSONException e) {            
            log.error(e, e);
            log.error("Error parsing service response \n" + jsonStr);
            throw(e);
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
    
    private class WorkbookStyles {
        
        private XSSFWorkbook wb;
        
        public WorkbookStyles(XSSFWorkbook wb) {
            this.wb = wb;
        }
        
        private CellStyle bottomBorderStyle;
        
        private CellStyle getBottomBorderStyle() {
            if(this.bottomBorderStyle != null) {
                return this.bottomBorderStyle;
            } else {
                CellStyle bottomBorderStyle = wb.createCellStyle();
                bottomBorderStyle.setBorderBottom(BorderStyle.MEDIUM);
                this.bottomBorderStyle = bottomBorderStyle;
                return bottomBorderStyle;
            }
        }
        
        private CellStyle redStyle;
        
        private CellStyle getRedStyle() {
            if(this.redStyle != null) {
                return this.redStyle;
            } else {
                CellStyle redStyle = wb.createCellStyle();
                XSSFFont redFont = wb.createFont();
                redFont.setColor(IndexedColors.DARK_RED.index);
                redStyle.setFont(redFont);
                this.redStyle = redStyle;
                return redStyle;
            }
        }
        
        private CellStyle hyperlinkStyle;
        
        private CellStyle getHyperlinkStyle() {
            if(hyperlinkStyle != null) {
                return hyperlinkStyle;
            } else {
                CellStyle hyperlinkStyle = wb.createCellStyle();
                XSSFFont hyperlinkFont = wb.createFont();
                hyperlinkFont.setUnderline(FontUnderline.SINGLE);
                hyperlinkFont.setColor(IndexedColors.BLUE.getIndex());
                hyperlinkStyle.setFont(hyperlinkFont);
                this.titleStyleBold = hyperlinkStyle;
                return hyperlinkStyle;
            }
        }
        
        private CellStyle titleStyleBold;
        
        private CellStyle getTitleStyleBold() {
            if(titleStyleBold != null) {
                return titleStyleBold;
            } else {
                CellStyle titleStyle = wb.createCellStyle();
                XSSFFont titleFont = wb.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 15);
                titleStyle.setFont(titleFont);
                this.titleStyleBold = titleStyle;
                return titleStyle;
            }
        }
        
        private CellStyle titleStyleThin;
        
        private CellStyle getTitleStyleThin() {
            if(titleStyleThin != null) {
                return titleStyleThin;
            } else {
                CellStyle titleStyle = wb.createCellStyle();
                XSSFFont titleFont = wb.createFont();
                titleFont.setBold(false);
                titleFont.setFontHeightInPoints((short) 15);
                titleStyle.setFont(titleFont);
                this.titleStyleThin = titleStyle;
                return titleStyle;
            }
        }
        
        private CellStyle subtitleStyle;
        
        private CellStyle getSubtitleStyle() {
            if(subtitleStyle != null) {
                return subtitleStyle; 
            } else {
                CellStyle subtitleStyle = wb.createCellStyle();
                XSSFFont subtitleFont = wb.createFont();
                subtitleFont.setBold(true);
                subtitleFont.setFontHeightInPoints((short) 13);
                subtitleStyle.setFont(subtitleFont);
                this.subtitleStyle = subtitleStyle;
                return subtitleStyle;
            }
        }
          
        private CellStyle dataStyleText;
        
        private CellStyle getDataStyleText() {
            if(this.dataStyleText != null) {
                return this.dataStyleText;
            } else {
                CellStyle dataStyle = getBaseDataStyle();
                dataStyle.setAlignment(HorizontalAlignment.LEFT);
                this.dataStyleText = dataStyle;
                return dataStyle;
            }
        }
        
        private CellStyle impactStyle;
        
        private CellStyle getImpactStyle() {
            if(this.impactStyle != null) {
                return this.impactStyle;
            } else {
                CellStyle dataStyle = getBaseDataStyle();
                dataStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat("0.00"));
                this.impactStyle = dataStyle;
                return dataStyle;
            }
        }
       
        private CellStyle dataStyle;
        
        private CellStyle getDataStyle() {
            if(this.dataStyle != null) {
                return this.dataStyle;
            } else {
                CellStyle dataStyle = getBaseDataStyle();
                dataStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat("#,##0"));
                this.dataStyle = dataStyle;
                return dataStyle;
            }
        }
        
        private CellStyle getBaseDataStyle() {
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
        
        private CellStyle getHeaderStyleFirstColumn() {
            return getHeaderStyleFirstColumn(THICK_BOTTOM_BORDER);
        }
        
        private CellStyle getHeaderStyleRemainingColumns() {
            return getHeaderStyleRemainingColumns(THICK_BOTTOM_BORDER);
        }
        
        private CellStyle getHeaderStyleFirstColumn( 
                boolean thickBottomBorder) {
            CellStyle headerStyle = getHeaderStyle(thickBottomBorder);
            headerStyle.setAlignment(HorizontalAlignment.LEFT);
            return headerStyle;
        }
        
        private CellStyle getHeaderStyleRemainingColumns( 
                boolean thickBottomBorder) {
            CellStyle headerStyle = getHeaderStyle(thickBottomBorder);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            return headerStyle;
        }
        
        private CellStyle getHeaderStyle(boolean thickBottomBorder) {
            CellStyle headerStyle = wb.createCellStyle();        
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);       
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            if(thickBottomBorder) {
                headerStyle.setBorderBottom(BorderStyle.MEDIUM);
            } else {
                headerStyle.setBorderBottom(BorderStyle.THIN);
            }        
            headerStyle.setWrapText(true);
            return headerStyle;
        }
        
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
