
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.geom.util.AffineTransformationBuilder;
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
public class ReadTxt {
    private static final Integer ONE = 1;

    BigDecimal area;
    private Map<String, String> attribute;

    private Geometry geometry;

    private String crsSourceCode;

    private String crsTargetCode = "EPSG:4490";

    List<List<Double[]>> points = new ArrayList<>();
    List<List<String[]>> points2 = new ArrayList<>();

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

    public ReadTxt() {
        this.attribute = new HashMap<String, String>();
        this.geometry = null;
    }

   static   GeomUtil  geomUtil = GeomUtil.getInstance();
    public static void main(String[] args) {
        ReadTxt readTxt = new ReadTxt();
        File file = new File("C:\\Users\\Administrator\\Desktop\\项目范围线.txt");
        /* 输出数据 */
        try {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:/value_map.txt")),"UTF-8"));
            bw.write(readTxt.readTxtFile(file).toText());
            bw.newLine();
            bw.newLine();
            bw.newLine();
            bw.newLine();
            bw.newLine();
            bw.newLine();
            bw.write(readTxt.getGeometry().toText());
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("write errors :" + e);
        }
    }


    public Geometry readTxtFile(File file) {

        /* 读取数据 */
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
            String lineTxt = null;
            String startKey = "";
            int readStartKey = 0;

            int i = -1;
            boolean flag = false;//是否开始记录点数据
            boolean flag2 = false;//是否开始记录一个新的多边形点数据
            while ((lineTxt = br.readLine()) != null) {//数据以逗号分隔
                if(readStartKey == 1){
                    startKey = lineTxt.split(",")[0] + ",";
                    readStartKey = 2;
                }
                if (readStartKey != 0 && lineTxt.contains(startKey)) {
                    if(!flag2){
                        i++;
                        List<Double[]> list = new ArrayList<>();
                        List<String[]> list2 = new ArrayList<>();
                        points.add(list);
                        points2.add(list2);

                    }else{
                        readLineTxt(lineTxt, i);
                    }
                    flag2 = !flag2;

                }
                if (!flag && lineTxt.contains("=")) {
                    String[] arr = lineTxt.split("=");
                    String key = "";
                    String value = "";
                    if(arr.length > 1){
                        key = arr[0];
                        value = arr[1];
                        this.attribute.put(key, value);
                    }

                } else if (flag && flag2 && !lineTxt.contains("@")) {
                    readLineTxt(lineTxt, i);
                }

                if (lineTxt.contains("@")) {
                    flag = true;
                    if(readStartKey == 0){
                        readStartKey = 1;
                    }
                }
            }
            br.close();

            String wkt = getWkt(points);
            String wkt2 = getWkt2(points2);

            this.geometry = wktToGeometry(wkt);
            Geometry geometry = wktToGeometry(wkt2);
            area = new BigDecimal(geometry.getArea()).setScale(4,BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return geometry;
    }

    public void readLineTxt(String lineTxt, int i) throws Exception{
        String a = lineTxt.split(",")[2];
        String b = lineTxt.split(",")[3];


        Point point = (Point)this.projectTransform(createPoint(Double.parseDouble(a), Double.parseDouble(b)));
        Double[] arr = new Double[]{point.getY(), point.getX()};
        /*String[] arr2 = new String[]{lineTxt.split(",")[2], lineTxt.split(",")[3]};*/
        String[] arr2 = new String[]{a, b};
        points.get(i).add(arr);
        points2.get(i).add(arr2);
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
        if(zoning == 3 && "2000国家大地坐标系".equals(crs)){
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
        com.vividsolutions.jts.geom.Geometry geom = null;
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
    private String getWkt2(List<List<String[]>> points) {
        String wkt = "MULTIPOLYGON (";
        boolean flag = true;
        for (List<String[]> list : points) {
            wkt += "((";
            for (String[] arr : list) {
                wkt += arr[0] + " " + arr[1] + ",";
            }
            wkt = wkt.substring(0, wkt.lastIndexOf(","));
            wkt += ")),";
        }
        wkt = wkt.substring(0, wkt.lastIndexOf(","));
        wkt += ")";
        return wkt;
    }
    public static Point createPoint(double longitude, double latitude){
        GeometryFactory gf = new GeometryFactory();

        Coordinate coord = new Coordinate(longitude, latitude );
        Point point = gf.createPoint( coord );

        return point;
    }

}