import org.apache.commons.io.FileUtils;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * FileName: ReadGty.java
 * Author:   author
 * Date:     2020/7/26 17:18
 * Description:
 */
public class ReadGty {


    public static void main(String[] args) {
        try {
            File shapeFile = new File("d://gty//补划永久基本农田图斑.gty");
            File zipFile = null;
            String uploadDir = "d://gty//zip";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            zipFile = File.createTempFile("tmp", ".gty", dir);
            FileUtils.copyInputStreamToFile(new FileInputStream(shapeFile), zipFile);

            String zipName = zipFile.getAbsolutePath();
            String unzipFileDir = zipName.substring(0, zipName.lastIndexOf("."));
            Zip4jUtils.unzip(zipName, unzipFileDir, "gtdcy2019");

            read(unzipFileDir);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void read(String unzipFileDir) throws Exception{

        URL url = null;
        File file = null;
        file = new File(unzipFileDir);
        File[] content = file.listFiles();
        for (int j = 0, ln = content.length; j < ln; ++j) {
            if (content[j].getName().contains(".shp")) {
                url = content[j].toURI().toURL();
                break;
            }
        }
        ShapefileDataStore shpDataStore = new ShapefileDataStore(url);
        String chasetStr = shpDataStore.getCharset().toString();
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = shpDataStore
                .getFeatureSource(shpDataStore.getTypeNames()[0]);
        //获取图层原始坐标系，转换目标坐标系
        CoordinateReferenceSystem sourceCRS = featureSource.getInfo().getCRS();
        if (sourceCRS == null) {
            sourceCRS = createCRSByPrjFile(url.toString());
        }
        MathTransform transform = CRS.findMathTransform(sourceCRS, DefaultGeographicCRS.WGS84, true);
        SimpleFeature sf = null;
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures();
        FeatureIterator<SimpleFeature> itertor = collection.features();

        while (itertor.hasNext()) {
            sf = itertor.next();
            saveData(sf, chasetStr);
        }
    }


    private static void saveData(SimpleFeature sf, String chasetStr) {
        List<Object> list = sf.getAttributes();
        String chaset = chasetStr;
        for(Object obj : list){

            String value1 = getStrValue(obj, chaset);
            System.out.println(obj);
            System.out.println(value1);
        }


    }
    private static String getStrValue(Object obj, String chasetStr) {
        String val = "";
        try {
            if (obj != null && !obj.equals("")) {
                String objStr = obj.toString();
                obj = new String(objStr.getBytes(chasetStr), StandardCharsets.UTF_8);
                if (obj != null /*&& isMessyCode(obj.toString())*/) {
                    obj = new String(objStr.getBytes(chasetStr), "GBK");
                }
                val = String.valueOf(obj);
            }
        } catch (Exception ex) {
        }
        return val;
    }
    // 判断是否为乱码
    private static boolean isMessyCode(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // 当从Unicode编码向某个字符集转换时，如果在该字符集中没有对应的编码，则得到0x3f（即问号字符?）
            // 从其他字符集向Unicode编码转换时，如果这个二进制数在该字符集中没有标识任何的字符，则得到的结果是0xfffd
            if ((int) c == 0xfffd) {
                // 存在乱码
                return true;
            }
        }
        return false;
    }


    private static CoordinateReferenceSystem createCRSByPrjFile(String filePath) {
        String prjFilePath = filePath.replace(".shp", ".prj");
        String wkt = txt2String(new File(prjFilePath));
        String wkt2 = wkt.replaceAll("[',']+", ",").replaceAll(",]", "]");

        CoordinateReferenceSystem sCRS = null;
        try {
            sCRS = CRS.parseWKT(wkt2);
        } catch (FactoryException e) {
            e.printStackTrace();
        }
        return sCRS;
    }


    private static String txt2String(File file) {
        StringBuilder result = new StringBuilder();
        try {
            //构造一个BufferedReader类来读取文件
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            while ((s = br.readLine()) != null) {
                //使用readLine方法，一次读一行
                result.append(s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}