package cn.cunchang.itext;

public class PdfBDO {
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

    /*******计算clean的矩阵位置*******/
    /*******千万别动*******/
    public float getLlx() {
        return x;
    }

    public float getLly() {
        return y - 2;
    }

    public float getUrx() {
        return x + width - 2;
    }

    public float getUry() {
        return y + height - 2;
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



