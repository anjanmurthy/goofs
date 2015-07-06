# goofs-0.6 is available #
goofs-0.6 is available now.  There are now 5 backends complete: calendar, documents, picasa, contacts, and blogger.

# details #

calendar backend supports:
  * creating events (using googles quick add feature)
  * deleting events
  * searching for events
  * updating event details

picasa backend includes support for:
  * creating albums
  * deleting albums
  * creating photos
  * deleting photos
  * renaming photos
  * updating photo content

contact backend supports:
  * creating contacts
  * deleting contacts
  * renaming contacts
  * editing phone, email, notes, organization and address metadata

blogger backend supports:
  * creating new posts
  * editing post title and content
  * creating comments
  * editing comments
  * deleting posts
  * deleting comments

documents backend supports:
  * creating wp docs, spreadsheets, and presentations
  * editing document title and updating content
  * removing wp docs, spreadsheets, and presentations
  * reading wp docs and presentations as html (support for reading back the original uploaded document has to wait until [issue70](http://code.google.com/p/gdata-issues/issues/detail?id=70) is fixed)


# requirements #
  * python >= 2.5
  * python-fuse
  * python-gdata
  * subversion (optional - only needed if you do not use the goofs-0.6 download)

first make sure you have the latest and greatest python-gdata

```
sudo apt-get remove python-gdata
```

then install python-gdata according to the instructions at
http://code.google.com/p/gdata-python-client/

finally install the remaining dependencies

```
sudo apt-get install python-fuse subversion
```

# installation #

Download the goofs source code with subversion
```
svn checkout http://goofs.googlecode.com/svn/trunk/ goofs-0.6-read-only
```

Or download it from the downloads page and unpack it:

http://goofs.googlecode.com/files/goofs-0.6.tar.gz

```
tar -zxvf goofs-0.6.tar.gz
```

# running goofs #

cd into the goofs/src/goofs directory

```
python goofs.py mntpoint
```

You will be prompted to enter you google username and password.  The username is your full google username (e.g. bigwynnr@gmail.com).

You can also pass the username and password into goofs via the command line

```
python goofs.py mntpoint --user bigwynnr@gmail.com --pw secretcode
```

# working with calendars #

mntpoint will contain a **calendars** folder which will contain your google calendars.

To view the events in the calendar named **Ryan Wynn** that occur in the next 24 hours you would

```
cd calendars/Ryan Wynn/Today
ls
```

or for the next 7 days...

```
cd calendars/Ryan Wynn/7_Days
ls
```

or the next 30 days...

```
cd calendars/Ryan Wynn/30_Days
ls
```

To search for events containing the string **Dad** you just create a directory named **Dad** under the calendar you want to search

```
cd calendars/Ryan Wynn
mkdir Dad
cd Dad
ls
```

To search for events within a date range you would create a folder named YYYYmmdd-YYYYmmdd, for example, 20080601-20080610

```
cd calendars/Ryan Wynn
mkdir 20080601-20080610
cd 20080601-20080610
ls
```

Each event directory will have the following files inside it: content, recurrence, when, where

You can edit these files to update the event details.  The when file contains the start time followed by a single space followed by the end time.  This file may be empty if the event is a recurring event (in which case the recurrence file will contain the event time information).

To create new events you use google's really handy [quick-add](http://www.google.com/support/calendar/bin/answer.py?hl=en&answer=36604#text) feature.

```
cd calendars/Ryan Wynn
echo "Dinner at 7pm" > quick
```

You simply need to write a file named **quick** under one of your calendars.  The rules for formatting quick add events can be found [here](http://www.google.com/support/calendar/bin/answer.py?hl=en&answer=36604#text)

To delete events, you guessed it

```
cd calendars/Ryan Wynn
rm -rf Dinner
```


# working with documents #

mntpoint should contain a **documents**, **spreadsheets**, and **presentations** directory.

To create a new spreadsheet

```
cd spreadsheets
echo "1,2,3" > OneTwoThree.csv
```

Or use your favorite editor and save the file to the appropriate directory.

  * The following types of spreadsheet are supported: CSV,TSV,TAB,ODS,XLS.
  * And for documents: DOC,ODT,RTF,SXW,TXT,HTM,HTML
  * And finally for presentations: PPT,PPS

To remove a document

```
cd documents
rm DontNeedAnymore.odt
```


# working with blogs #

mntpoint should contain a **blogs** directory.  Within this directory you should see a list of all your blogs from blogger.

You need to create brand new blogs through the blogger web interface (you cannot create blogs through goofs).  For some reason this interface is read-only at the moment.

However you can create new posts and comments through goofs.  To create a new post cd into the blog directory.

```
cd blogs/Ryan\'s\ Blog/
mkdir "Goofs 0.6 Released Today"
```

This will create a new post to the blog named Ryan's Blog.  The new post will have a title "Goofs 0.6 Release Today".  Now to add content to this blog entry...

```
cd Goofs\ 0.6\ Released\ Today/
echo "Please download it from <a href='http://goofs.googlecode.com/files/goofs-0.6.tar.gz'>goofs.googlecode.com</a>" > content
```

To change the title of a post use the mv command
```
mv Goofs\ 0.6\ Released\ Today/ "Goofs 0.6 Released Yesterday"
```

To comment on a blog entry you would do the following:
```
cd Goofs\ 0.6\ Released\ Today/
cd comments
echo "you did a great job" > new
```

You can name the comment file whatever you like, it will be renamed to a snippet of the contents of the comment.  If you cat the file it will contain the entire comment.

You can delete posts and comments using the rm command...

```
cd blogs/Ryan\'s\ Blog/
rm -rf Goofs\ 0.6\ Released\ Today/
```


# working with contacts #

mntpoint should now contain a **contacts** directory.  This directory will hold up to 1000 of your google contacts.  Any changes you make on the filesystem will be reflected when you log in to Gmail.

To create a new contact cd into the contacts directory and then

```
mkdir Christina
```

Now you can cd into that directory and edit attributes associated with Christina.

```
cd Christina
echo "a wonderful girl" > notes
cd email
echo "chrissy@home.org" > home
echo "chrissy@work.org" > work
```

To rename a contact

```
cd contacts
mv Christina Chrissy
```

To delete a Contact
```
cd contacts
rm -rf Chrissy
```

# working with photos #

mntpoint should also contain a **photos** directory.  Inside photos you will find 2 more directories, namely public and private.  Your shared picasa albums go into public and your private albums go into private.

goofs will immediately begin synching the local filesystem with your existing picasa photos.  You may have to wait a couple seconds for albums and photos to start showing up.

to create a new album you would simply create a new folder under private or public.  to upload photos to the album you would cp or save images directly under the album directory.
you can only save the following types of images: .bmp, .gif, .png, .jpg, and .jpeg.

to move photos between albums you would simply use the mv command.
```
cd photos/public/Album1
mv should_be_in_album2.jpg ../Album2
```

deleting albums and photos is as easy as using the rmdir and rm commands
```
rm photos/public/Album1/*
rmdir photos/public/Album1
```