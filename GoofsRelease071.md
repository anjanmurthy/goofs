# goofs-0.7.1 is available #

goofs-0.7.1 fixes some performance issues found in 0.7 as well as some bug related to creating and updating contacts.  To see what we plan on implementing for goofs-1.0 check out RoadMap

# details #

calendar backend supports:
  * creating events (using google quick add feature)
  * deleting events
  * searching for events by date range and text query
  * updating event details

picasa backend includes support for:
  * creating albums
  * deleting albums
  * uploading photos
  * deleting photos
  * renaming photos
  * updating photo content

contact backend supports:
  * creating contacts
  * viewing contact photos
  * deleting contacts
  * renaming contacts
  * editing notes, email, and address metadata

blogger backend supports:
  * creating new posts
  * editing post title and content
  * creating comments
  * editing comments
  * deleting posts
  * deleting comments

documents backend supports:
  * nested folders
  * creating wp docs, spreadsheets, and presentations
  * editing document content
  * removing wp docs, spreadsheets, and presentations
  * support for reading back the original uploaded document has to wait until [issue70](http://code.google.com/p/gdata-issues/issues/detail?id=70) is fixed

# requirements #
  * fuse version 2.7.2 or 2.7.3
  * java 5 jre

you can install the requirements as follows on opensuse

```
zypper in fuse java-1_5_0-sun
```

# know issues #

I have had problems uploading images and documents when using sun's java 6 jre.  The problem does not appear when using sun's java 5 jre.  The problem seems to be that the java activation framework is bundled with java 6 and has not been updated with the fixes in the latest activation.jar (version 1.1.1).  I created a ticket to address the issue at http://code.google.com/p/gdata-java-client/issues/detail?id=75&can=5.

I would suggest installing java 5 either through your package manager or from http://java.sun.com/products/archive/j2se/5.0_16/index.html

# installation #

Download the goofs binary and unpack it:

```
wget http://goofs.googlecode.com/files/goofs-0.7.1.tar.gz
tar -zxvf goofs-0.7.1.tar.gz
```

# configuration #

create a file in your home directory called **.goofs.properties**
add the following 2 properties and save

```
username=yourusername@gmail.com
password=yourpassword
```

edit the file goofs-0.7.1/goofs-mount.sh to define:
  * JAVA\_HOME
  * GOOFS\_HOME
  * Optionally change jni/fuse-2.7.2 to jni/fuse-2.7.3 if you have 2.7.3 installed

add goofs-0.7.1 to your PATH

# advanced configuration (optional) #

You can customize some of the properties used throughout the application by overriding values in your **~/.goofs.properties** file.  You can turn services off completely by setting the enabled property to false.  You can also provide your own strings for the names of some of the stock directories for a service.  Finally you can set the interval at which folders are synchronized with the server (measured in milliseconds).  The defaults values are shown below.

```
# blogger
goofs.blogger.enabled=true
goofs.blogger.blogs=blogs
goofs.blogger.comments=comments

# calendar
goofs.calendar.enabled=true
goofs.calendar.calendars=calendars
goofs.calendar.today=Today
goofs.calendar.next7=7_Days
goofs.calendar.next30=30_Days
goofs.calendar.when=when
goofs.calendar.recurrence=recurrence
goofs.calendar.where=where
goofs.calendar.summary=summary

# contacts
goofs.contacts.enabled=true
goofs.contacts.contacts=contacts
goofs.contacts.email=email
goofs.contacts.notes=notes
goofs.contacts.address=addresses

# photos
goofs.photos.enabled=true
goofs.photos.photos=photos
goofs.photos.public=public
goofs.photos.private=private

# documents
goofs.docs.enabled=true
goofs.docs.documents=documents

# synchronization
goofs.folder.synch.threshold=60000
```

# running goofs #

```
goofs-mount.sh /path/to/mount
```

# umounting #

```
fusermount -u /path/to/mount
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

To search for events containing the string **birthday** you just create a directory named **birthday** under the calendar you want to search

```
cd calendars/Ryan Wynn
mkdir birthday
cd birthday
ls
```

To search for events within a date range you would create a folder named YYYYmmdd-YYYYmmdd, for example, 20080601-20080610

```
cd calendars/Ryan Wynn
mkdir 20080601-20080610
cd 20080601-20080610
ls
```

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

mntpoint should contain a **documents** directory.  This directory will contain your spreadsheets, wp documents, and presentations.  You can create folders (optionally nested) to file your documents just as you would on the google documents website.

To create a new folder

```
cd documents
mkdir "Stuff"
```

To create a new spreadsheet

```
cd Stuff
echo "1,2,3" > OneTwoThree.csv
```

Or use your favorite editor and save the file to the appropriate directory.

  * The following types of spreadsheet are supported: CSV,TSV,TAB,ODS,XLS.
  * And for documents: DOC,ODT,RTF,SXW,TXT,HTM,HTML
  * And finally for presentations: PPT,PPS

To remove a document

```
cd Stuff
rm OneTwoThree.csv
```


# working with blogs #

mntpoint should contain a **blogs** directory.  Within this directory you should see a list of all your blogs from blogger.

You need to create brand new blogs through the blogger web interface (you cannot create blogs through goofs).  For some reason this interface is read-only at the moment.

However you can create new posts and comments through goofs.  To create a new post cd into the blog directory.

```
cd blogs/Ryan\'s\ Blog/
mkdir "Goofs 0.7.1 Released Today"
```

This will create a new post to the blog named Ryan's Blog.  The new post will have a title "Goofs 0.7.1 Release Today".  Now to add content to this blog entry...

```
cd Goofs\ 0.7.1\ Released\ Today/
echo "Please download it from <a href='http://goofs.googlecode.com/files/goofs-0.7.1.tar.gz'>goofs.googlecode.com</a>" > content
```

To change the title of a post use the mv command
```
mv Goofs\ 0.7.1\ Released\ Today/ "Goofs 0.7.1 Released Yesterday"
```

To comment on a blog entry you would do the following:
```
cd Goofs\ 0.7.1\ Released\ Today/
cd comments
echo "you did a great job" > new
```

You can name the comment file whatever you like, it will be renamed to a snippet of the contents of the comment.  If you cat the file it will contain the entire comment.

You can delete posts and comments using the rm command...

```
cd blogs/Ryan\'s\ Blog/
rm -rf Goofs\ 0.7.1\ Released\ Today/
```


# working with contacts #

mntpoint should contain a **contacts** directory.  This directory will hold up to 1000 of your google contacts.  Any changes you make on the filesystem will be reflected when you log in to Gmail (with the exception of the contact's photo which is currently read-only).

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
echo "chrissy@xyz.org" > other
cd ../addresses
echo "123 Some street Springfield, MO 12345" > home
echo "234 Another Dr. Springfield, MO 12345" > work
echo "999 Spring Terrace, MO 12345" > other
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