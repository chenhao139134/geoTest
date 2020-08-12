import com.alibaba.druid.pool.DruidDataSource;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FileName: ReadShapeFile.java
 * Author:   chenhao
 * Date:     2020.06.15 15:03
 * Description:
 */
public class ReadShapeFile {

    private static String crsSourceCode;

    private static BigDecimal area = new BigDecimal(0);

    private static String crsTargetCode = "EPSG:4490";

    static String uploadDir = "D:\\data\\upload";

    private static JdbcTemplate jdbcTemplate;

    /**
     * 数据库连接需要的字符串
     * */
    public static final String username = "postgres";
    public static final String password = "postgresGwYZG";
    public static final String jdbcUrl = "jdbc:postgresql://139.196.140.101:5432/sd_gzpt_cq?useUnicode=true";
    public static final String driverName = "org.postgresql.Driver";

    public static JdbcTemplate getJdbcTemplate() {
        if(jdbcTemplate!=null)
            return jdbcTemplate;
        // com.alibaba.druid.pool.DruidDataSource
        DruidDataSource dataSource = new DruidDataSource();

        // 设置数据源属性参数
        dataSource.setPassword(password);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setDriverClassName(driverName);
        // 获取spring的JdbcTemplate
        jdbcTemplate = new JdbcTemplate();
        // 设置数据源
        jdbcTemplate.setDataSource(dataSource);

        return jdbcTemplate;
    }



    public static void main(String[] args) {
        /*File file = new File("C:\\Users\\Administrator\\Desktop\\2000.zip");*/
        File file = new File("C:\\Users\\Administrator\\Desktop\\项目范围线.zip");
        /*File file = new File("C:\\Users\\Administrator\\Desktop\\渝万补征地范围线2.zip");*/
        /* 输出数据 */
        try {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:/value_map.txt")),"UTF-8"));

            Geometry geometry = shapeFile(file);
            bw.write(geometry.toText());

            getJdbcTemplate();
            int srid = 4490;
            int transform = 4508;

            Double area =geometry.getArea();
            bw.write(area + "");
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("write errors :" + e);
        }
    }

    public static Coordinate[] projectTransform(Coordinate[] coordinates) throws FactoryException,
            MismatchedDimensionException, TransformException {

        for(int i = 0; i < coordinates.length; i++){
            Point sourcePoint = createPoint(coordinates[i].y, coordinates[i].x);

            CoordinateReferenceSystem crsTarget = CRS.decode("EPSG:4490");
            CoordinateReferenceSystem crsSource = CRS.decode(crsSourceCode);
            // 投影转换

            MathTransform transform = CRS.findMathTransform(crsSource, crsTarget);
            Coordinate coordinate = JTS.transform(sourcePoint, transform).getCoordinate();
            coordinates[i] = new Coordinate(coordinate.y,  coordinate.x);
        }


        return coordinates;
    }

    public static Geometry projectTransform2(Geometry geometry) throws FactoryException,
            MismatchedDimensionException, TransformException {

        /*GeometryFactory geoFactory = new GeometryFactory();
        Point sourcePoint = geoFactory.createPoint(coordinate);
        getCoordinateReferenceSystem(coordinate);*/

        CoordinateReferenceSystem crsSource = CRS.decode(crsTargetCode);
        CoordinateReferenceSystem crsTarget = CRS.decode("EPSG:4496");
        // 投影转换

        MathTransform transform = CRS.findMathTransform(crsSource, crsTarget);
        geometry = JTS.transform(geometry, transform);

        return geometry;
    }

    public static Geometry shapeFile(File zipFile) throws Exception{
        File shpFile = null;
        File[] files = ZipUtils.unZip(zipFile, uploadDir);
        if (files == null) {
            return null;
        }
        List<Geometry> geoms = null;
        List<Map> fields=null;
        GeometryFactory factory = new GeometryFactory();

        // 解压文件夹名称
        String zipDirFileName = uploadDir +File.separator + FilenameUtils.getBaseName(zipFile.getName());;
        File zipDirFile = new File(zipDirFileName);
        // 获取shp文件
        List<String> shpFileList = FileUtil.findFiles(zipDirFileName, "*.shp");
        ShapeOprate shapeOprate = new ShapeOprate();
        List<Geometry> geometries = new ArrayList<>();
        if (shpFileList != null && shpFileList.size() > 0) {
                geoms = shapeOprate.readShp(shpFileList.get(0));

        }

        List<String> dbfFileList = FileUtil.findFiles(zipDirFileName, "*.dbf");
        if (dbfFileList != null && dbfFileList.size() > 0) {
            fields = shapeOprate.readDBF(dbfFileList.get(0));
        }
        ShapefileDataStore shpDataStore = new ShapefileDataStore(new File(shpFileList.get(0)).toURI().toURL());
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = shpDataStore
                .getFeatureSource(shpDataStore.getTypeNames()[0]);
        //获取图层原始坐标系，转换目标坐标系
        CoordinateReferenceSystem sourceCRS = featureSource.getInfo().getCRS();
        String wkt = sourceCRS.toWKT();



        Polygon[] polygons = new Polygon[geoms.size()];
        for(int i = 0; i < geoms.size(); i++){
            CoordinateSystem coordinateSystem = sourceCRS.getCoordinateSystem();
            Set<ReferenceIdentifier> identifiers1 = sourceCRS.getIdentifiers();
            Set<ReferenceIdentifier> identifiers = coordinateSystem.getIdentifiers();
            Iterator<ReferenceIdentifier> iterator = identifiers.iterator();

            Map<String,String> crsDataMap = StrUtil.splitCrsData(coordinateSystem.toString().toLowerCase());
            String crsId = StrUtil.getCrsId(crsDataMap).toString();
            crsSourceCode= "EPSG:" + crsId;
            if(!"4490".equals(crsId)){

                        area = area.add(new BigDecimal(geoms.get(i).getArea()));

                    polygons[i] = (Polygon) factory.createPolygon(projectTransform(geoms.get(i).getCoordinates()));

            }else{
                area = area.add(new BigDecimal(projectTransform2(geoms.get(i)).getArea()));
                polygons[i] = factory.createPolygon(geoms.get(i).getCoordinates());
            }

        }
        MultiPolygon multiPolygon = factory.createMultiPolygon(polygons);
        // 删除解压文件夹
        return multiPolygon;
    }
    public static Point createPoint(double longitude, double latitude){
        GeometryFactory gf = new GeometryFactory();

        Coordinate coord = new Coordinate(longitude, latitude );
        Point point = gf.createPoint( coord );

        return point;
    }

    private static String getWkt(List<List<Double[]>> points) {
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

    private static Geometry wktToGeometry(String wktString) {
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
    public Geometry projectTransform(Geometry geometry) throws FactoryException,
            MismatchedDimensionException, TransformException {

        /*GeometryFactory geoFactory = new GeometryFactory();
        Point sourcePoint = geoFactory.createPoint(coordinate);
        getCoordinateReferenceSystem(coordinate);*/

        CoordinateReferenceSystem crsSource = CRS.decode(crsSourceCode);
        CoordinateReferenceSystem crsTarget = CRS.decode("EPSG:4490");
        // 投影转换

        MathTransform transform = CRS.findMathTransform(crsSource, crsTarget);
        geometry = JTS.transform(geometry, transform);

        return geometry;
    }

}