package org.walrus.testwalrus;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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

}
