#!/bin/bash
java -jar ./target/cockroach-lang.jar -i ./tst/example1.txt -o ./tst/example1.ll
llc ./tst/example1.ll
gcc -s ./tst/example1.s -o ./tst/example1.out -no-pie
./tst/example1.out