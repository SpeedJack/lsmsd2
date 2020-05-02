#!/bin/bash

rm -rf /var/lib/mongodb/{cfg{0,1,2},rs{0,1}-{0,1,2}}
rm -rf /var/log/mongodb/{cfg{0,1,2},rs{0,1}-{0,1,2}}.log

mkdir -p /var/lib/mongodb/{cfg{0,1,2},rs{0,1}-{0,1,2}}

chown -R mongodb:mongodb /var/lib/mongodb
chown mongodb:mongodb /var/log/mongodb

su -s /bin/bash -c 'mongod --quiet --fork --configsvr --replSet cfgReplSet --port 27917 --logpath /var/log/mongodb/cfg0.log --dbpath /var/lib/mongodb/cfg0' mongodb

su -s /bin/bash -c 'mongod --quiet --fork --configsvr --replSet cfgReplSet --port 27918 --logpath /var/log/mongodb/cfg1.log --dbpath /var/lib/mongodb/cfg1' mongodb
su -s /bin/bash -c 'mongod --quiet --fork --configsvr --replSet cfgReplSet --port 27919 --logpath /var/log/mongodb/cfg2.log --dbpath /var/lib/mongodb/cfg2' mongodb

sleep 5

mongo --quiet --port 27917 --eval 'rs.initiate({_id : "cfgReplSet", configsvr: true, members: [{ _id: 0, host: "localhost:27917" }, { _id: 1, host: "localhost:27918" }, { _id: 2, host: "localhost:27919" }]})'

su -s /bin/bash -c 'mongod --quiet --fork --shardsvr --replSet rs0 --port 27117 --logpath /var/log/mongodb/rs0-0.log --dbpath /var/lib/mongodb/rs0-0' mongodb
su -s /bin/bash -c 'mongod --quiet --fork --shardsvr --replSet rs0 --port 27118 --logpath /var/log/mongodb/rs0-1.log --dbpath /var/lib/mongodb/rs0-1' mongodb
su -s /bin/bash -c 'mongod --quiet --fork --shardsvr --replSet rs0 --port 27119 --logpath /var/log/mongodb/rs0-2.log --dbpath /var/lib/mongodb/rs0-2' mongodb

su -s /bin/bash -c 'mongod --quiet --fork --shardsvr --replSet rs1 --port 27217 --logpath /var/log/mongodb/rs1-0.log --dbpath /var/lib/mongodb/rs1-0' mongodb
su -s /bin/bash -c 'mongod --quiet --fork --shardsvr --replSet rs1 --port 27218 --logpath /var/log/mongodb/rs1-1.log --dbpath /var/lib/mongodb/rs1-1' mongodb
su -s /bin/bash -c 'mongod --quiet --fork --shardsvr --replSet rs1 --port 27219 --logpath /var/log/mongodb/rs1-2.log --dbpath /var/lib/mongodb/rs1-2' mongodb

sleep 5

mongo --quiet --port 27117 --eval 'rs.initiate({_id : "rs0", members: [{ _id: 0, host: "localhost:27117" }, { _id: 1, host: "localhost:27118" }, { _id: 2, host: "localhost:27119" }]})'
mongo --quiet --port 27217 --eval 'rs.initiate({_id : "rs1", members: [{ _id: 0, host: "localhost:27217" }, { _id: 1, host: "localhost:27218" }, { _id: 2, host: "localhost:27219" }]})'

sleep 10

su -s /bin/bash -c 'mongos --quiet --fork --configdb cfgReplSet/localhost:27917,localhost:27918,localhost:27919 --port 27017 --logpath /var/log/mongodb/mongos0.log' mongodb
su -s /bin/bash -c 'mongos --quiet --fork --configdb cfgReplSet/localhost:27917,localhost:27918,localhost:27919 --port 27018 --logpath /var/log/mongodb/mongos1.log' mongodb

sleep 5

mongo --quiet mongodb://localhost:27017,localhost:27018 --eval 'sh.addShard("rs0/localhost:27117,localhost:27118,localhost:27119")'
mongo --quiet mongodb://localhost:27017,localhost:27018 --eval 'sh.addShard("rs1/localhost:27217,localhost:27218,localhost:27219")'
