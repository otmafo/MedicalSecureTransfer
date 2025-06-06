package shared;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import java.io.File;

public class DicomParser {
    public static String parseDicomInfo(File dicomFile) {
        StringBuilder info = new StringBuilder();
        try (DicomInputStream dis = new DicomInputStream(dicomFile)) {
            Attributes metadata = dis.readDataset(-1, -1);
            
            info.append("DICOM File Information:\n");
            info.append("Patient Name: ").append(metadata.getString(Tag.PatientName, "N/A")).append("\n");
            info.append("Patient ID: ").append(metadata.getString(Tag.PatientID, "N/A")).append("\n");
            info.append("Study Date: ").append(metadata.getString(Tag.StudyDate, "N/A")).append("\n");
            info.append("Modality: ").append(metadata.getString(Tag.Modality, "N/A")).append("\n");
            info.append("Image Size: ").append(metadata.getInt(Tag.Columns, 0)).append("x")
                .append(metadata.getInt(Tag.Rows, 0)).append("\n");
            info.append("Bits Stored: ").append(metadata.getInt(Tag.BitsStored, 0)).append("\n");
            info.append("SOP Class UID: ").append(metadata.getString(Tag.SOPClassUID, "N/A")).append("\n");
            info.append("Instance Number: ").append(metadata.getString(Tag.InstanceNumber, "N/A")).append("\n");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing DICOM file: " + e.getMessage();
        }
        return info.toString();
    }
}