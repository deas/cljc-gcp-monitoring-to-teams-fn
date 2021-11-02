#!/bin/bash

npm install
mkdir nodejs
cd nodejs
ln -s ../node_modules node_modules
cd ..
zip -r layer.zip nodejs
