This is adapter for SpotOnResponse which will pull JSON data from json_ds which is defined in configuration.

Each configuration's file name will be used as index for the data in NoSQL, for example, landslide.config will
generate data with Title as landslide so it can be queried easier. Each configuration will be running at the rate
of every 6 hours which is defined as cron sechedule in application.properties.

For the testing purpose, you can place the test configuration file which file name is prefixed as 'test.',
for example, 'test.landslide.config' the this configuration will be run only once after the file is copied into
directory defined by 'config.path'.

This application also provide simple REST services for query the data and delete data. The web console can be
accessed by http://localhost:8088/query?config=landslide for query the data stored in NoSQL. And, you can delete
the whole set of data by accessing http://localhost:8080/delete?config=landslide.
