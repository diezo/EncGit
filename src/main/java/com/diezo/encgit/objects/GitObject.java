package com.diezo.encgit.objects;

import com.diezo.encgit.exceptions.UnknownFlagException;
import com.diezo.encgit.utils.Compressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.util.Arrays;

public abstract class GitObject {
    public static final int CAT_CONTENT = 0;
    public static final int CAT_TYPE = 1;

    protected static final Logger log = LoggerFactory.getLogger(GitObject.class);
    protected static final Path repoRoot =  Paths.get("").toAbsolutePath();

    // Combined content = Header + Body Content
    public byte[] bodyContent;
    public String objectType;

    public GitObject(String objectType, byte[] bodyContent) {
        this.objectType = objectType;
        this.bodyContent = bodyContent;
    }

    public static void catObject(String objectHash, int flag) throws UnknownFlagException {

        // Corresponding object file path
        Path objectFilePath = repoRoot
                .resolve(".encgit")
                .resolve("objects")
                .resolve(objectHash.substring(0, 2))
                .resolve(objectHash.substring(2));

        // Validate object file exists
        if (!Files.isRegularFile(objectFilePath)) {
            System.out.println("Couldn't find corresponding object file " + objectHash);
            return;
        }

        try {
            byte[] data = Files.readAllBytes(objectFilePath);
            byte[] inflated = Compressor.zlib_inflate(data);

            int headerEnd = -1;

            for (int i = 0; i < inflated.length; i++) {
                if (inflated[i] == (byte)'\0') {
                    headerEnd = i;
                    break;
                }
            }

            // Missing null separator in header
            if (headerEnd == -1) {
                System.out.println("Corrupted object file " + objectHash);
                return;
            }

            String header = new String(inflated, 0, headerEnd, StandardCharsets.UTF_8);

            // Invalid header format
            int spaceIndex = header.indexOf(" ");
            if (spaceIndex == -1) {
                System.out.println("Corrupted object file " + objectHash);
                return;
            }

            // Parse header
            String objectType = header.substring(0, spaceIndex);
            long size = Long.parseLong(header.substring(spaceIndex + 1));

            int contentStart = headerEnd + 1;
            int contentLength = inflated.length - contentStart;

            // Content length size mismatch between actual length and length declared in header
            if (contentLength != size) {
                System.out.println("Corrupted object file " + objectHash);
                return;
            }

            // Parse content
            byte[] content = Arrays.copyOfRange(inflated, contentStart, inflated.length);

            if (flag == CAT_TYPE) {  // Print object type
                System.out.println(objectType);
            } else if (flag == CAT_CONTENT) {  // Print object content
                switch (objectType) {  // Respective child classes will handle rest
                    case "blob": BlobObject.catBlobObject(content); break;
                    case "tree": TreeObject.catTreeObject(content); break;
                    case "commit": CommitObject.catCommitObject(content); break;
                    default: System.out.println("Unknown type of object file " + objectHash); break;
                }
            } else {  // Invalid flag
                throw new UnknownFlagException();
            }

        } catch (IOException e) {
            System.out.println("Couldn't read object file " + objectHash);
        }
    }

    void writeObject(String contentHash) throws IOException {

        // Prepare header
        String header = objectType + " " + bodyContent.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

        // Combine header + content bytes
        byte[] combinedObject = new byte[headerBytes.length + bodyContent.length];
        System.arraycopy(headerBytes, 0, combinedObject, 0, headerBytes.length);
        System.arraycopy(bodyContent, 0, combinedObject, headerBytes.length, bodyContent.length);

        // Compress combined bytes
        byte[] compressedObject = Compressor.zlib_deflate(combinedObject);

        // Resolve target object path
        Path objectsDir = repoRoot.resolve(".encgit").resolve("objects");
        Path subObjectDir = objectsDir.resolve(contentHash.substring(0, 2));
        Path objectFilePath = subObjectDir.resolve(contentHash.substring(2));

        // Create sub-directory
        if (!Files.exists(subObjectDir) || !Files.isDirectory(subObjectDir)) {
            Files.createDirectory(subObjectDir);
        }

        // Write combined object bytes
        Files.write(objectFilePath, compressedObject);
    }

    public static boolean objectExists(Path repoRoot, String hash) {
        Path subObjectDir = repoRoot.resolve("objects").resolve(hash.substring(0, 2));
        Path objectFile = subObjectDir.resolve(hash.substring(2));

        return Files.exists(objectFile) && Files.isRegularFile(objectFile);
    }
}
