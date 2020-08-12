import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FileName: ReadTxt.java
 * Author:   chenhao
 * Date:     2020.06.08 09:32
 * Description: 读取txt
 */
public class ReadTxt2 {
    private static final Integer ONE = 1;

    static Double area;
    private Map<String, String> attribute;

    private Geometry geometry;

    private String crsSourceCode;

    private String crsTargetCode = "EPSG:4490";

    public Map<String, String> getAttribute() {
        return attribute;
    }

    public void setAttribute(Map<String, String> attribute) {
        this.attribute = attribute;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public ReadTxt2() {
        this.attribute = new HashMap<String, String>();
        this.geometry = null;
    }

    static GeomUtil geomUtil = GeomUtil.getInstance();

    public static void main(String[] args) {
        ReadTxt2 readTxt = new ReadTxt2();
        File file = new File("D:\\WeChatFiles\\WXWork\\1688853956472127\\Cache\\File\\2020-07\\项目用地边界拐点坐标36.txt");
        //File file = new File("D:\\WeChatFiles\\WXWork\\1688853956472127\\Cache\\File\\2020-06\\2020.5.26材料打印\\永久基本农田占用审批\\1建设项目\\项目范围线.txt");
        /* 输出数据 */
        try {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("d:/value_map.txt")), "UTF-8"));
            readTxt.readTxtFile(file);

            bw.newLine();
            bw.newLine();
            bw.newLine();
            bw.newLine();
            bw.newLine();
            bw.newLine();
            /*bw.write(readTxt.getGeometry().toText());*/
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("write errors :" + e);
        }
    }


    public Geometry readTxtFile(File file) {
        List<List<Double[]>> points = new ArrayList<>();
        List<List<BigDecimal[]>> points2 = new ArrayList<>();
        /* 读取数据 */
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
            String lineTxt = null;

            int i = -1;
            boolean flag = false;//是否开始记录点数据
            String ttt = "";
            while ((lineTxt = br.readLine()) != null) {//数据以逗号分隔
               if (lineTxt.contains("J1,")) {
                    if(StringUtils.isEmpty(ttt)){
                        ttt = lineTxt;
                    }else{
                        if(ttt.equals(lineTxt)){
                            ttt = "";
                        }else{
                            System.out.println(ttt);
                            ttt = "";
                        }

                    }
                }
            }
            br.close();
/*            while ((lineTxt = br.readLine()) != null) {//数据以逗号分隔
                if (lineTxt.contains("J1,")) {
                    if(map.containsKey(lineTxt)){
                        map.put(lineTxt, map.get(lineTxt)+1);
                    }else{
                        map.put(lineTxt, 1);
                    }
                }
            }*/
            /*String wkt = getWkt(points);
            String wkt2 = getWkt2(points2);

            this.geometry = wktToGeometry(wkt);
            Geometry geometry = wktToGeometry(wkt2);
            area = geometry.getArea();*/

            /* this.geometry = this.projectTransform(this.geometry);*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 投影转换， lon=经度，lat=纬度，ESPG格式（例）：EPSG:4610
     */
    public Geometry projectTransform(Geometry geometry) throws FactoryException,
            MismatchedDimensionException, TransformException {

        this.getCoordinateReferenceSystem();

        CoordinateReferenceSystem crsSource = CRS.decode(this.crsSourceCode);
        CoordinateReferenceSystem crsTarget = CRS.decode(this.crsTargetCode);
        // 投影转换

        MathTransform transform = CRS.findMathTransform(crsSource, crsTarget);
        geometry = JTS.transform(geometry, transform);

        return geometry;
    }

    private void getCoordinateReferenceSystem() {

        Integer zoning = Integer.parseInt(this.attribute.get("几度分带"));
        Integer zoningNum = Integer.parseInt(this.attribute.get("带号"));
        String crs = (String) this.attribute.get("坐标系");
        Integer crsId = null;
        if (zoning == 3 && "2000国家大地坐标系".equals(crs)) {
            crsId = 4513 + zoningNum - 25;
        }
        this.crsSourceCode = "EPSG:" + String.valueOf(crsId);
    }
/*

    public Point createPoint(double longitude, double latitude) {
        GeometryFactory gf = new GeometryFactory();

        Coordinate coord = new Coordinate(longitude, latitude);
        Point point = gf.createPoint(coord);

        return point;
    }
*/

    private Geometry wktToGeometry(String wktString) {
        StringBuffer sbBuffer = new StringBuffer();
        WKTReader fromText = new WKTReader();
        String wkt = sbBuffer.append(wktString).toString();
        Geometry geom = null;
        try {
            geom = fromText.read(wkt);
            geom.setSRID(4490);

        } catch (ParseException e) {
            throw new RuntimeException("Not a WKT string:" + wktString);
        }
        return geom;
    }

    private String getWkt(List<List<Double[]>> points) {
        String wkt = "MULTIPOLYGON (";
        boolean flag = true;
        for (List<Double[]> list : points) {
            wkt += "((";
            for (Double[] arr : list) {
                wkt += arr[0] + " " + arr[1] + ",";
            }
            wkt = wkt.substring(0, wkt.lastIndexOf(","));
            wkt += ")),";
        }
        wkt = wkt.substring(0, wkt.lastIndexOf(","));
        wkt += ")";
        return wkt;
    }
    private String getWkt2(List<List<BigDecimal[]>> points) {
        String wkt = "MULTIPOLYGON (";
        boolean flag = true;
        for (List<BigDecimal[]> list : points) {
            wkt += "((";
            for (BigDecimal[] arr : list) {
                wkt += arr[0].toString() + " " + arr[1].toString() + ",";
            }
            wkt = wkt.substring(0, wkt.lastIndexOf(","));
            wkt += ")),";
        }
        wkt = wkt.substring(0, wkt.lastIndexOf(","));
        wkt += ")";
        return wkt;
    }
    public static Point createPoint(double longitude, double latitude) {
        GeometryFactory gf = new GeometryFactory();

        Coordinate coord = new Coordinate(longitude, latitude);
        Point point = gf.createPoint(coord);

        return point;
    }

}