# Wave Software Development Challenge

## Summary

This is an implementation of the development challenge using the Play
Framework for Scala. The basic design in an application server that
delivers a single web page that ultimately becomes a single page web
application. The same service that served the page also acts as a REST
service to provide JSON to the single page application.

While I'm familiar with a number of web app frameworks, I decided to
try to implement the challenge in something I've never used
before. Since I've recently become very interested in Scala (and Akka,
particularly) I decided that I'd try to use the top Scala web
framework (Play).

I'm pleased with the implementation because it was quite challenging
for me - having never before used Scala in this setting. I'm really
quite taken with how the Actor portion of the REST service turned
out. This patten will be useful for me in several cases.

## Running the application

All of these instructions require an installation of SBT. On MacOS,
this can be installed with homebrew:

```
$ brew install sbt
```

For other platforms refer to the [SBT
site](https://www.scala-sbt.org/).

### Development

In development, a local Postgres service will need to be running on
localhost. You will need to bootstrap the database to create the
application's DB. Once that's done, you can simple run the
application.

```
$ psql -h localhost -U postgres -a -f bootstrap.sql
$ sbt run
```

Once the application is running, you can access it at
[http://localhost:9000](http://localhost:9000). The application may
ask you to run migrations (evolutions in Play parlance).

### Production

The production version of the application is run via Docker and Docker
Compose. To build it, you should assemble the required Docker images
and then run the composition. You will require a "secret" that is
passed via an environment variable.

To generate the secret:

```
$ sbt playGenerateSecret
```

To run the application:

```
$ ./build-containers.sh
$ APP_PLAY_SECRET="<secret>" docker-compose -f docker-compose.yml up
```

Once the application is running, you can access it at
[http://localhost:9000](http://localhost:9000).
