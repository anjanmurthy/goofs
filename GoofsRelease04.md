# goofs-0.4 is available #
goofs-0.4 is available now.  There are now 3 backends complete: picasa, contacts, and blogger.  These features allow you to manipulate your picasa photos, gmail contacts, and blogs just as you would any other file on the filesytem.


# details #

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


# requirements #
  * python-fuse
  * python-gdata
  * subversion (optional - only needed if you do not use the goofs-0.4 download)

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
svn checkout http://goofs.googlecode.com/svn/trunk/ goofs-0.4-read-only
```

Or download it from the downloads page and unpack it:

http://goofs.googlecode.com/files/goofs-0.4.tar.gz

```
tar -zxvf goofs-0.4.tar.gz
```

# running goofs #

cd into the goofs/src/goofs directory

```
python goofs.py mntpoint
```

You will be prompted to enter you google username and password.  The username is your full google username (e.g. bigwynnr@gmail.com).

# working with blogs #

mntpoint should contain a **blogs** directory.  Within this directory you should see a list of all your blogs from blogger.

You need to create brand new blogs through the blogger web interface (you cannot create blogs through goofs).  For some reason this interface is read-only at the moment.

However you can create new posts and comments through goofs.  To create a new post cd into the blog directory.

```
cd blogs/Ryan\'s\ Blog/
mkdir "Goofs 0.4 Released Today"
```

This will create a new post to the blog named Ryan's Blog.  The new post will have a title "Goofs 0.4 Release Today".  Now to add content to this blog entry...

```
cd Goofs\ 0.4\ Released\ Today/
echo "Please download it from <a href='http://goofs.googlecode.com/files/goofs-0.4.tar.gz'>goofs.googlecode.com</a>" > content
```

To change the title of a post use the mv command
```
mv Goofs\ 0.4\ Released\ Today/ "Goofs 0.4 Released Yesterday"
```

To comment on a blog entry you would do the following:
```
cd Goofs\ 0.4\ Released\ Today/
cd comments
echo "you did a great job" > new
```

You can name the comment file whatever you like, it will be renamed to a snippet of the contents of the comment.  If you cat the file it will contain the entire comment.

You can delete posts and comments using the rm command...

```
cd blogs/Ryan\'s\ Blog/
rm -rf Goofs\ 0.4\ Released\ Today/
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