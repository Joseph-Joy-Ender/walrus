package org.walrus.testwalrus;

import lombok.Data;

@Data
public class DocumentResponse {
    private String userId;
    private String blobId;
    private String hash;
}
