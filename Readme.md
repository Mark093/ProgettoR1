NLP Project, Project R1
Until now there are three classes:
    -Main: it contains the main function that loads the xml dump of Wiktionary and starts parsing it by using the TitlesHandler class described below. (FINISHED)
    -TitlesHandler: it's an extension of the SAX default handler. It parses only the "title" tags in the XML file and calls HTMLParser on it. (FINISHED)
    -HTMLParser: here we download the HTML pages related to the titles parsed before and we get all the info from it.
    -HeaderList: by using a txt file it gets the tables from few pages of wiktionary and use them to get the names of the headers of the tables. (FINISHED)
    -PrepTable: it is used to preprocess the table: it gives the main info of it and the list of the cells contained in it (of type Word).
    -Word: A particular data structure: contains the single word and all the headers related to it.
    -WordHeaders: It's a particular data structure for representing an header of a word.

    Notes:
        the used wiktionary dump contains 4810922 pages.

        Started parsing at 10:12, 28 Jan 2017
        First 1000 pages loaded at 10:19
        Second loading finished at 10:26
        Third loading finished at 10:33

        Started parsing at 10:41, 28 Jan 2017
        Loaded 1000 pages at 10:44
        Loaded 5000 pages at 11:03
        Loaded 12000 pages at 11:57
        Loaded 26000 pages at 13:45