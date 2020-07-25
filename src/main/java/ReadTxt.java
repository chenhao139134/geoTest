
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.geom.util.AffineTransformationBuilder;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.*;
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

    public ReadTxt() {
        this.attribute = new HashMap<String, String>();
        this.geometry = null;
    }

   static   GeomUtil  geomUtil = GeomUtil.getInstance();
    public static void main(String[] args) {
        ReadTxt readTxt = new ReadTxt();
        File file = new File("E:\\WXWork\\1688853956472127\\Cache\\File\\2020-06\\2020.5.26材料打印\\永久基本农田占用审批\\1建设项目\\项目范围线.txt");
        /* 输出数据 */
        try {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("E:/value_map.txt")),"UTF-8"));
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
        List<List<Double[]>> points = new ArrayList<>();
        /* 读取数据 */
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "GBK"));
            String lineTxt = null;

            int i = -1;
            boolean flag = false;//是否开始记录点数据
            while ((lineTxt = br.readLine()) != null) {//数据以逗号分隔
                if (!flag && lineTxt.contains("=")) {
                    this.attribute.put(lineTxt.split("=")[0], lineTxt.split("=")[1]);
                } else if (flag && !lineTxt.contains("@")) {

                    Point point = (Point)this.projectTransform(createPoint(Double.parseDouble(lineTxt.split(",")[2]), Double.parseDouble(lineTxt.split(",")[3])));
                    Double[] arr = new Double[]{point.getY(), point.getX()};
                    points.get(i).add(arr);
                }
                if (lineTxt.contains("@")) {
                    List<Double[]> list = new ArrayList<>();
                    points.add(list);
                    flag = true;
                    i++;
                }
            }
            br.close();

            String wkt = getWkt(points);

            this.geometry = wktToGeometry(wkt);

           /* this.geometry = this.projectTransform(this.geometry);*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        return geometry;
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

    public static Point createPoint(double longitude, double latitude){
        GeometryFactory gf = new GeometryFactory();

        Coordinate coord = new Coordinate(longitude, latitude );
        Point point = gf.createPoint( coord );

        return point;
    }

}