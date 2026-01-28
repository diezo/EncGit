package com.diezo.encgit.core;

import com.diezo.encgit.objects.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.source.tree.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CommitManager {
    private static final Logger log = LoggerFactory.getLogger(CommitManager.class);
    private static final Path repoRoot = Paths.get("").toAbsolutePath();

    public static void commitCommand(String message) {
        // TODO: Detect nothing to commit
        // TODO: Add author, committer, timestamp to commit object

        try {
            JsonNode indexJson = StageManager.parseIndexFile(new ObjectMapper(), repoRoot);

            // Build Filesystem Hierarchy
            FilesystemDirectory rootDirectory = buildHierarchy(indexJson);

            // Build Tree Objects
            try {
                TreeObject treeObject = new TreeObject(rootDirectory);
                String rootTreeHash = treeObject.writeToObjectsDir();

                CommitObject commitObject = new CommitObject(rootTreeHash);
                String commitHash = commitObject.writeToObjectsDir(message);

                // Move branch pointer
                BranchManager.moveActiveBranchPointer(commitHash);

            } catch (IOException e) {
                System.out.println("Error: Could not write object!");
                log.error(e.toString());
            }
        } catch (IOException e) {
            System.out.println("Unable to parse index file! Looks like your encgit repository is corrupted");
        }
    }

    private static FilesystemDirectory buildHierarchy(JsonNode indexJson) {
        FilesystemDirectory root = new FilesystemDirectory("", new ArrayList<>());

        for (JsonNode node : indexJson) {
            Path path = Paths.get(node.get("path").asText());
            String hash = node.get("object_hash").asText();

            FilesystemDirectory curr = root;

            for (int i = 0; i < path.getNameCount(); i++) {
                String name = path.getName(i).toString();
                boolean isFile = (i == path.getNameCount() - 1);

                if (isFile) {
                    curr.entries.add(new FilesystemFile(name, hash));
                } else {
                    FilesystemDirectory next = null;

                    for (FilesystemEntry e : curr.entries) {
                        if (e instanceof FilesystemDirectory &&
                                e.title.equals(name)) {
                            next = (FilesystemDirectory) e;
                            break;
                        }
                    }

                    if (next == null) {
                        next = new FilesystemDirectory(name, new ArrayList<>());
                        curr.entries.add(next);
                    }

                    curr = next;
                }
            }
        }

        return root;
    }
}
