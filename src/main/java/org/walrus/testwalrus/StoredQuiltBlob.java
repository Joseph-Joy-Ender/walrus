package org.walrus.testwalrus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoredQuiltBlob {
    private String identifier;      // The filename or identifier
    private String quiltPatchId;
}
