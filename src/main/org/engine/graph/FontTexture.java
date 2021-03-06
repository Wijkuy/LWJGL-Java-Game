package main.org.engine.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class FontTexture {

	private static final String IMAGE_FORMAT = "png";

	private final Font font;

	private final String charSetName;

	private final Map<Character, CharInfo> charMap;

	private Texture texture;

	private int height;

	private int width;

	public FontTexture(Font font, String charSetName) throws Exception {
		this.font = font;
		this.charSetName = charSetName;
		this.charMap = new HashMap<>();

		buildTexture();
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public Texture getTexture() {
		return this.texture;
	}

	public CharInfo getCharInfo(char c) {
		return this.charMap.get(c);
	}

	private String getAllAvailableChars(String charsetName) {
		CharsetEncoder ce = Charset.forName(charsetName).newEncoder();
		StringBuilder result = new StringBuilder();
		for (char c = 0; c < Character.MAX_VALUE; c++) {
			if (ce.canEncode(c)) {
				result.append(c);
			}
		}
		return result.toString();
	}

	private void buildTexture() throws Exception {
		// Get the font metrics for each character for the selected font by
		// using image
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = img.createGraphics();
		g2D.setFont(this.font);
		FontMetrics fontMetrics = g2D.getFontMetrics();

		String allChars = getAllAvailableChars(this.charSetName);
		this.width = 0;
		this.height = 0;
		for (char c : allChars.toCharArray()) {
			// Get the size for each character and update global image size
			CharInfo charInfo = new CharInfo(this.width, fontMetrics.charWidth(c));
			this.charMap.put(c, charInfo);
			this.width += charInfo.getWidth();
			this.height = Math.max(this.height, fontMetrics.getHeight());
		}
		g2D.dispose();

		// Create the image associated to the charset
		img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
		g2D = img.createGraphics();
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setFont(this.font);
		fontMetrics = g2D.getFontMetrics();
		g2D.setColor(Color.WHITE);
		g2D.drawString(allChars, 0, fontMetrics.getAscent());
		g2D.dispose();

		// Dump image to a byte buffer
		InputStream is;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			ImageIO.write(img, IMAGE_FORMAT, out);
			out.flush();
			is = new ByteArrayInputStream(out.toByteArray());
		}

		this.texture = new Texture(is);
	}

	public static class CharInfo {

		private final int startX;

		private final int width;

		public CharInfo(int startX, int width) {
			this.startX = startX;
			this.width = width;
		}

		public int getStartX() {
			return this.startX;
		}

		public int getWidth() {
			return this.width;
		}
	}
}