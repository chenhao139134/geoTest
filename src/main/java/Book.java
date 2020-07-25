/**
 * FileName: Book.java
 * Author:   author
 * Date:     2020.07.15 16:30
 * Description:
 */
public class Book implements Cloneable{

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}