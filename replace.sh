#!/bin/bash

# Assign the filename
compose="docker-compose.yml"

read -p "Enter the DB_NAME: " db_name
read -p "Enter the DB_PASS: " db_pass

if [[ $db_name != "" && $db_pass != "" ]]; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s/DEFAULT_DB_NAME/$db_name/" $compose
        sed -i '' "s/DEFAULT_DB_PASS/$db_pass/" $compose
    else
        sed -i "s/DEFAULT_DB_NAME/$db_name/" $compose
        sed -i "s/DEFAULT_DB_PASS/$db_pass/" $compose
    fi
fi