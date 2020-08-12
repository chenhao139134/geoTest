import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.commons.lang3.StringUtils;
import org.geotools.data.shapefile.ShapefileDataStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yecongwang on 2020/7/17.
 */
public class StrUtil {
    /**
     * 获取求crsId所需的信息
     * @param crsDataSource
     * @return
     */
    public static Map<String,String> splitCrsData(String crsDataSource){
        String crsData = crsDataSource.toLowerCase();
        Map<String,String> headData = new HashMap<>();
        boolean is3Degree = true;
        boolean is6Degree = false;
        String daiHao = "0";//默认带号
        if(crsData.contains("cgcs2000")){
            headData.put("坐标系","2000国家大地坐标系");
        }

        String degreeRex = "3.*degree";
        String degreeRex2 = "3.*degree";
        Pattern degreePt = Pattern.compile(degreeRex);
        Pattern degreePt2 = Pattern.compile(degreeRex2);
        Matcher degreeMc = degreePt.matcher(crsData);
        Matcher degreeMc2 = degreePt2.matcher(crsData);
        if(degreeMc.find()){
            headData.put("几度分带","3");
        }else if(degreeMc2.find()){
            headData.put("几度分带","6");
            is3Degree = false;
            is6Degree = true;
        }else{
            headData.put("几度分带","0");
            is3Degree = false;
        }

        if(crsData.contains("epsg")){
            String crsId = crsData.split("epsg")[1].replaceAll("[^1-9]","");
            headData.put("crsId", crsId);
        }
        if(crsData.contains("zone")){
            String zone = crsData.split(",")[0];
            if (StringUtils.isNoneEmpty(zone)){
                daiHao = zone.split("zone")[1].replaceAll("[^1-9]","");
            }else{
                daiHao = crsData.split("zone")[1].replaceAll("[^1-9]","");
            }
            if(daiHao.length() > 4){
                daiHao = daiHao.substring(0, 2);
            }

        }else if(is3Degree || is6Degree){
            int longitude = Integer.parseInt(crsData.split("cm")[1].replaceAll("[^1-9]",""));
            if(is3Degree){
                daiHao = Math.floor(longitude/3)+"";
            }else {
                daiHao = Math.floor((longitude+3)/6)+"";
            }
        }
        headData.put("带号",daiHao);
        return headData;

    }

    /**
     * 求取crsId
     * @param crsAttrs
     * @return
     */
    public static Integer getCrsId(Map<String,String> crsAttrs) {
        Integer zoning = Integer.parseInt(crsAttrs.get("几度分带"));
        Integer zoningNum = Integer.parseInt(crsAttrs.get("带号"));
        String crs = (String) crsAttrs.get("坐标系");
        Integer crsId = null;
        if(crsAttrs.containsKey("crsId")){
            return Integer.parseInt(crsAttrs.get("crsId"));
        }
        if(zoning == 3 && "2000国家大地坐标系".equals(crs)){
            crsId = 4513 + zoningNum - 25;
        }else if(zoning == 6 && "2000国家大地坐标系".equals(crs)){
            crsId = 4514 + zoningNum - 25;
        }else{
            crsId = 4490;
        }
        return crsId;
    }

}
