#!/usr/bin/env bash
mvn assembly:assembly && java -jar /Users/yurykrikun/src/oradocletplus/oradocletplus/target/oradocletplus.jar bi_loader/bi_loader@localhost:51521:luxms docs STUB adm,ds_471
 
