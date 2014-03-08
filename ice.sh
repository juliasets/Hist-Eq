#!/bin/bash

echo "Username:" ${1?"Usage: $0 USERNAME"}
username="$1"

cssh $username@ice01.ee.cooper.edu:31415 $username@ice02.ee.cooper.edu:31415 $username@ice03.ee.cooper.edu:31415 $username@ice04.ee.cooper.edu:31415 $username@ice05.ee.cooper.edu:31415 $username@ice06.ee.cooper.edu:31415 $username@ice07.ee.cooper.edu:31415 $username@ice08.ee.cooper.edu:31415 $username@ice09.ee.cooper.edu:31415 $username@ice10.ee.cooper.edu:31415 $username@ice11.ee.cooper.edu:31415 $username@ice12.ee.cooper.edu:31415

