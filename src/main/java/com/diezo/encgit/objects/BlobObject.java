package com.diezo.encgit.objects;

import com.diezo.encgit.core.StageManager;
import com.diezo.encgit.exceptions.UnknownFlagException;
import com.diezo.encgit.utils.HashUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

public class BlobObject extends GitObject {

    public BlobObject(byte[] content) {
        super("blob", content);
    }

    public void writeToObjectsDir(Path filePath) {
        String contentHash = HashUtil.sha256(bodyContent);

        // Write new object file
        if (!objectExists(repoRoot, contentHash)) {
            try { writeObject(contentHash); }
            catch (IOException e) {
                System.out.println("Error: Could not write object " + contentHash);
                log.error(e.toString());
                return;
            }
        }

        // Write to index file
        try { StageManager.indexFile(filePath, contentHash, repoRoot); }
        catch (IOException e) { System.out.println("Error: Could not parse index file"); }
    }

    public static void catBlobObject(byte[] content) {
        System.out.println(new String(content, StandardCharsets.UTF_8));
    }
}
