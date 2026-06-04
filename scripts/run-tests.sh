#!/bin/bash
cd /home/rosner/projetosgit/novobolao
mvn -q -Dfrontend.skip=true test 2>&1 | tail -30
