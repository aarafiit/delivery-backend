package kn.org.deliverybackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {

    String uploadFile(MultipartFile file, String folder);

    void deleteFile(String fileKey);

    String getFileUrl(String fileKey);

    byte[] downloadFile(String fileKey);
}
