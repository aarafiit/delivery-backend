package kn.org.deliverybackend.controller;

import kn.org.deliverybackend.enumeration.StorageFolder;
import kn.org.deliverybackend.service.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class FileUploadController {

    private final ObjectStorageService objectStorageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folderType") StorageFolder folderType) {

        String fileUrl = objectStorageService.uploadFile(file, folderType.getPath());
        return ResponseEntity.status(HttpStatus.CREATED).body(fileUrl);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam("fileKey") String fileKey) {

        objectStorageService.deleteFile(fileKey);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/url")
    public ResponseEntity<String> getFileUrl(@RequestParam("fileKey") String fileKey) {

        String fileUrl = objectStorageService.getFileUrl(fileKey);
        return ResponseEntity.ok(fileUrl);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("fileKey") String fileKey) {

        byte[] fileData = objectStorageService.downloadFile(fileKey);
        
        String filename = fileKey.substring(fileKey.lastIndexOf('/') + 1);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", "application/octet-stream")
                .body(fileData);
    }
}
