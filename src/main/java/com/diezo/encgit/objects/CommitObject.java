package com.diezo.encgit.objects;

import com.diezo.encgit.core.BranchManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.diezo.encgit.RepositoryConstants.NULL_SHA256_HASH;

public class CommitObject extends GitObject {

    public CommitObject(String rootTreeHash) {
        super("commit", null, null, rootTreeHash);
    }

    public String writeToObjectsDir(String commitMessage) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(("tree " + rootTreeHash).getBytes(StandardCharsets.UTF_8));
        out.write(0);
        out.write(("parent " + BranchManager.getParentCommitHash()).getBytes(StandardCharsets.UTF_8));
        out.write(0);
        out.write("message ".getBytes(StandardCharsets.UTF_8));
        out.write(commitMessage.getBytes(StandardCharsets.UTF_8));

        return writeTreeObject(out.toByteArray());
    }

    public static void catCommitObject(byte[] content) {
        while (content.length > 0) {
            int i = 0;
            while (i < content.length && content[i] != 0) i++;

            String line = new String(Arrays.copyOfRange(content, 0, i), StandardCharsets.UTF_8);
            String key = line.split(" ", 2)[0];
            String value = line.split(" ", 2)[1];

            // Pretty print
            if (!(key.equals("parent") && value.equals(NULL_SHA256_HASH))) {
                System.out.println((!key.equals("message") ? (key + " ") : "") + value);
            }

            content = Arrays.copyOfRange(content, ((i < content.length) ? (i + 1) : i), content.length);
        }
    }
}
