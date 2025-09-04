package org.walrus.testwalrus;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/documents")
@AllArgsConstructor
public class DocumentController {
    private final WalrusService walrusService;


    @PostMapping("/upload")
    public ResponseEntity<String> uploadUserDocument(@RequestParam("file") MultipartFile file) {
        try {
            walrusService.upload(file);
            return ResponseEntity.ok("Uploaded and saved!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/upload-quilt")
    public ResponseEntity<?> uploadQuilt(@RequestParam("file") List<MultipartFile> file) {
        try {
            walrusService.uploadFile(file);
            return ResponseEntity.ok("Quilts Uploaded and saved!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{blobId}")
    public ResponseEntity<byte[]> getFile(@PathVariable String blobId) {
        return walrusService.fetchBlob(blobId);
    }

}
