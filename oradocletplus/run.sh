#!/usr/bin/env bash
mvn assembly:assembly && java -jar target/oradocletplus.jar bi_loader/bi_loader@localhost:51521:luxms docs STUB adm,ds_471
 
