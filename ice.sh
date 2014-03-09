#!/bin/bash

echo "Username:" ${1?"Usage: $0 USERNAME"}
user="$1"

bash -c 'cssh $0@ice{01..12}.ee.cooper.edu:31415' $user

