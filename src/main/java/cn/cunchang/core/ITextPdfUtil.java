package cn.cunchang.core;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
 
public class ITextPdfUtil {
/*
* 参数说明：
* src:原始文件
* dest:打码文件
* keywords：敏感词查找
* */
    public boolean manipulatePdf(String src, String dest, List<String> keywords) throws Exception {
        PdfReader pdfReader = null;
        PdfStamper stamper = null;
        try {
            pdfReader = new PdfReader(src);
            stamper = new PdfStamper(pdfReader, new FileOutputStream(dest));
            List<TextLineMode> list = renderText(pdfReader, keywords);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    TextLineMode mode = list.get(i);
 
                    PdfContentByte canvas = stamper.getOverContent(mode.getCurPage());
                    //初始阶段完成 开始替换
                    canvas.saveState();
                    //黑色背景覆盖
                    canvas.setColorFill(BaseColor.LIGHT_GRAY);
                    // canvas.setColorFill(BaseColor.BLUE);
                    // 以左下点为原点，x轴的值，y轴的值，总宽度，总高度：
                    canvas.rectangle(mode.getX() - 2 , mode.getY(),
                            mode.getWidth()+2 , mode.getHeight());
//                     canvas.rectangle(mode.getX() - 1, mode.getY(),
//                     mode.getWidth() + 2, mode.getHeight());
                    //定位
                   // canvas.rectangle(0, mode.getY(), 10000, mode.getHeight());
                    //填充
                    canvas.fill();
                    //还原状态
                    canvas.restoreState();
                }
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stamper != null)
                stamper.close();
            if (pdfReader != null)
                pdfReader.close();
        }
        return false;
    }
/*
* 查找关键词坐标*/
    public List<TextLineMode> renderText(PdfReader pdfReader, final List<String> keywords) {
        final List<TextLineMode> list = new ArrayList<TextLineMode>();
        try {
            PdfReaderContentParser pdfReaderContentParser = new PdfReaderContentParser(pdfReader);
            int pageNum = pdfReader.getNumberOfPages();
            for (int i = 1; i <= pageNum; i++) {
                final int curPage = i;
 
                pdfReaderContentParser.processContent(curPage, new RenderListener() {
                    @Override
                    public void renderText(TextRenderInfo textRenderInfo) {
                        String text = textRenderInfo.getText();
                        if (text != null) {
                            for (int j = 0; j < keywords.size(); j++) {
                                String keyword = keywords.get(j);
                                if (text.contains(keyword)) {
                                    com.itextpdf.awt.geom.Rectangle2D.Float bound = textRenderInfo.getBaseline()
                                            .getBoundingRectange();
                                    TextLineMode lineMode = new TextLineMode();
                                    lineMode.setHeight(bound.height == 0 ? TextLineMode.defaultHeight : bound.height);
                                    lineMode.setWidth(bound.width);
                                    lineMode.setX(bound.x);
                                    lineMode.setY(bound.y - TextLineMode.fixHeight);
                                    lineMode.setCurPage(curPage);
                                    list.add(lineMode);
                                }
                            }
                        }
                    }
                    @Override
                    public void renderImage(ImageRenderInfo arg0) {
                    }
                    @Override
                    public void endTextBlock() {
 
                    }
 
                    @Override
                    public void beginTextBlock() {
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
 
    public static void main(String[] args) throws Exception {
        List<String> keywords = new ArrayList<String>();
        keywords.add("刘郑辉");//需要打码的文字
        new ITextPdfUtil().manipulatePdf("C:\\Users\\11712\\Desktop\\刘郑辉简历.pdf",
                "E:\\刘郑辉简历打码版.pdf", keywords);
    }
}
 
