#!/usr/bin/python

import sys
from os.path import join
from urllib2 import urlopen


def snapshotversion (s):
    s = s[s.find("<snapshotVersion>") + len("<snapshotVersion>"):]
    return s, s[ : s.find("</snapshotVersion>")]


def getjar (s):
    while True:
        s, snapshot = snapshotversion(s)
        if "<extension>" not in snapshot:
            raise ValueError("Could not find extension.")
        if snapshot[snapshot.find("<extension>") + len("<extension>") :
            snapshot.find("</extension>")] != "jar":
            continue
        if "<value>" not in snapshot:
            raise ValueError("Could not find value.")
        value = snapshot[snapshot.find("<value>") + len("<value>") :
            snapshot.find("</value>")]
        return value + ".jar"


def main (argv):
    if len(argv) != 3:
        return 1
    path = argv[1]
    print >>sys.stderr, "Finding metadata on", path
    s = urlopen(join(path, "maven-metadata.xml")).read()
    if "<latest>" not in s:
        raise ValueError("Could not find latest version.")
    s = s[s.find("<latest>") + len("<latest>") : s.find("</latest>")]
    path = join(path, s)

    print >>sys.stderr, "Finding metadata on", path
    s = urlopen(join(path, "maven-metadata.xml")).read()
    if "<artifactId>" not in s:
        raise ValueError("Could not find artifactId.")
    artifactId = s[s.find("<artifactId>") + len("<artifactId>") :
        s.find("</artifactId>")]

    path = join(path, artifactId + "-" + getjar(s))
    print >>sys.stderr, "Downloading", path
    with open(argv[2], "wb") as f:
        f.write(urlopen(path).read())


if __name__ == '__main__':
    if main(sys.argv):
        print >>sys.stderr, "Usage: %s MAVEN_REPOSITORY_DIRECTORY OUTPUT" % \
            sys.argv[0]
        sys.exit(1)


