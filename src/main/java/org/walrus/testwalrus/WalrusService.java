package org.walrus.testwalrus;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpMethod.PUT;

@Service
@AllArgsConstructor
@Slf4j
public class WalrusService {
    private final DocumentRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    public void upload(MultipartFile file) {
        String WALRUS_PUBLISHER_URL = "https://publisher.walrus-testnet.walrus.space/v1/blobs?epochs=5";
        extractBlobIdFrom(restTemplate.exchange(WALRUS_PUBLISHER_URL, PUT,
                buildUploadRequest(file), WalrusUploadResponse.class));
    }


    private HttpEntity<?> buildUploadRequest(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        Resource resource = file.getResource();
        return new HttpEntity<>(resource, headers);
    }

    private void extractBlobIdFrom(ResponseEntity<WalrusUploadResponse> response) {
        WalrusUploadResponse walrusUploadResponse = response.getBody();
        boolean isFileAlreadyExists = walrusUploadResponse != null && walrusUploadResponse.getNewlyCreated() == null;
        if (isFileAlreadyExists) return;
        assert walrusUploadResponse != null;
        String blobId = walrusUploadResponse.getNewlyCreated().getBlobObject().getBlobId();
        log.info("blobId: {}", blobId);
        Document document = new Document();
        document.setBlobId(blobId);
        repository.save(document);
    }

    public void uploadFile(List<MultipartFile> files) throws IOException {
        String WALRUS_PUBLISHER_URL = "https://publisher.walrus-testnet.walrus.space/v1/quilts?epochs=5";
        extractBlobIdsFrom(restTemplate.exchange(WALRUS_PUBLISHER_URL, HttpMethod.PUT,
                buildMultiUploadRequest(files), WalrusUploadResponse.class));
    }

    private HttpEntity<MultiValueMap<String, Object>> buildMultiUploadRequest(List<MultipartFile> files) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            body.add(Objects.requireNonNull(file.getOriginalFilename()), new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

        }
        return new HttpEntity<>(body, headers);
    }

    private void extractBlobIdsFrom(ResponseEntity<WalrusUploadResponse> response) {
        WalrusUploadResponse walrusResponse = response.getBody();
        if (walrusResponse == null || walrusResponse.getStoredQuiltBlob() == null) return;

        // Get the main quilt blobId
        String quiltBlobId = walrusResponse.getNewlyCreated().getBlobObject().getBlobId();
        log.info("Quilt BlobId: {}", quiltBlobId);

        // Iterate over each stored blob in the quilt
        List<StoredQuiltBlob> blobs = walrusResponse.getStoredQuiltBlob();
        if (blobs != null) {
            for (StoredQuiltBlob blob : blobs) {
                String identifier = blob.getIdentifier();
                String quiltPatchId = blob.getQuiltPatchId();
                log.info("File: {}, QuiltPatchId: {}", identifier, quiltPatchId);

                Document document = new Document();
                document.setBlobId(quiltPatchId); // or store both quiltBlobId and quiltPatchId if needed
                document.setIdentifier(identifier);
                repository.save(document);
            }
        }
    }


    public ResponseEntity<byte[]> fetchBlob(String blobId) {
        String walrusUrl = "https://aggregator.walrus-testnet.walrus.space/v1/blobs/" + blobId;

        ResponseEntity<byte[]> response = restTemplate.exchange(
                walrusUrl,
                HttpMethod.GET,
                null,
                byte[].class
        );

        MediaType contentType = response.getHeaders().getContentType();
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType.toString() : "application/octet-stream")
                .body(response.getBody());
    }

}
