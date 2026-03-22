package com.diezo.encgit.objects;

import com.diezo.encgit.core.EncryptionManager;
import com.diezo.encgit.exceptions.UnknownFlagException;
import com.diezo.encgit.utils.Compressor;
import com.diezo.encgit.utils.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public abstract class GitObject {
    public static final int CAT_CONTENT = 0;
    public static final int CAT_TYPE = 1;

    protected static final Logger log = LoggerFactory.getLogger(GitObject.class);
    protected static final Path repoRoot =  Paths.get("").toAbsolutePath();

    // Combined content = Header + Body Content
    public byte[] blobContent;
    public FilesystemDirectory rootDirectory;
    public String rootTreeHash;
    public String objectType;

    public GitObject(
            String objectType,
            byte[] blobContent,
            FilesystemDirectory rootDirectory,
            String rootTreeHash
    ) {
        this.objectType = objectType;
        this.blobContent = blobContent;
        this.rootDirectory = rootDirectory;
        this.rootTreeHash = rootTreeHash;
    }

    public static void catObject(String objectHash, int flag, SecretKey secureKey) throws UnknownFlagException {

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
            byte[] decrypted = EncryptionManager.decrypt(inflated, secureKey);

            int headerEnd = -1;

            for (int i = 0; i < decrypted.length; i++) {
                if (decrypted[i] == (byte)'\0') {
                    headerEnd = i;
                    break;
                }
            }

            // Missing null separator in header
            if (headerEnd == -1) {
                System.out.println("Corrupted object file " + objectHash);
                return;
            }

            String header = new String(decrypted, 0, headerEnd, StandardCharsets.UTF_8);

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
            int contentLength = decrypted.length - contentStart;

            // Content length size mismatch between actual length and length declared in header
            if (contentLength != size) {
                System.out.println("Corrupted object file " + objectHash);
                return;
            }

            // Parse content
            byte[] content = Arrays.copyOfRange(decrypted, contentStart, decrypted.length);

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

    void writeObject(String contentHash, SecretKey secureKey) throws IOException {

        // TODO: Fundamental mistake!! Hash is calculated of content and not the entire object!!

        // Prepare header
        String header = objectType + " " + blobContent.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

        // Combine header + content bytes
        byte[] combinedObject = new byte[headerBytes.length + blobContent.length];
        System.arraycopy(headerBytes, 0, combinedObject, 0, headerBytes.length);
        System.arraycopy(blobContent, 0, combinedObject, headerBytes.length, blobContent.length);

        // Encrypt combined bytes
        byte[] encryptedBytes = EncryptionManager.encrypt(combinedObject, secureKey);

        // Compress encrypted bytes
        byte[] compressedObject = Compressor.zlib_deflate(encryptedBytes);

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

    String writeTreeObject(byte[] treeContent, SecretKey secureKey) throws IOException {

        // Prepare header
        String header = objectType + " " + treeContent.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

        // Combine header + content bytes
        byte[] combinedObject = new byte[headerBytes.length + treeContent.length];
        System.arraycopy(headerBytes, 0, combinedObject, 0, headerBytes.length);
        System.arraycopy(treeContent, 0, combinedObject, headerBytes.length, treeContent.length);

        // Encrypt combined bytes
        byte[] encryptedBytes = EncryptionManager.encrypt(combinedObject, secureKey);

        // Compute object hash
        String objectHash = HashUtil.sha256(encryptedBytes);

        // Compress encrypted bytes
        byte[] compressedObject = Compressor.zlib_deflate(encryptedBytes);

        // Resolve target object path
        Path objectsDir = repoRoot.resolve(".encgit").resolve("objects");
        Path subObjectDir = objectsDir.resolve(objectHash.substring(0, 2));
        Path objectFilePath = subObjectDir.resolve(objectHash.substring(2));

        // Create sub-directory
        if (!Files.exists(subObjectDir) || !Files.isDirectory(subObjectDir)) {
            Files.createDirectory(subObjectDir);
        }

        // Write combined object bytes
        Files.write(objectFilePath, compressedObject);

        // Return object hash
        return objectHash;
    }

    String writeCommitObject(byte[] content) throws IOException {

        // Prepare header
        String header = objectType + " " + content.length + "\0";
        byte[] headerBytes = header.getBytes(StandardCharsets.UTF_8);

        // Combine header + content bytes
        byte[] combinedObject = new byte[headerBytes.length + content.length];
        System.arraycopy(headerBytes, 0, combinedObject, 0, headerBytes.length);
        System.arraycopy(content, 0, combinedObject, headerBytes.length, content.length);

        // Compute object hash
        String objectHash = HashUtil.sha256(combinedObject);

        // Compress combined bytes
        byte[] compressedObject = Compressor.zlib_deflate(combinedObject);

        // Resolve target object path
        Path objectsDir = repoRoot.resolve(".encgit").resolve("objects");
        Path subObjectDir = objectsDir.resolve(objectHash.substring(0, 2));
        Path objectFilePath = subObjectDir.resolve(objectHash.substring(2));

        // Create sub-directory
        if (!Files.exists(subObjectDir) || !Files.isDirectory(subObjectDir)) {
            Files.createDirectory(subObjectDir);
        }

        // Write combined object bytes
        Files.write(objectFilePath, compressedObject);

        // Return object hash
        return objectHash;
    }

    public static boolean objectExists(Path repoRoot, String hash) {
        Path subObjectDir = repoRoot.resolve("objects").resolve(hash.substring(0, 2));
        Path objectFile = subObjectDir.resolve(hash.substring(2));

        return Files.exists(objectFile) && Files.isRegularFile(objectFile);
    }
}
