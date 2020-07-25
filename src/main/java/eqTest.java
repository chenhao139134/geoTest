/**
 * FileName: eqTest.java
 * Author:   chenhao
 * Date:     2020.07.08 18:11
 * Description:
 */
public class eqTest {

    public static void main(String[] args) {
        Long a = new Long(1L);
        Long d = new Long(1L);
        Object b = 1;
        Integer c = 1;
        System.out.println(a==b);
        System.out.println(a.equals(d));
        System.out.println(a == d);
    }
}