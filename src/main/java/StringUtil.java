import org.apache.commons.lang3.StringUtils;
import org.opengis.geometry.Geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * FileName: StringUtil.java
 * Author:   chenhao
 * Date:     2020.06.28 14:44
 * Description:
 */
public class StringUtil {

    public static void main(String[] args) {
        List list = new ArrayList();
        List list2 = new ArrayList();

        list.add("a");
        list.add("a");
        list.add("a");
        list.add("a");
        list.add("a");
        list.add("a");
        list2.add("a");
        System.out.println(StringUtils.join(list,","));
        System.out.println(String.join("','",list2));

        System.out.println(list.toArray().toString());
    }
}