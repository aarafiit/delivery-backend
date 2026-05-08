package kn.org.deliverybackend.util;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FileStorageUtil {

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "pdf", "application/pdf",
            "gif", "image/gif"
    );

    public static String generateUniqueFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        return uuid + "_" + timestamp + "." + extension;
    }

    public static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public static String buildFileKey(String folder, String filename) {
        return folder + "/" + filename;
    }

    public static String buildFileUrl(String endpoint, String bucketName, String fileKey) {
        return endpoint + "/" + bucketName + "/" + fileKey;
    }

    public static boolean isValidFileExtension(String extension, List<String> allowedExtensions) {
        return allowedExtensions.contains(extension.toLowerCase());
    }

    public static boolean isValidFileSize(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }

    public static String sanitizeFileName(String filename) {
        String name = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        return name.replaceAll("_{2,}", "_");
    }

    public static String extractFileKeyFromUrl(String fileUrl) {
        String[] parts = fileUrl.split("/");
        if (parts.length < 2) {
            return fileUrl;
        }
        return parts[parts.length - 2] + "/" + parts[parts.length - 1];
    }

    public static String getContentType(String extension) {
        return CONTENT_TYPES.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static boolean isImageFile(String extension) {
        List<String> imageExtensions = List.of("jpg", "jpeg", "png", "gif");
        return imageExtensions.contains(extension.toLowerCase());
    }

    public static boolean isDocumentFile(String extension) {
        List<String> documentExtensions = List.of("pdf", "doc", "docx");
        return documentExtensions.contains(extension.toLowerCase());
    }
}
