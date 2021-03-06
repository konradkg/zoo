package zoo.htmunit.tryouts;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

public class OCR {
    private static final String INPUT = "d:/Temp/Tesseract-OCR/temp/captcha2.png";
    private static final String OUTPUT = "d:/Temp/Tesseract-OCR/temp/captcha-out.png";
    private static final String TESSERACT_BIN = "d:/Temp/Tesseract-OCR/tesseract.exe";
    private static final String TESSERACT_OUTPUT = "d:/Temp/Tesseract-OCR/temp/captcha-out";
    private static final int WHITE = 0x00FFFFFF, BLACK = 0x00000000;

    public static void main(String... args) throws Exception {
	BufferedImage image = ImageIO.read(new FileInputStream(INPUT));
	int average = 0;

	for (int row = 0; ++row < image.getHeight();)
	    for (int column = 0; ++column < image.getWidth();)
		average += image.getRGB(column, row) & 0x000000FF;
	average /= image.getWidth() * image.getHeight();

	for (int row = 0; ++row < image.getHeight();)
	    for (int column = 0; ++column < image.getWidth();)
		if ((image.getRGB(column, row) & 0x000000FF) <= average * .80)
		    image.setRGB(column, row, BLACK);
		else
		    image.setRGB(column, row, WHITE);

	for (int row = 0; ++row < image.getHeight();)
	    for (int column = 0; ++column < image.getWidth();)
		if ((image.getRGB(column, row) & WHITE) == WHITE) {
		    int height = countVerticalWhite(image, column, row);
		    int width = countHorizontalWhite(image, column, row);
		    if ((width * height <= 6) || (width == 1) || (height == 1))
			image.setRGB(column, row, BLACK);
		}

	for (int row = 0; ++row < image.getHeight();)
	    for (int column = 0; ++column < image.getWidth();)
		if ((image.getRGB(column, row) & WHITE) != WHITE)
		    if (countBlackNeighbors(image, column, row) <= 3)
			image.setRGB(column, row, WHITE);

	ImageIO.write(image, "png", new File(OUTPUT));
	Process tesseractProc = Runtime.getRuntime().exec(
	        TESSERACT_BIN + " " + OUTPUT + " " + TESSERACT_OUTPUT + " nobatch");
	tesseractProc.waitFor();
	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(TESSERACT_OUTPUT
	        + ".txt")));
	System.out.println("CAPTCHA: " + reader.readLine());
	reader.close();
    }

    private static int countVerticalWhite(BufferedImage image, int x, int y) {
	return (countAboveWhite(image, x, y) + countBelowWhite(image, x, y)) + 1;
    }

    private static int countHorizontalWhite(BufferedImage image, int x, int y) {
	return (countLeftWhite(image, x, y) + countRightWhite(image, x, y)) + 1;
    }

    
    private static int countAboveWhite(BufferedImage image, int x, int y) {
	int leftWhite = 0;
	y--;
	while (y-- > 0)
	    if ((image.getRGB(x, y) & WHITE) == WHITE)
		leftWhite++;
	    else
		break;
	return leftWhite;
    }
    
    private static int countBelowWhite(BufferedImage image, int x, int y) {
	int rightWhite = 0;
	y++;
	while (y < image.getHeight())
	    if ((image.getRGB(x, y++) & WHITE) == WHITE)
		rightWhite++;
	    else
		break;
	return rightWhite;
    }
    
    private static int countLeftWhite(BufferedImage image, int x, int y) {
	int leftWhite = 0;
	x--;
	while (x-- > 0)
	    if ((image.getRGB(x, y) & WHITE) == WHITE)
		leftWhite++;
	    else
		break;
	return leftWhite;
    }

    private static int countRightWhite(BufferedImage image, int x, int y) {
	int rightWhite = 0;
	x++;
	while (x < image.getWidth())
	    if ((image.getRGB(x++, y) & WHITE) == WHITE)
		rightWhite++;
	    else
		break;
	return rightWhite;
    }

    private static int countBlackNeighbors(BufferedImage image, int x, int y) {
	int numBlacks = 0;
	if (pixelColor(image, x - 1, y) != WHITE)
	    numBlacks++;
	if (pixelColor(image, x - 1, y + 1) != WHITE)
	    numBlacks++;
	if (pixelColor(image, x - 1, y - 1) != WHITE)
	    numBlacks++;
	if (pixelColor(image, x, y + 1) != WHITE)
	    numBlacks++;
	if (pixelColor(image, x, y - 1) != WHITE)
	    numBlacks++;
	if (pixelColor(image, x + 1, y) != WHITE)
	    numBlacks++;
	if (pixelColor(image, x + 1, y + 1) != WHITE)
	    numBlacks++;
	if (pixelColor(image, x + 1, y - 1) != WHITE)
	    numBlacks++;
	return numBlacks;
    }

    private static int pixelColor(BufferedImage image, int x, int y) {
	if (x >= image.getWidth() || x < 0 || y < 0 || y >= image.getHeight())
	    return WHITE;
	return image.getRGB(x, y) & WHITE;
    }

}
