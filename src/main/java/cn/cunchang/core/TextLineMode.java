package cn.cunchang.core;

public class TextLineMode {
    public static final float defaultHeight = 14;
    public static final float fixHeight = 4;
 
    private int curPage = 1;
    private float height = 0;
    private float width = 0;
    private float x = 0;
    private float y = 0;
 
    public int getCurPage() {
        return curPage;
    }
 
    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }
 
    public float getHeight() {
        return height;
    }
 
    public void setHeight(float height) {
        this.height = height;
    }
 
    public float getWidth() {
        return width;
    }
 
    public void setWidth(float width) {
        this.width = width;
    }
 
    public float getX() {
        return x;
    }
 
    public void setX(float x) {
        this.x = x;
    }
 
    public float getY() {
        return y;
    }
 
    public void setY(float y) {
        this.y = y;
    }
 
}
