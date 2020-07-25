/*
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

*/
/**
 * FileName: Test1.java
 * Author:   chenhao
 * Date:     2020.06.07 15:50
 * Description: 坐标转换
 *//*

public class Test1 {

    final String path = "E:\\WXWork\\1688853956472127\\Cache\\File\\2020-06\\1建设项目（经纬度）\\1建设项目（经纬度）.zip";
    final String uploadDir = "E:\\WXWork\\1688853956472127\\Cache\\File\\2020-06";


    public static void main(String[] args) {
        Test1 test1 = new Test1();
        Geometry geometry = test1.parsingShapeFile();
        System.out.println(geometry.toText());
    }


    private Geometry parsingShapeFile() {


        File shpFile = null;
        File zipFile = new File(path);
        if(zipFile.exists()){
            File[] files = ZipUtils.unZip(zipFile, this.uploadDir);
            if (files == null) {
                return null;
            }
            for (File file2 : files) { // 获取文件夹下的SHP文件
                String fileN = file2.getName();
                fileN = fileN.toLowerCase();
                if (fileN.endsWith(".shp")) {
                    shpFile = file2;
                    break;
                }
            }
            if (files[0].isDirectory()) {
                for (File file2 : files[0].listFiles()) { // 获取文件夹下的SHP文件
                    String fileN = file2.getName();
                    fileN = fileN.toLowerCase();
                    if (fileN.endsWith(".shp")) {
                        shpFile = file2;
                        break;
                    }
                }
            }
            ShapefileDataStore shpDataStore = null;
            try {
                shpDataStore = new ShapefileDataStore(shpFile.toURI().toURL());
                shpDataStore.setCharset(Charset.forName("GBK"));
                String typeName = shpDataStore.getTypeNames()[0];
                FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = null;
                featureSource = (FeatureSource<SimpleFeatureType, SimpleFeature>) shpDataStore.getFeatureSource(typeName);
                FeatureCollection<SimpleFeatureType, SimpleFeature> result = featureSource.getFeatures();
                FeatureIterator<SimpleFeature> itertor = result.features();
                List<Geometry> list = new ArrayList<Geometry>();
                while (itertor.hasNext()) {
                    SimpleFeature feature = itertor.next();
                    Geometry geom = (Geometry) feature.getDefaultGeometry();
                    list.add(geom);
                    break;
                }
                return list.get(0);
                */
/*response.setData(list.get(0).toText());*//*

            } catch (Exception e) {
            } finally {
                */
/*zipFile.delete();
                files[0].getParentFile().delete();*//*

            }
        }

        return null;
    }
}*/
