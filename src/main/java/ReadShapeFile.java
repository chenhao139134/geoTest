import com.alibaba.druid.pool.DruidDataSource;
import com.vividsolutions.jts.geom.*;
import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
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

/**
 * FileName: ReadShapeFile.java
 * Author:   chenhao
 * Date:     2020.06.15 15:03
 * Description:
 */
public class ReadShapeFile {

    private static String crsSourceCode;

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
        File file = new File("D:\\重庆耕保\\补划修改.zip");
        /* 输出数据 */
        try {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:/value_map.txt")),"UTF-8"));

            Geometry geometry = shapeFile(file);
            bw.write(geometry.toText());

            getJdbcTemplate();
            int srid = 4490;
            int transform = 4508;

            geometry = projectTransform2(geometry);
            Double area =geometry.getArea();
            bw.write(area + "");
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("write errors :" + e);
        }
    }

    public static Coordinate projectTransform(Coordinate coordinate) throws FactoryException,
            MismatchedDimensionException, TransformException {

        GeometryFactory geoFactory = new GeometryFactory();
        Point sourcePoint = geoFactory.createPoint(coordinate);
        getCoordinateReferenceSystem(coordinate);

        CoordinateReferenceSystem crsSource = CRS.decode(crsTargetCode);
        CoordinateReferenceSystem crsTarget = CRS.decode(crsSourceCode);
        // 投影转换

        MathTransform transform = CRS.findMathTransform(crsSource, crsTarget);
        coordinate = JTS.transform(sourcePoint, transform).getCoordinate();

        return coordinate;
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

    private static void getCoordinateReferenceSystem(Coordinate coordinate) {

        Integer crsId = 4507;

        crsSourceCode = "EPSG:" + String.valueOf(crsId);
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
        Polygon[] polygons = new Polygon[geoms.size()];
        for(int i = 0; i < geoms.size(); i++){
            Coordinate[] coordinates = geoms.get(i).getCoordinates();
            /*for(int j = 0; j < coordinates.length; j++){
                coordinates[j] = projectTransform(coordinates[j]);
            }*/

            polygons[i] = factory.createPolygon(coordinates);
        }
        MultiPolygon multiPolygon = factory.createMultiPolygon(polygons);
        // 删除解压文件夹
        return multiPolygon;
    }
}