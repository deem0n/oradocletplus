ORADOCLETPLUS 2016
=============
Copyright (C) 2015  Vladimir Katchourovski & Richard Nichols & Viniscius Ribeiro

Copyright (C) 2016  Yuriy Krikun & Dmitry Dorofeev

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

What is it?
-----------

OraDocletPlus is an enhanced version of the OraDoclet database documentation generator available at http://oradoclet.sourceforge.net/

The tool will automatically generate a set of HTML documents based on the Oracle database schema that you connect it to. It will pull our pretty much all meta-data about the schema, including indexes, column comments, foreign keys, and PL/SQL source code.

I threw these changes together as I needed to generate some database schema documentation quickly and found the OraDoclet to be a pretty good solution, although it had a couple of bugs, looked very out of date and hasn’t been updated in quite a while.

Note that the original author of OraDoclet was Vladimir Katchourovski – kudos to him for creating a concise & simple tool.

Why this fork A.K.A. what's new?
-----------
This fork adds ability to generate docs from several Oracle schemas at once. SQL requests to Oracle database were changed to support this new functionality. HTML formatting was also changed slightly. 

Original SQL to generate line numbers in Oracle was proven not working in case of length of text equal to Oracle maximum of 4000 symbols for varchar2 datatype. Generation of line numbers was reprogrammed in HTML using \<ol\> and \<li\> tags.

Usage
-----

java -jar oradocletplus.jar username/password@hostname:port:sid \<output\_directory\> [\<copyright\_notice\>] [\<comma\_separated\_schema\_list\>]

for example:

```
java -jar oradocletplus.jar user/password@localhost:1521:mydb \
docs/ "My Company Inc." SCH1,PUB23,SYS_DOCS
```

Building
-----
You need [Maven](https://maven.apache.org/) to compile this project. Firstly, run [install_libs.sh](./oradocletplus/install_libs.sh) to install JDBC and other necessary jars. Secondly, build the project with Maven:

```
mvn assembly:assembly
```

Contributing
-----
**!!Outdated!!:** More details on how to build from source https://github.com/japonicius/oradocletplus/wiki
