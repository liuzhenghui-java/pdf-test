package cn.cunchang.itext;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

import cn.cunchang.core.ReplaceRegion;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor;


import javax.imageio.ImageIO;

import static com.itextpdf.text.pdf.PdfWriter.getInstance;

public class ITextPdfUtil {

    public static void main(String[] args) throws Exception {
        String source = "C:\\Users\\11712\\Desktop\\刘郑辉简历.pdf";
        String target = "E:\\刘郑辉简历打码版13.pdf";
        //关键字之后替换,仅仅支持关键字之后的100范围内的文字覆盖，超出范围将覆盖不到
        ITextPdfUtil.stringReplace(source, target, "Spring", 50, "*****");

    }

    /**
     * @Description : 字符串替换
     * @Author : mabo
     * maxDistance 字符之间最大距离
     */
    public static boolean stringReplace(String source, String target, String keyword, float maxDistance, String replace) throws Exception {
        boolean success = false;
        List<String> keywords = new ArrayList<>();
        keywords.add(keyword);
        success = manipulatePdf(source, target, keywords, replace);
        if (!success) {
            success = compareText(source, target, keyword, maxDistance, replace);
        }
        return success;
    }


    /**
     * @Description : 关键字替换
     * @Author : mabo
     * maxDistance 字符之间最大距离
     */
    public static boolean afterKeyReplace(String source, String target, String keyword, float maxDistance, String replace) throws Exception {
        boolean success = false;
        List<String> keywords = new ArrayList<>();
        keywords.add(keyword);
        success = manipulatePdfAfterKey(source, target, keywords, replace);
        if (!success) {
            success = compareTextAfterKey(source, target, keyword, maxDistance, replace);
        }
        return success;
    }


    /**
     * @Author mabo
     * @Description 由于部分pdf文字会被分割
     * 采用如下文字分割方式，寻找距离最近的字符再进行匹配
     */

    public static boolean compareText(String src, String dest, String keyword, float maxDistance, String replace) throws Exception {
        boolean success = false;
        PdfReader pdfReader = null;
        PdfStamper stamper = null;
        try {
            pdfReader = new PdfReader(src);
            stamper = new PdfStamper(pdfReader, new FileOutputStream(dest));
            char[] chars = keyword.toCharArray();
            HashMap<String, List<PdfBDO>> textMap = new HashMap<>();
            for (char c : chars) {
                String s = String.valueOf(c);
                List<PdfBDO> textLineModes = renderText(pdfReader, s);
                textMap.put(s, textLineModes);
            }
            List<PdfBDO> textLineModes = textMap.get(String.valueOf(chars[0]));
            Map<Float, Float> mapY = new HashMap<>();
            for (PdfBDO textLineMode : textLineModes) {
                //根据首字符 找出同一行的文字
                float y = textLineMode.getY();
                float x = textLineMode.getX();
                mapY.put(y, x);
            }
            Set<Float> floats = mapY.keySet();
            Iterator<Float> iterator = floats.iterator();
            HashMap<Float, Map<String, PdfBDO>> keyYMap = new HashMap<>();
            while (iterator.hasNext()) {
                Float y = iterator.next();
                Float x = mapY.get(y);
                HashMap<String, PdfBDO> tMap = new HashMap<>();
                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    List<PdfBDO> textLineModes1 = textMap.get(String.valueOf(c));
                    for (PdfBDO t : textLineModes1) {
                        if (t.getY() == y) {
                            //判断两文字之间的具体是否符合要求
                            float x1 = t.getX();
                            float absoluteValue = getAbsoluteValue(x1, x);
                            if (absoluteValue < maxDistance) {
                                Object o = tMap.get(String.valueOf(c));
                                if (o != null) {
                                    PdfBDO o1 = (PdfBDO) o;
                                    if (getAbsoluteValue(o1.getX(), x) > absoluteValue) {
                                        tMap.put(String.valueOf(c), t);
                                    }
                                } else {
                                    tMap.put(String.valueOf(c), t);
                                }
                            }
                        }
                    }
                }
                keyYMap.put(y, tMap);
            }
            Set<Float> keySet = keyYMap.keySet();
            Iterator<Float> iterator1 = keySet.iterator();
            while (iterator1.hasNext()) {
                Float next = iterator1.next();
                Map<String, PdfBDO> map = keyYMap.get(next);
                if (map.size() == chars.length) {
                    PdfBDO t = map.get(String.valueOf(chars[0]));


                    PdfBDO endPdfBDO = map.get(String.valueOf(chars[chars.length - 1]));
                    float x = t.getX();
                    float y = t.getY();
                    float width = endPdfBDO.getX() - x + t.getWidth();
                    float height = t.getHeight();
                    int curPage = t.getCurPage();

                    Rectangle rectangle = new Rectangle(x, y,
                            x + width - 2, y + height - 2);
                    PdfCleanUpLocation pdfCleanUpLocation = new PdfCleanUpLocation(curPage, rectangle, BaseColor.WHITE);
                    PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(Collections.singletonList(pdfCleanUpLocation), stamper);
                    cleaner.cleanUp();
//                    PdfContentByte canvas = stamper.getOverContent(curPage);
//                    canvas.saveState();
//                    canvas.setColorFill(BaseColor.WHITE);
//                    // 以左下点为原点，x轴的值，y轴的值，总宽度，总高度：
//                    //开始覆盖内容,实际操作位置
//                    canvas.rectangle(x, y, width, height*1.3);
//                    canvas.fill();
//                    canvas.setColorFill(BaseColor.BLACK);
//                    //开始写入文本
//                    canvas.beginText();
//                    BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
//                    Font font = new Font(bf,height,Font.BOLD);
//                    //设置字体和大小
//                    canvas.setFontAndSize(font.getBaseFont(), height);
//                    //设置字体的输出位置
//                    canvas.setTextMatrix(x, y+3);
//                    //要输出的text
//                    canvas.showText(replace) ;
//                    canvas.endText();
//                    canvas.fill();
//                    canvas.restoreState();
                    success = true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stamper != null)
                stamper.close();
            if (pdfReader != null)
                pdfReader.close();
        }
        return success;
    }

    /**
     * @Description : 功能说明
     * 由于部分pdf文字会被分割
     * 采用如下文字分割方式，寻找距离最近的字符再进行匹配
     * 匹配后对该字符串后面的文字进行脱敏
     */

    public static boolean compareTextAfterKey(String src, String dest, String keyword, float maxDistance, String replace) throws Exception {
        boolean success = false;
        PdfReader pdfReader = null;
        PdfStamper stamper = null;
        try {
            pdfReader = new PdfReader(src);
            stamper = new PdfStamper(pdfReader, new FileOutputStream(dest));
            char[] chars = keyword.toCharArray();
            HashMap<String, List<PdfBDO>> textMap = new HashMap<>();
            for (char c : chars) {
                String s = String.valueOf(c);
                List<PdfBDO> textLineModes = renderText(pdfReader, s);
                textMap.put(s, textLineModes);
            }
            List<PdfBDO> textLineModes = textMap.get(String.valueOf(chars[0]));
            Map<Float, Float> mapY = new HashMap<>();
            for (PdfBDO textLineMode : textLineModes) {
                //根据首字符 找出同一行的文字
                float y = textLineMode.getY();
                float x = textLineMode.getX();
                mapY.put(y, x);
            }
            Set<Float> floats = mapY.keySet();
            Iterator<Float> iterator = floats.iterator();
            HashMap<Float, Map<String, PdfBDO>> keyYMap = new HashMap<>();
            while (iterator.hasNext()) {
                Float y = iterator.next();
                Float x = mapY.get(y);
                HashMap<String, PdfBDO> tMap = new HashMap<>();
                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    List<PdfBDO> textLineModes1 = textMap.get(String.valueOf(c));
                    for (PdfBDO t : textLineModes1) {
                        if (t.getY() == y) {
                            //判断两文字之间的具体是否符合要求
                            float x1 = t.getX();
                            float absoluteValue = getAbsoluteValue(x1, x);
                            if (absoluteValue < maxDistance) {
                                Object o = tMap.get(String.valueOf(c));
                                if (o != null) {
                                    PdfBDO o1 = (PdfBDO) o;
                                    if (getAbsoluteValue(o1.getX(), x) > absoluteValue) {
                                        tMap.put(String.valueOf(c), t);
                                    }
                                } else {
                                    tMap.put(String.valueOf(c), t);
                                }
                            }
                        }
                    }
                }
                keyYMap.put(y, tMap);
            }
            Set<Float> keySet = keyYMap.keySet();
            Iterator<Float> iterator1 = keySet.iterator();
            while (iterator1.hasNext()) {
                Float next = iterator1.next();
                Map<String, PdfBDO> map = keyYMap.get(next);
                if (map.size() == chars.length) {
                    PdfBDO t = map.get(String.valueOf(chars[chars.length - 1]));
                    float x = t.getX();
                    float y = t.getY();
                    float width = t.getWidth();
                    float height = t.getHeight();
                    int curPage = t.getCurPage();
                    PdfContentByte canvas = stamper.getOverContent(curPage);
                    canvas.saveState();
                    canvas.setColorFill(BaseColor.WHITE);
                    // 以左下点为原点，x轴的值，y轴的值，总宽度，总高度：
                    //开始覆盖内容,实际操作位置
                    canvas.rectangle(x + width, y, 100, height * 1.3);
                    canvas.fill();
                    canvas.setColorFill(BaseColor.BLACK);
                    //开始写入文本
                    canvas.beginText();
                    BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
                    Font font = new Font(bf, height, Font.BOLD);
                    //设置字体和大小
                    canvas.setFontAndSize(font.getBaseFont(), height);
                    //设置字体的输出位置
                    canvas.setTextMatrix(x + width, y + 3);
                    //要输出的text
                    canvas.showText(replace);
                    canvas.endText();
                    canvas.fill();
                    canvas.restoreState();
                    success = true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stamper != null)
                stamper.close();
            if (pdfReader != null)
                pdfReader.close();
        }
        return success;
    }


    /**
     * 根据关键字，在其后进行脱敏
     *
     * @Author : mabo
     */
    public static boolean manipulatePdfAfterKey(String src, String dest, List<String> keywords, String replace) throws Exception {
        boolean success = false;
        PdfReader pdfReader = null;
        PdfStamper stamper = null;
        try {
            pdfReader = new PdfReader(src);
            List<PdfBDO> list = renderText(pdfReader, keywords);
            stamper = new PdfStamper(pdfReader, new FileOutputStream(dest));
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    PdfBDO mode = list.get(i);

                    PdfContentByte canvas = stamper.getOverContent(mode.getCurPage());
                    canvas.saveState();
                    canvas.setColorFill(BaseColor.WHITE);
                    // 以左下点为原点，x轴的值，y轴的值，总宽度，总高度：
                    //开始覆盖内容,实际操作位置
                    canvas.rectangle(mode.getX() + mode.getWidth(), mode.getY(), 100, mode.getHeight() * 1.3);
                    canvas.fill();
                    canvas.setColorFill(BaseColor.BLACK);
                    //开始写入文本
                    canvas.beginText();
                    BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
                    Font font = new Font(bf, mode.getHeight(), Font.BOLD);
                    //设置字体和大小
                    canvas.setFontAndSize(font.getBaseFont(), mode.getHeight());
                    //设置字体的输出位置
                    canvas.setTextMatrix(mode.getX() + mode.getWidth() + 10, mode.getY() + 3);
                    //要输出的text
                    canvas.showText(replace);
                    canvas.endText();
                    canvas.fill();
                    canvas.restoreState();
                    success = true;
                }
            }
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
        return success;
    }


    /**
     * @Description : 匹配pdf中的文字，进行替换
     * @Author : mabo
     */
    public static boolean manipulatePdf(String src, String dest, List<String> keywords, String replace) throws Exception {
        boolean success = false;
        PdfReader pdfReader = null;
        PdfStamper stamper = null;
        try {
            pdfReader = new PdfReader(src);
            List<PdfBDO> list = renderText(pdfReader, keywords);
            stamper = new PdfStamper(pdfReader, new FileOutputStream(dest));
            if (list != null) {

//                List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<>(list.size());
//                for (PdfBDO replaceRegion : list) {
//                    // 移除矩形中的内容，文字被消除无法复制
//                    // ll代表左下角，ur代表右上角
//                    // abs(llx-urx)=x，从左以llx起点，走x；llx>urx，往左
//                    // abs(lly-ury)=y，从下以lly起点，走y；lly>ury，往下
//                    Rectangle rectangle = new Rectangle(replaceRegion.getLlx(), replaceRegion.getLly(),
//                            replaceRegion.getUrx(), replaceRegion.getUry());
//                    PdfCleanUpLocation pdfCleanUpLocation = new PdfCleanUpLocation(1, rectangle, BaseColor.WHITE);
//                    cleanUpLocations.add(pdfCleanUpLocation);
//                }
//                PdfCleanUpProcessor cleaner = new PdfCleanUpProcessor(cleanUpLocations, stamper);
//                cleaner.cleanUp();
//            }
                for (int i = 0; i < list.size(); i++) {
                    PdfBDO mode = list.get(i);

                    PdfContentByte canvas = stamper.getOverContent(mode.getCurPage());
                    canvas.saveState();
                    canvas.setColorFill(BaseColor.WHITE);
                    // 以左下点为原点，x轴的值，y轴的值，总宽度，总高度：
                    // canvas.rectangle(mode.getX() - 1, mode.getY(),
                    // mode.getWidth() + 2, mode.getHeight());
                    //开始覆盖内容,实际操作位置
                    canvas.rectangle(mode.getX(), mode.getY(), mode.getWidth(), mode.getHeight() * 1.3);
                    canvas.fill();
                    canvas.setColorFill(BaseColor.BLACK);
                    //开始写入文本
                    canvas.beginText();
                    BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
                    Font font = new Font(bf, mode.getHeight(), Font.BOLD);
                    //设置字体和大小
                    canvas.setFontAndSize(font.getBaseFont(), mode.getHeight());
                    //设置字体的输出位置
                    canvas.setTextMatrix(mode.getX(), mode.getY() + 3);
                    //要输出的text
                    canvas.showText(replace);
                    canvas.endText();
                    canvas.fill();
                    canvas.restoreState();
                    success = true;
                }
            }
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
        return success;
    }

    public static List<PdfBDO> renderText(PdfReader pdfReader, final List<String> keywords) {
        final List<PdfBDO> list = new ArrayList<PdfBDO>();
        try {
            PdfReaderContentParser pdfReaderContentParser = new PdfReaderContentParser(pdfReader);
            int pageNum = pdfReader.getNumberOfPages();
            for (int i = 1; i <= pageNum; i++) {
                final int curPage = i;

                pdfReaderContentParser.processContent(curPage, new RenderListener() {
                    public void renderText(TextRenderInfo textRenderInfo) {
                        String text = textRenderInfo.getText();
                        System.out.println(text);
                        if (text != null) {
                            for (int j = 0; j < keywords.size(); j++) {
                                String keyword = keywords.get(j);
                                if (text.contains(keyword)) {
                                    com.itextpdf.awt.geom.Rectangle2D.Float bound = textRenderInfo.getBaseline()
                                            .getBoundingRectange();
                                    PdfBDO lineMode = new PdfBDO();
                                    lineMode.setHeight(bound.height == 0 ? PdfBDO.defaultHeight : bound.height);
                                    lineMode.setWidth(bound.width);
                                    lineMode.setX(bound.x);
                                    lineMode.setY(bound.y - PdfBDO.fixHeight);
                                    lineMode.setCurPage(curPage);
                                    list.add(lineMode);
                                }
                            }
                        }
                    }

                    public void renderImage(ImageRenderInfo arg0) {
                    }

                    public void endTextBlock() {

                    }

                    public void beginTextBlock() {
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<PdfBDO> renderText(PdfReader pdfReader, String keyword) {
        final List<PdfBDO> list = new ArrayList<PdfBDO>();
        try {
            PdfReaderContentParser pdfReaderContentParser = new PdfReaderContentParser(pdfReader);
            int pageNum = pdfReader.getNumberOfPages();
            for (int i = 1; i <= pageNum; i++) {
                final int curPage = i;
                pdfReaderContentParser.processContent(curPage, new RenderListener() {
                    public void renderText(TextRenderInfo textRenderInfo) {
                        String text = textRenderInfo.getText();
                        if (text != null) {
                            if (text.contains(keyword)) {
                                com.itextpdf.awt.geom.Rectangle2D.Float bound = textRenderInfo.getBaseline()
                                        .getBoundingRectange();
                                PdfBDO lineMode = new PdfBDO();
                                lineMode.setHeight(bound.height == 0 ? PdfBDO.defaultHeight : bound.height);
                                lineMode.setWidth(bound.width);
                                lineMode.setX(bound.x);
                                lineMode.setY(bound.y - PdfBDO.fixHeight);
                                lineMode.setCurPage(curPage);
                                list.add(lineMode);
                            }
                        }
                    }

                    public void renderImage(ImageRenderInfo arg0) {
                    }

                    public void endTextBlock() {

                    }

                    public void beginTextBlock() {
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static float getAbsoluteValue(float f1, float f2) {
        if (f1 > f2) {
            return f1 - f2;
        } else {
            return f2 - f1;
        }
    }


    public static File outputStream2File(ByteArrayOutputStream out, File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file.getName());
        fileOutputStream.write(out.toByteArray());
        return file;
    }

    public File inputStream2File(InputStream in, File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int ch = 0;
        while ((ch = in.read()) != -1) {
            out.write(ch);
        }
        outputStream2File(out, file);
        return file;
    }

    public static InputStream File2InputStream(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        return inputStream;
    }

    /**
     * @param destPath 生成pdf文件的路劲
     * @param images   需要转换的图片路径的数组
     *                 imagesToPdf("G:/test333.pdf",new String[]{"G:/test.jpg"});
     * @throws IOException
     * @throws DocumentException
     */
    public static void imagesToPdf(String destPath, String[] images)
            throws IOException, DocumentException {
        // 第一步：创建一个document对象。
        Document document = new Document();
        document.setMargins(0, 0, 0, 0);

        // 第二步：
        // 创建一个PdfWriter实例，

        getInstance(document, new FileOutputStream(destPath));

        // 第三步：打开文档。
        document.open();

        // 第四步：在文档中增加图片。
        int len = images.length;
        for (int i = 0; i < len; i++) {
            Image img = Image.getInstance(images[i]);
            img.setAlignment(Image.ALIGN_CENTER);
            //根据图片大小设置页面，一定要先设置页面，再newPage（），否则无效
            document.setPageSize(new Rectangle(img.getWidth(), img.getHeight()));
            document.newPage();
            document.add(img);
        }

        // 第五步：关闭文档。
        document.close();

    }

    /**
     * 将PDF文件转换成多张图片
     *
     * @param pdfFile PDF源文件
     * @return 图片字节数组列表
     */
//    public static void pdf2images(File pdfFile) throws Exception {
//        String name = pdfFile.getName();
//        String[] split = name.split("\\.");
//        //加载PDF
//        PDDocument pdDocument = PDDocument.load(pdfFile);
//        //创建PDF渲染器
//        PDFRenderer renderer = new PDFRenderer(pdDocument);
//        int pages = pdDocument.getNumberOfPages();
//        for (int i = 0; i < pages; i++) {
//            ByteArrayOutputStream output = new ByteArrayOutputStream();
//            //将PDF的每一页渲染成一张图片
//            BufferedImage image = renderer.renderImage(i);
//            ImageIO.write(image, "png", output);
//            FileOutputStream fileOutputStream = new FileOutputStream("G:/"+split[0]+i+".png");
//            fileOutputStream.write(output.toByteArray());
//            fileOutputStream.flush();;
//            fileOutputStream.close();
//        }
//        pdDocument.close();
//    }
}

