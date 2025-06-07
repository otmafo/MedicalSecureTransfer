package common;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.SafeClose;

import java.io.File;
import java.io.IOException;

public class DICOMUtils {
    public static String parseDICOM(File dicomFile) throws IOException {
        StringBuilder info = new StringBuilder();
        DicomInputStream dis = null;
        
        try {
            dis = new DicomInputStream(dicomFile);
            Attributes dataset = dis.readDataset();
            
            // 使用Tag常量代替字符串
            info.append("Patient Name: ").append(getTagValue(dataset, Tag.PatientName)).append("\n");
            info.append("Patient ID: ").append(getTagValue(dataset, Tag.PatientID)).append("\n");
            info.append("Study Date: ").append(getTagValue(dataset, Tag.StudyDate)).append("\n");
            info.append("Modality: ").append(getTagValue(dataset, Tag.Modality)).append("\n");
            
            // 获取图像尺寸
            int width = dataset.getInt(Tag.Columns, 0);
            int height = dataset.getInt(Tag.Rows, 0);
            info.append("Image Size: ").append(width).append(" x ").append(height).append("\n");
            
            // 添加更多有用的DICOM信息
            info.append("Study Description: ").append(getTagValue(dataset, Tag.StudyDescription)).append("\n");
            info.append("Series Description: ").append(getTagValue(dataset, Tag.SeriesDescription)).append("\n");
            info.append("Instance Number: ").append(getTagValue(dataset, Tag.InstanceNumber)).append("\n");
            
        } catch (IOException ex) {
            throw ex;
        } finally {
            SafeClose.close(dis);
        }
        return info.toString();
    }

    private static String getTagValue(Attributes dataset, int tag) {
        String value = dataset.getString(tag);
        return value != null ? value.trim() : "N/A";
    }
    
    // 可选：添加更多DICOM元数据提取方法
    public static String getPatientName(File dicomFile) throws IOException {
        try (DicomInputStream dis = new DicomInputStream(dicomFile)) {
            Attributes dataset = dis.readDataset();
            return dataset.getString(Tag.PatientName, "Unknown");
        }
    }
}