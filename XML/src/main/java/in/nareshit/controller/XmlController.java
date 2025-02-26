package in.nareshit.controller;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Controller
public class XmlController {

    private static final String DEFAULT_JSON_PATH = "src/main/resources/output.json"; // Default JSON save location

    // ✅ Fix: Add "/xmlx" mapping correctly
    @GetMapping("/xmlx")
    public String uploadPage(Model model) {
        return "upload"; // Refers to src/main/resources/templates/upload.html
    }

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "filePath", required = false) String customFilePath) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            // Read XML content
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode jsonNode = xmlMapper.readTree(file.getInputStream());

            // Convert to JSON string
            ObjectMapper jsonMapper = new ObjectMapper();
            String jsonString = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

            // Determine save location
            String savePath = (customFilePath != null && !customFilePath.isEmpty()) ? customFilePath : "src/main/resources/output.json";
            File jsonFile = new File(savePath);

            // ✅ Fix: Check if parent directory exists before creating it
            File parentDir = jsonFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Save JSON file
            jsonMapper.writeValue(jsonFile, jsonNode);

            return ResponseEntity.ok("XML converted to JSON and saved at: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error processing file: " + e.getMessage());
        }
    }


    @GetMapping("/xml")
    public ResponseEntity<JsonNode> getConvertedJson(
            @RequestParam(value = "filePath", required = false) String customFilePath) {
        try {
            String filePath = (customFilePath != null && !customFilePath.isEmpty()) ? customFilePath : DEFAULT_JSON_PATH;
            ObjectMapper jsonMapper = new ObjectMapper();
            JsonNode jsonNode = jsonMapper.readTree(new File(filePath));

            return ResponseEntity.ok(jsonNode);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
