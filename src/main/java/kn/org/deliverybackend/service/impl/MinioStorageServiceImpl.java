package kn.org.deliverybackend.service.impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import kn.org.deliverybackend.config.MinioStorageProperties;
import kn.org.deliverybackend.exception.FileStorageException;
import kn.org.deliverybackend.service.ObjectStorageService;
import kn.org.deliverybackend.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements ObjectStorageService {

    private final MinioClient minioClient;
    private final MinioStorageProperties properties;

    @Override
    public String uploadFile(MultipartFile file, String folder) {

        if (file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new FileStorageException("File name is null");
        }

        String extension = FileStorageUtil.getFileExtension(originalFilename);

        if (!FileStorageUtil.isValidFileExtension(extension, properties.getAllowedExtensions())) {
            throw new FileStorageException("Invalid file type. Allowed types: " + properties.getAllowedExtensions());
        }

        if (!FileStorageUtil.isValidFileSize(file.getSize(), properties.getMaxFileSize())) {
            throw new FileStorageException("File size exceeds maximum limit of " + 
                    FileStorageUtil.formatFileSize(properties.getMaxFileSize()));
        }

        String sanitizedFilename = FileStorageUtil.sanitizeFileName(originalFilename);
        String uniqueFileName = FileStorageUtil.generateUniqueFileName(sanitizedFilename);
        String fileKey = FileStorageUtil.buildFileKey(folder, uniqueFileName);

        try {
            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(fileKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(FileStorageUtil.getContentType(extension))
                            .build()
            );

            return FileStorageUtil.buildFileUrl(properties.getEndpoint(), properties.getBucketName(), fileKey);

        } catch (Exception e) {
            throw new FileStorageException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String fileKey) {

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(fileKey)
                            .build()
            );

        } catch (Exception e) {
            throw new FileStorageException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String fileKey) {
        return FileStorageUtil.buildFileUrl(properties.getEndpoint(), properties.getBucketName(), fileKey);
    }

    @Override
    public byte[] downloadFile(String fileKey) {

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(fileKey)
                            .build()
            );

            return stream.readAllBytes();

        } catch (Exception e) {
            throw new FileStorageException("Failed to download file: " + e.getMessage(), e);
        }
    }
}
