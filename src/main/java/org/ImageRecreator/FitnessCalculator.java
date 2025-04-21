package org.ImageRecreator;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

import java.awt.*;
import java.awt.image.*;

public class FitnessCalculator {
    private static final int alpha = 128;
    private final BufferedImage targetImg;
    private BufferedImage paintedImg;

    public FitnessCalculator(BufferedImage targetImg) {
        this.targetImg = targetImg;
        paintedImg = new BufferedImage(targetImg.getWidth(), targetImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    public BufferedImage getPaintedImg() {
        return paintedImg;
    }

    public BufferedImage paintImage(Polygon polygon, boolean paintFittest) {
        BufferedImage canvas = cloneImage(paintedImg);
        Graphics g = canvas.createGraphics();
        //Drawing on canvas using Graphics
        int[] xCoordinates = polygon.getXCoordinates();
        int[] yCoordinates = polygon.getYCoordinates();
        int n = polygon.getNumOfPoints();
        g.setColor(new Color(polygon.getGene(0), polygon.getGene(1), polygon.getGene(2), alpha));
        g.fillPolygon(xCoordinates, yCoordinates, n);
        g.dispose();

        if (paintFittest) paintedImg = canvas;

        return canvas;
    }

    public long calculateFitnessSIMD(Polygon polygon) {
        BufferedImage paintedPolygonImg = paintImage(polygon, false);
        Raster paintedRaster = paintedPolygonImg.getRaster();
        Raster targetRaster = targetImg.getRaster();

        DataBuffer paintedDataBuffer = paintedRaster.getDataBuffer();
        DataBuffer targetDataBuffer = targetRaster.getDataBuffer();

        // Make sure we are using DataBufferInt for direct array access
        int[] painted = ((DataBufferInt) paintedDataBuffer).getData();
        int[] target = ((DataBufferInt) targetDataBuffer).getData();

        long fitness = 0;
        final int length = painted.length;
        final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;

        int i = 0;
        for (; i < SPECIES.loopBound(length); i += SPECIES.length()) {
            IntVector p = IntVector.fromArray(SPECIES, painted, i);
            IntVector t = IntVector.fromArray(SPECIES, target, i);

            IntVector r1 = p.lanewise(VectorOperators.ASHR, 16).and(0xFF);
            IntVector g1 = p.lanewise(VectorOperators.ASHR, 8).and(0xFF);
            IntVector b1 = p.and(0xFF);

            IntVector r2 = t.lanewise(VectorOperators.ASHR, 16).and(0xFF);
            IntVector g2 = t.lanewise(VectorOperators.ASHR, 8).and(0xFF);
            IntVector b2 = t.and(0xFF);

            IntVector dr = r1.sub(r2);
            IntVector dg = g1.sub(g2);
            IntVector db = b1.sub(b2);

            IntVector sq = dr.mul(dr).add(dg.mul(dg)).add(db.mul(db));

            fitness += sq.reduceLanes(VectorOperators.ADD);
        }

        for (; i < length; i++) {
            int rgb1 = painted[i];
            int rgb2 = target[i];
            int dr = ((rgb1 >>> 16) & 0xFF) - ((rgb2 >>> 16) & 0xFF);
            int dg = ((rgb1 >>> 8) & 0xFF) - ((rgb2 >>> 8) & 0xFF);
            int db = (rgb1 & 0xFF) - (rgb2 & 0xFF);
            fitness += dr * dr + dg * dg + db * db;
        }

        return fitness;
    }

//    public long calculateFitness(Polygon polygon) {
//        // Paint polygon on paintedImg
//        BufferedImage paintedPolygonImg = paintImage(polygon, false);
//        long fitness = 0;
//        Raster paintedRaster = paintedPolygonImg.getRaster();
//        Raster targetRaster = targetImg.getRaster();
//
//        DataBuffer paintedDataBuffer = paintedRaster.getDataBuffer();
//        DataBuffer targetDataBuffer = targetRaster.getDataBuffer();
//
//        for (int y = 0; y < targetImg.getHeight(); y++) {
//            for (int x = 0; x < targetImg.getWidth(); x++) {
//                int rgbImg1 = paintedDataBuffer.getElem(x + y * paintedRaster.getWidth());
//                int rgbImg2 = targetDataBuffer.getElem(x + y * paintedRaster.getWidth());
//
//                int redImg1 = (rgbImg1 >> 16) & 0xff;
//                int greenImg1 = (rgbImg1 >> 8) & 0xff;
//                int blueImg1 = (rgbImg1) & 0xff;
//                int redImg2 = (rgbImg2 >> 16) & 0xff;
//                int greenImg2 = (rgbImg2 >> 8) & 0xff;
//                int blueImg2 = (rgbImg2) & 0xff;
//                int deltaRed = redImg1 - redImg2;
//                int deltaGreen = greenImg1 - greenImg2;
//                int deltaBlue = blueImg1 - blueImg2;
//                fitness += deltaRed * deltaRed + deltaGreen * deltaGreen + deltaBlue * deltaBlue;
//            }
//        }
//        return fitness;
//    }

    public static BufferedImage cloneImage(BufferedImage bufferImage) {
        ColorModel colorModel = bufferImage.getColorModel();
        WritableRaster raster = bufferImage.copyData(null);
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }
}
