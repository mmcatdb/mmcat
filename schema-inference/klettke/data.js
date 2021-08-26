
db.Species.save( {"id": 123, "name": "Mya_arenaria", "ts": 1} );
db.Species.save( {"id": 124, "name": "Abra_prismatica", "ts": 3, "category": "141436"});
db.Species.save( {"id": 125, "name": "Abra_alba",  "ts": 5, "worms": "141433"});
db.Species.save( {"id": 126, "name": "Abra_aequalis", "ts": 7, "worms": "293683"});

db.Protocols.save(  {"id": 1900, "timemarker": "2017-07-21", "location": "x:19.863281, y:58.487952, z:-1400", "spec_id": 123, "ts": 2});
db.Protocols.save( {"id": 1901, "timemarker": "2017-07-21", "location": "x:19.863285, y:58.487952, z:-1400", "spec_id": 123, "ts": 4} );
db.Protocols.save(  {"id": 1902, "timemarker": "2017-07-23", "location": "x:19.863281, y:58.487961, z:-1350", "spec_id": 125, "ts": 6} );
db.Protocols.save( {"id": 1903, "timemarker": "2017-07-24", "location": "x:19.863285, y:58.487952, z:-1400", "spec_id": 126, "ts": 8, "worms": "293683"}   );

db.Protocols.save( {"id": 1111, "ts": 66});
db.Protocols.save( {"id": 1111, "ts": 66, "test" : {"first": "a", "second" : 12, "ts": 999}});

db.Protocols.save({ "id" : 1904, "article_id" : -1.7976931348623157e+308, "comments" : "abcd", "cmts" : { "last_name" : "sss", "location" : { "address" : "sss" }, "phone_number" : -1.7976931348623157e+308, "first_name" : "sss" }, "ratings" : [ ], "published" : false, "body" : "sss", "timestamp" : "sss" });