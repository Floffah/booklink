# Booklink

This is a simple mod that allows you to synchronise in game books with text files stored on your computer. It supports single player AND servers, as long as it is installed on both the server and the client.

The mod adds two items you can craft: the Unlinked Book, and the Unlinked Book and Quill. The Unlinked Book allows you to import from text files (it will re-read the file every time you open the book). The Unlinked Book and Quill allows you to write a book in game like a normal book and quill, and when you sign it the book is saved in the booklink folder.

Books are stored in `data/booklink` as `.txt` files, relative to your save game (level name on server, `saves/name` on singleplayer). The books have no particular format and are basically plaintext but with the exception that you can separate manual page breaks with `---` on a new line.

This mod is a personal project for one of my singleplayer world builds, I thought I'd make it public in case anyone else wished to use it. You may suggest changes, features, or report bugs on the issue tracker linked in the modrinth page

## Unlinked Book

You can craft the unlinked book as below (both items are vanilla)

![Unlinked Book crafting recipe](https://cdn.modrinth.com/data/cached_images/35cbea7139900acc10c14adce1ed10f23b3c8c48.png)

Once you've crafted the book, right click it and the below menu will pop up. You must write the exact file name (excluding the .txt extension)

![Unlinked Book sync menu](https://cdn.modrinth.com/data/cached_images/db382fbc66f2c85fe3fea9254f18dc967d331934.png)

## Unlinked Book and Quill

You can craft the unlinked book and quill as below (both items are vanilla EXCEPT the use of an unlinked book as a base)

![Unlinked Book and Quill crafting recipe](https://cdn.modrinth.com/data/cached_images/37f7c083e9e5253d666c36bb204069199cc04ca2.png)
![Unlinked Book and Quill crafting recipe showing the Unlinked Book in there](https://cdn.modrinth.com/data/cached_images/2b8dc8554eddaf8b649cf5c3f03eb12612a3777a.png)

When you open the book and quill, it will act exactly like a regular book and quill, allowing you to write content.

![Unlinked Book and Quill edit page](https://cdn.modrinth.com/data/cached_images/0c904a28e9af4bd010320a494838722069528632.png)

When you sign the book, the name you use is used as the file name. The name will however be normalised, so the use of punctuation may vary on operating system. For example, "My Book!" will be saved as `my-book.txt`. It will however be parsed back properly, but `my-book.txt` will read as "My Book" obviously losing the punctuation

![Unlinked Book and Quill edit page](https://cdn.modrinth.com/data/cached_images/2515acd8f023152d7292c3571f9ff7dd4921bfc3.png)

Sign it and you will be given a book that is linked to the file in your savegame. Note that you may make changes to the text file and next time you open the book in game, you will see the changes!

![Linked Book preview](https://cdn.modrinth.com/data/cached_images/4b96fc4e8e76948b062ef468071accaed1d4537f.png)
![the saved file](https://cdn.modrinth.com/data/cached_images/9436a8c49c0e1a793d58b3995cf507c08c33c735.png)

## Compatibilities

Booklink is not compatible with any other mods *on purpose*, BUT it has been tested with mods such as:
- Scribble (mod that adds text formatting and colours to books)

See below an unlinked book and quill and linked book that both support Scribble's colours which saved properly

![Unlinked Book and Quill with Scribble formatting](https://cdn.modrinth.com/data/cached_images/6630bae99a8fc1426690582a5333178dc8670919_0.webp)
![Linked Book with saved Scribble formatting](https://cdn.modrinth.com/data/cached_images/8658f80eb4b5e42ffc0fb00d2b0472f54bec29e9_0.webp)

As you can see, we retained formatting but also see below the saved text file

![Book file saved with Scribble formatting](https://cdn.modrinth.com/data/cached_images/a03bda50a8c903fb059c788f4a52753809e66fd9.png)

Moral of the story here is that if any formatting mod uses builtin or text-based formatting, it will work with Booklink. The only exception to this rule is if your formatting mod uses `---` to separate lines.
