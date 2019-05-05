package net.idea.templates.annotation.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;

import junit.framework.Assert;
import net.idea.templates.annotation.JsonConfigGenerator;

public class TestQRCode {

	protected File getFile(String prefix) {
		return new File(String.format("%s_%s.png", prefix,  BarcodeFormat.QR_CODE));
	}


	@Test
	public void test_generate() throws Exception {
		JsonNode node = null;
		ObjectMapper mapper = new ObjectMapper();
		try (InputStream in = getClass().getClassLoader()
				.getResourceAsStream("net/idea/templates/config/config.json")) {
			node = mapper.readTree(in);
		} catch (Exception x) {
			throw x;
		}

		BufferedImage image = JsonConfigGenerator.config2qrcode(node, 128);
		File qrCodeFile = getFile("test");
		ImageIO.write(image, "png", qrCodeFile);
		System.out.println("\n\nYou have successfully created QR Code.");
		Assert.assertEquals(node.toString(), JsonConfigGenerator.QRcode2config(qrCodeFile));
	}

	
}
