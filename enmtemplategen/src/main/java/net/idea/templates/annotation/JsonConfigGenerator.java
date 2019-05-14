package net.idea.templates.annotation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.poi.hssf.util.CellReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.enanomapper.maker.IAnnotator;
import net.enanomapper.maker.TR;
import net.enanomapper.parser.KEYWORD;

public class JsonConfigGenerator implements IAnnotator {
	protected ObjectMapper mapper = new ObjectMapper();
	protected ObjectNode config = mapper.createObjectNode();

	public JsonConfigGenerator() throws IOException {
		super();
	}

	protected void setColumnIndex(ObjectNode node, int col) {
		node.put(KEYWORD.COLUMN_INDEX.name(), CellReference.convertNumToColString(col));
	}


	@Override
	public void init() {
		
	}
	
	protected ObjectNode getJsonConfig(String key) {
		JsonNode _root = config.get(key);
		if (_root == null) {
			try (InputStream in = getClass().getClassLoader()
					.getResourceAsStream("net/idea/templates/config/config.json")) {
				JsonNode node = mapper.readTree(in);
				config.set(key, (ObjectNode) node);
				return (ObjectNode) node;
			} catch (IOException x) {
				x.printStackTrace();
				ObjectNode root = mapper.createObjectNode();
				ObjectNode node_template = mapper.createObjectNode();
				ObjectNode node_dataaccess = mapper.createObjectNode();
				ObjectNode node_substancerecord = mapper.createObjectNode();
				ArrayNode node_protocolapplications = mapper.createArrayNode();
				ObjectNode node_protocolapplication = mapper.createObjectNode();
				node_protocolapplication.set(KEYWORD.PARAMETERS.name(), mapper.createObjectNode());
				ArrayNode node_effects = mapper.createArrayNode();
				node_protocolapplication.set(KEYWORD.EFFECTS.name(), node_effects);
				node_protocolapplications.add(node_protocolapplication);

				root.set(KEYWORD.TEMPLATE_INFO.name(), node_template);
				root.set(KEYWORD.DATA_ACCESS.name(), node_dataaccess);
				root.set(KEYWORD.SUBSTANCE_RECORD.name(), node_substancerecord);
				root.set(KEYWORD.PROTOCOL_APPLICATIONS.name(), node_protocolapplications);

				config.set(key, root);
				return root;
			}

		} else {
			return (ObjectNode) _root;
		}

	}

	@Override
	public void process(TR record, String queryField, String query, int maxhits, String label) {
	}

	protected ObjectNode getProtocolApplication(String id, int index) {
		ObjectNode root = getJsonConfig(id);
		ArrayNode papp = (ArrayNode) (root.get(KEYWORD.PROTOCOL_APPLICATIONS.name()));
		return (ObjectNode) papp.get(0);
	}

	@Override
	public void process(TR record) {
		int row = Integer.parseInt(record.get(TR.hix.Row.name()).toString());
		int col = Integer.parseInt(record.get(TR.hix.Column.name()).toString());
		String id = record.get(TR.hix.id.name()).toString();
		String value = record.get(TR.hix.cleanedvalue.name()).toString();
		if (row == 0)
			return;
		if (row > 1)
			return;
		Object annotation = TR.hix.Annotation.get(record);
		if ("data".equals(annotation.toString()))
			return;
		Object level1 = TR.hix.JSON_LEVEL1.get(record);

		try {
			KEYWORD key = KEYWORD.valueOf(level1.toString());
			switch (key) {
			case PARAMETERS: {
				ObjectNode params = (ObjectNode) getProtocolApplication(id, 0).get(KEYWORD.PARAMETERS.name());
				ObjectNode param = mapper.createObjectNode();
				setColumnIndex(param, col);
				params.set(value, param);
				break;

			}
			case EFFECTS: {
				/*
				 * ArrayNode effects = (ArrayNode)
				 * getProtocolApplication(id,0).get(KEYWORD.EFFECTS.name());
				 * ObjectNode effect = mapper.createObjectNode();
				 * 
				 * ObjectNode effect_endpoint = mapper.createObjectNode();
				 * //setColumnIndex(param,col); //params.set(value, param);
				 * effects.add(effect);
				 */
				break;
			}
			case SUBSTANCE_RECORD: {
				/*
				 * ObjectNode params = (ObjectNode) getProtocolApplication(id,
				 * 0).get(KEYWORD.PARAMETERS.name()); ObjectNode param =
				 * mapper.createObjectNode(); setColumnIndex(param, col);
				 * params.set("E.sop_reference", param);
				 */
				break;
			}

			}

		} catch (Exception x) {
			// skip
		}

	}

	@Override
	public String getLabel_tag() {
		return null;
	}

	@Override
	public String getTerm_tag() {
		return null;
	}

	@Override
	public void setTerm_tag(String term_tag) {
	}

	@Override
	public String getQueryField() {
		return null;
	}

	@Override
	public void setQueryField(String queryField) {
	}

	@Override
	public String toString() {
		try {
			return mapper.writeValueAsString(config);
		} catch (Exception x) {
			return x.getMessage();
		}

	}

	public static BufferedImage config2qrcode(JsonNode node, int size) throws Exception {
		String codeText = node.toString();
		System.out.println(codeText);

		try {

			Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
			hintMap.put(EncodeHintType.CHARACTER_SET, "ASCII");

			hintMap.put(EncodeHintType.MARGIN, 1);
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

			BufferedImage image = null;

			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix byteMatrix = qrCodeWriter.encode(codeText, BarcodeFormat.QR_CODE, size, size, hintMap);
			int w = byteMatrix.getWidth();
			image = new BufferedImage(w, w, BufferedImage.TYPE_INT_RGB);
			image.createGraphics();

			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, w, w);
			graphics.setColor(Color.BLACK);

			for (int i = 0; i < w; i++) {
				for (int j = 0; j < w; j++) {
					if (byteMatrix.get(i, j)) {
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}

			return image;

		} catch (Exception e) {
			throw e;
		} finally {

		}

	}

	public static String QRcode2config(File qrCodeFile) throws Exception {
		try (FileInputStream in = new FileInputStream(qrCodeFile)) {
			BufferedImage image = ImageIO.read(in);
			BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
			QRCodeReader reader = new QRCodeReader();
			Result result = reader.decode(binaryBitmap);
			return result.getText();
		}
	}

	@Override
	public void done() {
		
	}	
}
