import org.apache.commons.lang3.StringUtils;
import org.opengis.geometry.Geometry;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

/**
 * FileName: StringUtil.java
 * Author:   chenhao
 * Date:     2020.06.28 14:44
 * Description:
 */
public class StringUtil {

    public static void main(String[] args) {

        System.out.println(TimeZone.getDefault());
        String[] zoneIDs = TimeZone.getAvailableIDs();
        for(String zoneID: zoneIDs) {
            System.out.println(TimeZone.getTimeZone(zoneID));
        }
    }
}