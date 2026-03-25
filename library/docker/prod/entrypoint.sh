#!/bin/bash

# TODO Not done automatically - do it yourself
cd aimm
python3 -m venv venv
source .venv/bin/activate
pip install -e .

# java -jar server/target/server-1.0-SNAPSHOT-app.jar
