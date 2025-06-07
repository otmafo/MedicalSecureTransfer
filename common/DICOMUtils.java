package common;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.SafeClose;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DICOMUtils {
    private static final Logger logger = Logger.getLogger(DICOMUtils.class.getName());
    
    public static String parseDICOM(File dicomFile) throws IOException {
        StringBuilder info = new StringBuilder();
        DicomInputStream dis = null;
        
        try {
            logger.info("开始解析DICOM文件: " + dicomFile.getAbsolutePath());
            dis = new DicomInputStream(dicomFile);
            
            // 设置解析选项以提高兼容性
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            
            // 读取数据集
            Attributes dataset = dis.readDataset();
            
            // 基本信息
            info.append("========== DICOM 元数据 ==========\n");
            info.append("患者姓名: ").append(getTagValue(dataset, Tag.PatientName)).append("\n");
            info.append("患者ID: ").append(getTagValue(dataset, Tag.PatientID)).append("\n");
            info.append("检查日期: ").append(getTagValue(dataset, Tag.StudyDate)).append("\n");
            info.append("检查时间: ").append(getTagValue(dataset, Tag.StudyTime)).append("\n");
            info.append("设备类型: ").append(getTagValue(dataset, Tag.Modality)).append("\n");
            
            // 图像尺寸
            int width = dataset.getInt(Tag.Columns, 0);
            int height = dataset.getInt(Tag.Rows, 0);
            info.append("图像尺寸: ").append(width).append(" x ").append(height).append("\n");
            
            // 检查信息
            info.append("检查描述: ").append(getTagValue(dataset, Tag.StudyDescription)).append("\n");
            info.append("序列描述: ").append(getTagValue(dataset, Tag.SeriesDescription)).append("\n");
            
            // 实例信息
            info.append("实例编号: ").append(getTagValue(dataset, Tag.InstanceNumber)).append("\n");
            info.append("SOP实例UID: ").append(getTagValue(dataset, Tag.SOPInstanceUID)).append("\n");
            
            // 其他
            info.append("机构名称: ").append(getTagValue(dataset, Tag.InstitutionName)).append("\n");
            info.append("设备型号: ").append(getTagValue(dataset, Tag.ManufacturerModelName)).append("\n");
            
            logger.info("成功解析DICOM文件: " + dicomFile.getName());
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "解析DICOM文件失败: " + dicomFile.getName(), ex);
            throw new IOException("解析DICOM文件失败: " + ex.getMessage(), ex);
        } finally {
            SafeClose.close(dis);
        }
        return info.toString();
    }

    private static String getTagValue(Attributes dataset, int tag) {
        try {
            String value = dataset.getString(tag);
            return value != null ? value.trim() : "未提供";
        } catch (Exception e) {
            return "获取失败";
        }
    }
    
    // 验证文件是否为DICOM格式
    public static boolean isDICOMFile(File file) {
        try (DicomInputStream dis = new DicomInputStream(file)) {
            // 尝试读取文件头
            dis.readFileMetaInformation();
            return true;
        } catch (Exception e) {
            // 尝试第二种验证方式
            return verifyDICOMBySignature(file);
        }
    }
    
    // 通过文件签名验证DICOM
    private static boolean verifyDICOMBySignature(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            // DICOM文件应以"DICM"开头
            raf.seek(128);
            byte[] prefix = new byte[4];
            raf.readFully(prefix);
            return new String(prefix).equals("DICM");
        } catch (Exception e) {
            return false;
        }
    }
}