db.getCollection("CacheLock").drop();
db.createCollection("CacheLock");
db.CacheLock.createIndex({"product": 1, "key": 1}, {name: "productKey", unique: "true"});
db.CacheLock.createIndex({"createdAt": 1}, {name: "cacheLockTTL", expireAfterSeconds: 120});
