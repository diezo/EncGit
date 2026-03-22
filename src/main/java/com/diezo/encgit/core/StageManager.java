package com.diezo.encgit.core;

import com.diezo.encgit.RepositoryConstants;
import com.diezo.encgit.objects.BlobObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StageManager {
    private static final Logger log = LoggerFactory.getLogger(StageManager.class);

    public static void stageCommand(List<Path> filePaths, Path repoRoot) {
        Path encgitDir = repoRoot.resolve(".encgit");

        // Attempt to load secure key
        SecretKey secureKey = EncryptionManager.loadSecureKey(encgitDir);

        for (Path path : filePaths) {

            // Read file content bytes
            byte[] fileContent;

            try {
                fileContent = Files.readAllBytes(path);
            } catch (IOException e) {
                System.out.println("Error: Could not read file bytes \"" + path + "\"");
                continue;
            }

            BlobObject blobObject = new BlobObject(fileContent);
            blobObject.writeToObjectsDir(path, secureKey);
        }
    }

    public static void indexFile(Path filePath, String contentHash, Path repoRoot) throws IOException {
        String relativizedFilePath = repoRoot.relativize(filePath).toString().replace("\\", "/");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode indexJson =  mapper.readTree(RepositoryConstants.INDEX_DEFAULT_TEXT);

        try { indexJson = parseIndexFile(mapper, repoRoot); }  // Parse index file as JSON
        catch (IOException e) { initialiseIndex(repoRoot); }  // Invalid index json, reinitialise!

        ArrayNode indexArray = (ArrayNode) indexJson;

        // Remove redundant index item with same path
        for (int i = indexArray.size() - 1; i >= 0; i--) {
            JsonNode node = indexArray.get(i);

            if (node.get("path").asText().equals(relativizedFilePath)) {  // Entry already exists
                indexArray.remove(i);
                break;
            }
        }

        ObjectNode item = mapper.createObjectNode();
        item.put("path", relativizedFilePath);
        item.put("object_hash", contentHash);

        indexArray.add(item);

        writeToIndexFile(mapper, indexArray, repoRoot);
    }

    private static void writeToIndexFile(ObjectMapper mapper, ArrayNode indexArray, Path repoRoot) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(
                repoRoot.resolve(".encgit").resolve("index.json").toFile(),
                indexArray
        );
    }

    public static void initialiseIndex(Path repoRoot) throws IOException {
        Files.writeString(
                repoRoot.resolve(".encgit").resolve("index.json"),
                RepositoryConstants.INDEX_DEFAULT_TEXT
        );
    }

    public static JsonNode parseIndexFile(ObjectMapper mapper, Path repoRoot) throws IOException {
        byte[] data = Files.readAllBytes(repoRoot.resolve(".encgit").resolve("index.json"));
        return mapper.readTree(!(new String(data).isEmpty()) ? data : "[]".getBytes(StandardCharsets.UTF_8));
    }
}
