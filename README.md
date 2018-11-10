# SummonerData
This is an in Kotlin written Data Miner for the OP.GG website, it requests the HTML content, parses that and puts things like: 
Accounts, Ranked Data, Champions in a MySql Database.

## ATTENTION 
This was never used in a runtime longer than around 20 seconds, since its a data mining tool which violates the Op.GG Terms of Use

## Usage
Java 8 or newer is required, this is a console only application, arguments could look like:

`-db-host 21xayah.com -db-user user -db-pass 123 -db-target cooldb -target euw Liz3 -target na Yassuo -target kr Hide-on-Bush`

The first parameter are defining needed information for database Access, after that targets are defined which represent the mining Threads, 
Its recommended to use only one Job per Region.

Further optional parameters:

`-version 8.22.1` Can be any valid League version used for Champion Name to ID match

`-limit 1000000` Defines the maximal size of the Queue size per Job
