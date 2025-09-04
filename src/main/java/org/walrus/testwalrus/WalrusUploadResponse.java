package org.walrus.testwalrus;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WalrusUploadResponse {
    private NewlyCreated newlyCreated;
    private AlreadyCertified alreadyCertified;
    private List<StoredQuiltBlob> storedQuiltBlob;
}
