package com.example.leavesystem.controller;

import com.example.leavesystem.common.Result;
import com.example.leavesystem.dto.UploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UploadController {

    // 上传目录：默认项目运行目录下 uploads/
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "pdf", "doc", "docx");

    @PostMapping("/upload")
    public Result<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            // 前端会识别 413 更好，但这里先按业务错误返回也行
            throw new IllegalArgumentException("文件太大（最大 5MB）");
        }

        String original = file.getOriginalFilename();
        original = (original == null || original.isBlank()) ? "file" : Paths.get(original).getFileName().toString();

        String ext = getExt(original);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持 JPG/PNG/PDF/DOC/DOCX");
        }

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = dir.resolve(storedName).normalize();

        // 防路径穿越
        if (!target.startsWith(dir)) {
            throw new IllegalArgumentException("非法文件路径");
        }

        file.transferTo(target.toFile());

        // 返回给前端保存的访问地址（走 /api/files，方便 dev 代理）
        UploadResponse resp = new UploadResponse("/api/files/" + storedName, original, file.getSize());
        return Result.success(resp);
    }

    @GetMapping("/files/{name}")
    public ResponseEntity<Resource> getFile(@PathVariable("name") String name) throws Exception {
        // 基础校验（防止 ../）
        if (name == null || name.length() > 200 || !name.matches("[a-zA-Z0-9._-]+")) {
            return ResponseEntity.badRequest().build();
        }

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = dir.resolve(name).normalize();
        if (!filePath.startsWith(dir) || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        if (!StringUtils.hasText(contentType)) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        boolean inline = contentType.startsWith("image/") || "application/pdf".equalsIgnoreCase(contentType);
        String disposition = (inline ? "inline" : "attachment") + "; filename=\"" + name + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }

    private String getExt(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "bin";
        return filename.substring(idx + 1).toLowerCase();
    }
}
