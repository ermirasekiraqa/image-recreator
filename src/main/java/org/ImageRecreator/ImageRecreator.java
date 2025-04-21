package org.ImageRecreator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageRecreator {
    public static void main(String[] args) {
        // Read the image
        BufferedImage img;
        try {
            String fileName = "apple.jpg";
            File file = new File("input-images/" + fileName);
            if (!file.exists()) {
                System.out.println("Error: File not found.");
                return;
            }

            img = ImageIO.read(file);
            if (img == null) {
                System.out.println("Error: Unsupported file format or corrupted image.");
            } else {
                System.out.println("Image read successfully: " + file.getAbsolutePath());

                //Compressing if necessary
                double scaleSize = calcScaleSize(img);
                BufferedImage resizedImg = scale(img, BufferedImage.TYPE_INT_RGB, 1 / scaleSize, 1 / scaleSize);

                GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(350, (byte) 3, 700, resizedImg);
                long startTime = System.currentTimeMillis();
                BufferedImage recreatedImg = geneticAlgorithm.runGeneticAlgorithm();
                long endTime = System.currentTimeMillis();

                double durationInSeconds = (endTime - startTime) / 1000.0;
                int minutes = (int) durationInSeconds / 60;
                int seconds = (int) Math.ceil(durationInSeconds - minutes * 60);
                System.out.println("Time taken: " + minutes + "min " + seconds + "s");

                // Resize recreated image back to original dimensions
                BufferedImage finalOutputImage = recreatedImg;
                if (scaleSize != 1.0) {
                    finalOutputImage = scale(recreatedImg, BufferedImage.TYPE_INT_RGB, scaleSize, scaleSize);
                }

                // Ensure the output directory exists
                File outputDir = new File("output-images");
                // Save the recreated image to the output folder
                File outputFile = new File(outputDir, fileName);
                try {
                    ImageIO.write(finalOutputImage, "PNG", outputFile);
                    System.out.println("Recreated image saved successfully: " + outputFile.getAbsolutePath());
                } catch (IOException e) {
                    System.out.println("Error saving the image: " + e.getMessage());
                }

            }
        } catch (IOException e) {
            System.out.println("Error reading the image: " + e.getMessage());
        }
    }

    private static double calcScaleSize(BufferedImage srcImage) {
        double scaleSize = 1.0;

        //Compressing the image for faster calculations
        if (Math.max(srcImage.getHeight(), srcImage.getWidth()) > 256) {
            //Setting scaleSize such that the larger dimension of the image scales down to 256 px
            scaleSize = (srcImage.getWidth() > srcImage.getHeight()) ? srcImage.getWidth() / 256.0 : srcImage.getHeight() / 256.0;
        }
        return scaleSize;
    }

    public static BufferedImage scale(BufferedImage srcImage, int imageType, double fWidth, double fHeight) {
        BufferedImage scaledImage = null;
        if (srcImage != null) {
            scaledImage = new BufferedImage((int) (srcImage.getWidth() * fWidth), (int) (srcImage.getHeight() * fHeight), imageType);
            Graphics2D g = scaledImage.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
            g.drawRenderedImage(srcImage, at);
        }
        return scaledImage;
    }
}
