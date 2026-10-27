package dev.floffah.booklink;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

final class BookFiles {
    static final int MAX_BYTES = 24_000;

    private static Path file(Path world, String name) throws IOException {
        if (!name.matches("[a-zA-Z0-9_-]{1,64}")) {
            throw new IOException("Book names must be 1–64 letters, digits, underscores or hyphens (without .txt).");
        }
        Path directory = world.resolve("data").resolve("booklink");
        if (Files.isSymbolicLink(world.resolve("data")) || Files.isSymbolicLink(directory)) {
            throw new IOException("The data and booklink directories must not be symbolic links.");
        }
        return directory.resolve(name + ".txt");
    }

    static String read(Path world, String name) throws IOException {
        Path file = file(world, name);
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Missing book: data/booklink/" + name + ".txt (symbolic links are not supported).");
        }
        byte[] bytes;
        try (var input = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
            bytes = input.readNBytes(MAX_BYTES + 1);
        }
        if (bytes.length > MAX_BYTES) {
            throw new IOException("Book is too large; maximum is " + MAX_BYTES + " UTF-8 bytes.");
        }
        return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString()
                .replace("\r\n", "\n").replace('\r', '\n').replaceFirst("^\uFEFF", "");
    }

    static String slug(String title) throws IOException {
        String name = Normalizer.normalize(title, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (name.isEmpty() || name.length() > 64 || title.length() > 32) {
            throw new IOException("Use a title up to 32 characters containing at least one letter A–Z or digit.");
        }
        if (name.matches("con|prn|aux|nul|com[1-9]|lpt[1-9]")) {
            throw new IOException("That title is a reserved filename; choose another title.");
        }
        return name;
    }

    static String displayName(String name) {
        return Arrays.stream(name.split("[-_]+"))
                .filter(word -> !word.isEmpty())
                .map(word -> word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }

    static void validatePages(List<String> pages) throws IOException {
        if (pages.size() > 100 || pages.stream().anyMatch(page -> page.length() > 1024)) {
            throw new IOException("Books support up to 100 pages of 1024 characters each.");
        }
    }

    static String create(Path world, String title, List<String> pages) throws IOException {
        validatePages(pages);
        String name = slug(title);
        String text = pages.stream().map(page -> page.replaceAll("(?m)^(\\\\*---[ \\t]*)$", "\\\\$1"))
                .collect(Collectors.joining("\n---\n"));
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BYTES) {
            throw new IOException("Book is too large; maximum is " + MAX_BYTES + " UTF-8 bytes. Your draft is kept.");
        }
        Path file = file(world, name);
        Files.createDirectories(file.getParent());
        boolean created = false;
        try {
            try (var output = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                created = true;
                output.write(bytes);
            }
        } catch (IOException exception) {
            if (created) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException cleanup) {
                    exception.addSuppressed(cleanup);
                }
            }
            if (exception instanceof java.nio.file.FileAlreadyExistsException) {
                throw new IOException("Book already exists: " + name + ".txt. Choose another title; your draft is kept.", exception);
            }
            throw exception;
        }
        return name;
    }
}
