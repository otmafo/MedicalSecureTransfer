package client;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class DICOMViewer extends JFrame {
    private final JTextArea infoText = new JTextArea();
    private final JLabel imageLabel = new JLabel();
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();

    public DICOMViewer() {
        setLayout(new BorderLayout());
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(infoText));
        splitPane.setRightComponent(new JScrollPane(imageLabel));
        
        add(splitPane, BorderLayout.CENTER);
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void displayDICOM(File dicomFile) throws Exception {
        // Parse DICOM metadata
        Attributes attributes;
        try (DicomInputStream dis = new DicomInputStream(dicomFile)) {
            attributes = dis.readDataset();
        }
        
        // Display metadata
        StringBuilder sb = new StringBuilder();
        for (int tag : attributes.tags()) {
            String keyword = DICT.keywordOf(tag);
            if (keyword == null) {
                keyword = "Unknown";
            }
            
            String value = attributes.getString(tag);
            if (value == null) {
                value = "";
            }
            
            sb.append(String.format("%s (%04X,%04X): %s\n", 
                    keyword, 
                    tag >>> 16, 
                    tag & 0xFFFF,
                    value));
        }
        infoText.setText(sb.toString());
        
        // Display image
        if (attributes.containsValue(Tag.PixelData)) {
            int rows = attributes.getInt(Tag.Rows, 0);
            int cols = attributes.getInt(Tag.Columns, 0);
            byte[] pixels = attributes.getBytes(Tag.PixelData);
            
            if (pixels != null && rows > 0 && cols > 0) {
                // Create grayscale image
                BufferedImage image = new BufferedImage(cols, rows, BufferedImage.TYPE_BYTE_GRAY);
                image.getRaster().setDataElements(0, 0, cols, rows, pixels);
                
                // Scale for display
                Image scaledImage = image.getScaledInstance(400, 400, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                imageLabel.setIcon(icon);
            }
        }
    }
}