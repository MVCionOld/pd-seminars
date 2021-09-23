#! /bin/bash

g++ -fopenmp integral.cpp

echo 'N = 1000'
for i in {1..10}; do ./a.out $i 1000; done

echo '------------------------'
echo 'N = 1000000'
for i in {1..10}; do ./a.out $i 1000000; done
