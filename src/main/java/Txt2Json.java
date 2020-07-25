import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import javax.sound.midi.Soundbank;
import java.util.ArrayList;
import java.util.List;

/**
 * FileName: Txt2Json.java
 * Author:   chenhao
 * Date:     2020.07.09 18:29
 * Description:
 */
public class Txt2Json {

    public static void main(String[] args) {
        try {

            List<String> strs = new ArrayList<>();

            strs.add("1");
            System.out.println(StringUtils.join(",", strs));


        }catch (Exception e){
            e.printStackTrace();
        }
    }

}