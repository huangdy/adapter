This is adapter for SpotOnResponse which will pull JSON data from json_ds which is defined in configuration.

Table Of Content

Each configuration's file name will be used as index for the data in NoSQL, for example, landslide.config will
generate data with Title as landslide so it can be queried easier. Each configuration will be running at the rate
of every 6 hours which is defined as cron sechedule in application.properties.

For the testing purpose, you can place the test configuration file which file name is prefixed as 'test.',
for example, 'test.landslide.config' then this configuration will be run only once after the file is updated.

To upload a configuration, you can use browser to access http://localhost/. You can use 'Configuration File Upload' tab to upload the configuration file into adapter.

Currently, you can use browser to VIEW the content for the configuration file, xcore.config by
http://hostname/api/query?config=xcore

you can use browser to DELETE the content for the configuration file: xcore.config by
http://hostname/api/delete?config=xcore

you can see the uploaded configuration using browser by
http://hostname/api/configurations

Only the file upload is coded as web application, will integrate the rest into the web application later.
