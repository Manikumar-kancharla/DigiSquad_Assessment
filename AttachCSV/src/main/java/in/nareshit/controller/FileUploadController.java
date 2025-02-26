package in.nareshit.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.nareshit.service.FileProcessor;

@Controller
public class FileUploadController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("startRow", 1);
        return "upload";
    }

    @PostMapping("/upload")
    public String handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("startRow") int startRow,
            Model model) {

        try {
            FileProcessor fileProcessor = new FileProcessor();
            List<List<String>> data = fileProcessor.processFile(file, startRow);
            
            model.addAttribute("headers", data.isEmpty() ? List.of() : data.get(0));
            model.addAttribute("rows", data.size() > 1 ? data.subList(1, data.size()) : List.of());
            model.addAttribute("message", "File processed successfully");
        } catch (Exception e) {
            model.addAttribute("message", "Error processing file: " + e.getMessage());
        }
        
        return "result";
    }
}