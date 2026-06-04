#!/bin/bash
cd /home/rosner/projetosgit/novobolao
mvn -Dfrontend.skip=true test 2>&1 | grep -E "Tests run|BUILD|FAILURE|ERROR" | grep -v "^$"
