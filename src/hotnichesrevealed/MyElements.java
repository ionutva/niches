/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hotnichesrevealed;

/**
 *
 * @author iova
 */
public class MyElements {

    public MyElements(int position, String keyword) {
        this.position = position;
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    private int position;
    private String keyword;

}
