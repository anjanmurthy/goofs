#!/usr/bin/python

from distutils.core import setup


setup(
    name='goofs.py',
    version='0.1',
    description='Google Filesystem in Userspace',
    long_description = """\
goofs is a fuse filesystem for google services written
in python
""",
    author='Ryan Wynn',
    author_email='bigwynnr@gmail.com',
    license='LGPL',
    url='http://code.google.com/p/goofs/',
    packages=['goofs'],
    package_dir = {'goofs':'src/goofs'}
)
