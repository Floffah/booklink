package dev.floffah.booklink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class BookFilesCheck {
    public static void main(String[] args) throws Exception {
        var splitter = new net.minecraft.client.StringSplitter((codePoint, style) -> 1.0F);
        var pages = ClientBooks.pages("First\n---\nSecond", splitter);
        check(pages.size() == 2 && pages.get(1).getString().equals("Second"), "explicit page breaks");
        check(ClientBooks.pages("", splitter).size() == 1, "empty book has a page");
        String lines = java.util.stream.IntStream.range(0, 15).mapToObj(i -> "Line " + i)
                .collect(java.util.stream.Collectors.joining("\n"));
        pages = ClientBooks.pages(lines, splitter);
        check(pages.size() == 2 && pages.get(1).getString().equals("Line 14"), "14 lines per page");
        pages = ClientBooks.pages("x".repeat(114 * 15), splitter);
        check(pages.size() == 2, "long text wraps onto more pages");
        check(pages.stream().map(p -> p.getString().replace("\n", "")).collect(
                java.util.stream.Collectors.joining()).equals("x".repeat(114 * 15)), "pagination retains text");
        var buffer = io.netty.buffer.Unpooled.buffer();
        try {
            for (boolean offHand : new boolean[]{false, true}) {
                LinkBook expected = new LinkBook("guide", offHand);
                LinkBook.CODEC.encode(buffer, expected);
                check(LinkBook.CODEC.decode(buffer).equals(expected), "link request preserves name and hand");
                buffer.clear();
            }
            try {
                LinkBook.CODEC.encode(buffer, new LinkBook("x".repeat(65), false));
                throw new AssertionError("Oversized link request accepted");
            } catch (io.netty.handler.codec.EncoderException expected) {
            }
        } finally {
            buffer.release();
        }
        Path world = Files.createTempDirectory("booklink-check-");
        try {
            String signedName = BookFiles.create(world, "My First Book!", java.util.List.of("First", "Second"));
            check(signedName.equals("my-first-book"), "signed title becomes a filename");
            check(BookFiles.displayName(signedName).equals("My First Book"), "filename becomes item title");
            check(BookFiles.slug(" Café_Notes! ").equals("cafe-notes"), "title normalization");
            check(BookFiles.displayName("some_notes").equals("Some Notes"), "existing filename display name");
            Path signedFile = world.resolve("data/booklink/my-first-book.txt");
            String signedText = Files.readString(signedFile);
            rejectsCreate(world, "My First Book?", java.util.List.of("Must not overwrite"));
            check(Files.readString(signedFile).equals(signedText), "existing file is never overwritten");
            var readPages = ClientBooks.pages(BookFiles.read(world, signedName), splitter);
            check(readPages.size() == 2 && readPages.get(0).getString().equals("First")
                    && readPages.get(1).getString().equals("Second"), "signed pages round trip");
            String literal = "  Indented\n\n---\n\\---\nLast";
            BookFiles.create(world, "Literal", java.util.List.of(literal));
            readPages = ClientBooks.pages(BookFiles.read(world, "literal"), splitter);
            check(readPages.size() == 1 && readPages.getFirst().getString().equals(literal),
                    "literal separator lines and indentation survive");
            for (String title : new String[]{"!!!", " ", "a".repeat(33), "CON", "LPT1"}) {
                rejectsCreate(world, title, java.util.List.of("text"));
            }
            rejectsCreate(world, "Too Many Pages", java.util.Collections.nCopies(101, "x"));
            rejectsCreate(world, "Long Page", java.util.List.of("x".repeat(1025)));
            rejectsCreate(world, "Too Many Bytes", java.util.Collections.nCopies(24, "☃".repeat(1000)));
            check(!Files.exists(world.resolve("data/booklink/too-many-bytes.txt")), "failed signing creates no file");
            BookFiles.create(world, "Empty", java.util.List.of());
            check(BookFiles.read(world, "empty").isEmpty(), "empty signed book");
            Path legacy = Files.createDirectory(world.resolve("booklink"));
            Files.writeString(legacy.resolve("guide.txt"), "Old location must not be used");
            rejects(world, "guide");
            Path folder = world.resolve("data/booklink");
            for (Path created : java.util.List.of(signedFile, folder.resolve("literal.txt"), folder.resolve("empty.txt"))) {
                Files.delete(created);
            }
            Path oldFormat = folder.resolve("guide.md");
            Files.writeString(oldFormat, "Old extension must not be used or modified");
            rejects(world, "guide");
            check(Files.readString(oldFormat).equals("Old extension must not be used or modified"),
                    "old extension is left untouched");
            Files.delete(oldFormat);
            Path file = folder.resolve("guide.txt");
            String original = "\uFEFFHello ☃\r\n---\rNext page";
            Files.writeString(file, original);
            check(BookFiles.read(world, "guide").equals("Hello ☃\n---\nNext page"), "UTF-8 and line endings");
            check(Files.readString(file).equals(original), "source is never modified");
            Files.writeString(file, "Updated externally");
            check(BookFiles.read(world, "guide").equals("Updated externally"), "reload on every read");
            for (String name : new String[]{"", "../guide", "/etc/passwd", "guide.txt", "a".repeat(65), "missing"}) {
                rejects(world, name);
            }
            Files.write(file, new byte[BookFiles.MAX_BYTES]);
            check(BookFiles.read(world, "guide").length() == BookFiles.MAX_BYTES, "size boundary");
            Files.write(file, new byte[BookFiles.MAX_BYTES + 1]);
            rejects(world, "guide");
            Files.write(file, new byte[]{(byte) 0xc3, 0x28});
            rejects(world, "guide");
            Files.delete(file);
            rejects(world, "guide");
            Path outside = world.resolve("outside.txt");
            Files.writeString(outside, "private");
            Files.createSymbolicLink(file, outside);
            rejects(world, "guide");
            rejectsCreate(world, "Guide", java.util.List.of("Must not follow symlink"));
            check(Files.readString(outside).equals("private"), "signing does not follow file symlinks");
            Files.delete(file);
            Files.delete(folder);
            Path other = Files.createDirectory(world.resolve("other"));
            Files.writeString(other.resolve("guide.txt"), "private");
            Files.createSymbolicLink(folder, other);
            rejects(world, "guide");
            rejectsCreate(world, "New", java.util.List.of("Must not follow directory symlink"));
            check(!Files.exists(other.resolve("new.txt")), "signing does not follow directory symlinks");
            Files.delete(folder);
            Files.delete(world.resolve("data"));
            Files.createDirectories(other.resolve("booklink"));
            Files.writeString(other.resolve("booklink/guide.txt"), "private");
            Files.createSymbolicLink(world.resolve("data"), other);
            rejects(world, "guide");
            rejectsCreate(world, "New", java.util.List.of("Must not follow data symlink"));
            check(!Files.exists(other.resolve("booklink/new.txt")), "signing does not follow data symlinks");
            System.out.println("Booklink file, signing, pagination and link request checks passed");
        } finally {
            try (var paths = Files.walk(world)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    private static void rejectsCreate(Path world, String title, java.util.List<String> pages) throws Exception {
        try {
            BookFiles.create(world, title, pages);
            throw new AssertionError("Expected signing rejection: " + title);
        } catch (IOException expected) {
        }
    }

    private static void rejects(Path world, String name) throws Exception {
        try {
            BookFiles.read(world, name);
            throw new AssertionError("Expected rejection: " + name);
        } catch (IOException expected) {
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
