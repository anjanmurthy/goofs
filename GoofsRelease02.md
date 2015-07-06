# goofs-0.2 is available #
goofs-0.2 is available now.  At present 1 back end is complete: picasa.  This feature allows you to manipulate your picasa photos just as you would any other file on the filesytem.


# details #

picasa backend includes support for:
  * creating albums
  * deleting albums
  * creating photos
  * deleting photos
  * renaming photos
  * updating photo content

# requirements #
  * python-fuse
  * python-gdata
  * subversion

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

Download the goofs source code with subversion:
```
svn checkout http://goofs.googlecode.com/svn/trunk/ goofs-0.2-read-only
```

# running goofs #

cd into the src/goofs directory

```
python goofs.py mntpoint
```

You will be prompted to enter you google username and password.  The username is your full google username (i.e. bigwynnr@gmail.com).

mntpoint should now contain a photos directory.  Inside photos you will find 2 more directories, namely public and private.  Your shared albums go into public and your private albums go into private.

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