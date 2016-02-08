# Relayvatr

An Elevator Control System.


## Overview

On each iteration, an elevator can be performing one of the following operations:

  * Being idle;
  * Passing through a floor without stopping;
  * Arriving at a specific floor;
  * Leaving the floor to continue its trip.

Users can leave or enter an elevator car only when the elevator arrives at a floor. They also interact with the system in two ways:

* Requesting an elevator at their current floor, and giving a direction that they want to travel (up or down);
* Pressing a floor button when inside an elevator.

The behaviour determining which elevator will handle a given call and how an elevator will move between floors are described respectively by two classes:

1. `ClosestElevatorScheduler`;
2. `SameDirectionElevator`.

Each elevator available is mapped to an instance of `SameDirectionElevator`. On each iteration of the system, the `ClosestElevatorScheduler` asks each elevator to evaluate how well they can handle each of the open user calls. Finally, it decides the ones that seem to be best for each call. If a call cannot be handled straight away, a new attempt will be made on the next iteration.

Briefly described, the elevators decide if they can handle a user call based on the direction they are going and how close the elevator is to the user.  For the first, if the user is:

  *  on an upper floor:
     - and wants to go down: the car will go straight to the user floor, and only when descending get others on the way down;
     - and wants to go up: the card will collect anyone on the way that is going up;
  * on an lower floor:
     - and wants to go down: the will collect anyone on the way that is going down;
     - and wants to go up: the car will go straight to the user floor, and only when ascending get others on the way up;
  * on the same floor:
     - will go on the direction requested by the first user, picking up users that are going on the same direction.

Furthermore, two metrics are used to compute the associate closeness, or cost, between an elevator and a user:

1. The actual distance between the car and the user (floors);
2. Its load (number of passengers already on the car).

When compared to a simple `first-come, first-served` (FCFS), the current implementation can use the closest possible elevator to the user, distribute load between cars, and will try to pick the largest number of users on its way.

Implementation details can be found on the ScalaDoc of each class.


## Building and Running

Operations can be performed with the `sbt` script available on the source's directory.

### Dependencies

  * Java 8

### Running

The following command will run a simulated version of the system, where random user trips will be generated and applied to it. The parameters used on the simulation can be seen on the class `relayvatr.Main`.

```bash
./sbt run
```

### Test

This command will run all the unit tests available:

```bash
./sbt test
```

### ScalaDoc

After running the following command, the index page will be available at `target/scala-2.11/api/index.html`.

```bash
./sbt doc
```

### Project Zip

A zip file of the project can be generated with:

```bash
git archive HEAD --format zip -o revalyvatr.zip
```
